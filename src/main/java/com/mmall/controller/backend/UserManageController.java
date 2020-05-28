package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtils;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 后台管理员登录service层;
 */
@Controller
@RequestMapping("/manage/user/")
public class UserManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private RedisUtil redisUtil;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> login(String username,
                                       String password,
                                       HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse,
                                       String access_token){

        if(StringUtils.isNotBlank(access_token)){
            User user = (User)redisUtil.get(access_token);
            if(user!= null){
                return ServiceResponse.createBySuccess("已登录",user);
            }
        }

        ServiceResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            // 写入redis里面登录缓存;
            redisUtil.set(MD5Util.MD5EncodeUtf8(username),response.getData(),30*60);
            // 写入客户端cookie;
            CookieUtils.setCookie(httpServletRequest,httpServletResponse,"access_token",MD5Util.MD5EncodeUtf8(username),30*60);
        }
        return response;
    }
}