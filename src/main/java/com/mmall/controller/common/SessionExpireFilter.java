package com.mmall.controller.common;

import com.mmall.pojo.User;
import com.mmall.util.CookieUtils;
import com.mmall.util.RedisUtil;
import javafx.application.Application;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//这个是针对controller影响的包
// 这个是servlet 的过滤器

/**
 * 基于servlet需要注入bean对象;
 *
 * 本过滤器 的目的是:
 * 1.判定是否登录状态;
 * 2.每次操作延长session失效时间;
 *
 */
public class SessionExpireFilter implements Filter {
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("进入了init方法");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String loginToken = CookieUtils.getCookieValue(httpServletRequest, "access_token");
        if(StringUtils.isNotEmpty(loginToken)){
            // 如果不为空,符合条件,从redis里面继续找到登录信息;
            User user = (User) redisUtil.get(loginToken);
            if(user != null){
                // 如果user不为空, 则重置session时间, 即调用expire命令(延长下次操作)
                redisUtil.expire(loginToken,1800);
//                CookieUtils.setCookie(httpServletRequest,httpServletResponse,"access_token",loginToken,180);
                // 流转到controller
//                chain.doFilter(request,response);
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
