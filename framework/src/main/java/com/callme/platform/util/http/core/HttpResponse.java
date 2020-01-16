package com.callme.platform.util.http.core;

import java.util.HashMap;
import java.util.Map;

/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：网络请求后的响应封装实体
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class HttpResponse {
	public static final int SC_MOVED_PERMANENTLY = 301;
	public static final int SC_MOVED_TEMPORARILY = 302;
	public static final int SC_NOT_MODIFIED = 304;
	public static final int SC_FORBIDDEN = 403;
	public static final int SC_UNAUTHORIZED = 401;
	public static final int SC_OK = 200;

	private int responseCode = -1;
	private String responseMessage;
	private HttpEntity entityFromConnection;
	private Map<String, String> httpHeaders = new HashMap<String, String>();

	public HttpResponse(int responseCode, String responseMessage) {
		this.setResponseCode(responseCode);
		this.setResponseMessage(responseMessage);
	}

	public void setEntity(HttpEntity entityFromConnection) {
		this.entityFromConnection = entityFromConnection;
	}

	public void addHeader(String key, String value) {
		httpHeaders.put(key, value);
	}

	public String getHeader(String name){
		return httpHeaders.get(name);
	}
	public Map<String, String> getAllHeaders() {
		return httpHeaders;
	}

	public HttpEntity getEntity() {
		return entityFromConnection;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

}
