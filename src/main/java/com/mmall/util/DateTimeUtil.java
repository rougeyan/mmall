package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by geely
 */
public class DateTimeUtil {

    //joda-time

    //str->Date
    //Date->str
    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";



    public static Date strToDate(String dateTimeStr,String formatStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date,String formatStr){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }
    // 封装重载
    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }
    // 封装重载
    public static String dateToStr(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }

    /**
     * 获取当前时间小时整点时间
     *
     * @param
     * @return
     */

//    [java获取时间整点工具代码](https://blog.csdn.net/zhan107876/article/details/95943256)
    public static Date getCurrentHourTime() {
        return getHourTime(new Date(), 0, "=");
    }

    /**
     * 获取指定时间上n个小时整点时间
     *
     * @param date
     * @return
     */
    public static Date getLastHourTime(Date date, int n) {
        return getHourTime(date, n, "-");
    }

    /**
     * 获取指定时间下n个小时整点时间
     *
     * @param date
     * @return
     */
    public static Date getNextHourTime(Date date, int n) {
        return getHourTime(date, n, "+");
    }

    /**
     * 获取指定时间n个小时整点时间 0分0秒 0毫秒
     *
     * @param date
     * @return
     */
    public static Date getHourTime(Date date, int n, String direction) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.MINUTE, 0); // 分钟设置为 0
        ca.set(Calendar.SECOND, 0); // 秒数设置为 0
        ca.set(Calendar.MILLISECOND,0); // 毫秒设置为 0
        switch (direction) {
            case "+":
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY) + n);
                break;
            case "-":
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY) - n);
                break;
            case "=":
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY));
                break;
            default:
                ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY));
        }

        date = ca.getTime();
        return date;
    }




    public static void main(String[] args) {
        System.out.println(DateTimeUtil.dateToStr(new Date(),"yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateTimeUtil.strToDate("2019-01-01 11:11:11","yyyy-MM-dd HH:mm:ss"));

    }


}
