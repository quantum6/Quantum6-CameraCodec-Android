package net.quantum6.cameracodec;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.quantum6.cameracodec.R;
import net.quantum6.kit.SystemKit;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 *
 */
public abstract class AbstractVideoActivity extends Activity implements OnItemSelectedListener, OnClickListener
{
    private final static String TAG = AbstractVideoActivity.class.getCanonicalName();

    private final static int MESSAGE_CHECK_FPS      = 1;
    private final static int MESSAGE_CHECK_INIT     = 2;
    private final static int MESSAGE_CHANGE_SHAPE   = 3;
    
    private final static int TIME_DELAY             = 1000;
    
    /**
     * 事实上chrome过去使用TextureView作为合成表面，但我们出于几个原因切换到SurfaceView：
     * 由于失效(invalidation)和缓冲的特性，TextureView增加了额外1~3帧的延迟显示画面更新
     * TextureView总是使用GL合成，而SurfaceTexture可以使用硬件overlay后端，可以占用更少的内存带宽，消耗更少的能量
     * TextureView的内部缓冲队列导致比SurfaceView使用更多的内存。
     * TextureView的动画和变换能力我们用不上。
     * 
     * 所以结论是Chromium for Android中可以使用TextureView替代SurfaceView作为合成表面，
     * 但带来的后果是占用更多的内存，性能下降。
     */
    protected   View      mDisplayView;
    protected   View      mPreviewView;
    private     Spinner   mResolution;
    private     TextView  mInfoText;
    
    protected AbstractCameraHelper mCameraHelper;
    protected AbstractCodecHelper  mCodecHelper;
    private int mSelectedIndex      = -1;
    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        initHelpers();
        mCameraHelper.mCodecHelper = mCodecHelper;

        mDisplayView = initDisplayView();
        mPreviewView = initPreviewView();

        mResolution = (Spinner)this.findViewById(R.id.resolution);
        mResolution.requestFocus();
        
        mInfoText = (TextView)this.findViewById(R.id.info_text);
        
        Button toggle = (Button)this.findViewById(R.id.toogle);
        toggle.setOnClickListener(this);
        
        mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_INIT, TIME_DELAY);
    }

    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    
    abstract protected int getLayout();
    
    abstract protected void initHelpers();
    
    abstract protected View initDisplayView();
    
    abstract protected View initPreviewView();
    
    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
    
    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    @Override
    public void onClick(android.view.View view)
    {
        mCameraHelper.toggleCamera();
        changeResolution();
    }
    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
    
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
        	if (mHandler == null)
        	{
        		return;
        	}
            switch (msg.what)
            {
                case MESSAGE_CHECK_FPS:
                    if (null != mCameraHelper.mPreviewSize)
                    {
                        mInfoText.setText("("+mCameraHelper.mPreviewSize.width+", "+mCameraHelper.mPreviewSize.height
                                +")"
                                +", camera=" + mCameraHelper.getFps()
                                +", encode=" + mCodecHelper.getFps()
                                +"\n"+SystemKit.getText(getApplicationContext()));
                        mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_FPS, TIME_DELAY);
                    }
                    break;
                    
                case MESSAGE_CHECK_INIT:
                    if (mCameraHelper.isInited)
                    {
                        addResolutions();
                        mResolution.setOnItemSelectedListener(AbstractVideoActivity.this);
                        mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_FPS, TIME_DELAY);
                    }
                    else
                    {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_INIT, TIME_DELAY);
                    }
                    break;
                    
                case MESSAGE_CHANGE_SHAPE:
                    if (mCameraHelper.isInited)
                    {
                        double ratio = 1.0 * mCameraHelper.mPreviewSize.width/mCameraHelper.mPreviewSize.height;
                        adjustViewShape(mPreviewView, ratio);
                        //adjustViewShape(mDisplayView, ratio);
                    }
                    else
                    {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_CHANGE_SHAPE, TIME_DELAY);
                    }
                    break;
                    
                default:
                    break;
            }
        }
    };
    
    private void changeResolution()
    {
        String selected = (String) mResolution.getItemAtPosition(mSelectedIndex);
        selected        = selected.substring(selected.indexOf('(')+1, selected.indexOf(')'));
        int pos         = selected.indexOf(',');
        int width       = Integer.parseInt(selected.substring(0, pos));
        int height      = Integer.parseInt(selected.substring(pos+1).trim());

        mCameraHelper.changeResolution(width, height);
        addResolutions();
    }
    
    private void addResolutions()
    {
        List<String> resolutions = new LinkedList<String>();
        if (null != mCameraHelper.mSupportedSizes)
        {
            for (int i = 0; i < mCameraHelper.mSupportedSizes.size(); i++)
            {
                Size size = mCameraHelper.mSupportedSizes.get(i);
                resolutions.add("分辨率"+i+"=("+size.width+", "+size.height+")");
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this.getApplicationContext(), 
                R.layout.spinner_item,
                resolutions
                );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mResolution.setAdapter(adapter);
    }

    /**
     * 根据比例调整View的大小，保证比例协调。
     * @param view
     * @param ratio
     */
    private void adjustViewShape(View view, double ratio)
    {
        int height   = view.getMeasuredHeight();
        int newWidth = (int)(height*ratio);
        int width    = view.getMeasuredHeight();
        //变化不大就不要处理了。
        if (Math.abs(newWidth-width)<5)
        {
            return;
        }
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
        lp.width = newWidth;
        view.setLayoutParams(lp);
    }
    
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
    {
        if (position == mSelectedIndex)
        {
            return;
        }
        
        mSelectedIndex = position;
        changeResolution();

        initPreviewView();
        mHandler.sendEmptyMessage(MESSAGE_CHANGE_SHAPE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        //
    }
    
    @Override
    public void onPause()
    {
        finish();
        super.onPause();
    }
    
    @Override
    public void onDestroy()
    {
        mHandler = null;
        
        mCameraHelper.release();
        mCameraHelper = null;
        
        mCodecHelper.release();
        mCodecHelper  = null;
        
        mDisplayView  = null;
        mPreviewView  = null;
        mResolution   = null;

        super.onDestroy();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
    	if (KeyEvent.KEYCODE_BACK == keyCode)
    	{
    		System.exit(0);
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
}
