package net.quantum6.codectest;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;

import net.quantum6.codectest.R;


/**
 * 安卓4.4上的。
 * 
 * @author PC
 *
 */
public final class SSActivity extends AbstractActivity implements OnItemSelectedListener
{
    private final static String TAG = SSActivity.class.getCanonicalName();
    

    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    @Override
    protected int getLayout()
    {
    	return R.layout.ss_activity;
    }
    
    @Override
    protected String getTitleString()
    {
    	return "SS";
    }
    
    @Override
    protected void initHelpers()
    {
    	if (null == mCodecHelper)
    	{
    		mCodecHelper	= new SurfaceViewCodecHelper();
    	}
    	if (null == mCameraHelper)
    	{
    		mCameraHelper	= new SurfaceViewCameraHelper();
    		
    	}
    	mCameraHelper.mCodecHelper = mCodecHelper;
    }
    
    @Override
    protected View initDisplayView()
    {
    	SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.displayview);
    	surfaceView.getHolder().addCallback((SurfaceViewCodecHelper)mCodecHelper);
    	return surfaceView;
    }
    
    @Override
    protected View initPreviewView()
    {
    	SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.preview);
    	surfaceView.setZOrderOnTop(true);
        SurfaceHolder previewHolder = surfaceView.getHolder();
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewHolder.addCallback((SurfaceViewCameraHelper)mCameraHelper);

        return surfaceView;
    }
    
    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
}
