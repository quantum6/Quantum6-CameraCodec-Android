package net.quantum6.codectest;

import android.graphics.ImageFormat;
import android.util.Log;
import android.view.Surface;

import net.quantum6.mediacodec.AndroidVideoDecoder;
import net.quantum6.mediacodec.AndroidVideoEncoder;
import net.quantum6.mediacodec.H264SpsParser;
import net.quantum6.mediacodec.MediaCodecData;
import net.quantum6.mediacodec.MediaCodecKit;

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


    protected int mFrameWidth;
    protected int mFrameHeight;
    private MediaCodecData mFrameData;
    private MediaCodecData mEncodedData;
    private AndroidVideoEncoder mEncoder;

    protected int mDecoderWidth;
    protected int mDecoderHeight;
    private MediaCodecData mDecodedData;
    private AndroidVideoDecoder mDecoder;
    
    protected abstract Surface getSurface();
    protected abstract void clearSurface();
    
    AbstractCodecHelper()
    {
        //
    }
    
    public synchronized void clearCodec()
    {
    	isInited     = false;
        mFrameWidth  = 0;
        mFrameHeight = 0;
        
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
    	mFrameWidth  = width;
    	mFrameHeight = height;
        if (0 == mFrameWidth
        		|| 0 == mFrameHeight
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
            mFrameData = new MediaCodecData(mFrameWidth, mFrameHeight);
            mFrameData.getInfo()[0] = mFrameWidth;
            mFrameData.getInfo()[1] = mFrameHeight;
            
            mEncodedData = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncoder     = new AndroidVideoEncoder(mFrameWidth, mFrameHeight, mFrameRate, mBitRate);
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
            mFrameData.setData(data);
            dataLen = mEncoder.process(mFrameData, mEncodedData);
            Log.d(TAG, "encoded length=" + dataLen);
        }

        if (dataLen <= 0)
        {
            return;
        }

        if (null == mDecoder)
        {
            int[] size = H264SpsParser.getSizeFromSps(mEncodedData.mDataArray);
            if (size != null)
            {
                mDecoderWidth  = size[0];
                mDecoderHeight = size[1];
            }
            else
            {
                mDecoderWidth  = mFrameWidth;
                mDecoderHeight = mFrameHeight;
            }
            Log.d(TAG, "initCodec() mDecoder=("+mDecoderWidth+", "+mDecoderHeight+"), ("+mFrameWidth+", "+mFrameHeight+")");
            mDecodedData = new MediaCodecData(mDecoderWidth, mDecoderHeight);
            mDecoder     = new AndroidVideoDecoder(getSurface(), mDecoderWidth, mDecoderHeight);
        }
        
    	dataLen = mDecoder.process(mEncodedData, mDecodedData);
        Log.d(TAG, "decoded length first=" + dataLen);
    	if (dataLen == -1)
    	{
            Log.d(TAG, "release and new AndroidVideoDecoder");
    	    //Thread.dumpStack();
    	    mDecoder.release();
            mDecoder = new AndroidVideoDecoder(getSurface(), mDecoderWidth, mDecoderHeight);
            dataLen = mDecoder.process(mEncodedData, mDecodedData);
            Log.d(TAG, "decoded length second=" + dataLen);
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
