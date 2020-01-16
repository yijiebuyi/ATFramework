package com.callme.platform.widget.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.callme.platform.glsrender.core.BitmapTileProvider;
import com.callme.platform.glsrender.core.GLImageView;
import com.callme.platform.glsrender.core.TileImageView;
import com.callme.platform.glsrender.gl11.GLRootView;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：
 * 作者：huangyong
 * 创建时间：2018/1/24
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class PhotoView extends GLRootView {
    private GLImageView mImageView;

    public PhotoView(Context context) {
        super(context);
        init(context);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mImageView = new GLImageView(context);
        setContentPane(mImageView);
    }

    public void onResume() {
        super.onResume();

        lockRenderThread();
        try {
            mImageView.resume();
        } finally {
            unlockRenderThread();
        }
    }

    public void onPause() {
        super.onPause();

        lockRenderThread();

        try {
            mImageView.pause();
        } finally {
            unlockRenderThread();
        }
    }

    public void setImageBitmap(Bitmap bmp) {
        setImageBitmap(bmp, 0);
    }

    public void setImageBitmap(Bitmap bmp, int rotation) {
        mImageView.setDataModel(new BitmapTileProvider(bmp, 512), rotation);
    }


    public void setDataModel(TileImageView.Model dataModel) {
        setDataModel(dataModel, 0);
    }

    public void setDataModel(TileImageView.Model dataModel, int rotation) {
        mImageView.setDataModel(dataModel, rotation);
    }

}
