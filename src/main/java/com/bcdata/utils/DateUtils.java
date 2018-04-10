package com.bcdata.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     * @param format 字符串的时间格式
     * @return 日期格式的字符串
     */
    public static String timeStampToDate(String seconds, String format) {

        if (seconds == null || seconds.isEmpty () || seconds.equals ("null")) {
            return "";
        }

        if(format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat (format);

        return simpleDateFormat.format (new Date (Long.valueOf (seconds + "000")));
    }

    public static String timeStampToDate(long seconds, String format) {

        if (seconds <= 0) {
            return "";
        }

        if(format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat (format);

        return simpleDateFormat.format (new Date (seconds * 1000));
    }

    /**
     * 日期格式字符串转换成时间戳,精确到秒的时间
     * @param dateStr 字符串日志
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return 返回的秒数
     */
    public static long dateToTimeStamp(String dateStr, String format) {

        if (dateStr == null || dateStr.isEmpty () || dateStr.equals ("null")) {
            return 0;
        }

        if(format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat (format);
            return simpleDateFormat.parse (dateStr).getTime ()/1000;
        } catch (ParseException pe) {
            pe.printStackTrace ();
        }

        return 0;
    }
}
