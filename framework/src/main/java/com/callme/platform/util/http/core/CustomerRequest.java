package com.callme.platform.util.http.core;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：Request的实现类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class CustomerRequest extends Request {

    private static final String URL_CONTENT_TYPE = "UTF-8";
    private static final String MULTI_CONTENT_TYPE = "";

    private Map<String, String> headers;

    public CustomerRequest(int method, String url) {
        super(method, url);
        headers = new HashMap<String, String>();
    }

    @Override
    public String getBodyContentType() {
        if (mParams != null) {
            if (mParams.isStringParam()) {
                return URL_CONTENT_TYPE;
            } else {
                return MULTI_CONTENT_TYPE;
            }
        }

        return null;
    }

    @Override
    public byte[] getBody() {
//		if (mParams != null) {
//			return mParams.formatPostParam();
//		}
        if (mParams != null) {
            try {
                return mParams.formatGetParam(false).getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    @Override
    public String makeGetParams() {
        if (mParams != null) {
            return mParams.formatGetParam(true);
        }

        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        // TODO Auto-generated method stub
        return headers;
    }

    @Override
    public void addHeader(String key, String value) {
        if (headers != null) {
            headers.put(key, value);
        }
    }

    @Override
    public boolean containsHeader(String key) {
        return headers.containsKey(key);
    }

    @Override
    public void removeHeader(String key) {
        headers.remove(key);
    }

}
