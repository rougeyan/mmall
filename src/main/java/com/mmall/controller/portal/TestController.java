package com.mmall.controller.portal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.OrderItem;
import com.mmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/test/")
public class TestController {
    /**
     * 依赖注入redisUtils
     * 动态去创建redisUtils这个工具类;
     * 由spring来生成这个redisUtils
     * 因为需要线程池 每次访问都应该从线程池种取一个工具类;
     */
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(value = "testSetRedis.do", method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse testpost(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session){
        List list = Lists.newArrayList();
        // 假如定义了 了list<objvo> 的话 就是 输出 [{objectVo},{objectVo}]
        list.add("string");
        list.add(1);
        System.out.println(list);
        Set testSet = Sets.newHashSet();


        testSet.add("set1");
        testSet.add("set2");

        // 测试object
        OrderItem orderItem = new OrderItem();
        orderItem.setId(12344);
        orderItem.setProductId(1482364);
        orderItem.setProductName("你好啊");

        // Map
        Map mappe = Maps.newHashMap();
        // Map<String,List<xxx> mappa = Maps.newHashMap();
        // Map即 kv  k是 hashset value 是arraylist;
        mappe.put("key1String","value1");
        mappe.put("key2Number",12); // 传入去的时候还没序列化呢?
        mappe.put("key3List",list); // 传入去的时候还没序列化呢?
        mappe.put("key4set",testSet);
        mappe.put("key5obj",orderItem);
//        redisUtil.hmset("mappe",mappe,600);
        String redisName = "test60times";
        String redisValue = "1234";
//        redisUtil.set(redisName,redisValue);
//        redisUtil.expire(redisName,30L,TimeUnit.SECONDS);

        Cookie cookie = new Cookie("ihavesomecookies","1234444444");
        cookie.setMaxAge(20);//最大有效时间
        cookie.setPath("/");
        response.addCookie(cookie);
        // 遍历就是 迭代器
        return ServiceResponse.createBySuccess(mappe);
    }


    @RequestMapping(value = "getRedis.do", method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse getRedis(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session){
//        Map<Object,Object> getmapper = redisUtil.hmget("mappe");
        // 遍历就是 迭代器
        return ServiceResponse.createBySuccess("getRedis");
    }


}