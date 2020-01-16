package com.callme.platform.widget.pulltorefresh;

import com.callme.platform.util.http.RequestParams;
import com.callme.platform.widget.pulltorefresh.AbsSwipeCacheAdapter.OnDataLoadListener;

/**
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：
 * 作者：mikeyou
 * 创建日期：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class AbsSwipeCacheRequestParams {
	public String url; //http网络请求url
	
	public RequestParams httpReqParams;
	
	public String cachePath; //本地缓存路径
	
	public OnDataLoadListener dataLoadListener;
}
