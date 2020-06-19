package com.mmall.controller.portal;

import com.google.common.collect.Maps;
import com.mmall.common.CheckLoginStatus;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Controller 注解定义是usercontroller
 * @RequestMapping("/user/")
 *
 * @ResponseBody() 通过springmvc 的json插件 返回值序列化成Json
 *
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 登陆接口
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse login(String username,
                                       String password,
                                       HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse,
                                       String access_token
                                       ){
        // 测试全局异常 runtimeException;
        // 流程
        //service -->mybatis --> dao
        // 如果有access_token的话去判定是否已经登录;
        User user = (User)redisUtil.get(access_token);
        if(user!= null){
            return ServiceResponse.createBySuccessMessage("已登录");
        }
        // 否则请求数据库校验登录讯息
        ServiceResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            // 写入redis里面登录缓存;
            redisUtil.set(MD5Util.MD5EncodeUtf8(username),response.getData(),30*60);
            // 写入客户端cookie;
            // 客户端的缓存失效时间要比redis的大
            CookieUtils.setCookie(httpServletRequest,httpServletResponse,"access_token",MD5Util.MD5EncodeUtf8(username),60*60*4);
            return ServiceResponse.createBySuccessMessage("登录成功");
        }
        return ServiceResponse.createByErrorMessage("登录失败,请重新登录或者联系管理员");

    }

    /**
     * 从redis中获取user的Id;
     * @param access_token
     * @return
     */
    @RequestMapping(value="getUserId.do")
    @ResponseBody()
    public ServiceResponse getUserId(String access_token){
        User user = (User)redisUtil.get(access_token);
        if(user!= null){
            Map result = Maps.newHashMap();
            result.put("userId",user.getId());
            return ServiceResponse.createBySuccess(result);
        }
        return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
    }

    /**
     * 登出接口
     * @param session
     * @return
     */
    @RequestMapping(value ="logout.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> logout(HttpSession session,
                                        String access_token,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse){
        if(access_token == null || StringUtils.isBlank(access_token)){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT_TOKEN.getCode(),ResponseCode.ILLEGAL_ARGUEMENT_TOKEN.getDesc());
        }else{
            redisUtil.del(access_token);
            CookieUtils.deleteCookie(httpServletRequest,httpServletResponse,"access_token");
            return ServiceResponse.createBySuccessMessage("登出成功");
        }
    }

    /**
     * 注册接口;
     * 如果不传user
     * 注册并且登录
     */
    @RequestMapping(value ="register.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     *  校验账号 邮箱
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value ="checkValid.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str, type);
    }

    /**
     * 获取用户信息
     * @param access_token
     * @return
     */
    @RequestMapping(value ="get_user_info.do",method = RequestMethod.GET)
    @ResponseBody()
    public ServiceResponse<User> getUserInfo(String access_token){
        User user = (User)redisUtil.get(access_token);
        // 空判断
        if(user != null){
            return ServiceResponse.createBySuccess(user);
        }
        return ServiceResponse.createByErrorMessage("用户未登陆,无法获取当前用户的信息");
    }

    /**
     * 获取 忘记密码问题
     * @param username
     * @return
     */
    @RequestMapping(value ="forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    /**
     * 检查问题答案
     * @param username
     * @return
     */
    @RequestMapping(value ="forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> forgetCheckAnswer(String username,String question, String answer){
        // 会使用到 guawa 缓存;
        return iUserService.checkAnswer(username,question,answer);
    }

    // 重置密码 有时效性 所以使用缓存;
    @RequestMapping(value ="forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> forgetResetPassword(String username, String passwordnew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordnew,forgetToken);
    }


    // 登录状态的状态下 => 重置密码
    // 重置密码 有时效性 所以使用缓存;
    @RequestMapping(value ="reset_password.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> resetPassword(HttpSession session,
                                                 String passwordOld,
                                                 String passwordNew,
                                                 String access_token){
        User user = (User)redisUtil.get(access_token);
        if(user == null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    // 更新个人信息;
    @RequestMapping(value ="update_information.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> update_Information(HttpSession session,
                                                    User user,
                                                    String access_token){
        User currentUser = (User)redisUtil.get(access_token);
//        User currentUser =(User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());

        ServiceResponse<User> response = iUserService.updateInformation(user);

        if(response.isSuccess()){
            // 在redis 更新用户信息
            // 遍历非null的属性 set入新的User里面;
//            User updateUser = response.getData();
            redisUtil.set(access_token,response.getData());
//            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }


    @RequestMapping(value ="get_information.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> get_information(String access_token){
        // 需要强制登录
//        User currentUser =(User)session.getAttribute(Const.CURRENT_USER);
        User currentUser = (User)redisUtil.get(access_token);
        if(currentUser == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
