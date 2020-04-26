package net.quantum6.cameracodec;


import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import net.quantum6.cameracodec.R;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 *
 */
public final class VideoCodecActivity extends AbstractVideoActivity
{

    //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
    
    @Override
    protected int getLayout()
    {
        return R.layout.video_activity;
    }
    
    @Override
    protected void initHelpers()
    {
        if (null == mCodecHelper)
        {
            mCodecHelper    = new SurfaceViewCodecHelper();
        }
        if (null == mCameraHelper)
        {
            mCameraHelper   = new SurfaceViewCameraHelper();
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
