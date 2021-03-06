package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.RedisUtil;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart/")
public class CartController {
    @Autowired
    private ICartService iCartService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     *
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<CartVo> add(String access_token){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }
    /**
     * 新增一个产品到购物车
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @PostMapping("add.do")
    @ResponseBody
    public ServiceResponse<CartVo> add(String access_token, Integer count, Integer productId){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN_SHOWLOGINDIALOG.getCode(),ResponseCode.NEED_LOGIN_SHOWLOGINDIALOG.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }


    /**
     * 更新购物车 改变购物车数量 - +
     */
    @PostMapping("update.do")
    @ResponseBody
    public ServiceResponse<CartVo> update(String access_token, Integer count, Integer productId){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }


    /**
     * 删除产品
     * 删除一个或多个产品 传Strings;
     */
    @PostMapping("delete_product.do")
    @ResponseBody
    public ServiceResponse<CartVo> deleteProduct(String access_token,String productIds){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    // 全选 全反选  单独选 单独反选

    // 全选
    @PostMapping("select_all.do")
    @ResponseBody
    public ServiceResponse<CartVo> selectAll(String access_token){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),Const.Cart.CHECKED,null);
    }
    // 全反选
    @PostMapping("unselect_all.do")
    @ResponseBody
    public ServiceResponse<CartVo> unSelectAll(String access_token){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),Const.Cart.UNCHECKED,null);
    }


    // 单选
    @PostMapping("select.do")
    @ResponseBody
    public ServiceResponse<CartVo> select(String access_token,Integer productId){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),Const.Cart.CHECKED,productId);
    }
    // 单反选
    @PostMapping("unselect.do")
    @ResponseBody
    public ServiceResponse<CartVo> unSelectAll(String access_token,Integer productId){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),Const.Cart.UNCHECKED,productId);
    }


    // 查询当前用户购物车里面的产品数量; 如果一个产品有10个 ,name这里数量就是10 (通用电商做法
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServiceResponse<Integer> getCartProductCount(String access_token){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }

}
