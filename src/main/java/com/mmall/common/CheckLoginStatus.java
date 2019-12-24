package com.mmall.common;

import com.mmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;


// 检测是否登陆台 取代原来的session 检测;
public class CheckLoginStatus {

    private RedisUtil redisUtil;

    public CheckLoginStatus(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 判定请求中query url中的access_token是否有效 => 读redis,
     *
     * 函数1: 检查token check(Http
     *
     * 判定redis中是否存有登录态;
     * 带上access_token;
     * 若有 则直接返回 response登陆成功
     * 若无 则调用userService => 加盐 生成map k value
     * 存入Redis里面
     * 设置response.addCookise
     * new cookies(access_token,value)
     * 以及其他 有效时间 domain ....
     */
    // 检测登陆状态
    public Object check_token_valid(String sessionId){
        //  若没有sessionId 为空的情况下
        if (StringUtils.isBlank(sessionId)){
            return null;
        }
        // 有session的情况下
        Object object = redisUtil.get(sessionId);
        if(object != null){
            return object;
        }
        return null;
    }
}
