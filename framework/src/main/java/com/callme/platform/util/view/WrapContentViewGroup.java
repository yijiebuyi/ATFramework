/*
 * Copyright (C) 
 * 版权所有
 *
 * 功能描述：根据根据WrapContentViewGroup的宽度适配屏幕宽度能容乃子view的个数，动态添加
 * 作者：桑毅
 * 创建时间：2015年4月23日 下午5:25:51
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
package com.callme.platform.util.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class WrapContentViewGroup extends LinearLayout {

	private int mMaxViewNum = 1;// 最大的子view数
	private int mAmongViewMargin = 0;// 子view之间的间距
	private int mAmongRowMargin = 0;// 排之间的间距

	private Context mContext;
	private ArrayList<View> mViews = new ArrayList<View>();// 子view

	public WrapContentViewGroup(Context context) {
		super(context);
		mContext = context;
	}

	public void setAmongRowMargin(int mAmongRowMargin) {
		this.mAmongRowMargin = mAmongRowMargin;
	}

	public int getMaxViewNum() {
		return mMaxViewNum;
	}

	public void setAmongViewMargin(int mAmongViewMargin) {
		this.mAmongViewMargin = mAmongViewMargin;
	}

	public void setMaxViewNum(int mMaxViewNum) {
		this.mMaxViewNum = mMaxViewNum;
	}

	public WrapContentViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	@TargetApi(11)
	public WrapContentViewGroup(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	private void refreshView() {
		removeAllViews();
		LinearLayout row = null;
		int rowWidth = 0;
		if (row == null) {
			row = new LinearLayout(mContext);
			row.setOrientation(LinearLayout.HORIZONTAL);
		}
		for (int i = 0; i < mViews.size(); i++) {
			View view = mViews.get(i);
			if (row.getChildCount() != 0) {// 如果这一排已经有label了
				if (rowWidth + view.getLayoutParams().width + mAmongViewMargin <= getMeasuredWidth()) {// 如果可以容纳下一个label的长度，就addview()
					View lastLabel = row.getChildAt(row.getChildCount() - 1);
					LayoutParams lp = (LayoutParams) lastLabel
							.getLayoutParams();
					lp.rightMargin = mAmongViewMargin;
					lastLabel.setLayoutParams(lp);
					row.addView(view, view.getLayoutParams().width,
							view.getLayoutParams().height);
					rowWidth += (view.getLayoutParams().width + mAmongViewMargin);
				} else {
					LayoutParams rowllp = new LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					rowllp.bottomMargin = mAmongRowMargin;
					addView(row, rowllp);
					row = new LinearLayout(mContext);
					row.setOrientation(LinearLayout.HORIZONTAL);
					rowWidth = 0;
					if (rowWidth + view.getLayoutParams().width <= getMeasuredWidth()) {// 如果可以容纳下一个label的长度，就addview()
						row.addView(view, view.getLayoutParams().width,
								view.getLayoutParams().height);
						rowWidth = view.getLayoutParams().width;
					} else {// 尽然屏幕无法容纳下这个标签的长度，那么抛弃这个标签不予显示
						rowWidth = 0;
					}
				}
			} else {// 这一排还没有label
				if (rowWidth + view.getLayoutParams().width <= this
						.getMeasuredWidth()) {// 如果可以容纳下一个label的长度，就addview()
					row.addView(view, view.getLayoutParams().width,
							view.getLayoutParams().height);
					rowWidth = view.getLayoutParams().width;
				} else {// 尽然屏幕无法容纳下这个标签的长度，那么抛弃这个标签不予显示
					rowWidth = 0;
				}
			}
			if (i == mViews.size() - 1) {// 如果是最后一个
				addView(row, ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			}
		}
	}

	/**
	 * 加入子views
	 */
	public void addSubViews(ArrayList<? extends View> listView) {
		for (View view : listView) {
			if (getChildCount() < mMaxViewNum) {
				mViews.add(view);
			} else {
				break;
			}
		}
		refreshView();
	}

	/**
	 * 加入子view
	 */
	public void addSubView(final View view) {
		if (getChildCount() < mMaxViewNum) {
			mViews.add(view);
			refreshView();
		} else {
			// 超过子view设置的最大值
		}
	}

	/**
	 * 移除子view
	 */
	public void removeSubView(final View view) {
		if (getChildCount() > 0) {
			mViews.remove(view);
			refreshView();
		}
	}

	/**
	 * 移除子view
	 */
	public void removeAllSubView() {
		removeAllViews();
		mViews.clear();
	}
}
