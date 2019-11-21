package com.mmall.service.impl;

//import com.alipay.api.domain.ExtendParams;
//import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePayRequestBuilder;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;

import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.OrderItemMapper;
import com.mmall.dao.OrderMapper;
import com.mmall.dao.PayInfoMapper;
import com.mmall.pojo.Order;
import com.mmall.pojo.OrderItem;
import com.mmall.pojo.PayInfo;
import com.mmall.service.ICategoryService;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;

    public ServiceResponse pay(Long orderNo ,Integer userId, String path){
        Map<String,String> resultMap = Maps.newHashMap();
        // 校验 根于userId 和orderNo 到底存不存在
        Order order =  orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServiceResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));

        // 组装支付宝订单信息:


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymall扫码支付,订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder()
                .append("订单")
                .append(outTradeNo)
                .append("购买商品共")
                .append(totalAmount)
                .append("元")
                .toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "60m";

        // 构造商品列表:
        // 利用foreach循环

        // 电商流程现讲支付

        // 集合, 获取订单明细:
        // 订单在下一章节,保证流程 先讲支付 再讲订单, 否则订单状态无法进行:
        // 根据orderNumber 和userid 拿到订单集合 遍历item集合, 把goodsDetail填充;

        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);

        // 遍历
        // 组装goodDetailsList
        for (OrderItem orderItem :orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            // 创建好一个商品后添加至商品明细列表
            GoodsDetail goods = GoodsDetail.newInstance(
                    orderItem.getProductId().toString(), // 商品id
                    orderItem.getProductName(),  // 名称
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(), // 价格
                    orderItem.getQuantity()); // 数量

            // 商品明细列表，需填写购买商品详细信息，
            goodsDetailList.add(goods);
        }


        // 用配置的方式读取回调地址;
        // PropertiesUtil.getProperty("alipay.callback.url")

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        //  原demo tradeService 是一个静态service , 是通过static静态块


        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        // 匹配返回结果
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                // 打印响应:
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 生成二维码
                // 上传到服务器上面
                // 组装出url 返回给前端
                File folder  = new File(path);
                // 判断是否存在
                if(!folder.exists()){
                    folder.setWritable(true);
                    // 创建目录
                    folder.mkdirs();
                }
                // 需要修改为运行机器上的路径
                // 细节: 需要加 /
                // 流程:根据 根据外部订单号 -> 生成路径 ->路径生成二维码 ->先创建新文件名 -> 路径和新文件名传到FTP上
                // 然后组装url
                String qrPath = String.format(path+"/qr-%s.png", response.getOutTradeNo());

                String qfFileName =String.format("qr-%s.png",response.getOutTradeNo());
                // 支付宝封装
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path,qfFileName);
                try{
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                }catch (IOException e){
                    logger.error("上传二维码异常",e);
                }
                logger.info("qrPath :" + qrPath);

                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();

                resultMap.put("qrUrl",qrUrl);

                return ServiceResponse.createBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServiceResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServiceResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServiceResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    // 阿里回调检测
    public ServiceResponse aliCallback(Map<String,String> params){
        // 外部订单号
        Long orderNo =  Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus =params.get("trade_status");
        //
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServiceResponse.createByErrorMessage("非商城订单,忽略回调");
        }
        // 当这个订单>=20 就不应该再处理
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServiceResponse.createBySuccess("支付宝重复调用");
        }
        // 交易成功
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment"))); // 更新时间
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 组装payInfo对象
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServiceResponse.createBySuccess();

    }
    // 轮询指引
    public ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo){

        Order order =  orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServiceResponse.createByErrorMessage("没有该订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }
}
