package net.quantum6.codectest;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import net.quantum6.mediacodec.AndroidVideoDecoder;
import net.quantum6.mediacodec.AndroidVideoEncoder;
import net.quantum6.mediacodec.H264SpsParser;
import net.quantum6.mediacodec.MediaCodecData;

/**
 * 
 * @author PC
 * 
 */
abstract class AbstractCodecHelper
{
    private final static String TAG      = AbstractCodecHelper.class.getCanonicalName();

    private final static int FPS_MS_TIME      = 1000; 
    
    private final static int DEFAULT_FPS      = 60;
    private final static int DEFAULT_BIT_RATE = 1000*1000;

    /**
     * 当前一秒的帧数。
     */
    private long mFpsStartTime  = 0;
    private int  mFpsCounter    = 0;
    public  int  mFpsCurrent    = 0;
    
    private boolean  isInited   = false;


    protected int               mFrameWidth;
    protected int               mFrameHeight;
    private MediaCodecData      mFrameData;
    private MediaCodecData      mEncodedData;
    private AndroidVideoEncoder mEncoder;

    protected int               mDecoderWidth;
    protected int               mDecoderHeight;
    private MediaCodecData      mDecodedData;
    private AndroidVideoDecoder mDecoder;
    
    protected abstract Surface  getSurface();
    protected abstract void     clearSurface();
    
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
        if (null != decoder)
        {
            decoder.release();
            decoder = null;
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
            mFrameData   = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncodedData = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncoder     = new AndroidVideoEncoder(mFrameWidth, mFrameHeight, DEFAULT_FPS, DEFAULT_BIT_RATE);
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
    
    MediaCodec decoder;
    ByteBuffer[] decoderInputBuffers = null;
    ByteBuffer[] decoderOutputBuffers = null;
    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    private void decode(byte[] data, int size)
    {
        ByteBuffer buf = ByteBuffer.wrap(data, 0, size);
        buf.position(0);
        
        Log.d("sakalog", "create decoder.");
        if (decoder == null)
        {
            if (data[4] != 0x67)
            {
                return;
            }

        try
        {
        decoder = MediaCodec.createDecoderByType("video/avc");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", mFrameWidth, mFrameHeight);
        format.setByteBuffer("csd-0", buf);
        Log.d("sakalog", "Configuring decoder with input format : " + format);
        decoder.configure(
                format,     //The format of the input data (decoder)
                getSurface(),    //a surface on which to render the output of this decoder.
                null,       //a crypto object to facilitate secure decryption of the media data.
                0           //configure the component as an decoder.
                );
        decoder.start();
        decoderInputBuffers  = decoder.getInputBuffers();
        decoderOutputBuffers = decoder.getOutputBuffers();
        return;
    } else {

        // Codec(エンコーダ)からのOutputバッファが、h.264符号化データである場合、Codec(デコーダ)へ入力する
        int decIndex = decoder.dequeueInputBuffer(-1);
        //Log.d("sakalog", "decoder input buf index " + decIndex);
        decoderInputBuffers[decIndex].clear();
        decoderInputBuffers[decIndex].put(data, 0, size);
        decoder.queueInputBuffer(decIndex, 0, size, 0, 0);
    }
    
    
        int    res = decoder.dequeueOutputBuffer(info, 5000);
Log.d(TAG, "dequeueOutputBuffer="+res);
        if (res >= 0) {
            //Log.d("sakalog", "decoder output buf index " + outputBufIndex);
            int outputBufIndex = res;
            ByteBuffer buf2 = decoderOutputBuffers[outputBufIndex];

            buf2.position(info.offset);
            buf2.limit(info.offset + info.size);

            if (info.size > 0) {
                //errors = checkFrame(buf, info, oformat, width, height, threshold);
            }

            // 使い終わったOutputバッファはCodec(デコーダ)に戻す
            decoder.releaseOutputBuffer(outputBufIndex, true);
        }
    else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
        decoderOutputBuffers = decoder.getOutputBuffers();

        Log.d("sakalog", "decoder output buffers have changed.");
    } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        Log.d("sakalog", "decoder output format has changed to " + decoder.getOutputFormat());
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

        /*
        if (null == mDecoder)
        {
            Log.d(TAG, "mEncodedData.mDataArray[4]="+Integer.toHexString(mEncodedData.mDataArray[4]));
            if (mEncodedData.mDataArray[4] != 0x67)
            {
                return;
            }
            mDecoderWidth  = mFrameWidth;
            mDecoderHeight = mFrameHeight;
            Log.d(TAG, "initCodec() mDecoder=("+mDecoderWidth+", "+mDecoderHeight+"), ("+mFrameWidth+", "+mFrameHeight+")");
            mDecodedData = new MediaCodecData(mDecoderWidth, mDecoderHeight);
            ByteBuffer sps = ByteBuffer.wrap(mEncodedData.mDataArray, 0, mEncodedData.mDataSize);
            mDecoder     = new AndroidVideoDecoder(getSurface(), mDecoderWidth, mDecoderHeight, sps);
            return;
        }
            */
        
    	//dataLen = mDecoder.process(mEncodedData, mDecodedData);
        //Log.d(TAG, "decoded length first=" + dataLen);
        try
        {
            decode(mEncodedData.mDataArray, mEncodedData.mDataSize);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
