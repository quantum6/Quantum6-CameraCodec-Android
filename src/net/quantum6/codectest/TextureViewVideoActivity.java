package net.quantum6.codectest;


import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import net.quantum6.codectest.R;

/**
 * 安卓4.4上的。
 * 
 * @author PC
 *
 */
public final class TextureViewVideoActivity extends AbstractVideoActivity
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
            mCodecHelper    = new TextureViewCodecHelper();
        }
        if (null == mCameraHelper)
        {
            mCameraHelper   = new TextureViewCameraHelper();
        }
        mCameraHelper.mCodecHelper = mCodecHelper;
    }
    
    @Override
    protected View initDisplayView()
    {
        SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.displayview);
        surfaceView.getHolder().addCallback((TextureViewCodecHelper)mCodecHelper);
        return surfaceView;
    }
    
    @Override
    protected View initPreviewView()
    {
        SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.preview);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder previewHolder = surfaceView.getHolder();
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewHolder.addCallback((TextureViewCameraHelper)mCameraHelper);

        return surfaceView;
    }
    //}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}
    
}
