package com.callme.platform.util.cookie;

import com.callme.platform.util.LogUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：cookie的管理类
 * 作者：mikeyou
 * 创建时间：2014-11-11
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class CookieManager {
	private static final String TAG = "CookieManager";

	private static CookieManager mCookieManager;
	private CookieStore mCookieStore;

	public CookieManager() {
		mCookieStore = new CookieStore();
	}

	public static CookieManager getInstance() {
		if (mCookieManager == null) {
			mCookieManager = new CookieManager();
		}
		return mCookieManager;
	}

	public void load() {
		try {
			mCookieStore.load();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public void shutdown() {
		save();
	}

	/**
	 * Cookie 回写 防止程序被后台杀掉，或者CRASH的情况Cookie丢失
	 */
	public void save() {
		try {
			mCookieStore.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 清除Cookie
	 *
	 * @return
	 */
	public boolean clearCookies() {
		try {
			mCookieStore.clear();
			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public String get(URL url) {
		if (url == null)
			return null;
		
		List<Cookie> cookies = mCookieStore.get(url.getHost());

		LogUtil.d(TAG, "get(URL) : " + url);
		
		if (cookies == null)
			return null;
		
		for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
			Cookie cookie = it.next();

			LogUtil.d(TAG,
					"Get Cookie : " + cookie + " - " + cookie.getDomain()
							+ " - " + cookie.getMaxAge());

			// apply path-matches rule (RFC 2965 sec. 3.3.4)
			LogUtil.d(TAG, "url.getPath : " + url.getPath());
			LogUtil.d(TAG, "cookie : " + cookie.getPath());
			if (!pathMatches(url.getPath(), cookie.getPath())) {
				LogUtil.d(TAG, "REMOVE");
				it.remove();
			}
		}

		// apply sort rule (RFC 2965 sec. 3.3.4)
		return inflate(cookies);
	}

	/**
	 * HttpClient 版本
	 *
	 * @param url
	 * @param responseHeaders
	 */
//	public boolean put(URL url, Header[] responseHeaders) {
//		// 是否需要回写Cookie，处理当程序Crash或者切到后台被系统杀掉，Cookie丢失的问题
//		boolean isNeedBackWrite = false;
//
//		// pre-condition check
//		if (url == null || responseHeaders == null)
//			throw new IllegalArgumentException("Argument is null");
//
//		for (Header header : responseHeaders) {
//
//			// RFC 2965 3.2.2, key must be 'Set-Cookie2'
//			// we also accept 'Set-Cookie' here for backward compatibility
//			if (header == null)
//				continue;
//			LogUtil.d(TAG, header.toString());
//
//			String headerKey = header.getName();
//			// 屏蔽set-cookie2
//			if (headerKey == null
//					|| !(headerKey.equalsIgnoreCase(Cookie.SET_COOKIE)))
//				continue;
//
//			// 有Cookie，此时置标记为true，表示需要回写
//			isNeedBackWrite = true;
//
//			LogUtil.d(TAG, "put(URL) : " + url);
//			try {
//				String headerValue = header.getValue();
//				LogUtil.d(TAG, "headerKey : " + headerKey);
//				LogUtil.d(TAG, "headerValue : " + headerValue);
//
//				List<Cookie> cookies = Cookie.parse(headerKey + ":"
//						+ headerValue);
//				if (cookies != null) {
//					for (Cookie cookie : cookies) {
//						// 补 domain
//						if (cookie.getDomain() == null)
//							cookie.setDomain(url.getHost());
//
//						LogUtil.d(
//								TAG,
//								"Add Cookie : " + cookie + " - "
//										+ cookie.getDomain() + " - "
//										+ cookie.getMaxAge());
//
//						mCookieStore.addCookie(cookie);
//					}
//				}
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//				// invalid set-cookie header string
//				// no-op
//			}
//		}
//
//		return isNeedBackWrite;
//	}

	public boolean put(URL url, Map<String, List<String>> responseHeaders) {
		// 是否需要回写Cookie，处理当程序Crash或者切到后台被系统杀掉，Cookie丢失的问题
		boolean isNeedBackWrite = false;

		// pre-condition check
		if (url == null || responseHeaders == null)
			throw new IllegalArgumentException("Argument is null");

		for (String headerKey : responseHeaders.keySet()) {
			// Logger.d(TAG, headerKey);
			// RFC 2965 3.2.2, key must be 'Set-Cookie2'
			// we also accept 'Set-Cookie' here for backward compatibility
			// 当header != set-cookie && != set-cookie2
			if (headerKey == null
					|| !(headerKey.equalsIgnoreCase(Cookie.SET_COOKIE) || headerKey
							.equalsIgnoreCase(Cookie.SET_COOKIE2)))
				continue;

			// 有Cookie，此时置标记为true，表示需要回写
			isNeedBackWrite = true;

			// Logger.d(TAG, "put(URL) : " + url);
			for (String headerValue : responseHeaders.get(headerKey)) {
				try {
					LogUtil.d(TAG, "headerKey : " + headerKey);
					LogUtil.d(TAG, "headerValue : " + headerValue);
					List<Cookie> cookies = Cookie.parse(headerKey + ":"
							+ headerValue);
					if (cookies != null) {
						for (Cookie cookie : cookies) {
							// 补 domain
							if (cookie.getDomain() == null)
								cookie.setDomain(url.getHost());
							LogUtil.d(
									TAG,
									"Add Cookie : " + cookie + " - "
											+ cookie.getDomain() + " - "
											+ cookie.getMaxAge());

							mCookieStore.addCookie(cookie);
						}
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					// invalid set-cookie header string
					// no-op
				}
			}
		}

		return isNeedBackWrite;
	}

	/*
	 * path-matches algorithm, as defined by RFC 2965
	 */
	private boolean pathMatches(String path, String pathToMatchWith) {
		if (path == pathToMatchWith)
			return true;
		if (path == null || pathToMatchWith == null)
			return false;

		if (path == null || path.length() == 0)
			path = "/";

        return path.startsWith(pathToMatchWith);

    }

	/*
	 * sort cookies with respect to their path: those with more specific Path
	 * attributes precede those with less specific, as defined in RFC 2965 sec.
	 * 3.3.4
	 */
	private String inflate(List<Cookie> cookies) {
		StringBuilder sb = new StringBuilder();
		for (Cookie cookie : cookies) {
			// Netscape cookie spec and RFC 2965 have different format of Cookie
			// header; RFC 2965 requires a leading $Version="1" string while
			// Netscape
			// does not.
			// The workaround here is to add a $Version="1" string in advance
			if (cookies.indexOf(cookie) == 0 && cookie.getVersion() > 0) {
				sb.append("$Version=\"1\"");
			}

			sb.append(cookie.toString());
			sb.append(';');
		}
		return sb.toString();
	}

	static class CookiePathComparator implements Comparator<Cookie> {
		@Override
		public int compare(Cookie c1, Cookie c2) {
			if (c1 == c2)
				return 0;
			if (c1 == null)
				return -1;
			if (c2 == null)
				return 1;

			// path rule only applies to the cookies with same name
			if (!c1.getName().equals(c2.getName()))
				return 0;

			// those with more specific Path attributes precede those with less
			// specific
			if (c1.getPath().startsWith(c2.getPath()))
				return -1;
			else if (c2.getPath().startsWith(c1.getPath()))
				return 1;
			else
				return 0;
		}
	}
	
	
	/**
	 * 根据名称获取cookie的值
	 * @param url
	 * @param name
	 * @return
	 */
	public String getCookieValue(URL url,String name){
		Cookie cookie = mCookieStore.getByKey(url.getHost(), name);
		return cookie == null?null:cookie.getValue();
	}
}
