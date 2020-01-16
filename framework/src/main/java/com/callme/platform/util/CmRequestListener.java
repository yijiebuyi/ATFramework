package com.callme.platform.util;

import org.json.JSONObject;

public abstract class CmRequestListener<T> {
    private Object comeFrom;

    public CmRequestListener() {

    }

    /**
     * from:listener使用的地方， 只能用于BaseActivity或BaseFragment子类
     *
     * @param from
     */
    public CmRequestListener(Object from) {
        comeFrom = from;
    }

    public Object getComeFrom() {
        return comeFrom;
    }

    public void onStart() {
    }

    public void onCancelled() {
    }

    public void onLoginTimeout() {
    }

    public void onReSendReq() {
    }

    public void onLoading(long total, long current, boolean isUploading) {
    }

    public abstract void onSuccess(JSONObject response);

    public abstract void onFailure(int exceptionCode, String response);

}
