package com.callme.platform.glsrender.gl11;

import android.graphics.Bitmap;

import com.callme.platform.glsrender.core.Utils;

public class BitmapTexture extends UploadedTexture {
    protected Bitmap mContentBitmap;

    public BitmapTexture(Bitmap bitmap) {
        this(bitmap, false);
    }

    public BitmapTexture(Bitmap bitmap, boolean hasBorder) {
        super(hasBorder);
        Utils.assertTrue(bitmap != null && !bitmap.isRecycled());
        mContentBitmap = bitmap;
    }

    @Override
    protected void onFreeBitmap(Bitmap bitmap) {
        // Do nothing.
    }

    @Override
    protected Bitmap onGetBitmap() {
        return mContentBitmap;
    }

    public Bitmap getBitmap() {
        return mContentBitmap;
    }
}