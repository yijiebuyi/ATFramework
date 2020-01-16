package com.callme.platform.util.http.core;

import java.io.IOException;


/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：网络请求的基础抽象类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public abstract class HttpStack {
	/**
	 * Performs an HTTP request with the given parameters.
	 * 
	 * <p>
	 * A GET request is sent if request.getPostBody() == null. A POST request is
	 * sent otherwise, and the Content-Type header is set to
	 * request.getPostBodyContentType().
	 * </p>
	 * 
	 * @param request
	 *            the request to perform
	 * @return the HTTP response
	 */
	public abstract HttpResponse performRequest(Request request) throws IOException;

	protected HttpUrlRequestInterceptor requestInterceptor;
	protected HttpUrlResponseInterceptor responseInterceptor;

	/**
	 * 添加请求拦截器
	 * 
	 * @param requestInterceptor
	 */
	public void addRequestInterceptor(HttpUrlRequestInterceptor requestInterceptor) {
		this.requestInterceptor = requestInterceptor;
	}

	/**
	 * 添加响应拦截器
	 * 
	 * @param responseInterceptor
	 */
	public void addResponseInterceptor(HttpUrlResponseInterceptor responseInterceptor) {
		this.responseInterceptor = responseInterceptor;
	}

}
