package com.callme.platform.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;


public class SharedPreferencesUtil {
	public final static String APP_CONFIGURE = "configure";// 配置文件名
	SharedPreferences mShared = null;
	static SharedPreferencesUtil mUtil = null;

	public static synchronized SharedPreferencesUtil getInstance(Context context) {
		if (mUtil == null) {
			mUtil = new SharedPreferencesUtil(context.getApplicationContext());
		}
		return mUtil;
	}

	private SharedPreferencesUtil(Context ctx) {
		mShared = ctx
				.getSharedPreferences(
						APP_CONFIGURE,
						DevicesUtil.getSystemVersionLevel() > Build.VERSION_CODES.FROYO ? 4
								: Context.MODE_PRIVATE);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return mShared.getBoolean(key, defValue);
	}

	public void putBoolean(String key, boolean value) {
		mShared.edit().putBoolean(key, value).commit();
	}

	public float getFloat(String key, float defValue) {
		return mShared.getFloat(key, defValue);
	}

	public void putFloat(String key, float value) {
		mShared.edit().putFloat(key, value).commit();
	}

	public long getLong(String key, long defValue) {
		return mShared.getLong(key, defValue);
	}

	public void putLong(String key, long value) {
		mShared.edit().putLong(key, value).commit();
	}

	public int getInt(String key, int defValue) {
		return mShared.getInt(key, defValue);
	}

	public void putInt(String key, int value) {
		mShared.edit().putInt(key, value).commit();
	}

	public String getString(String key, String defValue) {
		return mShared.getString(key, defValue);
	}

	public void putString(String key, String value) {
		mShared.edit().putString(key, value).commit();
	}

	public void commit() {
		mShared.edit().commit();
	}
}
