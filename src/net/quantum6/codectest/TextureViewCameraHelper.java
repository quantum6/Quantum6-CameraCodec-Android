package net.quantum6.codectest;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.TextureView;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 * 
 */
final class TextureViewCameraHelper extends AbstractCameraHelper
{
    private final static String TAG         = TextureViewCameraHelper.class.getCanonicalName();

    private SurfaceTexture mSurface;
    
    TextureViewCameraHelper()
    {
        //
    }
    
    public void setTextureView(SurfaceTexture tv)
    {
        mSurface = tv;
    }
    
    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    @Override
    protected void setCameraPreviewDisplay(Camera camera)
    {
    	try
    	{
    		camera.setPreviewTexture(mSurface);
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
    		camera.setPreviewTexture(null);
    	}
    	catch (Exception e)
    	{
    		//
    	}
    }

    @Override
    protected void clearSurface()
    {
    	//
    }
    
    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}

}
