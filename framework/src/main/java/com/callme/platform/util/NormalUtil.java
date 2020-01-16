package com.callme.platform.util;

import android.content.Intent;


public class NormalUtil {


    // 判断是否从长按home返回
    public static boolean isBackFromHistory(Intent startintent) {
        int flag = Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
        int flag_sdk = 0;

        // 4.0以上的机器从历史启动的flag有所不同
        if (DevicesUtil.getSystemVersionLevel() >= 14)
            flag_sdk = Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                    | Intent.FLAG_ACTIVITY_TASK_ON_HOME;
        else
            flag_sdk = Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;

        return startintent.getFlags() == flag
                || startintent.getFlags() == flag_sdk;
    }
}
