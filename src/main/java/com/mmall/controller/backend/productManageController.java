package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProducetService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product")
public class productManageController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProducetService iProducetService;

    /**
     * 新增/更新商品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServiceResponse productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 增加产品的业务逻辑
            return iProducetService.saveOrUpdateProduct(product);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    /**
     * 设置产品上下架;
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServiceResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){

            return iProducetService.setSaleStatus(productId,status);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse getDetail(HttpSession session, Integer productId,Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录");
        }
        // 校验一下是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            // 填充业务;
            // properties读取
            // vo的建立;
            return null;
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作,需要管理员权限");
        }
    }
}
