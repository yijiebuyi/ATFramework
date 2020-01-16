package com.callme.platform.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.callme.platform.R;
import com.callme.platform.common.dialog.CmDialog;
import com.callme.platform.common.dialog.CmDialog.DialogOnClickListener;

public class PhoneUtil {

    public static void takePhone(final Context context, final String num) {
        final CmDialog dialog = new CmDialog(context, num, 0);
        dialog.setPositiveButton(R.string.common_dial_telephone,
                new DialogOnClickListener() {
                    @Override
                    public void onClick() {
                        dialog.dismiss();
                        callPhone(context, num);
                    }
                });
        dialog.setNegativeButton(R.string.common_cancel, new DialogOnClickListener() {
            @Override
            public void onClick() {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void sendMessage(Context context, String num, String content) {
        Uri uri = Uri.parse("smsto:" + num);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", content);
        context.startActivity(intent);
    }

    public static void callPhone(Context context, String number) {
        callPhone(context, number, 9876);
    }

    /**
     * @param context
     * @param number
     */
    public static void callPhone(Context context, String number, boolean isDoubleCard) {
        callPhone(context, number, 9876, isDoubleCard);
    }

    public static void callPhone(Context context, String phoneNumber, int requestCode) {
        callPhone(context, phoneNumber, requestCode, false);
    }

    /**
     * 拨打电话
     *
     * @param context
     * @param phoneNumber
     * @param requestCode
     * @param isDoubleCard 是否有双卡可用
     */
    public static void callPhone(Context context, String phoneNumber, int requestCode, boolean isDoubleCard) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, requestCode);
            } else {
                ToastUtil.showCustomViewToast(context, "请开启应用拨打电话权限");
            }
        } else {
            Intent intent;
            if (isDoubleCard) {
                intent = new Intent(Intent.ACTION_DIAL);
            } else {
                intent = new Intent(Intent.ACTION_CALL);
            }
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
