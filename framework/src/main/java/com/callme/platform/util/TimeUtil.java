package com.callme.platform.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;


/*
 * Copyright (C)
 * 版权所有
 *
 * 功能描述：时间工具类
 * 作者：mikeyou
 *
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class TimeUtil {

    public static final String TIME_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String TIME_YYYY_MM_DD2 = "yyyy'/'MM'/'dd";
    public static final String TIME_YYYY_MM_DD3 = "yyyy.MM.dd";
    public static final String TIME_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String TIME_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String TIME_MM_DD_HH_MM = "MM-dd HH:mm";
    public static final String TIME_MM_DD_HH_MM_SS = "MM-dd HH:mm:ss";
    public static final String TIME_HH_MM = "HH:mm";
    public static final String TIME_HH_MM_SS = "HH:mm:ss";
    public static final String TIME_MM_DD = "MM-dd";
    public static final String TIME_MM_DD_HH_MM_CHINESE = "MM月dd日HH:mm";
    public static final String TIME_YYYY_MM_DD_HH_MM_CHINESE = "yyyy年MM月dd日HH:mm";

    /**
     * 时间字符串格式化
     *
     * @param date      需要被处理的日期字符串
     * @param parseStr  需要被处理的日期的格式串
     * @param formatStr 最终返回的日期字符串的格式串
     * @return 已经格式化的日期字符串
     */
    public static String formatDate(String date, String parseStr,
                                    String formatStr) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(parseStr);
        Date d = null;
        try {
            d = sdf.parse(date);
            sdf.applyPattern(formatStr);
            return sdf.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatDate(long date, String formatStr) {
        if (date <= 0) {
            return "";
        }
        DateFormat sdf = new SimpleDateFormat(formatStr);
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatDate(String date, String format) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        String str = date;
        if (str.contains("T")) {
            str = str.replace("T", " ");
        }
        if (str.contains("/")) {
            str = str.replaceAll("/", "-");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = sdf.parse(str);
            return sdf.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 时间为 昨天或今天时,不显示年月日改为显示"昨天"或"今天" 比如 "2014-01-01 12:10:26 --> 今天 12:10:26"
     * ,"2015-12-23 18:51 --> 昨天 18:51"
     *
     * @param time 当天时间
     * @param f    时间格式
     * @return
     */
    public static String formatDate2(String time, String f) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(
                    TIME_YYYY_MM_DD_HH_MM, Locale.ENGLISH);
            Date date = null;
            time = formatDate(time, f);
            date = format.parse(time);

            Calendar current = Calendar.getInstance();
            current.setTime(date);
            Calendar today = getSomeDay(0);
            Calendar tomorrow = getSomeDay(1);
            Calendar yesterday = getSomeDay(-1);

            if (current.before(tomorrow) && current.after(today)) {// 今天
                return "今天 " + time.split(" ")[1];
            } else if (current.before(today) && current.after(yesterday)) {// 昨天
                return "昨天 " + time.split(" ")[1];
            } else if (current.before(yesterday)) {// 昨天前面的时间
                return time;
            } else if (current.after(tomorrow)) {// 今天之后的时间
                return time;
            } else {// 应该不会存在
                return time;
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 今天的消息显示“时分”、昨天的消息显示“昨天 时:分”，昨天之前的信息显示“年-月-日 时:分”
     *
     * @param time 当天时间
     * @return
     */
    public static String formatDate3(long time) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat(TIME_HH_MM,
                    Locale.ENGLISH);
            SimpleDateFormat format2 = new SimpleDateFormat(
                    TIME_YYYY_MM_DD_HH_MM, Locale.ENGLISH);
            Calendar current = Calendar.getInstance();
            Date date = new Date(time);
            current.setTime(date);
            Calendar today = getSomeDay(0);
            Calendar tomorrow = getSomeDay(1);
            Calendar yesterday = getSomeDay(-1);

            if (current.before(tomorrow) && current.after(today)) {// 今天
                return format1.format(date);
            } else if (current.before(today) && current.after(yesterday)) {// 昨天
                return "昨天" + format1.format(date);
            } else if (current.before(yesterday)) {// 昨天前面的时间
                return format2.format(date);
            } else if (current.after(tomorrow)) {// 今天之后的时间
                return format2.format(date);
            } else {// 应该不会存在
                return format2.format(date);
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 得到某天零点零分零秒的Calendar对象
     *
     * @param dayOfMonth 今天传0 昨天传-1 明天传1 其他天++或者--
     * @return Calendar对象
     */
    private static Calendar getSomeDay(int dayOfMonth) {
        Calendar current = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, current.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, current.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH)
                + dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public static String getWeak(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String week = "星期日";
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                week = "星期日";
                break;
            case 2:
                week = "星期一";
                break;
            case 3:
                week = "星期二";
                break;
            case 4:
                week = "星期三";
                break;
            case 5:
                week = "星期四";
                break;
            case 6:
                week = "星期五";
                break;
            case 7:
                week = "星期六";
                break;
            default:
                break;
        }
        return week;
    }

    /**
     * 字符时间转换成Date
     *
     * @param value
     * @param format 需要的Date格式
     * @return
     */
    public static Date strToDate(String value, String format) {
        if (TextUtils.isEmpty(value)) {
            return new Date();
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date strtodate = formatter.parse(value);
            return strtodate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long strToMillis(String time) {
        Date date = strToDate(time, "yyyy-MM-dd");
        if (date != null) {
            return date.getTime();
        } else {
            return 0;
        }
    }

    public static long strToMillis(String time, String format) {
        Date date = strToDate(time, format);
        if (date != null) {
            return date.getTime();
        } else {
            return 0;
        }
    }

    /**
     * 计算两个时间之间的相差天数，第二个参数减第一个参数
     *
     * @param first
     * @param second
     * @return
     */
    public static int compareDifference(long first, long second) {
        try {
            long time = second - first;
            int diff = (int) (time / 60 / 60 / 1000 / 24);
            return diff;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 比较两个日期大小，如果第一个小于第二个返回true，
     *
     * @param first
     * @param second
     * @return
     */
    public static boolean compareDate(String first, String second) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date firstDate;
        try {
            firstDate = sdf.parse(first);
            Date secondDate = sdf.parse(second);
            return firstDate.before(secondDate);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据传入date获取前后多少天的日期
     *
     * @param date
     * @param day         正数就是后几天 负数就是前几天
     * @param returnParse 返回时间的格式
     * @return
     */
    public static String getDateByDay(String date, int day, String returnParse) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(strToMillis(date));
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return formatDate(calendar.getTimeInMillis(), returnParse);
    }

    /**
     * 比较两个日期的大小（前者大 返回true,比较到分钟数）
     *
     * @param one
     * @param two
     * @return
     */
    public static boolean compareDate(Date one, Date two) {
        if (one.getYear() > two.getYear()) {
            return true;
        } else if (one.getYear() == two.getYear()
                && one.getMonth() > two.getMonth()) {
            return true;
        } else if (one.getYear() == two.getYear()
                && one.getMonth() == two.getMonth()
                && one.getDate() > two.getDate()) {
            return true;
        } else if (one.getYear() == two.getYear()
                && one.getMonth() == two.getMonth()
                && one.getDate() == two.getDate()
                && one.getHours() > two.getHours()) {
            return true;
        } else return one.getYear() == two.getYear()
                && one.getMonth() == two.getMonth()
                && one.getDate() == two.getDate()
                && one.getHours() == two.getHours()
                && one.getMinutes() > two.getMinutes();
    }

    public static String getTimeStrFromDatePicker(DatePicker date,
                                                  TimePicker time) {
        String timeStr = date.getYear() + "-";
        if (date.getMonth() + 1 < 10) {
            timeStr += "0" + (date.getMonth() + 1);
        } else {
            timeStr += (date.getMonth() + 1);
        }
        timeStr += "-";
        if (date.getDayOfMonth() < 10) {
            timeStr += "0" + date.getDayOfMonth();
        } else {
            timeStr += date.getDayOfMonth();
        }
        if (time != null) {
            if (time.getCurrentHour() < 10) {
                timeStr += " 0" + time.getCurrentHour();
            } else {
                timeStr += " " + time.getCurrentHour();
            }
            if (time.getCurrentMinute() < 10) {
                timeStr += ":0" + time.getCurrentMinute();
            } else {
                timeStr += ":" + time.getCurrentMinute();
            }
        }
        return timeStr;
    }

    /**
     * @param date
     * @return 1天5小时20分
     * @throws ParseException
     */
    @SuppressLint("SimpleDateFormat")
    public static String distanceDay(String date) {
        String reulst = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = formatDate(date, "yyyy-MM-dd HH:mm:ss");
        // 给定的时间
        Date end = null;
        try {
            end = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 当前时间
        Date now = new Date();
        // 得到时间差
        long diff = (end.getTime() - now.getTime()) / 1000;
        long mm = (diff / 60) % 60;
        long hh = (diff / 60 / 60) % 24;
        long dd = (diff / 60 / 60 / 24);

        reulst = (dd + "天" + hh + "小时" + mm + "分");

        return reulst;
    }

    /**
     * @return 获取当前月第一天
     */
    @SuppressLint("SimpleDateFormat")
    public static String getMonthForFirstDay() {
        SimpleDateFormat format = new SimpleDateFormat(TIME_YYYY_MM_DD);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        return format.format(cal.getTime());
    }

    /**
     * @return 获取当前月最后一天
     */
    @SuppressLint("SimpleDateFormat")
    public static String getMonthForLastDay() {
        SimpleDateFormat format = new SimpleDateFormat(TIME_YYYY_MM_DD);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,
                cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return format.format(cal.getTime());
    }

    /**
     * 刚刚------2分钟及以内的
     * <p>
     * 今天 hh:mm------除了刚刚，且在当天24:00内的
     * <p>
     * yy-mm-dd hh:mm----除了以上的，就是它
     *
     * @param time
     * @return
     */
    public static String getProductHistory(long time) {
        String result = null;
        if ((System.currentTimeMillis() - time) <= 2 * 1000 * 60) {
            result = "刚刚";
        } else if (isSameDay(time, System.currentTimeMillis())) {
            result = "今天" + formatDate(time, TIME_HH_MM);
        } else {
            result = formatDate(time, TimeUtil.TIME_YYYY_MM_DD_HH_MM);
        }

        return result;
    }

    public static boolean isSameDay(long timeOne, long timeTwo) {
        Date dateOne = new Date(timeOne);
        Date dateTwo = new Date(timeTwo);
        return dateOne.getYear() == dateTwo.getYear()
                && dateOne.getMonth() == dateTwo.getMonth()
                && dateOne.getDate() == dateTwo.getDate();
    }

    public static boolean isSameDay(String timeOne, String timeTwo) {
        Date dateOne = strToDate(timeOne, TIME_YYYY_MM_DD);
        Date dateTwo = strToDate(timeTwo, TIME_YYYY_MM_DD);
        return dateOne.getYear() == dateTwo.getYear()
                && dateOne.getMonth() == dateTwo.getMonth()
                && dateOne.getDate() == dateTwo.getDate();
    }

    public static boolean isYesterdayDay(long timeOne, long timeTwo) {
        Date dateOne = new Date(timeOne);
        Date dateTwo = new Date(timeTwo);
        return dateOne.getYear() == dateTwo.getYear()
                && dateOne.getMonth() == dateTwo.getMonth()
                && (dateOne.getDate() == dateTwo.getDate() - 1);
    }

    public static String getPostBoardHeaderTime(long time) {
        String result = null;
        if (isSameDay(time, System.currentTimeMillis())) {
            result = "今天";
        } else if (isYesterdayDay(time, System.currentTimeMillis())) {
            result = "昨天";
        } else {
            result = formatDate(time, TimeUtil.TIME_YYYY_MM_DD);
        }

        return result;
    }

    public static String getPostBoardHeaderTime(String time) {
        long localTime = strToMillis(
                formatDate(time, TIME_YYYY_MM_DD_HH_MM_SS),
                TIME_YYYY_MM_DD_HH_MM_SS);
        return getPostBoardHeaderTime(localTime);
    }


    /**
     * 截取格式 **年**月**日
     *
     * @param date
     * @return
     */
    public static String formatDate(String date) {
        if (TextUtils.isEmpty(date)) {
            return "";
        }
        date = formatDate(date, TIME_YYYY_MM_DD);
        String[] arr = date.split("-");
        if (arr != null && arr.length == 3) {
            String s = arr[0] + "年" + arr[1] + "月" + arr[2] + "日";
            return s;
        }
        return "";
    }

    /**
     * 当前时间推迟一年加一天
     *
     * @param format 返回数据的时间格式
     * @return
     */
    public static String getTheDayNextYear(String format) {
        String nextYear = "";
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
            calendar.set(Calendar.DAY_OF_YEAR,
                    calendar.get(Calendar.DAY_OF_YEAR) + 1);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            nextYear = sdf.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextYear;
    }

    /**
     * 把时间毫秒数转换为指定格式的字符串
     *
     * @param time   时间毫秒数
     * @param format 指定格式
     * @return 时间字符串
     */
    public static String formatTime(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm",
                Locale.ENGLISH);
        return sdf.format(new Date(time));
    }

    /**
     * (^\\d{4}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D*$","yyyy-MM-dd-HH-mm-ss")
     * //2017年10月24日 10时05分34秒，2017-10-24 10:05:34，2017/010/24 10:05:34
     * <p>
     * ("^\\d{4}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd-HH-mm")  //2017-10-24 10:05:34
     * ("^\\d{4}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd-HH") //2017-10-24 10
     * ("^\\d{4}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd");//2017-10-24
     * ("^\\d{4}\\D+\\d{2}$", "yyyy-MM");//2017-10
     * ("^\\d{4}$", "yyyy");//2017
     * ("^\\d{14}$", "yyyyMMddHHmmss");//20171024100534
     * ("^\\d{12}$", "yyyyMMddHHmm");//201710241005
     * ("^\\d{10}$", "yyyyMMddHH");//2017102410
     * ("^\\d{8}$", "yyyyMMdd");//20171024
     * ("^\\d{6}$", "yyyyMM");//201710
     * ("^\\d{2}\\s*:\\s*\\d{2}\\s*:\\s*\\d{2}$", "yyyy-MM-dd-HH-mm-ss");//10:05:34 拼接当前日期
     * ("^\\d{2}\\s*:\\s*\\d{2}$", "yyyy-MM-dd-HH-mm");//10:05 拼接当前日期
     * ("^\\d{2}\\D+\\d{1,2}\\D+\\d{1,2}$", "yy-MM-dd");//17.10.24(年.月.日)
     * ("^\\d{1,2}\\D+\\d{1,2}$", "yyyy-dd-MM");//24.10(日.月) 拼接当前年份
     * ("^\\d{1,2}\\D+\\d{1,2}\\D+\\d{4}$", "dd-MM-yyyy");//24.10.2017(日.月.年)
     *
     * @param srcTime
     * @see #getTime(String srcTime)
     */
    public static long getTime(String srcTime) {
        if (TextUtils.isEmpty(srcTime)) {
            return 0;
        }

        HashMap<String, String> dateRegFormat = new HashMap<String, String>();
        dateRegFormat.put("^\\d{4}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D+\\d{1,2}\\D*$", "yyyy-MM-dd-HH-mm-ss");
        dateRegFormat.put("^\\d{4}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd-HH-mm");
        dateRegFormat.put("^\\d{4}\\D+\\d{2}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd-HH");
        dateRegFormat.put("^\\d{4}\\D+\\d{2}\\D+\\d{2}$", "yyyy-MM-dd");
        dateRegFormat.put("^\\d{4}\\D+\\d{2}$", "yyyy-MM");
        dateRegFormat.put("^\\d{4}$", "yyyy");
        dateRegFormat.put("^\\d{14}$", "yyyyMMddHHmmss");
        dateRegFormat.put("^\\d{12}$", "yyyyMMddHHmm");
        dateRegFormat.put("^\\d{10}$", "yyyyMMddHH");
        dateRegFormat.put("^\\d{8}$", "yyyyMMdd");
        dateRegFormat.put("^\\d{6}$", "yyyyMM");
        dateRegFormat.put("^\\d{2}\\s*:\\s*\\d{2}\\s*:\\s*\\d{2}$", "yyyy-MM-dd-HH-mm-ss");
        dateRegFormat.put("^\\d{2}\\s*:\\s*\\d{2}$", "yyyy-MM-dd-HH-mm");
        dateRegFormat.put("^\\d{2}\\D+\\d{1,2}\\D+\\d{1,2}$", "yy-MM-dd");
        dateRegFormat.put("^\\d{1,2}\\D+\\d{1,2}$", "yyyy-dd-MM");
        dateRegFormat.put("^\\d{1,2}\\D+\\d{1,2}\\D+\\d{4}$", "dd-MM-yyyy");

        String curDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dateReplace;
        long time = 0;
        try {
            for (String key : dateRegFormat.keySet()) {
                if (Pattern.compile(key).matcher(srcTime).matches()) {
                    if (key.equals("^\\d{2}\\s*:\\s*\\d{2}\\s*:\\s*\\d{2}$") || key.equals("^\\d{2}\\s*:\\s*\\d{2}$")) {
                        srcTime = curDate + "-" + srcTime;
                    } else if (key.equals("^\\d{1,2}\\D+\\d{1,2}$")) {
                        srcTime = curDate.substring(0, 4) + "-" + srcTime;
                    }
                    dateReplace = srcTime.replaceAll("\\D+", "-");
                    time = TimeUtil.strToMillis(dateReplace, dateRegFormat.get(key));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("------invalid date format:" + srcTime);
            throw new Exception("invalid date format");
        } finally {
            return time;
        }
    }

    /**
     * 获得需要时间：当天只展示HH:mm；同年展示MM-DD HH:mm;不同年份展示YYYY-MM-DD HH:mm
     *
     * @param startTime
     * @return
     */
    public static String getRequiredTime(long startTime) {
        if (startTime < 0) {
            return "";
        }
        String tagTime = TimeUtil.formatDate(startTime, TimeUtil.TIME_YYYY_MM_DD_HH_MM);
        String currDate = TimeUtil.formatDate(System.currentTimeMillis(), TimeUtil.TIME_MM_DD);
        if (tagTime.contains(currDate)) {//年月日相同，则只显示时分
            return tagTime.substring(tagTime.indexOf(" ") + 1, tagTime.length());
        } else if (tagTime.startsWith(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)))) {
            //年份相同，则展示月日时分
            return TimeUtil.formatDate(startTime, TimeUtil.TIME_MM_DD_HH_MM_CHINESE);
        } else {
            return TimeUtil.formatDate(startTime, TimeUtil.TIME_YYYY_MM_DD_HH_MM_CHINESE);
        }
    }
}
