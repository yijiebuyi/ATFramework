package com.callme.platform.widget.crop;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.callme.platform.R;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：简单图片裁剪，只支持固定比例的裁剪
 * 作者：huangyong
 * 创建时间：2018/1/24
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class SimpleCropActivity extends BaseCropActivity implements View.OnClickListener {
    private TextView mLeftBtnTv;
    private TextView mRightBtnTv;

    @Override
    protected View getBottomContainerView() {
        return LayoutInflater.from(this).inflate(R.layout.layout_simple_crop, null);
    }

    @Override
    protected void initContainerViews(View topParent, View bottomParent) {
        if (bottomParent != null) {
            mLeftBtnTv = bottomParent.findViewById(R.id.left_btn);
            mRightBtnTv = bottomParent.findViewById(R.id.right_btn);

            mLeftBtnTv.setOnClickListener(this);
            mRightBtnTv.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.left_btn) {
            finish();
        } else if (v.getId() == R.id.right_btn) {
            saveCropBitmap();
        }
    }

    public void setLeftBtnText(String txt) {
        if (TextUtils.isEmpty(txt) || mLeftBtnTv == null) {
            return;
        }

        mLeftBtnTv.setText(txt);
    }

    public void setRightBtnText(String txt) {
        if (TextUtils.isEmpty(txt) || mRightBtnTv == null) {
            return;
        }

        mRightBtnTv.setText(txt);
    }
}
