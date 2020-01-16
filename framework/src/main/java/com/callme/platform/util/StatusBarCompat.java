package com.callme.platform.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * author: zyl
 * time: 2016/7/19 9:02
 */
public class StatusBarCompat {
    public static View statusBarView;

    private StatusBarCompat() {
    }


    /**
     * 兼容状态栏颜色
     *
     * @param activity
     * @param color
     */
    public static void compat(Activity activity, int color, boolean isDark) {
        if (activity == null)
            return;

        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            if (decorView != null) {
                int vis = decorView.getSystemUiVisibility();
                if (isDark) {
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
            //window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.blue_1aacf7));//底部导航栏
            window.setStatusBarColor(color);
        } else {
            //通过添加View改变状态栏颜色
            ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(activity);

            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
                //如果已经为 ChildView 设置过了 marginTop, 再次调用时直接跳过
                if (lp != null && lp.topMargin < statusBarHeight && lp.height != statusBarHeight) {
                    //不预留系统空间
                    ViewCompat.setFitsSystemWindows(mChildView, false);
                    lp.topMargin += statusBarHeight;
                    mChildView.setLayoutParams(lp);
                }
            }


            statusBarView = mContentView.getChildAt(0);
            if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == statusBarHeight) {
                //避免重复调用时多次添加 View
                statusBarView.setBackgroundColor(color);
                return;
            }
            statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
            statusBarView.setBackgroundColor(color);

            mContentView.addView(statusBarView, 0, lp);


            /**
             *注释代码:获取内容布局的父布局,设置内容布局的topMargin值,并设置父布局的backgroud.
             **/
            //            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //            AppUtils.showStatusBar(this);
            //
            //            //设置全屏后,要显示状态栏须以下两行代码
            //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            //
            //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //
            //            FrameLayout mContentView = (FrameLayout) findViewById(android.R.id.content);//Window.ID_ANDROID_CONTENT是一个东东
            //            mContentView.setPadding(0, AppUtils.getStatusBarHeight(this), 0, 0);
            //            ViewGroup parent = (ViewGroup) mContentView.getParent();
            //            parent.setBackgroundColor(ContextCompat.getColor(this,R.color.gray_f4f4f4));

        }

        setMiuiStatusBarDarkMode(activity, isDark);
        setMeizuStatusBarDarkIcon(activity, isDark);
    }

    /**
     * 修改MIUI-StatusBar
     *
     * @param activity
     * @param darkmode
     * @return
     */
    public static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 修改Flyme(魅族)-StatusBar
     *
     * @param activity
     * @param dark
     * @return
     */
    public static boolean setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        boolean result = false;
        if (activity != null) {
            try {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                activity.getWindow().setAttributes(lp);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 隐藏状态栏
     *
     * @param activity
     */
    public static void hideStatusBar(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);
    }

    /**
     * 显示状态栏
     *
     * @param activity
     */
    public static void showStatusBar(Activity activity) {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);
    }


    /**
     * 资源获取顶部状态栏高度
     *
     * @param context 上下文
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        if (null == context)
            return 0;

        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);

        return height;
    }

}
