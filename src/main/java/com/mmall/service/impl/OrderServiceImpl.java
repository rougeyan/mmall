package com.mmall.service.impl;

//import com.alipay.api.domain.ExtendParams;
//import com.alipay.api.domain.GoodsDetail;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;

import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

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
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    // ==================== 前台接口 ================

    //API - 创建订单(userId, 联系地址id)
    public ServiceResponse createOrder(Integer userId, Integer shippingId){
        // 从购物车获取数据
        // 从购物车中 当前 userId 所勾选的产品
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

            if(null == cartList || cartList.size() ==0){
            return ServiceResponse.createByErrorMessage("提交订单错误,请重新提交");
        }
        // 获取整个购物车详细信息
        ServiceResponse<List<OrderItem>> serviceResponse = this.getCartOrderItem(userId,cartList);
        if(!serviceResponse.isSuccess()){
            return serviceResponse;
        }

        // 强转 获取data
        List<OrderItem> orderItemList = (List<OrderItem>) serviceResponse.getData();
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        // 生成订单
        Order order = this.assembleOrder(userId,shippingId,payment);

        // 订单为空;
        if(order == null){
            return ServiceResponse.createByErrorMessage("生成订单错误");
        }

        // 购物车为空
        if(CollectionUtils.isEmpty(orderItemList)){
            return ServiceResponse.createByErrorMessage("购物车为空");
        }

        // 循环, 批量设定订单号;
        for (OrderItem orderitem : orderItemList) {
            orderitem.setOrderNo(order.getOrderNo());
        }
        // mybatis 批量插入
        orderItemMapper.batchInsert(orderItemList);

        // 生成成功 减去库存;
        this.reduceProductStock(orderItemList);

        // 清空购物车
        this.cleanCart(cartList);

        // 返回给前端数据

        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServiceResponse.createBySuccess(orderVo);
    }

    // 组合 视图View订单对象
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo()); // 订单号
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeof(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        // 获取描述
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeof(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping !=null){
            orderVo.setReceivedName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        // 组装时间
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        // 组装orderItemList
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem: orderItemList) {
            OrderItemVo orderItemVo =  assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }

        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;


    }

    // 组合 视图View 订单明细List 对象;
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    // 组装shippingVo
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        return  shippingVo;
    }

    private void cleanCart(List<Cart> cartList){
        for(Cart cart: cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }
    // 减库存
    private void reduceProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            // 减库存;
            product.setStock(product.getStock() - orderItem.getQuantity());

            productMapper.updateByPrimaryKeySelective(product);
        }
    }


    // 组装Order订单对象 存入到数据库里面;
    private Order assembleOrder(Integer userId, Integer shipping,BigDecimal payment){
        // 生成订单号
        Order order = new Order();

        long orderNo = this.generateOrderNo();

        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shipping);
        // 发货时间等等
        // 付款时间等等..


        // 插入order 判定是否插入成功
        int rowCount =orderMapper.insert(order);
        if (rowCount >0){
            return order;
        }
        return null;
    }

    // 生成订单号(考虑并发环境)
    private long generateOrderNo(){
        // 分库分表 多数据源 // 高并发的情况下如何生成订单号
        // 简单粗表 时间戳取余
        long currentTime = System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    // 计算总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem: orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    // 获取整个订单明细  List<OrderItem>;
    private ServiceResponse<List<OrderItem>> getCartOrderItem(Integer userId, List<Cart> cartList){
        List<OrderItem> orderItemList = Lists.newArrayList();
        // 若购物车数据为空 返回错误信息;
        if(CollectionUtils.isEmpty(cartList)){
            return ServiceResponse.createByErrorMessage("购物车为空");
        }
        // 校验购物车的数据,包括产品的状态和数量;
        for (Cart cartItem: cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            // 判定产品是否还在上架状态
            if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                return ServiceResponse.createByErrorMessage(product.getName()+"产品已下架");
            }
            // 校验库存
            if(cartItem.getQuantity() > product.getStock()){
                return ServiceResponse.createByErrorMessage(product.getName()+"库存不足下架");
            }

            // 组装 orderItemList;
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage()); // 主图
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return  ServiceResponse.createBySuccess(orderItemList);

    }



    //  API - 取消订单
    public ServiceResponse<String> cancel(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServiceResponse.createByErrorMessage("该用户此订单不存在");
        }
        // 判定订单状态
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()){
            return ServiceResponse.createByErrorMessage("已付款,无法取消订单");
        }

        // 修改订单状态
        Order updateOrder  = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCEL.getCode());

        int row  = orderMapper.updateByPrimaryKey(updateOrder);
        if (row>0){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }


    // API - 获取预下单订单商品详情(即 别被勾选的订单信息  (在选择收货人的页面显示)=> 此时未创建订单)
    public ServiceResponse getOrderCartProduct(Integer userId){
        OrderProductVo orderProductVo = new OrderProductVo();
        // 从购物车中获取数据;

        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        ServiceResponse serviceResponse = this.getCartOrderItem(userId,cartList);
        if(!serviceResponse.isSuccess()){
            return serviceResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serviceResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");

        for (OrderItem orderItem: orderItemList){
            payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServiceResponse.createBySuccess(orderProductVo);
    }

    // API - 获取订单详情;
    public ServiceResponse<OrderVo> getOrderDetail(Integer userId,Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId,orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServiceResponse.createBySuccess(orderVo);
        }
        return ServiceResponse.createByErrorMessage("找不到该订单");
    }

    // API - 获取订单列表
    public ServiceResponse<PageInfo> getOrderList(Integer userId,int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        // 获取订单集合
        List<Order> orderList = orderMapper.selectByUserId(userId);

        // 转化为listordervo
        List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return  ServiceResponse.createBySuccess(pageResult);
    }

    // 组合订单列表;
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId){
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order: orderList){
            List<OrderItem> orderItemList = Lists.newArrayList();
            if(userId == null){
                //todo 管理员查询不需要userId;
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else{
                orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }










    // =================== 后台接口 ================

    // 后台: API - 订单详情 (带分页)

    public ServiceResponse<PageInfo> manageList(int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        // 若无Order参数则查询所有订单
        List<Order> orderList = orderMapper.selectAllOrder();
        // 因为这个是复用的 为了重用 不传就是管理员;
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageResult);

        // 模糊匹配订单信息
    }

    // API - 订单详情
    public ServiceResponse<OrderVo> manageDetail(Long orderNo){
        Order order= orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            return ServiceResponse.createBySuccess(orderVo);
        }
        return ServiceResponse.createByErrorMessage("订单不存在");
    }

    // API - 关键字匹配(模糊匹配) 未来增加模糊匹配分页功能
    public ServiceResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        Order order= orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
            pageResult.setList(Lists.newArrayList(orderVo));
            return ServiceResponse.createBySuccess(pageResult);

        }
        return ServiceResponse.createByErrorMessage("订单不存在");
    }

    // API - 发货
    public ServiceResponse<String> manageSendGoods(Long orderNo){
        Order order= orderMapper.selectByOrderNo(orderNo);
        if(order != null){
            // 支付成功才能发货
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServiceResponse.createBySuccess("发货成功");
            }else{
                return ServiceResponse.createByErrorMessage("未知的错误,无法发货");
            }
        }
        return ServiceResponse.createByErrorMessage("订单不存在");
    }






    // ====== 联动支付宝部分 Start ======

    // 支付接口;
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


    // ====== 联动支付宝部分 End ======
}
