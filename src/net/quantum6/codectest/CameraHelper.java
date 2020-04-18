package net.quantum6.codectest;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 * 
 */
final class CameraHelper extends AbstractCameraHelper implements SurfaceHolder.Callback
{
    private final static String TAG         = CameraHelper.class.getCanonicalName();

    private SurfaceHolder       mPreviewHolder;
    CameraHelper()
    {
        //
    }
    
    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    @Override
    protected void setCameraPreviewDisplay(Camera camera)
    {
    	try
    	{
    		camera.setPreviewDisplay(mPreviewHolder);
    	}
    	catch (Exception e)
    	{
    		//
    	}
    }

    @Override
    protected void clearCameraPreviewDisplay(Camera camera)
    {
    	try
    	{
    		camera.setPreviewDisplay(null);
    	}
    	catch (Exception e)
    	{
    		//
    	}
    }

    @Override
    protected void clearSurface()
    {
    	mPreviewHolder = null;
    }
    
    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}

    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceCreated()");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        //清除原来跟surface相关的
    	mCodecHelper.clearCodec();
        
        Log.d(TAG, "surfaceChanged()");
      	mPreviewHolder = holder;
        // AvcCodec.listCodec();
        //必须初始化摄像头，以获得各种分辨率。
        initCamera(0, 0);
        
        mCodecHelper.initCodec(this.mPreviewSize.width, this.mPreviewSize.height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceDestroyed()");
        mCodecHelper.clearCodec();
    }

    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
}
