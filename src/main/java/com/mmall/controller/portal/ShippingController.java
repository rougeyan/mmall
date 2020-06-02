package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import com.mmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 *
 */
@Controller()
@RequestMapping("/shipping/")
public class ShippingController {
    @Autowired
    private IShippingService iShippingService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * srpingmvc的对象绑定
     * 不用一个一个数据绑定;
     * 需要在mapper里面配合使用11
     * @param session
     * @param shipping
     * @return
     */
    @PostMapping("add.do")
    @ResponseBody
    public ServiceResponse add(String access_token, Shipping shipping){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(),shipping);
    }

    @PostMapping("del.do")
    @ResponseBody
    public ServiceResponse del(String access_token, Integer shippingId){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(),shippingId);
    }

    @PostMapping("update.do")
    @ResponseBody
    public ServiceResponse update(String access_token, Shipping shipping){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }


    @PostMapping("select.do")
    @ResponseBody
    public ServiceResponse select(String access_token, Integer shippingId){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(user.getId(),shippingId);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> list(String access_token,
                                          @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId(),pageNum,pageSize);
    }
}
