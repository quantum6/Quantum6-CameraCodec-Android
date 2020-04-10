package net.quantum6.codectest;

import android.graphics.ImageFormat;
import android.util.Log;
import android.view.Surface;

import net.quantum6.mediacodec.AndroidVideoDecoder;
import net.quantum6.mediacodec.AndroidVideoEncoder;
import net.quantum6.mediacodec.MediaCodecData;

/**
 * 
 * @author PC
 * 
 */
abstract class AbstractCodecHelper
{
    private final static String TAG         = AbstractCodecHelper.class.getCanonicalName();

    
    private int mFrameRate                  = 20;
    private int mBitRate                    = 500*1000;

    /**
     * 当前一秒的帧数。
     */
    private long mFpsStartTime  = 0;
    private int  mFpsCounter    = 0;
    public  int  mFpsCurrent    = 0;
    private final int FPS_MS_TIME  = 1000; 
    
    private boolean         isInited                = false;


    protected int mWidth;
    protected int mHeight;

    private AndroidVideoEncoder mEncoder;

    private AndroidVideoDecoder mDecoder;
    
    private MediaCodecData mInputData;
    private MediaCodecData mOutputData;

    
    protected abstract Surface getSurface();
    protected abstract void clearSurface();
    
    AbstractCodecHelper()
    {
        //
    }
    
    public synchronized void clearCodec()
    {
    	isInited = false;
        mWidth = 0;
        mHeight = 0;
        
        if (null != mDecoder)
        {
            mDecoder.release();
            mDecoder = null;
        }

        if (null != mEncoder)
        {
            mEncoder.release();
            mEncoder = null;
        }
    }
    
    public void initCodec(int width, int height)
    {
    	mWidth = width;
    	mHeight = height;
        if (0 == mWidth
        		|| 0 == mHeight
        		|| isInited
        		|| getSurface() == null )
        {
            return;
        }
        Log.d(TAG, "initCodec()");
        //int bufSize = mWidth * mHeight * ImageFormat.getBitsPerPixel(SurfaceViewCameraHelper.PREVIEW_FORMAT) / 8;
        if (null == mEncoder)
        {
            Log.d(TAG, "initCodec() mEncoder");
            mInputData = new MediaCodecData();
            mInputData.getInfo()[0] = mWidth;
            mInputData.getInfo()[1] = mHeight;
            mEncoder = new AndroidVideoEncoder(mWidth, mHeight, mFrameRate, mBitRate);
        }
        
        if (null == mDecoder)
        {
            Log.d(TAG, "initCodec() mDecoder");
            int size = mWidth*mHeight*2;
            if (size < 128*1024)
            {
                size = 128*1024;
            }
            mOutputData = new MediaCodecData();
            mOutputData.setData(new byte[size]);
            mDecoder = new AndroidVideoDecoder(getSurface(), mWidth, mHeight);
        }
        isInited = true;
    }
    
    private void calculateFps()
    {
        long currentTime = System.currentTimeMillis();
        if (0 == mFpsStartTime)
        {
            mFpsCurrent   = 0;
            mFpsCounter   = 1;
            mFpsStartTime = currentTime;
            return;
        }
        if (currentTime - mFpsStartTime >= FPS_MS_TIME)
        {
            mFpsCurrent   = mFpsCounter;
            mFpsCounter   = 1;
            mFpsStartTime = currentTime;
        }
        else
        {
            mFpsCounter++;
        }
    }

    public void processData(final byte[] data)
    {
        calculateFps();

        int dataLen = 0;
        if (null != mEncoder)
        {
            mInputData.setData(data);
            dataLen = mEncoder.process(mInputData, mOutputData);
            Log.d(TAG, "encoded length=" + dataLen);
        }

        if (null != mDecoder && dataLen > 0)
        {
        	dataLen = mDecoder.process(mOutputData, mInputData);
            Log.d(TAG, "decoded length=" + dataLen);
        }
    }

    private void reset()
    {
    	clearCodec();
    	isInited      = false;

        mFpsStartTime = 0;
        mFpsCounter   = 0;
        mFpsCurrent   = 0;
    }
    
    public void release()
    {
        reset();
        
        clearSurface();
    }

}
