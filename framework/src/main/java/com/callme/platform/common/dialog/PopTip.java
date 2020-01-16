/**
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司 版权所有
 * 
 * 功能描述：气泡提示
 * 作者：mikeyou
 * 2017-10-6
 * 修改人：
 * 修改描述： 
 * 修改日期
 */
package com.callme.platform.common.dialog;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.callme.platform.R;
import com.callme.platform.util.ResourcesUtil;

public class PopTip extends PopupWindow{
	private View mShowView;
	private String mShowText;
	private Context mContext;
	private int x, y;

	public PopTip(Context context, View showView, String text) {
		this(context, showView, text, 0, 0);
	}

	public PopTip(Context context, View showView, String text, int x, int y) {
		mContext = context;
		mShowView = showView;
		mShowText = text;
		init(x, y);
	}

	private void init(int x, int y) {
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.base_layout_pop_tip, null);
		TextView textView = view.findViewById(R.id.pop_tip);
		textView.setText(mShowText);
		mShowView.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				dismiss();
			}
		});
		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(ResourcesUtil.getDimension(R.dimen.font_28px));
		int viewWidth = mShowView.getWidth();
		int viewHeight = mShowView.getHeight();
		if(viewWidth == 0 && viewHeight == 0){
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			mShowView.measure(w, h);
			viewWidth = mShowView.getMeasuredWidth();
			viewHeight = mShowView.getMeasuredHeight();
		}
		float textWidth = textPaint.measureText(mShowText);
		setBackgroundDrawable(new BitmapDrawable());
		setContentView(view);
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(true);
		setFocusable(false);
		setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		if (x == 0) {
			int divider = (int)(viewWidth - textWidth);
			x = divider > viewWidth ? 0 : divider - 10;
		}
		if (y == 0) {
			y = -(viewHeight + ResourcesUtil.dip2px(35));
		}
		this.x = x;
		this.y = y;
	}
	
	public void show(){
		showAsDropDown(mShowView, x, y);
	}

	public void dimiss() {
		dismiss();
	}
}
