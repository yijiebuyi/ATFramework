/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：获取从Intent中传递过来的long 型数据
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
package com.callme.platform.util;

import android.content.Intent;

public class IntentUtil {

	/**
	 * 
	 * @param intent
	 * @param intentKey
	 *            参数标识
	 * @param defaultValue
	 *            没有获取到数据的时候期望返回的值
	 * @return
	 */
	public static long getIntentLong(Intent intent, String intentKey,
			long defaultValue) {
		long result = intent.getLongExtra(intentKey, -1);

		if (result == -1) {
			result = intent.getIntExtra(intentKey, -1);
		}
		if (result == -1) {
			result = defaultValue;
		}
		return result;
	}
}
