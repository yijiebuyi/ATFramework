package com.callme.platform.widget.datapicker.core;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：
 *
 * 作者：huangyong
 * 创建时间：2017/11/26
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public interface OnWheelPickedListener {
	
	@SuppressWarnings("rawtypes")
    void onWheelSelected(AbstractWheelPicker wheelPicker, int index, Object data);
}
