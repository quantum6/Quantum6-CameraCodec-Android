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
final class TextureViewCameraHelper extends AbstractCameraHelper implements SurfaceHolder.Callback
{
    private final static String TAG         = TextureViewCameraHelper.class.getCanonicalName();

    private SurfaceHolder       mPreviewHolder;
    
    TextureViewCameraHelper()
    {
        //
    }
    
    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    @Override
    protected void setCameraPreviewDisplay(Camera camera)
    {
    	try
    	{
    	    camera.setPreviewTexture(null);
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
        Log.d(TAG, "surfaceChanged()");
        if (null != holder)
        {
            mPreviewHolder = holder;
        }
        // AvcCodec.listCodec();
        //必须初始化摄像头，以获得各种分辨率。
        changeResolution(mWantedWidth, mWantedHeight);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceDestroyed()");
        mCodecHelper.clearCodec();
    }

    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
}
