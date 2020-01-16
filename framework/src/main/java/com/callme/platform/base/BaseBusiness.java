package com.callme.platform.base;

import android.content.Context;
import android.text.TextUtils;

import com.callme.platform.R;
import com.callme.platform.util.CmHttpUtil;
import com.callme.platform.util.ResourcesUtil;
import com.callme.platform.util.http.RequestCallBack;
import com.callme.platform.util.http.RequestParams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;

/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：基本业务类，全局公共方法
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class BaseBusiness {


    public static String getResponseMsg(String response) {
        String errorMsg = ResourcesUtil.getString(R.string.get_data_error);
        if (TextUtils.isEmpty(response)) {
            return errorMsg;
        }
        try {
            Type type = new TypeToken<BaseResponseBean>() {
            }.getType();
            BaseResponseBean bean = new Gson().fromJson(
                    response.toString(), type);
            if (!TextUtils.isEmpty(bean.message) && bean.error == 1) {
                errorMsg = bean.message;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMsg;
    }


    public static BaseResponseBean paseBase(String reponse) {
        try {
            return new Gson().fromJson(reponse,
                    new TypeToken<BaseResponseBean>() {
                    }.getType());
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 获取图片验证码
     *
     * @param context
     * @param phone
     * @param responseHandler
     * @return
     */
    public static String getPictureVerifyCode(Context context, String apiUrl, String phone, String path,
                                              RequestCallBack<File> responseHandler) {
        if (TextUtils.isEmpty(phone)) {
            return null;
        }
        RequestParams params = new RequestParams();
        params.put("phoneNo", phone);
        //CallmeApi.VALIDATE_GET_CAPT_CHA
        return CmHttpUtil.getInstance(context).download(
                apiUrl, path, params, responseHandler);
    }
}
