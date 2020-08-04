package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private RedisUtil redisUtil;

    // 创建订单
    //  在order里面都有一个pay接口
    @RequestMapping("create.do")
    @ResponseBody
    public ServiceResponse create(String access_token, Integer shippingId){
        User user = (User)redisUtil.get(access_token);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    // 取消订单
    //  在order里面都有一个pay接口
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServiceResponse cancel(String access_token, Long orderNo){
        User user = (User)redisUtil.get(access_token);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    // 预下单, 未创建订单 但是从 购物车进入  购物车 ==>预下单(选择联系人) ==> 提交订单
    // 获取购物车中的产品 客户在购物车预览的时候 看到购物车中的明细
    // 获取已经选中的购物车产品列表;
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServiceResponse getOrderCartProduct(String access_token){
        User user = (User)redisUtil.get(access_token);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }


    @RequestMapping("detail.do")
    @ResponseBody
    public ServiceResponse detail(String access_token, Long orderNo){
        User user = (User)redisUtil.get(access_token);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse list(String access_token,
                                @RequestParam(value="pageNum",defaultValue = "1") int pageNum,
                                @RequestParam(value="pageSize",defaultValue = "10") int pageSize){
        User user = (User)redisUtil.get(access_token);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }


















    //  在order里面都有一个pay接口
    @RequestMapping("pay.do")
    @ResponseBody
    public ServiceResponse pay(String access_token, Long orderNo , HttpServletRequest requset){
        // requset 获取servlet 的上下文
        // 拿到upload的文件夹
        // 拿到自动生产的二维码
        // 传到ftp服务器上
        // 返回给前端 二维码的图片地址
        // 前端吧图片地址给展示
        // 前端 => 扫码支付;
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        //
        String path = requset.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(orderNo,user.getId(),path);
    }

    // 回调方法:
    // 依据alipay 定义的接口返回Object
    // 支付宝的回调
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        // 用自己的Map 去接受一下
        Map<String,String> params = Maps.newHashMap();

        // 获取 支付宝回调;
        Map requestParams = request.getParameterMap();
        // 遍历一下
        for (Iterator iter = requestParams.keySet().iterator();iter.hasNext();) {
            String name =(String) iter.next();
            String[] values = (String[]) requestParams.get(name);

            String valueStr = "";
            for(int i = 0; i<values.length; i++){
                // 逗号分隔;
                valueStr = (i ==values.length - 1)?valueStr +values[i]:valueStr +values[i]+",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",
                params.get("sign"),
                params.get("trade_status"),
                params.toString());

        params.remove("sign_type");
        try{
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            // todoSomething 验证各种数据 if true 业务逻辑
            if(!alipayRSACheckedV2){
                return  ServiceResponse.createByErrorMessage("非法请求,验证不通过");
            }
        }catch(AlipayApiException e){
            logger.error("支付宝回调异常",e);
        }
        // 处理alipay回调地址的 正常业务逻辑
        // 更新订单状态 判定订单是否已经支付过了 .. 逻辑
        ServiceResponse serviceResponse = iOrderService.aliCallback(params);
        // 判定订单状态
        if(serviceResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSED_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSED_FAIL;
    }


    // 前台轮训查询订单状态
    // 至付款好了之后 在二维码付款的页面 扫码支付付款之后
    // 前台调用该接口 看是否付款成功
    // 如果 付款成功会跳指引 调到订单

    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServiceResponse<Boolean> pay(String access_token, Long orderNo){
        User user = (User)redisUtil.get(access_token);
        // 空判断 强制登录
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        // 把true /false 返回
        ServiceResponse serviceResponse =  iOrderService.queryOrderPayStatus(user.getId(),orderNo);

        if(serviceResponse.isSuccess()){
            return ServiceResponse.createBySuccess(true);
        }
        return ServiceResponse.createBySuccess(false);
    }
}
