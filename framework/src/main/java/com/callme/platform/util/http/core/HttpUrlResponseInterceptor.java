package com.callme.platform.util.http.core;

import java.net.HttpURLConnection;

/*
 * 
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：网络请求后的回调接口
 * 作者：mikeoyu
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public interface HttpUrlResponseInterceptor {
	void process(HttpResponse response, HttpURLConnection connection);
}
