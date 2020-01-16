package com.callme.platform.util.http;

import android.text.TextUtils;

import com.callme.platform.util.cache.JsonCacheImp;
import com.callme.platform.util.http.core.Request;

import java.util.concurrent.ConcurrentHashMap;

/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：http的缓存类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class HttpCache {

	/**
	 * key: url value: response result
	 */
	private final JsonCacheImp mCache = new JsonCacheImp();

	public void put(String url, String result) {
		if (url == null || result == null)
			return;

		mCache.put(url, result);
	}

	public String get(String url) {
		return (url != null) ? mCache.get(url) : null;
	}

	public void clear() {
		mCache.clear();
	}

	public boolean isEnabled(String method) {
		if (TextUtils.isEmpty(method))
			return false;

		Boolean enabled = httpMethod_enabled_map.get(method.toUpperCase());
		return enabled == null ? false : enabled;
	}

	public void setEnabled(String method, boolean enabled) {
		httpMethod_enabled_map.put(method.toString(), enabled);
	}

	private final static ConcurrentHashMap<String, Boolean> httpMethod_enabled_map;

	static {
		httpMethod_enabled_map = new ConcurrentHashMap<String, Boolean>(10);
		httpMethod_enabled_map.put(Request.Method.getMethodName(Request.Method.GET), true);
	}
}
