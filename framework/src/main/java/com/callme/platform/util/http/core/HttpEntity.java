package com.callme.platform.util.http.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：网络请求返回后的封装实体
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class HttpEntity {
	public static final String DEFAULT_CHARSET = "UTF-8";
	protected static final int OUTPUT_BUFFER_SIZE = 4096;

	public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String APPLICATION_ATOM_XML = "application/atom+xml";
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String APPLICATION_SVG_XML = "application/svg+xml";
	public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";
	public static final String APPLICATION_XML = "application/xml";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_PLAIN = "text/plain";
	public static final String TEXT_XML = "text/xml";
	public static final String WILDCARD = "*/*";

	private InputStream inputStream;
	private int contentLength;
	private String contentEncoding;
	private String contentType;
	private String contentValue;
	
	public static String createContentType(String typeValue, String charset) {
		return typeValue + "; charset=" + charset;
	}

	public HttpEntity() {

	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContent(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getContentLength() {
		return contentLength;
	}

	public InputStream getContent() throws IOException {
		return inputStream;
	}

	public void consumeContent() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeTo(ByteArrayOutputStream baos) throws IOException {
		final InputStream instream = getContent();
		if (baos != null && instream != null) {
			try {
				int l;
				final byte[] tmp = new byte[OUTPUT_BUFFER_SIZE];
				while ((l = instream.read(tmp)) != -1) {
					baos.write(tmp, 0, l);
				}
			} finally {
				instream.close();
			}
		}
	}

	public String getContentValue() {
		return contentValue;
	}

	public void setContentValue(String contentValue) {
		this.contentValue = contentValue;
	}

	
}
