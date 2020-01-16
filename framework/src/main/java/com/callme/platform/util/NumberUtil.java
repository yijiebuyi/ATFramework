package com.callme.platform.util;

import android.text.TextUtils;

import java.math.RoundingMode;
import java.text.NumberFormat;


/*
 * Copyright (C) 2017 重庆呼我出行网络科技有限公司 版权所有
 * 
 * 功能描述： 数字工具类
 * 添加日期：2017.10.6
 * 作者：mikeyou
 * 
 * 修改人： 
 * 修改描述：
 * 修改日期
 */
public class NumberUtil {

	public static String parseToString(double doubleMoney) {
		return parseToString(String.valueOf(doubleMoney));
	}

	public static String parseToString(String doubleMoney) {
		if (doubleMoney.contains(".")) {
			int num = doubleMoney.length() - (doubleMoney.indexOf(".") + 1);
			if (num > 2) {
				doubleMoney = doubleMoney.substring(0,
						doubleMoney.indexOf(".") + 3);
			} else if (num < 2) {
				doubleMoney += "0";
			}
		} else {
			doubleMoney += ".00";
		}
		return doubleMoney;
	}

	/**
	 * 
	 * @param val
	 * @param precision
	 * @return
	 */
	public static Double roundDouble(double val, int precision) {
		Double ret = null;
		try {
			double factor = Math.pow(10, precision);
			ret = Math.floor(val * factor + 0.5) / factor;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * double转换成string 可以防止double显示成4.99958333E7
	 * 
	 * @param value
	 *            待转换值
	 * @param precision
	 *            需要保留的位数
	 * @return
	 */
	public static String doubleToString(double value, int precision) {
		return doubleToString(value, precision, true);
	}

	/**
	 * double转换成string 可以防止double显示成4.99958333E7 不四舍五入 不四舍五入
	 * 
	 * @param value
	 *            待转换值
	 * @param precision
	 *            需要保留的位数
	 * @param isUpDown
	 *            是否需要四舍五入 false 直接截取
	 * @return
	 */
	public static String doubleToString(double value, int precision,
			boolean isUpDown) {
		NumberFormat df = NumberFormat.getInstance();
		df.setMaximumFractionDigits(precision);
		df.setMinimumFractionDigits(precision);
		if (!isUpDown) {
			df.setRoundingMode(RoundingMode.DOWN);
		}
		return df.format(value).replace(",", "");
	}

	/**
	 * long转换成string 可以防止long显示成4.99958333E7
	 * 
	 * @param value
	 * @return
	 */
	public static String longToString(long value) {
		NumberFormat df = NumberFormat.getInstance();
		df.setMaximumFractionDigits(0);
		df.setMinimumFractionDigits(0);
		return df.format(value).replace(",", "");
	}

	// 从字符串中获取数字
	public static String getNumFromString(String str) {
		String str2 = "";
		str = str.trim();
		if (str != null && !"".equals(str)) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
					str2 += str.charAt(i);
				}
			}
		}
		return str2;
	}

	// 根据传入指定起始点隐藏(用*代替) 起点从0开始计算
	public static String hideString(String str, int start, int end) {
		if (TextUtils.isEmpty(str)) {
			return "";
		}
		if (end > str.length() || start > str.length() || end <= start) {
			return str;
		}
		String hideReplace = "";
		int count = end - start;
		for (int i = 0; i < count; i++) {
			hideReplace += "*";
		}
		return str.substring(0, start) + hideReplace
				+ str.substring(end, str.length());
	}

	/**
	 * 1转成01
	 * 
	 * @param value
	 * @return
	 */
	public static String intAddToString(int value) {
		if (value < 9) {
			return "0" + value;
		} else {
			return "" + value;
		}
	}

}
