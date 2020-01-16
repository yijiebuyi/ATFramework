package com.callme.platform.util;

import android.content.Context;
import android.widget.ImageView;

import com.callme.platform.util.bitmap.ImageLoader;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：图片加载显示工具类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class CmImageLoader {

    public static void displayImage(Context ctx, String url, ImageView view, int optionIndex) {
        ImageLoader.getInstance(ctx).displayImage(url, view, BitmapOptions.getOption(optionIndex));
    }

//    public static void displayImageWithGlide(Context ctx, String url, ImageView view, int imgId) {
//        Glide.with(ctx).load(url).bitmapTransform(new GlideCircleTransform(ctx))
//                .diskCacheStrategy(DiskCacheStrategy.RESULT).placeholder(imgId).error(imgId).into(view);
//    }
}
