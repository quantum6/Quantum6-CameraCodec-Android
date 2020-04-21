package net.quantum6.codectest;

import android.util.Log;
import android.view.Surface;

import net.quantum6.fps.FpsCounter;
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
    private final static String TAG      = AbstractCodecHelper.class.getCanonicalName();

    protected final static int FPS_MS_TIME      = 1000; 
    
    protected final static int DEFAULT_FPS      = 60;
    protected final static int DEFAULT_BIT_RATE = 1000*1000;

    private boolean  isInited   = false;

    FpsCounter          mEncodeFps = new FpsCounter();

    protected int                 mFrameWidth;
    protected int                 mFrameHeight;
    protected MediaCodecData      mFrameData;
    protected MediaCodecData      mEncodedData;
    protected AndroidVideoEncoder mEncoder;

    protected int                 mDecoderWidth;
    protected int                 mDecoderHeight;
    protected MediaCodecData      mDecodedData;
    protected AndroidVideoDecoder mDecoder;
    
    protected abstract Surface    getSurface();
    protected abstract void       clearSurface();
    
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
            mFrameData   = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncodedData = new MediaCodecData(mFrameWidth, mFrameHeight);
            mEncoder     = new AndroidVideoEncoder(mFrameWidth, mFrameHeight, DEFAULT_FPS, DEFAULT_BIT_RATE);
        }
        
        isInited   = true;
    }
    
    public int getFps()
    {
        return mEncodeFps.getFpsAndClear();
    }
    
    /*
    private final byte[] csd0 = 
        {
            0x0, 0x0, 0x0, 0x1, 0x67, 0x42, 0x0, 0x29, (byte)0x8d, (byte)0x8d, 0x40, 0x28, 0x2, (byte)0xdd, 0x0, (byte)0xf0, (byte)0x88, 0x45, 0x38,
            0x0, 0x0, 0x0, 0x1, 0x68, (byte)0xca, 0x43, (byte)0xc8 
        };
    
    private MediaCodec decoder;
    private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    
    private void decode(byte[] data, int size)
    {
        if (decoder == null)
        {
            try
            {
                decoder = MediaCodec.createDecoderByType(MediaCodecKit.MIME_CODEC_H264);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            
            MediaFormat format = MediaFormat.createVideoFormat(MediaCodecKit.MIME_CODEC_H264, mFrameWidth, mFrameHeight);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(csd0));
            decoder.configure(
                    format,
                    getSurface(),
                    null,
                    0
                    );
            decoder.start();
        }
        int decIndex = decoder.dequeueInputBuffer(-1);
        if (decIndex > 0)
        {
            ByteBuffer decBuffer = decoder.getInputBuffers()[decIndex];
            decBuffer.clear();
            decBuffer.put(data, 0, size);
            
            decoder.queueInputBuffer(decIndex, 0, size, 0, 0);
        }
        
        int    res = decoder.dequeueOutputBuffer(info, 5000);
        if (res >= 0)
        {
            int outputBufIndex = res;
            ByteBuffer buf2 = decoder.getOutputBuffers()[outputBufIndex];

            buf2.position(info.offset);
            buf2.limit(info.offset + info.size);

            decoder.releaseOutputBuffer(outputBufIndex, true);
        }
        else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
        {
            //
        }
        else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
        {
            //
        }
    }
    */

    public void processData(final byte[] data)
    {
        int dataLen = 0;
        if (null != mEncoder)
        {
            mFrameData.setData(data);
            dataLen = mEncoder.process(mFrameData, mEncodedData);
            //Log.e(TAG, "encoded length=" + dataLen);
        }

        if (dataLen <= 0)
        {
            return;
        }

        mEncodeFps.count();
        
        if (null == mDecoder)
        {
            mDecoderWidth  = mFrameWidth;
            mDecoderHeight = mFrameHeight;
            mDecodedData   = new MediaCodecData(mDecoderWidth, mDecoderHeight);
            mDecoder       = new AndroidVideoDecoder(getSurface(), mDecoderWidth, mDecoderHeight);
            return;
        }
        mEncodedData.mDataSize = dataLen;
        dataLen = mDecoder.process(mEncodedData, mDecodedData);
        //Log.d(TAG, "decoded length first=" + dataLen);
        
        /*
        try
        {
            decode(mEncodedData.mDataArray, dataLen);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception="+e);
            e.printStackTrace();
        }
        */
    }

    private void reset()
    {
        clearCodec();
        isInited      = false;

        mEncodeFps.reset();
    }
    
    public void release()
    {
        reset();
        
        clearSurface();
    }

}
