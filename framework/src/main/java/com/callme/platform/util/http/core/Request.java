package com.callme.platform.util.http.core;

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.callme.platform.util.http.RequestParams;
import com.callme.platform.util.http.core.VolleyLog.MarkerLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;


/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：Request的基础类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public abstract class Request {

	/**
	 * Default encoding for POST or PUT parameters. See
	 * {@link #getParamsEncoding()}.
	 */
	private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

	/**
	 * Supported request methods.
	 */
	public abstract static class Method {
		public static final int GET = 0;
		public static final int POST = 1;

		public static String getMethodName(int method){
			String res = "GET";
			switch (method){
				case GET:
					res = "GET";
					break;
				case POST:
					res = "POST";
					break;
				default:
					break;
			}
			return res;
		}
	}

	/** An event log tracing the lifetime of this request; for debugging. */
	private final MarkerLog mEventLog = MarkerLog.ENABLED ? new MarkerLog() : null;

	/**
	 * Request method of this request. Currently supports GET, POST, PUT,
	 * DELETE, HEAD, OPTIONS, TRACE, and PATCH.
	 */
	private int mMethod;

	/** URL of this request. */
	private String mUrl;

	protected RequestParams mParams;

	public void setmMethod(int mMethod) {
		this.mMethod = mMethod;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public RequestParams getmParams() {
		return mParams;
	}

	public void setmParams(RequestParams mParams) {
		this.mParams = mParams;
	}

	/** The redirect url to use for 3xx http responses */
	private String mRedirectUrl;

	/** Default tag for {@link TrafficStats}. */
	private final int mDefaultTrafficStatsTag;

	/** Whether or not responses to this request should be cached. */
	private boolean mShouldCache = true;

	/** Whether or not this request has been canceled. */
	private boolean mCanceled = false;

	// A cheap variant of request tracing used to dump slow requests.
	private long mRequestBirthTime = 0;

	/**
	 * Threshold at which we should log the request (even when debug logging is
	 * not enabled).
	 */
	private static final long SLOW_REQUEST_THRESHOLD_MS = 3000;

	private int mTimeoutMs;

	/**
	 * Creates a new request with the given method (one of the values from
	 * {@link Method}), URL, and error listener. Note that the normal response
	 * listener is not provided here as delivery of responses is provided by
	 * subclasses, who have a better idea of how to deliver an already-parsed
	 * response.
	 */
	public Request(int method, String url) {
		mMethod = method;
		mUrl = url;

		mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
	}

	/**
	 * Return the method for this request. Can be one of the values in
	 * {@link Method}.
	 */
	public int getMethod() {
		return mMethod;
	}

	public String getMethodName(){

		return  Method.getMethodName(mMethod);
	}

	/**
	 * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
	 */
	public int getTrafficStatsTag() {
		return mDefaultTrafficStatsTag;
	}

	/**
	 * @return The hashcode of the URL's host component, or 0 if there is none.
	 */
	private static int findDefaultTrafficStatsTag(String url) {
		if (!TextUtils.isEmpty(url)) {
			Uri uri = Uri.parse(url);
			if (uri != null) {
				String host = uri.getHost();
				if (host != null) {
					return host.hashCode();
				}
			}
		}
		return 0;
	}

	/**
	 * Adds an event to this request's event log; for debugging.
	 */
	public void addMarker(String tag) {
		if (MarkerLog.ENABLED) {
			mEventLog.add(tag, Thread.currentThread().getId());
		} else if (mRequestBirthTime == 0) {
			mRequestBirthTime = SystemClock.elapsedRealtime();
		}
	}

	/**
	 * Notifies the request queue that this request has finished (successfully
	 * or with error).
	 * 
	 * <p>
	 * Also dumps all events from this request's event log; for debugging.
	 * </p>
	 */
	void finish(final String tag) {
		if (MarkerLog.ENABLED) {
			final long threadId = Thread.currentThread().getId();
			if (Looper.myLooper() != Looper.getMainLooper()) {
				// If we finish marking off of the main thread, we need to
				// actually do it on the main thread to ensure correct ordering.
				Handler mainThread = new Handler(Looper.getMainLooper());
				mainThread.post(new Runnable() {
					@Override
					public void run() {
						mEventLog.add(tag, threadId);
						mEventLog.finish(this.toString());
					}
				});
				return;
			}

			mEventLog.add(tag, threadId);
			mEventLog.finish(this.toString());
		} else {
			long requestTime = SystemClock.elapsedRealtime() - mRequestBirthTime;
			if (requestTime >= SLOW_REQUEST_THRESHOLD_MS) {
				VolleyLog.d("%d ms: %s", requestTime, this.toString());
			}
		}
	}

	/**
	 * Returns the URL of this request.
	 */
	public String getUrl() {
		String res = (mRedirectUrl != null) ? mRedirectUrl : mUrl;
		if (mMethod == Method.GET) {
			String param = makeGetParams();
			if (param != null) {
				res += param;
			}
		}
		return res;
	}

	/**
	 * Returns the URL of the request before any redirects have occurred.
	 */
	public String getOriginUrl() {
		return mUrl;
	}

	/**
	 * Sets the redirect url to handle 3xx http responses.
	 */
	public void setRedirectUrl(String redirectUrl) {
		mRedirectUrl = redirectUrl;
	}

	/**
	 * Returns the cache key for this request. By default, this is the URL.
	 */
	public String getCacheKey() {
		return getUrl();
	}

	/**
	 * Mark this request as canceled. No callback will be delivered.
	 */
	public void cancel() {
		mCanceled = true;
	}

	/**
	 * Returns true if this request has been canceled.
	 */
	public boolean isCanceled() {
		return mCanceled;
	}

	/**
	 * Returns a list of extra HTTP headers to go along with this request. Can
	 * provide these values.
	 * 
	 */
	public abstract Map<String, String> getHeaders();

	/**
	 * 添加header
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void addHeader(String key, String value);

	/**
	 * 是否包含某个header
	 * 
	 * @param key
	 * @return
	 */
	public abstract boolean containsHeader(String key);

	/**
	 * 删除hedaer
	 * 
	 * @param key
	 */
	public abstract void removeHeader(String key);

	/**
	 * Returns a Map of POST parameters to be used for this request, or null if
	 *
	 * <p>
	 * Note that only one of getPostParams() and getPostBody() can return a
	 * non-null value.
	 * </p>
	 * 
	 */
	protected Map<String, String> getPostParams() {
		return getParams();
	}

	/**
	 * Returns which encoding should be used when converting POST parameters
	 * returned by {@link #getPostParams()} into a raw POST body.
	 * 
	 * <p>
	 * This controls both encodings:
	 * <ol>
	 * <li>The string encoding used when converting parameter names and values
	 * into bytes prior to URL encoding them.</li>
	 * <li>The string encoding used when converting the URL encoded parameters
	 * into a raw byte array.</li>
	 * </ol>
	 * 
	 */
	protected String getPostParamsEncoding() {
		return getParamsEncoding();
	}

	/**
	 */
	public String getPostBodyContentType() {
		return getBodyContentType();
	}

	/**
	 * Returns the raw POST body to be sent.
	 * 
	 */
	public byte[] getPostBody() {
		// Note: For compatibility with legacy clients of volley, this
		// implementation must remain
		// here instead of simply calling the getBody() function because this
		// function must
		// call getPostParams() and getPostParamsEncoding() since legacy clients
		// would have
		// overridden these two member functions for POST requests.
		Map<String, String> postParams = getPostParams();
		if (postParams != null && postParams.size() > 0) {
			return encodeParameters(postParams, getPostParamsEncoding());
		}
		return null;
	}

	/**
	 * Returns a Map of parameters to be used for a POST or PUT request. Can
	 *
	 * <p>
	 * Note that you can directly override {@link #getBody()} for custom data.
	 * </p>
	 * 
	 */
	protected Map<String, String> getParams() {
		return null;
	}

	/**
	 * Returns which encoding should be used when converting POST or PUT
	 * parameters returned by {@link #getParams()} into a raw POST or PUT body.
	 * 
	 * <p>
	 * This controls both encodings:
	 * <ol>
	 * <li>The string encoding used when converting parameter names and values
	 * into bytes prior to URL encoding them.</li>
	 * <li>The string encoding used when converting the URL encoded parameters
	 * into a raw byte array.</li>
	 * </ol>
	 */
	protected String getParamsEncoding() {
		return DEFAULT_PARAMS_ENCODING;
	}

	/**
	 * Returns the content type of the POST or PUT body.
	 */
	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}

	/**
	 * Returns the raw POST or PUT body to be sent.
	 * 
	 * <p>
	 * By default, the body consists of the request parameters in
	 * application/x-www-form-urlencoded format. When overriding this method,
	 * consider overriding {@link #getBodyContentType()} as well to match the
	 * new body format.
	 * 
	 */
	public byte[] getBody() {
		Map<String, String> params = getParams();
		if (params != null && params.size() > 0) {
			return encodeParameters(params, getParamsEncoding());
		}
		return null;
	}

	/**
	 * Converts <code>params</code> into an application/x-www-form-urlencoded
	 * encoded string.
	 */
	private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
		StringBuilder encodedParams = new StringBuilder();
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
				encodedParams.append('&');
			}
			return encodedParams.toString().getBytes(paramsEncoding);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
		}
	}

	/**
	 * Set whether or not responses to this request should be cached.
	 * 
	 * @return This Request object to allow for chaining.
	 */
	public final Request setShouldCache(boolean shouldCache) {
		mShouldCache = shouldCache;
		return this;
	}

	/**
	 * Returns true if responses to this request should be cached.
	 */
	public final boolean shouldCache() {
		return mShouldCache;
	}

	/**
	 * Priority values. Requests will be processed from higher priorities to
	 * lower priorities, in FIFO order.
	 */
	public enum Priority {
		LOW, NORMAL, HIGH, IMMEDIATE
	}

	/**
	 * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by
	 * default.
	 */
	public Priority getPriority() {
		return Priority.NORMAL;
	}

	/**
	 * Returns the socket timeout in milliseconds per retry attempt.
	 */
	public final int getTimeoutMs() {
		return mTimeoutMs;
	}

	/**
	 * 设置超时时间，毫秒单位
	 * 
	 * @param timeout
	 */
	public final void setTimeoutMs(int timeout) {
		this.mTimeoutMs = timeout;
	}

	@Override
	public String toString() {
		String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
		return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " " + getPriority();
	}

	/**
	 * 生成get方法的参数
	 * 
	 * @return
	 */
	public abstract String makeGetParams();
}
