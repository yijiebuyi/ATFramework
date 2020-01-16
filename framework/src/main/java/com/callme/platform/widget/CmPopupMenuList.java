/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：解决popup menu的item的宽度不能自适应的问题
 * 作者：
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
package com.callme.platform.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class CmPopupMenuList extends ListView {

	public CmPopupMenuList(Context context) {
		super(context);
	}

	public CmPopupMenuList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CmPopupMenuList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    int maxWidth = meathureWidthByChilds() + getPaddingLeft() + getPaddingRight();
	    super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);     
	}

	public int meathureWidthByChilds() {
	    int maxWidth = 0;
	    View view = null;
	    for (int i = 0; i < getAdapter().getCount(); i++) {
	        view = getAdapter().getView(i, view, this);
	        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	        if (view.getMeasuredWidth() > maxWidth){
	            maxWidth = view.getMeasuredWidth();
	        }
	    }
	    return maxWidth;
	}

}
