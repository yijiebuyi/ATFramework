package com.callme.platform.common.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.callme.platform.R;
import com.callme.platform.widget.LoadingView;

public class ProgressDialog extends Dialog {
    private TextView mTvTip;
    private String mTxt;
    private LoadingView mProgressBar;
    private int mLoadingBg;

    public ProgressDialog(Context context) {
        super(context, R.style.common_dialog2);
    }

    public ProgressDialog(Context context, int loadBg) {
        super(context, R.style.common_dialog2);
        mLoadingBg = loadBg;
    }

    public void setText(String txt) {
        mTxt = txt;
    }

    public void setText(int res) {
        if (res > 0)
            mTxt = getContext().getString(res);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress_loading);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTvTip = (TextView) findViewById(R.id.progress_txt);
        mProgressBar = (LoadingView) findViewById(R.id.progress_bar);
        if (mTvTip != null && !TextUtils.isEmpty(mTxt)) {
            mTvTip.setText(mTxt);
        }

        if (mProgressBar != null && mLoadingBg != 0) {
            mProgressBar.setBackgroundResource(mLoadingBg);
        }

    }

}
