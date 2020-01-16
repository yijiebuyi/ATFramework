package com.callme.platform.util;

import android.content.Context;
import android.util.Base64;

import com.callme.platform.base.BaseBean;
import com.callme.platform.base.BaseResponseBean;
import com.callme.platform.util.http.HttpUtil;
import com.callme.platform.util.http.RequestListener;
import com.callme.platform.util.http.RequestParams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.Signature;

/**
 * @功能描述： 加密工具类
 * @作者 mikeyou @创建日期：2017/10/6
 * @修改人： @修改描述：
 * @修改日期
 */
public class EncryptUtil {
    // 服务器端RSA加密模式，客户端加密传输
    public static int RSA_ERROR_ENCRYPT = 9001;
    // 服务器端RSA非加密模式，客户端明文传输
    public static int RSA_ERROR_TXT = 9002;

    /**
     * 以字节流的方式传输, 请求的ContentType为application/jsone 此格式表示加密的json数据
     */
    public static final int TYPE_RSA_NORMAL = 1;

    /**
     * 以base64格式字符串传输, 请求的ContentType为application/jsonet 此格式表示加密的json数据
     */
    public static final int TYPE_RSA_BASE64 = 2;

    /**
     * RSA 传输类型
     */
    public static int mRsaTansType = TYPE_RSA_BASE64;

    /**
     * RSA加密提交
     *
     * @param context
     * @param entry
     * @param url
     * @param responseHandler
     * @return
     */
    public static String RSAPost(Context context, String url, Object entry,
                                 RequestListener<JSONObject> responseHandler) {
        RsaDataBean data = EncryptUtil.buildRsaData(entry);
        RequestParams params = new RequestParams();
        params.put(EncryptUtil.getRsaStrByBase64(data));
        return HttpUtil.getInstance(context, "application/jsonet", 0).post(url, params, responseHandler);
    }

    public static String RSAPost(Context context, String url, Object entry, RequestListener<JSONObject> responseHandler,
                                 int type) {
        RsaDataBean data = EncryptUtil.buildRsaData(entry);
        RequestParams params = new RequestParams();
        if (TYPE_RSA_BASE64 == type) {
            params.put(EncryptUtil.getRsaStrByBase64(data));
            return HttpUtil.getInstance(context, "application/jsonet", 0).post(url, params, responseHandler);
        } else {
            params.put(EncryptUtil.getRsaStr(data));
            return HttpUtil.getInstance(context, "application/jsone", 0).post(url, params, responseHandler);
        }
    }

    /**
     * RSA校验
     *
     * @param result
     * @return
     */
    public static boolean RSAVerify(String result) {
        try {
            Type type = new TypeToken<BaseResponseBean>() {
            }.getType();
            BaseResponseBean resRsa = new Gson().fromJson(result.toString(), type);
            String sign = resRsa.Sign;
            String srcData = result.replaceAll("\"Sign\":\"([A-Za-z0-9+/=]+)\"[,]?", "");

            Signature signture = Signature.getInstance("SHA1withRSA");

            String modlus = RSAUtil.MODULUS;
            String exponent = RSAUtil.EXPONENT;
            PublicKey publickKey = null;
            try {
                byte[] m = Base64.decode(modlus, Base64.DEFAULT);
                byte[] e = Base64.decode(exponent, Base64.DEFAULT);
                publickKey = RSAUtil.generatePublicKey(new BigInteger(1, m), new BigInteger(1, e));
            } catch (Exception e) {
                e.printStackTrace();
            }
            signture.initVerify(publickKey);
            signture.update(srcData.getBytes("UTF-8"));
            return signture.verify(Base64.decode(sign, Base64.DEFAULT));
        } catch (Exception e) {

        }

        return false;
    }

    /**
     * RSA加密字符串:base64编码
     *
     * @param data
     * @return
     */
    public static String getRsaStrByBase64(RsaDataBean data) {
        byte[] b = getRsaData(data);
        if (b == null) {
            return null;
        }

        return new String(Base64.encode(b, Base64.DEFAULT));
    }

    /**
     * RSA加密字符串:
     *
     * @param data
     * @return
     */
    public static String getRsaStr(RsaDataBean data) {
        byte[] b = getRsaData(data);
        if (b == null) {
            return null;
        }

        return new String(b);
    }

    /**
     * RSA加密字节数组:
     *
     * @param data
     * @return
     */
    public static byte[] getRsaData(RsaDataBean data) {
        String modlus = RSAUtil.MODULUS;
        String exponent = RSAUtil.EXPONENT;
        PublicKey publickKey = null;
        try {
            byte[] m = Base64.decode(modlus, Base64.DEFAULT);
            byte[] e = Base64.decode(exponent, Base64.DEFAULT);
            publickKey = RSAUtil.generatePublicKey(new BigInteger(1, m), new BigInteger(1, e));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String srcData = new Gson().toJson(data);
        byte[] rsaData = null;
        try {
            rsaData = RSAUtil.encryptByBlock(publickKey, srcData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (rsaData == null) {
            return null;
        }

        return rsaData;
    }

    /**
     * RSA加密传输字符串:
     *
     * @param data
     * @return
     */
    public static String getRsaTrasData(RsaDataBean data) {
        if (mRsaTansType == TYPE_RSA_BASE64) {
            return getRsaStrByBase64(data);
        } else {
            return getRsaStr(data);
        }
    }

    /**
     * RSA加密ContentType
     *
     * @param data
     * @return
     */
    public static String getRsaContentType() {
        return mRsaTansType == TYPE_RSA_BASE64 ? "application/jsonet" : "application/jsone";
    }

    public static class RsaDataBean extends BaseBean {
        /**
         *
         */
        private static final long serialVersionUID = 2467240020583094726L;
        public String d;
    }

    public static RsaDataBean buildRsaData(Object entry) {
        RsaDataBean data = new RsaDataBean();
        data.d = new Gson().toJson(entry);
        return data;
    }

    public static RsaDataBean buildRsaData(String jsonData) {
        RsaDataBean data = new RsaDataBean();
        data.d = jsonData;
        return data;
    }

    /**
     * 处理RSA加密响应 如果加密失败，需要重新登录
     *
     * @param context
     * @param msg
     * @param phoneNum
     * @param pwd
     * @return RSA是否加密失败了；如果失败了，需要重新登录
     */
    public static boolean handleEncryptResponse(Context context, String msg, String phoneNum, String pwd,
                                                boolean isAgree) {
//        int code = BaseBusiness.getReponseCode(msg);
//        BaseResponseBean response = BaseBusiness.paseBase(msg);
//
//        if (code == RSA_ERROR_TXT) {
//            uplodRsaErrorReport(context, response.Data, phoneNum, pwd, isAgree);
//            // 需要明文传输
//            SystemConsts.RSA_ENCRYPT = false;
//        } else if (code == RSA_ERROR_ENCRYPT) {
//            uplodRsaErrorReport(context, response.Data, phoneNum, pwd, isAgree);
//            SystemConsts.RSA_ENCRYPT = true;
//        }
//
//        if (code == RSA_ERROR_TXT) {
//            // 需要重新登录
//            return true;
//        }

        return false;
    }

    /**
     * 上传RSA错误日志
     *
     * @param context
     * @param requestId
     * @param phoneNum
     * @param pwd
     */
    public static void uplodRsaErrorReport(Context context, Object requestId, String phoneNum, String pwd,
                                           boolean isAgreement) {
    }

    public static class RsaErrorReportBean extends BaseBean {
        /**
         *
         */
        private static final long serialVersionUID = 4329888678285793140L;
        public String UserAccount;
        public String Password;
        public boolean IsAgreement;

        public RsaErrorReportBean(String account, String pwd, boolean isAgreement) {
            UserAccount = account;
            Password = pwd;
            IsAgreement = isAgreement;
        }
    }
}
