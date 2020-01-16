package com.callme.platform.base;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.callme.platform.R;


public class HeaderNotifyView {
    private View mView;

    public HeaderNotifyView(Context context) {
        this(context, "");
    }

    public HeaderNotifyView(Context context, int msg) {
        this(context, msg, null);
    }

    public HeaderNotifyView(Context context, String msg) {
        this(context, msg, null);
    }

    public HeaderNotifyView(Context context, int msg, OnClickListener l) {
        mView = LayoutInflater.from(context).inflate(
                R.layout.base_layout_header_tip, null);
        mView.getBackground().setAlpha(230);
        if (msg != 0) {
            TextView txt = mView.findViewById(R.id.header_tip);
            txt.setText(msg);
        }
        if (null != l) {
            mView.setOnClickListener(l);
        }
    }

    public HeaderNotifyView(Context context, String msg, OnClickListener l) {
        mView = LayoutInflater.from(context).inflate(
                R.layout.base_layout_header_tip, null);
        mView.getBackground().setAlpha(230);
        if (!TextUtils.isEmpty(msg)) {
            TextView txt = mView.findViewById(R.id.header_tip);
            txt.setText(msg);
        }
        if (null != l) {
            mView.setOnClickListener(l);
        }
    }

    public View getView() {
        return mView;
    }
}
