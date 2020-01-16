package com.callme.platform.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.callme.platform.base.BaseBusiness;
import com.callme.platform.util.http.RequestCallBack;
import com.callme.platform.util.http.ResponseInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;



/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：图片验证码相关处理
 * 作者：mikeyou
 * 创建时间：2017-11-19
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */

public class VerifyCodeUtils {
    private static final String IMG_FILE_NAME = "img_verify.png";
    private static boolean mGotImgVerifyFinish = true;

    public static void getPictureVerifyCode(final Context ctx, final String apiUrl, final ImageView imgVerifCode, final String phone) {
        if (!mGotImgVerifyFinish) {
            return;
        }
        mGotImgVerifyFinish = false;
        final String filePath = FileUtil.getImgVerifyCachePath()
                + IMG_FILE_NAME;
        BaseBusiness.getPictureVerifyCode(ctx, apiUrl, phone, filePath,
                new RequestCallBack<File>() {
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        if (responseInfo != null
                                && responseInfo.statusCode == 200) {
                            FileInputStream fis = null;
                            try {
                                fis = FileUtil.openInputStream(filePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (fis != null) {
                                handleImgCode(imgVerifCode, fis);
                            }
                        }
                        mGotImgVerifyFinish = true;
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        ToastUtil.showCustomViewToast(ctx, msg);
                        mGotImgVerifyFinish = true;
//                        getPictureVerifyCode(ctx, imgVerifCode, phone);
                    }

                });
    }

    public static void resetVerifyReq() {
        mGotImgVerifyFinish = true;
    }

    public static void handleImgCode(final ImageView imgVerifCode, InputStream in) {
        Bitmap bitmap = getBitmap(in);
        if (bitmap != null) {
            imgVerifCode.setBackgroundDrawable(new BitmapDrawable(bitmap));
        }
    }

    public static Bitmap getBitmap(InputStream in) {
        Bitmap image = null;
        try {
            image = BitmapFactory.decodeStream(in);
        } catch (OutOfMemoryError err) {

            err.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return image;
    }
}
