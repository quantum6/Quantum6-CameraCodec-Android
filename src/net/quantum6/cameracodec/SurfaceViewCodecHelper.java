package net.quantum6.cameracodec;

import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 * 
 */
final class SurfaceViewCodecHelper extends AbstractCodecHelper implements SurfaceHolder.Callback
{
    private final static String TAG         = SurfaceViewCodecHelper.class.getCanonicalName();

    private SurfaceHolder       mDisplayHolder;
    
    SurfaceViewCodecHelper()
    {
        //
    }

    @Override
    protected Surface getSurface()
    {
    	if (null != mDisplayHolder)
    	{
    		return mDisplayHolder.getSurface();
    	}
    	return null;
    }
    
    @Override
    protected void clearSurface()
    {
    	mDisplayHolder = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceCreated()");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        mDisplayHolder = holder;
        
        initCodec(mFrameWidth, mFrameHeight);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceDestroyed()");
        clearCodec();
        mDisplayHolder = null;
    }

}
