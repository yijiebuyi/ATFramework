package com.callme.platform.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：可以监听软键盘的关闭事件
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class EditTextPreIme extends EditText {

	private ImeListener mListener;

	public EditTextPreIme(Context context) {
		super(context);
	}

	public EditTextPreIme(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditTextPreIme(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImeListener(ImeListener listener) {
		mListener = listener;
	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mListener != null) {
				mListener.onImeClose();
			}
		}
		return super.dispatchKeyEventPreIme(event);
	}

	public interface ImeListener {
		void onImeClose();
	}

}
