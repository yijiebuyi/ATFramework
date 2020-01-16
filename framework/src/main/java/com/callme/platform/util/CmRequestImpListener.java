package com.callme.platform.util;


import com.alibaba.fastjson.JSON;

import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class CmRequestImpListener<T> extends CmRequestListener<T> {
    private Type[] mTypes;

    public CmRequestImpListener(Object from) {
        super(from);
        mTypes = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    }

    public void onSuccess(JSONObject response) {
        if (response != null) {
            String str = response.toString();
            try {
                T t = JSON.parseObject(str, mTypes[0]);
                onResponse(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            onFailure(-1, "");
        }
    }

    public abstract void onResponse(T response);

}
