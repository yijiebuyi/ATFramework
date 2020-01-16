package com.callme.platform.widget.crop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import com.callme.platform.glsrender.core.DecodeUtils;
import com.callme.platform.util.thdpool.ThreadPool;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：图片加载
 * 作者：huangyong
 * 创建时间：2018/1/24
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class LocalImageRequest implements ThreadPool.Job<Bitmap> {
    private static final String TAG = "ImageCacheRequest";
    private static final int TARGET_SIZE = 1080;

    private String mLocalFilePath;
    private int mType;

    public LocalImageRequest(String path, int type) {
        mLocalFilePath = path;
        mType = type;
    }

    @Override
    public Bitmap run(ThreadPool.JobContext jc) {
        Bitmap bitmap = onDecodeOriginal(jc, mType);
        if (jc.isCancelled()) return null;

        if (bitmap == null) {
            return null;
        }

        return bitmap;
    }

    public Bitmap onDecodeOriginal(ThreadPool.JobContext jc, int type) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int targetSize = TARGET_SIZE;

        // try to decode from JPEG EXIF
        if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
            ExifInterface exif = null;
            byte[] thumbData = null;
            try {
                exif = new ExifInterface(mLocalFilePath);
                if (exif != null) {
                    thumbData = exif.getThumbnail();
                }
            } catch (Throwable t) {
                Log.w(TAG, "fail to get exif thumb", t);
            }
            if (thumbData != null) {
                Bitmap bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                if (bitmap != null) return bitmap;
            }
        }

        return DecodeUtils.decodeThumbnail(jc, mLocalFilePath, options, targetSize, type);
    }

}
