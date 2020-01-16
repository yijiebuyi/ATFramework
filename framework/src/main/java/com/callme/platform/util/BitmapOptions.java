package com.callme.platform.util;

import com.callme.platform.util.bitmap.DisplayImageOptions;
import com.callme.platform.util.bitmap.DisplayImageOptions.Builder;

/**
 * @author mikeyou 图片加载参数设置
 */
public class BitmapOptions {
    // 图片管理图片适配的各种状态，默认是长边适配，不需要设置状态
    public static final int TYPE_SHORT = 100; // 短边适配
    public static final int TYPE_CUT = 101; // 裁剪 OR 长边适配+裁剪
    public static final int TYPE_MATRIX = 102; // 拉伸变形
    public static final int TYPE_SHORT_CUT = 103; // 短边适配+裁剪
    /**
     * 首页banner
     */
    public static final int DEFAULT_USER_BANNER = 3000;

    private final static int WIDTH_480 = 480;
    private final static int WIDTH_720 = 720;
    private final static int WIDTH_800 = 800;
    private final static int WIDTH_1080 = 1080;
    private final static int TYPE_480 = 0;
    private final static int TYPE_720 = 1;
    private final static int TYPE_800 = 2;
    private final static int TYPE_1080 = 3;

    public static String getParamByType(int type) {
        String param = "";
        switch (type) {
            case TYPE_SHORT:
                param += "_1e_";
                break;
            case TYPE_CUT:
                param += "_1c_";
                break;
            case TYPE_MATRIX:
                param += "_2e_";
                break;
            case TYPE_SHORT_CUT:
                param += "_1e_1c_";
                break;
            default:
                break;
        }
        return param;
    }



    /**
     * 获取显示图片的options
     */
    public static DisplayImageOptions getOption(int type) {
        Builder options = new Builder();
        options.cacheInMemory(true);
        options.cacheOnDisk(true);
        int[] wh = getWhByType(type);
        if (wh != null && wh.length == 2) {
            options.setWidth(wh[0]);
            options.setHeight(wh[1]);
        }
        switch (type) {

            case DEFAULT_USER_BANNER:
//                options.showImageForEmptyUri(R.drawable.icon_user_banner);
//                options.showImageOnFail(R.drawable.icon_user_banner);
//                options.showImageOnLoading(R.drawable.icon_user_banner);
                break;
            default:
                break;
        }

        return options.build();
    }

    /**
     * 根据图片类型和屏幕宽度返回对应类型图片的尺寸
     *
     * @param imageType 图片类型，参考getOption中类型
     * @return 返回
     */
    public static final int[] getWhByType(int imageType) {
        int wh[] = new int[2];
        int screenType = getWhType();
        int[] height = null;
        switch (imageType) {
            case DEFAULT_USER_BANNER:
//                height = ResourcesUtil.getIntArray(R.array.user_banner_height);
//                wh[0] = ResourcesUtil.getScreenWidth();
//                wh[1] = height[screenType];
                break;

            default:
                break;
        }
        return wh;
    }

    /**
     * 返回屏幕类型
     *
     * @return 返回屏幕大小类型，从0开始
     */
    public static int getWhType() {
        int type = -1;
        int screenWidth = ResourcesUtil.getScreenWidth();
        switch (screenWidth) {
            case WIDTH_480:
                type = TYPE_480;
                break;
            case WIDTH_720:
                type = TYPE_720;
                break;
            case WIDTH_800:
                type = TYPE_800;
                break;
            case WIDTH_1080:
                type = TYPE_1080;
                break;
            default:
                type = TYPE_720;
                break;
        }
        if (screenWidth > WIDTH_1080) {
            type = TYPE_1080;
        } else if (screenWidth < WIDTH_480) {
            type = TYPE_480;
        }
        return type;
    }



}
