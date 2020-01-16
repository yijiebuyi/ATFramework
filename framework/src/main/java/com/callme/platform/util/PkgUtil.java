package com.callme.platform.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

/*
 * Copyright (C) 
 * 版权所有
 *
 * 功能描述：软件包相关的工具类
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class PkgUtil {

	/**
	 * 获取软件版本名称
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		PackageInfo info;
		try {
			info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取package名称
	 * 
	 * @param context
	 * @return
	 */
	public static String getPackageName(Context context) {
		PackageInfo info;
		try {
			info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.packageName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取软件版本code
	 * 
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		PackageInfo info;
		try {
			info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	// 获取ApiKey
		public static String getMetaValue(Context context, String metaKey) {
			Bundle metaData = null;
			String apiKey = null;
			if (context == null || metaKey == null) {
				return null;
			}
			try {
				ApplicationInfo ai = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(),
								PackageManager.GET_META_DATA);
				if (null != ai) {
					metaData = ai.metaData;
				}
				if (null != metaData) {
					Object objkey = metaData.get(metaKey);
					if (objkey != null) {
						apiKey = objkey.toString();
					}
				}
			} catch (NameNotFoundException e) {

			}
			return apiKey;
		}
}
