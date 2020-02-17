package com.mmall.test;

import com.mmall.util.DateTimeUtil;
import com.mmall.util.MD5Util;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

public class BigDecimalTest {
    @Test
    public void test1(){
        System.out.println(0.05+0.01);
        System.out.println(1.0-0.42);
        System.out.println(4.015*100);
        System.out.println(123.3/100);
    }
    @Test
    public void test2(){
        // String 构造器;
        /// float 和double 只能用在 科学计算中;
        // 商业中只能使用 BigDecimal
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }

    @Test
    public void timeTest(){
        Date date = new Date();
//        long date1 = DateTimeUtil.getCurrentHourTime().getTime();
//        System.out.println(DateTimeUtil.getCurrentHourTime().getTime());
        String dateTime = String.valueOf(DateTimeUtil.getCurrentHourTime().getTime());
        System.out.println(dateTime);
//        System.out.println(date1);
//
//        System.out.println(date.getTime());
//        System.out.println(DateTimeUtil.dateToStr(date));
    }

    @Test
    public void testAssemble(){
        String userName = "admin";
        String dateTime = String.valueOf(DateTimeUtil.getCurrentHourTime().getTime());
        String assembleString = new StringBuilder().append(userName).append("%").append(dateTime).append("*").toString();
        String assembleSessionId = MD5Util.MD5EncodeUtf8(assembleString);
        System.out.println(assembleString);
        System.out.println(assembleSessionId);
//        admin%1576116020000*
//        699C28E8B327B8BDD3449C8E7A13ABC5

//        admin%1576116049000*
//        3B6E944A22852B2946755AFC2F96CB78

//        admin%1576116000000*
//                A7BF2272E21205EC67B2177C15031299

//        admin%1576116000000*
//                A7BF2272E21205EC67B2177C15031299
    }

}
