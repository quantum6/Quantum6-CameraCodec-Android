package net.quantum6.codectest;

import net.quantum6.mediacodec.AndroidVideoEncoder;
import net.quantum6.mediacodec.AndroidVideoEncoderOfSurface;
import net.quantum6.mediacodec.MediaCodecData;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 * 
 */
final class TextureViewCodecHelper extends AbstractCodecHelper
{
    private final static String TAG         = TextureViewCodecHelper.class.getCanonicalName();

    private Surface       mSurface;
    
    TextureViewCodecHelper()
    {
        //
    }

    public void initCodec(int width, int height)
    {
        mFrameWidth  = width;
        mFrameHeight = height;
        if (0 == mFrameWidth
                || 0 == mFrameHeight
                || getSurface() == null )
        {
            return;
        }
        Log.d(TAG, "initCodec()");
        //int bufSize = mWidth * mHeight * ImageFormat.getBitsPerPixel(SurfaceViewCameraHelper.PREVIEW_FORMAT) / 8;
        if (null == mEncoder)
        {
            Log.d(TAG, "initCodec() mEncoder");
            mFrameData   = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncodedData = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncoder     = new AndroidVideoEncoderOfSurface(mFrameWidth, mFrameHeight, DEFAULT_FPS, DEFAULT_BIT_RATE);
            mSurface     = ((AndroidVideoEncoderOfSurface)mEncoder).getSurface();
        }
        
    }

    @Override
    protected Surface getSurface()
    {
        return mSurface;
    }
    
    @Override
    protected void clearSurface()
    {
        mSurface = null;
    }

}
