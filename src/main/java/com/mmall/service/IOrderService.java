package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface IOrderService {
    ServiceResponse pay(Long orderNo , Integer userId, String path);

    ServiceResponse aliCallback(Map<String,String> params);

    ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo);

    ServiceResponse createOrder(Integer userId, Integer shippingId);

    ServiceResponse<String> cancel(Integer userId,Long orderNo);

    ServiceResponse getOrderCartProduct(Integer userId);

    ServiceResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);

    ServiceResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);

    // backend;
    ServiceResponse<PageInfo> manageList(int pageNum,int pageSize);

    ServiceResponse<OrderVo> manageDetail(Long orderNo);

    ServiceResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);

    ServiceResponse<String> manageSendGoods(Long orderNo);

    ServiceResponse<PageInfo> manageQueryList(int pageNum, int pageSize, Long orderNo,String status, String paymentType);
}
