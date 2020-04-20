package net.quantum6.codectest;

import java.util.List;

import net.quantum6.kit.CameraDataThread;
import net.quantum6.kit.CameraKit;
import net.quantum6.kit.Log;

import android.graphics.ImageFormat;
import android.hardware.Camera;

/**
 * 
 * @author PC
 * 
 */
abstract class AbstractCameraHelper
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


    private boolean useBackCamera = true;
    boolean         isInited                = false;


    private Camera      mCamera;
    List<Camera.Size>   mSupportedSizes;
    Camera.Size         mPreviewSize;
    
    int mWantedWidth;
    int mWantedHeight;

    AbstractCodecHelper mCodecHelper;

    AbstractCameraHelper()
    {
        //
    }

    protected abstract void setCameraPreviewDisplay(Camera camera);
    
    protected abstract void clearCameraPreviewDisplay(Camera camera);

    protected abstract void clearSurface();

    public void toggleCamera()
    {
        if (Camera.getNumberOfCameras() > 1)
        {
            useBackCamera = !useBackCamera;
            mPreviewSize = null;
        }
    }
    
    public void changeResolution(int width, int height)
    {
        //如果没有变化，返回。
        if (null != mPreviewSize && width == mPreviewSize.width && height == mPreviewSize.height)
        {
            return;
        }
        reset();
        
        mWantedWidth = width;
        mWantedHeight= height;
        
        initCamera(mWantedWidth, mWantedHeight);
        mCodecHelper.initCodec(this.mPreviewSize.width, this.mPreviewSize.height);
    }
    
    protected void initCamera(int width, int height)
    {
        //要显式调用release();
        if (null != mCamera)
        {
            return;
        }
        
        if (null == mCamera)
        {
            try
            {
                mCamera = Camera.open(useBackCamera ? 0 : 1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            Camera.Parameters parameters = mCamera.getParameters();
            mSupportedSizes = CameraKit.getSupportedSizes(parameters);
            Camera.Size bestSize = CameraKit.getCameraBestPreviewSize(parameters, width, height);
            parameters.setPreviewSize(bestSize.width, bestSize.height);
            parameters.setPreviewFormat(PREVIEW_FORMAT);
            parameters.setPreviewFpsRange(MIN_FPS*1000, MAX_FPS*1000);
            parameters.setPreviewFrameRate(frameRate);
            CameraKit.setCameraFocus(parameters);
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
            mCodecHelper.initCodec(mPreviewSize.width, mPreviewSize.height);
            for (int i = 0; i < BUFFER_COUNT; i++)
            {
                mCamera.addCallbackBuffer(new byte[bufSize]);
            }
            mCamera.setPreviewCallbackWithBuffer(dataThread);
            new Thread(dataThread).start();
            mCamera.startPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        isInited = true;
    }

    private CameraDataThread dataThread = new CameraDataThread()
    {
        @Override
        public void onCameraDataArrived(final byte[] data, Camera camera)
        {
            if (data == null || data.length == 0)
            {
                return;
            }

            if (null != mCodecHelper)
            {
                mCodecHelper.processData(data);
            }
        }
    };
    
    public int getFps()
    {
        return dataThread.getFps();
    }
    
    private void closeCamera()
    {
        if (null == mCamera)
        {
            return;
        }
        dataThread.stop();
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

    protected void reset()
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

}
