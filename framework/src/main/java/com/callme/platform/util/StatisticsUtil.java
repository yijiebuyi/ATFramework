package com.callme.platform.util;

import android.app.Application;
import android.content.Context;

import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;
import com.tencent.stat.hybrid.StatHybridHandler;

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
     * @param isDebugMode
     * @param channel
     */
    public static void init(Application context, boolean isDebugMode, String channel) {
        // 请在初始化时调用，参数为Application或Activity或Service
        //StatisticsDataAPI.instance(this);
        StatService.setContext(context);

        // TLink功能，true：开启；false：关闭，默认值
        StatConfig.setTLinkStatus(true);

        // hybrid统计功能初始化
        StatHybridHandler.init(context);


        //  初始化MTA配置
        initStatConfig(isDebugMode);
        // 注册Activity生命周期监控，自动统计时长
        StatService.registerActivityLifecycleCallbacks(context);
        // 初始化MTA的Crash模块，可监控java、native的Crash，以及Crash后的回调
        //MTACrashModule.initMtaCrashModule(this);

        StatConfig.setInstallChannel(context, channel);
    }


    /**
     * 根据不同的模式，建议设置的开关状态，可根据实际情况调整，仅供参考。
     *
     * @param isDebugMode 根据调试或发布条件，配置对应的MTA配置
     */
    private static void initStatConfig(boolean isDebugMode) {

        if (isDebugMode) { // 调试时建议设置的开关状态
            // 查看MTA日志及上报数据内容
            StatConfig.setDebugEnable(true);
            // StatConfig.setEnableSmartReporting(false);
            // Thread.setDefaultUncaughtExceptionHandler(new
            // UncaughtExceptionHandler() {
            //
            // @Override
            // public void uncaughtException(Thread thread, Throwable ex) {
            // logger.error("setDefaultUncaughtExceptionHandler");
            // }
            // });
            // 调试时，使用实时发送
            // StatConfig.setStatSendStrategy(StatReportStrategy.BATCH);
            // // 是否按顺序上报
            // StatConfig.setReportEventsByOrder(false);
            // // 缓存在内存的buffer日志数量,达到这个数量时会被写入db
            // StatConfig.setNumEventsCachedInMemory(30);
            // // 缓存在内存的buffer定期写入的周期
            // StatConfig.setFlushDBSpaceMS(10 * 1000);
            // // 如果用户退出后台，记得调用以下接口，将buffer写入db
            // StatService.flushDataToDB(getApplicationContext());

            // StatConfig.setEnableSmartReporting(false);
            // StatConfig.setSendPeriodMinutes(1);
            // StatConfig.setStatSendStrategy(StatReportStrategy.PERIOD);
        } else { // 发布时，建议设置的开关状态，请确保以下开关是否设置合理
            // 禁止MTA打印日志
            StatConfig.setDebugEnable(false);
            // 根据情况，决定是否开启MTA对app未处理异常的捕获
            StatConfig.setAutoExceptionCaught(true);
            // 选择默认的上报策略
            StatConfig.setStatSendStrategy(StatReportStrategy.PERIOD);
            // 10分钟上报一次的周期
            StatConfig.setSendPeriodMinutes(10);
        }

        // 初始化java crash捕获
        //StatCrashReporter.getStatCrashReporter(getApplicationContext()).setJavaCrashHandlerStatus(true);
        // 初始化native crash捕获，记得复制so文件
        //StatCrashReporter.getStatCrashReporter(getApplicationContext()).setJniNativeCrashStatus(true);
        // crash的回调，请根据需要添加
        //StatCrashReporter.getStatCrashReporter(getApplicationContext()).addCrashCallback(new StatCrashCallback() {

        //    @Override
        //    public void onJniNativeCrash(String tombstoneMsg) {
        //        LogUtil.d("Test", "Native crash happened, tombstone message:" + tombstoneMsg);
        //    }

        //    @Override
        //    public void onJavaCrash(Thread thread, Throwable throwable) {
        //        LogUtil.d("Test", "Java crash happened, thread: " + thread + ",Throwable:" + throwable.toString());
        //    }
        //});

    }

    /**
     * 开始统计页面
     *
     * @param context
     */
    public static void onResume(Context context) {
        StatService.onResume(context);
    }

    /**
     * 结束统计页面
     *
     * @param context
     */
    public static void onPause(Context context) {
        StatService.onPause(context);
    }

    /**
     * 统计点击事件
     *
     * @param context
     * @param clickId
     */
    public static void trackClickEvent(Context context, String clickId) {
        StatService.trackCustomEvent(context, clickId, EVENT_CLICK);
    }
}
