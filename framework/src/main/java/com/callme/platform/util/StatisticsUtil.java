package com.callme.platform.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.baidu.mobstat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司
 * 版权所有
 * <p>
 * 功能描述：广告统计工具类
 * 作者：huangyong
 * 创建时间：2018/6/21
 * <p>
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class StatisticsUtil {
    /**
     * 闪屏广告
     */
    public final static String SPLASH_AD = "SplashAd";
    /**
     * 首页广告
     */
    public final static String HOME_AD = "HomeAd";

    /**
     * 点击事件
     */
    private final static String EVENT_CLICK = "click";

    /**
     * 初始化统计
     *
     * @param context
     */
    public static void init(Application context, String channel, boolean debug) {
        // 请在初始化时调用，参数为Application或Activity或Service
        UMConfigure.init(context, null, channel, UMConfigure.DEVICE_TYPE_PHONE, null);
        UMConfigure.setLogEnabled(debug);
        UMConfigure.setEncryptEnabled(true);
        initBaiDu(context, channel, debug);
    }

    public static void initBaiDu(Application context, String channel, boolean debug) {
        StatService.start(context);
        StatService.setDebugOn(debug);
        StatService.setAppChannel(channel);
    }


    /**
     * 开始统计页面
     *
     * @param activity
     */
    public static void onResume(Activity activity) {
        StatService.onResume(activity);
        MobclickAgent.onResume(activity);
    }

    /**
     * 结束统计页面
     *
     * @param activity
     */
    public static void onPause(Activity activity) {
        StatService.onPause(activity);
        MobclickAgent.onPause(activity);
    }

    /**
     * 统计点击事件
     *
     * @param context
     * @param clickId
     */
    public static void trackClickEvent(Context context, String clickId) {
        MobclickAgent.onEvent(context, clickId);
        StatService.onEvent(context, clickId, "");
    }
}
