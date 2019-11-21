package com.mmall.service;

import com.mmall.common.ServiceResponse;

import java.util.Map;

public interface IOrderService {
    ServiceResponse pay(Long orderNo , Integer userId, String path);

    ServiceResponse aliCallback(Map<String,String> params);

    ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
