package com.callme.platform.util.cookie;

import android.text.TextUtils;

import com.callme.platform.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：cookie的封装
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class Cookie {
	private static final String TAG = "Cookie";

	private String mName;

	private String mValue;

	private String mDomain;

	private String mPath = "/";

	private boolean mDiscard;

	private long mCreatedAt = 0;

	private long mMaxAge = MAX_AGE_UNSPECIFIED;

	private int mVersion;

	public final static String SET_COOKIE = "Set-Cookie";
	public final static String SET_COOKIE2 = "Set-Cookie2";

	private final static long MAX_AGE_UNSPECIFIED = -1;
	private final static String tspecials = ",;";
	public final static String NETSCAPE_COOKIE_DATE_FORMAT = "EEE, dd-MMM-yy HH:mm:ss 'GMT'";
	private final static String SPECIAL_COOKIE_DATE_FORMAT = "EEE, dd MMM yy HH:mm:ss 'GMT'";

	private static SimpleDateFormat df = new SimpleDateFormat(
			NETSCAPE_COOKIE_DATE_FORMAT, Locale.ENGLISH);
	private static SimpleDateFormat spdf = new SimpleDateFormat(
			SPECIAL_COOKIE_DATE_FORMAT, Locale.ENGLISH);
	static {
		df.setTimeZone(TimeZone.getTimeZone("GMT-8"));
		spdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
	}

	public Cookie() {
	}

	public Cookie(String name, String value) {
		name = name.trim();
		if (name.length() == 0 || !isToken(name))
			throw new IllegalArgumentException("Illegal cookie name");

		mName = name;
		mValue = value;
		mDiscard = false;

		mCreatedAt = System.currentTimeMillis();
	}

	/**
	 * 解析 Cookie
	 *
	 * @param cookieStr
	 */
	public static List<Cookie> parse(String cookieStr) {
		if (cookieStr == null || cookieStr.length() == 0)
			return null;

		int version = guessCookieVersion(cookieStr);
		boolean isQ = false;

		// if header start with set-cookie or set-cookie2, strip it off
		if (startsWithIgnoreCase(cookieStr, SET_COOKIE2)) {
			cookieStr = cookieStr.substring(SET_COOKIE2.length() + 1);
		} else if (startsWithIgnoreCase(cookieStr, SET_COOKIE)) {
			cookieStr = cookieStr.substring(SET_COOKIE.length() + 1);
		}

		List<Cookie> cookies = new java.util.ArrayList<Cookie>();
		// The Netscape cookie may have a comma in its expires attribute,
		// while the comma is the delimiter in rfc 2965/2109 cookie header
		// string.
		// so the parse logic is slightly different
		if (version == 0) {
			// Netscape draft cookie
			Cookie cookie = parseInternal(cookieStr, isQ);
			cookies.add(cookie);
		} else {
			// rfc2965/2109 cookie
			// if header string contains more than one cookie,
			// it'll separate them with comma
			List<String> cookieArray = splitMultiCookies(cookieStr);
			for (String str : cookieArray) {
				Cookie cookie = parseInternal(str, isQ);
				cookies.add(cookie);
			}
		}

		return cookies;
	}

	private static List<String> splitMultiCookies(String header) {
		List<String> cookies = new java.util.ArrayList<String>();
		int quoteCount = 0;
		int p, q;

		for (p = 0, q = 0; p < header.length(); p++) {
			char c = header.charAt(p);
			if (c == '"')
				quoteCount++;
			if (c == ',' && (quoteCount % 2 == 0)) { // it is comma and not
														// surrounding by
														// double-quotes
				cookies.add(header.substring(q, p));
				q = p + 1;
			}
		}
		cookies.add(header.substring(q));
		return cookies;
	}

	private static int guessCookieVersion(String cookieStr) {
		int version = 0;

		cookieStr = cookieStr.toLowerCase();
		if (cookieStr.indexOf("expires=") != -1) {
			// only netscape cookie using 'expires'
			version = 0;
		} else if (cookieStr.indexOf("version=") != -1) {
			// version is mandatory for rfc 2965/2109 cookie
			version = 1;
		} else if (cookieStr.indexOf("max-age") != -1) {
			// rfc 2965/2109 use 'max-age'
			version = 1;
		} else if (startsWithIgnoreCase(cookieStr, SET_COOKIE2)) {
			// only rfc 2965 cookie starts with 'set-cookie2'
			version = 1;
		}
		return version;
	}

	private static boolean startsWithIgnoreCase(String s, String start) {
		if (s == null || start == null)
			return false;

        return s.length() >= start.length()
                && start.equalsIgnoreCase(s.substring(0, start.length()));
    }

	public static String getCookieValue(String cookieStr, String cookieName) {
		if (TextUtils.isEmpty(cookieStr) || TextUtils.isEmpty(cookieName)) {
			return null;
		}
		String namevaluePair = null;
		StringTokenizer tokenizer = new StringTokenizer(cookieStr, ";");

		try {
			namevaluePair = tokenizer.nextToken();
			int index = namevaluePair.indexOf('=');
			if (index != -1) {
				String name = namevaluePair.substring(0, index).trim();
				if (cookieName.equalsIgnoreCase(name)) {
					String value = namevaluePair.substring(index + 1).trim();
					return stripOffSurroundingQuote(value);
				}
			} else {
				// no "=" in name-value pair; it's an error
				// throw new IllegalArgumentException(
				// "Invalid cookie name-value pair");
			}
		} catch (NoSuchElementException ignored) {
			// throw new IllegalArgumentException("Empty cookie header string");
		}

		// remaining name-value pairs are cookie's attributes
		while (tokenizer.hasMoreTokens()) {
			namevaluePair = tokenizer.nextToken();
			int index = namevaluePair.indexOf('=');
			String name, value;
			if (index != -1) {
				name = namevaluePair.substring(0, index).trim();
				value = namevaluePair.substring(index + 1).trim();
				if (cookieName.equalsIgnoreCase(name)) {
					return stripOffSurroundingQuote(value);
				}
			}

		}
		return null;
	}

	private static Cookie parseInternal(String cookieStr, boolean isQ) {
		Cookie cookie = null;
		String namevaluePair = null;

		StringTokenizer tokenizer = new StringTokenizer(cookieStr, ";");

		// there should always have at least on name-value pair;
		// it's cookie's name
		try {
			namevaluePair = tokenizer.nextToken();
			int index = namevaluePair.indexOf('=');
			if (index != -1) {
				String name = namevaluePair.substring(0, index).trim();
				String value = namevaluePair.substring(index + 1).trim();
				cookie = new Cookie(name, stripOffSurroundingQuote(value));
			} else {
				// no "=" in name-value pair; it's an error
				throw new IllegalArgumentException(
						"Invalid cookie name-value pair");
			}
		} catch (NoSuchElementException ignored) {
			throw new IllegalArgumentException("Empty cookie header string");
		}

		// remaining name-value pairs are cookie's attributes
		while (tokenizer.hasMoreTokens()) {
			namevaluePair = tokenizer.nextToken();
			int index = namevaluePair.indexOf('=');
			String name, value;
			if (index != -1) {
				name = namevaluePair.substring(0, index).trim();
				value = namevaluePair.substring(index + 1).trim();
			} else {
				name = namevaluePair.trim();
				value = null;
			}

			// assign attribute to cookie
			assignAttribute(cookie, name, value);
		}
		return cookie;
	}

	private static void assignAttribute(Cookie cookie, String attrName,
			String attrValue) {
		// strip off the surrounding "-sign if there's any
		attrValue = stripOffSurroundingQuote(attrValue);

		CookieAttributeAssignor assignor = assignors
				.get(attrName.toLowerCase());
		if (assignor != null) {
			assignor.assign(cookie, attrName, attrValue);
		} else {
			// must be an error
			LogUtil.d(TAG, "Ignore attr : " + attrName);
			// throw new IllegalArgumentException("Illegal cookie attribute");
		}
	}

	/*
	 * assign cookie attribute value to attribute name; use a map to simulate
	 * method dispatch
	 */
    interface CookieAttributeAssignor {
		void assign(Cookie cookie, String attrName, String attrValue);
	}

	static java.util.Map<String, CookieAttributeAssignor> assignors = null;
	static {
		assignors = new java.util.HashMap<String, CookieAttributeAssignor>();
		assignors.put("discard", new CookieAttributeAssignor() {
			@Override
			public void assign(Cookie cookie, String attrName, String attrValue) {
				cookie.setDiscard(true);
			}
		});
		assignors.put("domain", new CookieAttributeAssignor() {
			@Override
			public void assign(Cookie cookie, String attrName, String attrValue) {
				if (cookie.getDomain() == null)
					cookie.setDomain(attrValue);
			}
		});
		assignors.put("max-age", new CookieAttributeAssignor() {
			@Override
			public void assign(Cookie cookie, String attrName, String attrValue) {
				try {
					long maxage = Long.parseLong(attrValue);
					if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED)
						cookie.setMaxAge(maxage);
				} catch (NumberFormatException ignored) {
					throw new IllegalArgumentException(
							"Illegal cookie max-age attribute");
				}
			}
		});
		assignors.put("path", new CookieAttributeAssignor() {
			@Override
			public void assign(Cookie cookie, String attrName, String attrValue) {
				if (cookie.getPath() == null)
					cookie.setPath(attrValue);
			}
		});
		assignors.put("expires", new CookieAttributeAssignor() {
			// Netscape only
			@Override
			public void assign(Cookie cookie, String attrName, String attrValue) {
				LogUtil.d(TAG, "Set Cookie Expires : " + attrValue);
				LogUtil.d(TAG, "cookie.getMaxAge() : " + cookie.getMaxAge());
				// 本地永远不过期
				// if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED) {
				// cookie.setMaxAge(cookie.expiryDate2DeltaSeconds(attrValue));
				// }
			}
		});
		assignors.put("version", new CookieAttributeAssignor() {
			@Override
			public void assign(Cookie cookie, String attrName, String attrValue) {
				try {
					int version = Integer.parseInt(attrValue);
					cookie.setVersion(version);
				} catch (NumberFormatException ignored) {
					throw new IllegalArgumentException(
							"Illegal cookie version attribute");
				}
			}
		});
	}

	private static String stripOffSurroundingQuote(String str) {
		if (str != null && str.length() > 0 && str.charAt(0) == '"'
				&& str.charAt(str.length() - 1) == '"') {
			return str.substring(1, str.length() - 1);
		} else {
			return str;
		}
	}

	private static boolean isToken(String value) {
		int len = value.length();

		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);

			if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
				return false;
		}
		return true;
	}

	public boolean hasExpired() {
		if (mMaxAge == 0)
			return true;

		// if not specify max-age, this cookie should be
		// discarded when user agent is to be closed, but
		// it is not expired.
		if (mMaxAge == MAX_AGE_UNSPECIFIED)
			return false;

		long deltaSecond = (System.currentTimeMillis() - mCreatedAt) / 1000;
        return deltaSecond > mMaxAge;
	}

	public boolean domainMatches(String host) {
		if (mDomain.equals(host))
			return true;

		return host.endsWith(mDomain) || host.equals(mDomain.substring(1));
	}

	private static boolean equals(String s, String t) {
		if (s == t)
			return true;
		if ((s != null) && (t != null)) {
			return s.equals(t);
		}
		return false;
	}

	private static boolean equalsIgnoreCase(String s, String t) {
		if (s == t)
			return true;
		if ((s != null) && (t != null)) {
			return s.equalsIgnoreCase(t);
		}
		return false;
	}

	/*
	 * Constructs a string representation of this cookie. The string format is
	 * as Netscape spec, but without leading "Cookie:" token.
	 */
	private String toNetscapeHeaderString() {
		return getName() + "=" + getValue();
	}

	/*
	 * Constructs a string representation of this cookie. The string format is
	 * as RFC 2965/2109, but without leading "Cookie:" token.
	 */
	private String toRFC2965HeaderString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getName()).append("=\"").append(getValue()).append('"');
		if (getPath() != null)
			sb.append(";$Path=\"").append(getPath()).append('"');
		if (getDomain() != null)
			sb.append(";$Domain=\"").append(getDomain()).append('"');

		return sb.toString();
	}

	@Override
	public String toString() {
		if (getVersion() > 0)
			return toRFC2965HeaderString();
		else
			return toNetscapeHeaderString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Cookie))
			return false;
		Cookie other = (Cookie) obj;

		// One http cookie equals to another cookie (RFC 2965 sec. 3.3.3) if:
		// 1. they come from same domain (case-insensitive),
		// 2. have same name (case-insensitive),
		// 3. and have same path (case-sensitive).
		return equalsIgnoreCase(getName(), other.getName())
				&& equalsIgnoreCase(getDomain(), other.getDomain())
				&& equals(getPath(), other.getPath());
	}

	@Override
	public int hashCode() {
		int hash = 1;
		if (mName != null) {
			hash = hash * 31 + mName.toLowerCase().hashCode();
		}
		if (mDomain != null) {
			hash = hash * 31 + mDomain.toLowerCase().hashCode();
		}
		if (mPath != null) {
			hash = hash * 31 + mPath.toLowerCase().hashCode();
		}
		return hash;
	}

	public long expiryDate2DeltaSeconds(String dateString) {
		try {
			// System.out.println("expiryDate2DeltaSeconds : " + dateString);
			// System.out.println("whenCreated : " + whenCreated);

			Date date = null;
			if (dateString.indexOf('-') == -1)
				date = spdf.parse(dateString);
			else
				date = df.parse(dateString);

			// System.out.println("date.getTime : " + date.getTime());
			if (date != null) {
				return (date.getTime() - mCreatedAt) / 1000;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getValue() {
		return mValue;
	}

	public void setValue(String value) {
		this.mValue = value;
	}

	public String getDomain() {
		return mDomain;
	}

	public void setDomain(String domain) {
		if (!domain.startsWith("."))
			domain = '.' + domain;

		this.mDomain = domain;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
	}

	public boolean getDiscard() {
		return mDiscard;
	}

	public void setDiscard(boolean discard) {
		mDiscard = discard;
	}

	public long getMaxAge() {
		return mMaxAge;
	}

	public void setMaxAge(long maxAge) {
		mMaxAge = maxAge;
	}

	public long getCreatedAt() {
		return mCreatedAt;
	}

	public void setCreatedAt(long createdAt) {
		mCreatedAt = createdAt;
	}

	public int getVersion() {
		return mVersion;
	}

	public void setVersion(int version) {
		mVersion = version;
	}
}
