package com.callme.platform.base;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：所有的json返回数据格式
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class BaseResponseBean extends BaseBean {

	private static final long serialVersionUID = 2637801762857005767L;

	// 0：异常
	// 1：成功
	// -10：提示升级
	public int error;

	public String message;

	//接受数据为json格式，此格式不会加密响应数据，但会带有签名，客户端核对签名来判断是否被中间人攻击
	public String Sign;
}
