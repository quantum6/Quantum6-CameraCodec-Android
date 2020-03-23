package net.quantum6.codectest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

/**
 * 
 * @author PC
 * 
 */
abstract class AbstractCameraHelper implements Camera.PreviewCallback
{
    private final static String TAG         = AbstractCameraHelper.class.getCanonicalName();

    public  final static int PREVIEW_FORMAT = ImageFormat.NV21;
    public  final static int frameRate      = 30;

    private final static int MIN_FPS        = 10;
    private final static int MAX_FPS        = 60;
    
    private final static int BUFFER_COUNT   = 1;
    
    /**
     * 在创维盒子上，某摄像头分辨率是120x90，编码器出错。
     * 有些分辨率，如640x360，会出现很多条纹。判断是编码时的问题。
     */
    private final static int MIN_VIDEO_SIZE = 100;


    boolean         isInited                = false;


    private Camera      mCamera;
    List<Size>          mSupportedSizes;
    Camera.Size         mPreviewSize;

    AbstractCodecHelper mCodecHelper;

    AbstractCameraHelper()
    {
        //
    }

    protected abstract void setCameraPreviewDisplay(Camera camera);
    
    protected abstract void clearCameraPreviewDisplay(Camera camera);

    protected abstract void clearSurface();

    public void openCamera(int width, int height)
    {
        //如果没有变化，返回。
        if (null != mPreviewSize && width == mPreviewSize.width && height == mPreviewSize.height)
        {
            return;
        }
        reset();
        initCamera(width, height);
    }
    
    protected void initCamera(int width, int height)
    {
        //要显式调用release();
        if (null != mCamera)
        {
            return;
        }
        
        try
        {
            mCamera = Camera.open();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (null == mCamera)
        {
            try
            {
                mCamera = Camera.open(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            Camera.Parameters parameters = mCamera.getParameters();
            mSupportedSizes = parameters.getSupportedPreviewSizes();
            Collections.sort(mSupportedSizes, new SizeComparator());
            for (int i = 0; i < mSupportedSizes.size(); i++)
            {
                Size size = mSupportedSizes.get(i);
                if (       size.width  < MIN_VIDEO_SIZE
                        || size.height < MIN_VIDEO_SIZE)
                {
                    mSupportedSizes.remove(i);
                }
                Log.d(TAG, "i=" + i + ", " + size.width + ", " + size.height);
            }
            if (0 == width || 0 == height)
            {
                Size size = mSupportedSizes.get(0);
                width = size.width;
                height= size.height;
            }

            parameters.setPreviewSize(width, height);
            parameters.setPreviewFormat(PREVIEW_FORMAT);
            parameters.setPreviewFpsRange(MIN_FPS*1000, MAX_FPS*1000);
            parameters.setPreviewFrameRate(frameRate);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            // parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            // parameters.getSupportedPictureSizes();
            // parameters.setPictureSize(width, height);
            mCamera.setParameters(parameters);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
        	setCameraPreviewDisplay(mCamera);
            mPreviewSize = mCamera.getParameters().getPreviewSize();
            int bufSize = mPreviewSize.width * mPreviewSize.height * ImageFormat.getBitsPerPixel(PREVIEW_FORMAT) / 8;
            Log.d(TAG, "----" + bufSize + ", " + mPreviewSize.width + ", " + mPreviewSize.height);
            mCodecHelper.initCodec(mPreviewSize.width, mPreviewSize.height);
            for (int i = 0; i < BUFFER_COUNT; i++)
            {
                mCamera.addCallbackBuffer(new byte[bufSize]);
            }
            mCamera.setPreviewCallbackWithBuffer(this);

            mCamera.startPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        isInited = true;
    }

    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{ 
    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
    	if (data == null || data.length == 0)
    	{
    		return;
    	}
    	
    	{
            if (null != mCodecHelper)
            {
            	mCodecHelper.processData(data);
            }
    	}
    	
        if (null != mCamera)
        {
            mCamera.addCallbackBuffer(data);
        }
    }

    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
    
    private void closeCamera()
    {
        if (null == mCamera)
        {
            return;
        }
        try
        {
            mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void reset()
    {
        closeCamera();
        mCodecHelper.clearCodec();
        
        isInited            = false;
        mPreviewSize        = null;
    }
    
    public void release()
    {
        reset();
        
        clearSurface();
    }

    /**
     * 为了确保正确性，强制排序一次。
     * 
     * @author PC
     * 
     */
    private class SizeComparator implements Comparator<Size>
    {
        @Override
        public int compare(Size arg0, Size arg1)
        {
            if (arg0.width > arg1.width)
            {
                return 1;
            }
            if (arg0.width < arg1.width)
            {
                return -1;
            }
            if (arg0.height == arg1.height)
            {
                return 0;
            }
            return (arg0.height > arg1.height) ? 1 : -1;
        }
    }

}
