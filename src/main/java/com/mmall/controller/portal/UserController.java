package com.mmall.controller.portal;

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

/**
 * @Controller 注解定义是usercontroller
 * @RequestMapping("/user/")
 *
 * @ResponseBody() 通过springmvc 的json插件 返回值序列化成Json
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CheckLoginStatus checkLoginStatus;

    /**
     * 登陆接口
     * @param session_id
     * @param username
     * @param password
     * @param session
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> login(String session_id, String username, String password, HttpSession session, HttpServletResponse httpServletResponse, HttpServletRequest request){

        // 带session_id
        if(StringUtils.isNotBlank(session_id)){
            Object sessionValue = redisUtil.get(session_id);
            // 登陆已过期
            if(sessionValue == null){
                return ServiceResponse.createByErrorMessage("登陆过期,请重新登陆");
            }
            // 已经登录
            return ServiceResponse.createBySuccessMessage("请勿重复登陆");
        }
        // 未带session_id
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            return ServiceResponse.createByErrorMessage("账号密码填写错误");
        }
        ServiceResponse<User> response = iUserService.login(username,password);

        // 组装

        // 合成 sessionID 和session + timeStamp + endTime;
        String assembleSessionId = this.assembleSessionId(response.getData());
        // 因为我的时效性只有五分钟
        // 不能去getTime()组合字符串 因为这样回导致重复登陆的时候每次的缓存都不一样
        // 我采取的方法是
        // 添加到redis中
        redisUtil.set(assembleSessionId,response.getData(),300);
        //  添加cookies
        CookieUtils.setCookie(request,httpServletResponse,"session_id",assembleSessionId,300);

        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    // 组合assembleSessionId
    private String assembleSessionId(User user){
        String userName = user.getUsername();
        String dateTime = String.valueOf(DateTimeUtil.getCurrentHourTime().getTime());
        String assembleString = new StringBuilder().append(userName).append("%").append(dateTime).append("*").toString();
        String assembleSessionId = MD5Util.MD5EncodeUtf8(assembleString);
        return assembleSessionId;
    }

    /**
     * 登出接口
     * @param session
     * @return
     */
    @RequestMapping(value ="logout.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> logout(String session_id, HttpSession session, HttpServletRequest request, HttpServletResponse response){
        if(StringUtils.isNotBlank(session_id)){
            redisUtil.del(session_id);
        }

        CookieUtils.setCookie(request,response,"session_id","" );
        return ServiceResponse.createBySuccessMessage("登出成功");
//
//
//        session.removeAttribute(Const.CURRENT_USER);
//        return ServiceResponse.createBySuccessMessage("登出成功");
    }

    /**
     * 注册接口;
     * 如果不传user
     */
    @RequestMapping(value ="register.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     *  校验账号密码;
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
     * @param session
     * @return
     */
    @RequestMapping(value ="get_user_info.do",method = RequestMethod.GET)
    @ResponseBody()
    public ServiceResponse<User> getUserInfo(HttpSession session,String session_id){
//         这里就是通过获取session_id 来判定是否已登陆;
//        User redisuser = (User)checkLoginStatus.check_token_valid(session_id);
//
//        if(redisuser != null){
//            return ServiceResponse.createBySuccess(redisuser);
//        }

        User user = (User) session.getAttribute(Const.CURRENT_USER);
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
    public ServiceResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        // 先获取session
        User user =(User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    // 更新个人信息;
    @RequestMapping(value ="update_information.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> update_Information(HttpSession session,User user){
        User currentUser =(User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());

        ServiceResponse<User> response = iUserService.updateInformation(user);

        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }
    @RequestMapping(value ="get_information.do",method = RequestMethod.POST)
    @ResponseBody()
    public ServiceResponse<User> get_information(HttpSession session){
        // 需要强制登录
        User currentUser =(User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
