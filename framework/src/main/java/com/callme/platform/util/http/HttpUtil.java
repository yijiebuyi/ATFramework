package com.callme.platform.util.http;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.callme.platform.util.ApnUtil;
import com.callme.platform.util.DevicesUtil;
import com.callme.platform.util.EncryptUtil;
import com.callme.platform.util.OtherUtils;
import com.callme.platform.util.PkgUtil;
import com.callme.platform.util.ResourcesUtil;
import com.callme.platform.util.SharedPreferencesUtil;
import com.callme.platform.util.cookie.CookieManager;
import com.callme.platform.util.http.core.CustomerRequest;
import com.callme.platform.util.http.core.HttpResponse;
import com.callme.platform.util.http.core.HttpUrlRequestInterceptor;
import com.callme.platform.util.http.core.HttpUrlResponseInterceptor;
import com.callme.platform.util.http.core.HurlStack;
import com.callme.platform.util.http.core.Request;
import com.callme.platform.util.http.core.Request.Method;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLSocketFactory;

/*
 *
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 *
 * 功能描述：http、https请求的入口类，支持get、post、download、upload
 * 作者：mikeyou
 * 创建时间：2017-10-6
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class HttpUtil {

    private String responseTextCharset = "UTF-8";
    public static double LATITUDE = 0;
    public static double LONGTITUDE = 0;
    public static String USER_TOKEN = ""; // 用户token
    public static String API_LEAVEL = ""; // 服务端接口版本
    public static String CLIENT_VERSION = ""; // 客户端版本
    public static String CHANNEL_ID = "0";

    public final static String AUTO_GENERATE_IMEI = "auto_generate_imei"; // 自动生成的imei
    public final static String USER_NAME = "userName"; // 用户账号
    public final static String USER_PWD = "userPwd"; // 用户密码
    public static final String NEED_CACHE_KEY = "need_cache_key";
    public static final String BACKGROUND_REQUEST_KEY = "background_request_key";
    public static ConcurrentHashMap<String, HttpHandler> mHandlerMap = new ConcurrentHashMap<String, HttpHandler>();
    public final static HttpCache sHttpCache = new HttpCache();
    public static final String KEY_RSA_ENCRYPT = "key_rsa_encrypt"; // rsa 加密

    private final String CUSTOM_TOKEN = "c-ii"; // 自定义
    private final String TOKEN = "token";


    private final String NET_KEY = "c-nw"; // 网络类型 "unknow"; "wifi";"2G";"3G";"4G";
    private final String API_KEY = "c-iv"; // 客户端请求的接口版本 2.3.3 三位数
    private final String CLIENT_KEY = "c-cv"; // 客户端版本
    private final String CLIENT_TYPE = "c-ct"; // 客户端类型 1:安卓，2:IOS
    private final String SCREEN_WIDTH = "c-cw"; // 屏幕宽度
    private final String SCREEN_HEIGHT = "c-ch"; // 屏幕高度
    private final String SRC_KEY = "c-sr"; // 渠道   默认是0，其他渠道后续定义
    private final String LNG_KEY = "c-lng"; // 经度
    private final String LAT_KEY = "c-lat"; // 纬度
    private final String BRAND_KEY = "c-br"; // 品牌名,比如华为，小米，苹果x
    private final String MODEL_KEY = "c-mo"; // The end-user-visible name for the end product.
    private final String SDK_VERSION_KEY = "c-sv"; // The user-visible version string.
    private final String IMEI_KEY = "c-im"; // imei号.
    private final String CLIENT_SUB_TYPE = "c-st"; // 客户端子类型 1:用户，2:司机 3：出租车司机

    private final static int DEFAULT_CONN_TIMEOUT = 1000 * 30; // 30s

    private final static int DEFAULT_RETRY_TIMES = 3;

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "identity";

    private static final String CONTENT_TYPE = "Content-Type";
    private String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";//"application/json;charset=UTF-8";
    private static final String USER_AGENT = "User-Agent";

    private static final String COOKIE = "Cookie";

    private final static int DEFAULT_POOL_SIZE = 3;

    private static String mUserAgent;
    private static Context mAppContext;
    private static HttpUtil mClient;
    private static String userContentType = null;
    private static int userTimeOut = -1;

    public final static Object mLock = new Object();
    private static String mImei;
    // 随机imei号的固定前缀
    private static final String _rand_imei_prefix = "15748";

    private final static PriorityExecutor EXECUTOR = new PriorityExecutor(DEFAULT_POOL_SIZE);

    private HttpUtil(final Context ctx) {
        mAppContext = ctx.getApplicationContext();
//		mChannelId = Application.CHANNEL;
        CLIENT_VERSION = PkgUtil.getAppVersionName(ctx);
    }

    public static HttpUtil getInstance(Context ctx) {
        return getInstance(ctx, null, -1);
    }

    public static HttpUtil getInstance(Context ctx, int connTimeout) {
        return getInstance(ctx, null, connTimeout);
    }

    public static HttpUtil getInstance(Context ctx, String contentType, int connTimeout) {
        if (mClient == null) {
            mClient = new HttpUtil(ctx);
        }
        userTimeOut = connTimeout;
        userContentType = contentType;
        return mClient;
    }

    public <T> String get(String url, RequestListener<T> listener) {

        return get(url, null, listener);
    }

    public <T> String get(String url, RequestParams param, RequestListener<T> listener) {
        Request request = new CustomerRequest(Method.GET, url);
        if (param != null) {
            request.setmParams(param);
        }
        return sendQuery(request, listener.getCallBack());
    }

    public <T> String post(String url, RequestListener<T> listener) {
        return post(url, null, listener);
    }

    public <T> String post(String url, RequestParams param, RequestListener<T> listener) {
        Request request = new CustomerRequest(Method.POST, url);
        if (param != null) {
            // set RSA Contenet-type
            Object obj = param.get(KEY_RSA_ENCRYPT);
            if (obj instanceof Boolean) {
                if ((Boolean) obj) {
                    userContentType = EncryptUtil.getRsaContentType();
                }
            }
            request.setmParams(param);
        }
        return sendQuery(request, listener.getCallBack());
    }

    public String download(String url, String target, RequestParams param, RequestCallBack<File> callback) {
        return download(Method.POST, url, target, param, true, false, callback);
    }

    public String download(String url, String target, boolean autoResume, RequestCallBack<File> callback) {
        return download(Method.GET, url, target, null, autoResume, false, callback);
    }

    public String download(int method, String url, String target, RequestParams param, boolean autoResume,
                           boolean autoRename, RequestCallBack<File> callback) {

        String id = UUID.randomUUID().toString();

        if (url == null)
            throw new IllegalArgumentException("url may not be null");
        if (target == null)
            throw new IllegalArgumentException("target may not be null");

        Request request = new CustomerRequest(method, url.trim());
        if (param != null) {
            request.setmParams(param);
        }
        HurlStack hurlStack = initHurlStack();
        HttpHandler<File> handler = new HttpHandler<File>(hurlStack, responseTextCharset, callback, id, false);

        handler.executeOnExecutor(EXECUTOR, request, target, autoResume, autoRename);
        mHandlerMap.put(id, handler);
        return id;
    }

    private <T> String sendQuery(Request request, RequestCallBack<T> callBack) {
        // 当前是模拟器，就不发送任何http请求
        if (DevicesUtil.isEmulator()) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        boolean cacheEnable = false;
        boolean background = false;
        if (request != null) {
            RequestParams param = request.getmParams();
            if (param != null) {
                Object needCache = param.get(NEED_CACHE_KEY);
                if (needCache != null) {
                    try {
                        cacheEnable = (Boolean) needCache;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Object backgroudKey = param.get(BACKGROUND_REQUEST_KEY);
                if (backgroudKey != null) {
                    try {
                        background = (Boolean) backgroudKey;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (cacheEnable) {
                param.remove(NEED_CACHE_KEY);
            }
            if (background) {
                param.remove(BACKGROUND_REQUEST_KEY);
            }
        }
        HurlStack hurlStack = initHurlStack();
        HttpHandler<T> handler = new HttpHandler<T>(hurlStack, responseTextCharset, callBack, id, cacheEnable);
        handler.executeOnExecutor(EXECUTOR, request);
        if (!background) {
            mHandlerMap.put(id, handler);
        }
        return id;
    }

    public static void reset() {
        mClient = null;
    }

    private String getImei() {
        if (TextUtils.isEmpty(mImei)) {
            synchronized (mLock) {
                // 第一次从设备信息中取IMEI
                mImei = DevicesUtil.getIMEI(mAppContext);
                if (TextUtils.isEmpty(mImei) || "000000000000000".equals(mImei)) {
                    // 设备信息中取不到，从SharedPreferencesUtil中取IMEI
                    SharedPreferencesUtil spUtil = SharedPreferencesUtil.getInstance(mAppContext);
                    mImei = spUtil.getString(AUTO_GENERATE_IMEI, "");
                    if (TextUtils.isEmpty(mImei)) {
                        // 都没有，则生成随机IMEI号
                        Random rand = new Random();
                        mImei = _rand_imei_prefix + (100000000 + rand.nextInt(899999999));
                        spUtil.putString(AUTO_GENERATE_IMEI, mImei);
                    }
                }
            }
        }

        // imei号不是偶数位则后面补0
        if (mImei != null && mImei.length() % 2 != 0)
            mImei = mImei + "0";
        return mImei;
    }

    /**
     * 初始化HurlStack网络请求实例，为避免网络请求时网络连接发生混乱，每次网络请求产生一个新的实例
     *
     * @return
     */
    private HurlStack initHurlStack() {
        HurlStack hurlStack = new HurlStack();
        hurlStack.setRetry(DEFAULT_RETRY_TIMES);// 设置重试次数
        hurlStack.setSslSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
        hurlStack.addRequestInterceptor(new HttpUrlRequestInterceptor() {

            @Override
            public void process(Request request) {

                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }

                if (request.containsHeader(CONTENT_TYPE)) {
                    request.removeHeader(CONTENT_TYPE);
                }
                if (TextUtils.isEmpty(userContentType)) {
                    request.addHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
                } else {
                    request.addHeader(CONTENT_TYPE, userContentType);
                    userContentType = null;
                }

                if (userTimeOut > 0) {
                    request.setTimeoutMs(userTimeOut);
                    userTimeOut = -1;
                } else {
                    request.setTimeoutMs(DEFAULT_CONN_TIMEOUT);
                }

                if (TextUtils.isEmpty(mUserAgent)) {
                    mUserAgent = OtherUtils.getUserAgent();
                }
                request.addHeader(USER_AGENT, mUserAgent);

                String url = request.getUrl();

                // customToken、cookie处理
                if (!TextUtils.isEmpty(url)) {
                    try {
                        String token = null;
                        if (request.containsHeader(CUSTOM_TOKEN)) {
                            request.removeHeader(CUSTOM_TOKEN);
                        }

                        token = "";//
//                        request.addHeader(CUSTOM_TOKEN, token);

                        String cookie = CookieManager.getInstance().get(new URL(request.getUrl()));
                        if (cookie != null && !cookie.equals("")) {
                            if (request.containsHeader(COOKIE)) {
                                request.removeHeader(COOKIE);
                            }

                            request.addHeader(COOKIE, cookie);

                            // 检测用户token
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                // 杂项添加
//                private final String LNG_KEY = "c-lng"; // 经度
//                private final String LAT_KEY = "c-lat"; // 纬度
//                private final String NET_KEY = "c-nw"; // 网络类型 "unknow"; "wifi";"2G";"3G";"4G";
//                private final String API_KEY = "c-iv"; // 客户端请求的接口版本 2.3.3 三位数
//                private final String CLIENT_KEY = "c-cv"; // 客户端版本
//                private final String CLIENT_TYPE = "c-ct"; // 客户端类型 1:安卓，2:IOS
//                private final String CLIENT_SUB_TYPE = "c-st"; // 客户端子类型 1:用户，2:司机 3：出租车司机
//                private final String SCREEN_WIDTH = "c-cw"; // 屏幕宽度
//                private final String SCREEN_HEIGHT = "c-ch"; // 屏幕高度
//                private final String SRC_KEY = "c-sr"; // 渠道   默认是0，其他渠道后续定义
//                private final String BRAND_KEY = "c-br"; // 品牌名,比如华为，小米，苹果x
//                private final String MODEL_KEY = "c-mo"; // The end-user-visible name for the end product.
//                private final String SDK_VERSION_KEY = "c-sv"; // The user-visible version string.
//                private final String IMEI_KEY = "c-im"; // imei号.
                if (!request.containsHeader(NET_KEY)) {
                    request.addHeader(NET_KEY, ApnUtil.getNetTypeName(mAppContext));
                }
                if (!request.containsHeader(API_KEY)) {
                    request.addHeader(API_KEY, API_LEAVEL);
                }
                if (!request.containsHeader(CLIENT_TYPE)) {
                    request.addHeader(CLIENT_TYPE, "1");
                }
                if (!request.containsHeader(CLIENT_KEY)) {
                    request.addHeader(CLIENT_KEY, CLIENT_VERSION);
                }
                if (!request.containsHeader(SCREEN_WIDTH)) {
                    request.addHeader(SCREEN_WIDTH, ResourcesUtil.getScreenWidth() + "");
                }

                if (!request.containsHeader(SCREEN_HEIGHT)) {
                    request.addHeader(SCREEN_HEIGHT, ResourcesUtil.getScreenHeight() + "");
                }
                if (!request.containsHeader(SRC_KEY)) {
                    request.addHeader(SRC_KEY, CHANNEL_ID);
                }
                if (!request.containsHeader(TOKEN)) {
                    request.addHeader(TOKEN, USER_TOKEN);
                }

                if (!request.containsHeader(LNG_KEY)) {
                    request.addHeader(LNG_KEY, String.valueOf(LONGTITUDE));
                }
                if (!request.containsHeader(LAT_KEY)) {
                    request.addHeader(LAT_KEY, String.valueOf(LATITUDE));
                }
                if (!request.containsHeader(BRAND_KEY)) {
                    request.addHeader(BRAND_KEY, Build.BRAND);
                }

                if (!request.containsHeader(MODEL_KEY)) {
                    request.addHeader(MODEL_KEY, Build.MODEL);
                }
                if (!request.containsHeader(SDK_VERSION_KEY)) {
                    request.addHeader(SDK_VERSION_KEY, Build.VERSION.RELEASE);
                }
                if (!request.containsHeader(IMEI_KEY)) {
                    request.addHeader(IMEI_KEY, DevicesUtil.getIMEI(mAppContext));
                }
                if (!request.containsHeader(CLIENT_SUB_TYPE)) {
                    request.addHeader(CLIENT_SUB_TYPE, "3");
                }
            }
        });

        hurlStack.addResponseInterceptor(new HttpUrlResponseInterceptor() {

            @Override
            public void process(HttpResponse response, HttpURLConnection connection) {
                // cookie管理解析
                CookieManager.getInstance().put(connection.getURL(), connection.getHeaderFields());

                // 需要对token进行特殊处理,如果有返回token则更新系统的token
                String token = CookieManager.getInstance().getCookieValue(connection.getURL(), "token");
                if (!TextUtils.isEmpty(token)) {
                    USER_TOKEN = token;
                }
                // 保存cookie
                CookieManager.getInstance().save();
            }
        });

        return hurlStack;
    }
}
