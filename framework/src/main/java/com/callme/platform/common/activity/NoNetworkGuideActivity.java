/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：无网络的一个提示向导页面
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
package com.callme.platform.common.activity;

import android.view.View;

import com.callme.platform.R;
import com.callme.platform.base.BaseActivity;

public class NoNetworkGuideActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected View getContentView() {
        return inflate(R.layout.base_activity_no_network);
    }

    @Override
    protected void onContentAdded() {
        setTitle(R.string.no_network_title);
        mLeftView.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.left_view) {
            finish();
        }
    }

}
