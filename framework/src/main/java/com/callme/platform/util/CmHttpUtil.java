package com.callme.platform.util;

import android.content.Context;

import com.callme.platform.util.http.HttpUtil;
import com.callme.platform.util.http.RequestCallBack;
import com.callme.platform.util.http.RequestListener;
import com.callme.platform.util.http.RequestParams;

import java.io.File;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：http请求类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class CmHttpUtil {

    private static HttpUtil mClient;
    private static CmHttpUtil mClientProxy;


    public static CmHttpUtil getInstance(Context ctx) {
        return getInstance(ctx, null, -1);
    }

    public static CmHttpUtil getInstance(Context ctx, int connTimeout) {
        return getInstance(ctx, null, connTimeout);
    }

    public static CmHttpUtil getInstance(Context ctx, String contentType, int connTimeout) {
        if (mClientProxy == null) {
            mClientProxy = new CmHttpUtil();
        }
        mClient = HttpUtil.getInstance(ctx, contentType, connTimeout);
        return mClientProxy;
    }


    public <T> String get(String url, CmRequestListener<T> listener) {

        return mClient.get(url, null, new RequestListener<T>(listener));
    }

    public <T> String get(String url, RequestParams param, CmRequestListener<T> listener) {
        return mClient.get(url, param, new RequestListener<T>(listener));
    }

    public <T> String post(String url, CmRequestListener<T> listener) {
        return mClient.post(url, null, new RequestListener<T>(listener));
    }

    public <T> String post(String url, RequestParams param, CmRequestListener<T> listener) {

        return mClient.post(url, param, new RequestListener<T>(listener));
    }

    public String download(String url, String target, RequestParams param, RequestCallBack<File> callback) {
        return mClient.download(url, target, param, callback);
    }
}
