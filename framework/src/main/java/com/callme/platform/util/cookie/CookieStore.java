package com.callme.platform.util.cookie;

import com.callme.platform.util.FileUtil;
import com.callme.platform.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：cookie的存储类
 * 作者：mikeyou
 * 创建时间：2014-11-11
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class CookieStore {
	private static final String TAG = "CookieStore";

	private static final String FILE_COOKIE = "cookies.dat";

	// Version magic number write to the head of the file.
	private static final int VERSION_MAGIC = 0xccee0000;

	private final ArrayList<Cookie> cookies;

	private final Comparator<Cookie> cookieComparator;

	public CookieStore() {
		cookies = new ArrayList<Cookie>();
		cookieComparator = new CookieIdentityComparator();
	}

	/**
	 * 添加 cookie
	 *
	 * @param cookie
	 */
	public synchronized void addCookie(Cookie cookie) {
		if (cookie == null)
			return;

		// System.out.println("add cookie : " + cookie);

		// first remove any old cookie that is equivalent
		for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
			if (cookieComparator.compare(cookie, it.next()) == 0) {
				it.remove();
				break;
			}
		}
		if (!cookie.hasExpired()) {
			cookies.add(cookie);
		}
	}
	
	/**
	 * 根据name获取cookie
	 * @param host
	 * @param name
	 * @return
	 */
	public Cookie getByKey(String host,String name){
		for (Cookie cookie : cookies) {
			if(cookie.domainMatches(host) && cookie.getName().equalsIgnoreCase(name)){
				return cookie;
			}
		}
		
		return null;
	}

	/**
	 * 获取指定 host 的cookie列表
	 *
	 * @param host
	 * @return
	 */
	public synchronized List<Cookie> get(String host) {
		List<Cookie> cookieList = new ArrayList<Cookie>();

		for (Cookie cookie : cookies) {
			// LogUtil.d(TAG, cookie.getName());

			if (cookie.domainMatches(host)) {
				// LogUtil.d(TAG, "match : " + host);
				cookieList.add(cookie);
			}
		}

		return cookieList;
	}

	public synchronized boolean clearExpired() {
		boolean removed = false;
		for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
			if (it.next().hasExpired()) {
				it.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * 从文件系统载入Cookies
	 *
	 * @throws IOException
	 */
	public synchronized void load() throws IOException {
		LogUtil.d(TAG, "Load...");

		File cookiesFile = new File(FileUtil.getCookieCachePath(), FILE_COOKIE);
		if (!cookiesFile.exists())
			cookiesFile.createNewFile();

		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = FileUtil.openInputStream(cookiesFile);

			if (fis == null)
				return;

			dis = new DataInputStream(fis);
			if (dis.available() == 0)
				return;

			int magic = dis.readInt();
			if (magic != VERSION_MAGIC) {
				return;
			}

			int cookiesNum = dis.readInt();
			for (int i = 0; i < cookiesNum; i++) {
				Cookie cookie = new Cookie();
				cookie.setDomain(dis.readUTF());
				cookie.setName(dis.readUTF());
				cookie.setValue(dis.readUTF());
				cookie.setPath(dis.readUTF());
				// path为null时保存空字符串
				if (cookie.getPath().equals("")) {
					cookie.setPath("/");
				}
				cookie.setMaxAge(dis.readLong());

				cookie.setCreatedAt(dis.readLong());

				cookies.add(cookie);
			}

		} catch (IOException e) {
			throw e;
		} finally {
			if (dis != null)
				dis.close();
			if (fis != null)
				fis.close();
		}
	}

	/**
	 * 将Cookies序列化到文件系统
	 *
	 * @return
	 * @throws IOException
	 */
	public synchronized void save() throws IOException {
		LogUtil.d(TAG, "Save...");

		if (cookies == null || cookies.size() == 0)
			return;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeInt(VERSION_MAGIC);
		dos.writeInt(cookies.size());
		for (Cookie cookie : cookies) {
			dos.writeUTF(cookie.getDomain());
			dos.writeUTF(cookie.getName());
			dos.writeUTF(cookie.getValue());
			dos.writeUTF(cookie.getPath() == null ? "/" : cookie.getPath());
			dos.writeLong(cookie.getMaxAge());
			dos.writeLong(cookie.getCreatedAt());
		}
		dos.flush();
		dos.close();

		// 存入文件系统
		byte[] cookiesData = bos.toByteArray();
		if (cookiesData != null) {
			File cookiesFile = new File(FileUtil.getCookieCachePath(),
					FILE_COOKIE);
			FileUtil.save(cookiesFile, cookiesData);
		}
	}

	/**
	 * 清除Cookie
	 *
	 * @throws IOException
	 */
	public synchronized void clear() throws IOException {
		FileUtil.deleteQuietly(new File(FileUtil.getCookieCachePath()));
		cookies.clear();
	}

	/**
	 * Cookies are considered identical if their names are equal and their
	 * domain attributes match ignoring case.
	 */
	private class CookieIdentityComparator implements Serializable,
			Comparator<Cookie> {
		private static final long serialVersionUID = 4466565437490631532L;

		@Override
		public int compare(final Cookie c1, final Cookie c2) {
			int res = c1.getName().compareTo(c2.getName());
			if (res == 0) {
				// do not differentiate empty and null domains
				String d1 = c1.getDomain();
				if (d1 == null) {
					d1 = "";
				} else if (d1.indexOf('.') == -1) {
					d1 = d1 + ".local";
				}
				String d2 = c2.getDomain();
				if (d2 == null) {
					d2 = "";
				} else if (d2.indexOf('.') == -1) {
					d2 = d2 + ".local";
				}
				res = d1.compareToIgnoreCase(d2);
			}
			if (res == 0) {
				String p1 = c1.getPath();
				if (p1 == null) {
					p1 = "/";
				}
				String p2 = c2.getPath();
				if (p2 == null) {
					p2 = "/";
				}
				res = p1.compareTo(p2);
			}
			return res;
		}

	}
}
