package com.callme.platform.util.cache;

import android.text.TextUtils;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：json http请求返回数据的可缓存对象
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class JsonData implements Cacheable {

	public String data;

	public JsonData(String data) {
		this.data = data;
	}

	@Override
	public int getCachedType() {
		return Cacheable.TYPE_FILE;
	}

	@Override
	public int getCachedSize() {
		if (TextUtils.isEmpty(data)) {
			return 0;
		}
		return data.getBytes().length;
	}

	@Override
	public void recycle() {

	}

	@Override
	public byte[] serialize() {
		return data.getBytes();
	}

}
