package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;

import javax.servlet.http.HttpSession;

public interface IShippingService {
    ServiceResponse add(Integer userId, Shipping shipping);

    ServiceResponse del(Integer userId, Integer shippingId);

    ServiceResponse update(Integer userId, Shipping shipping);

    ServiceResponse<Shipping> select(Integer userId, Integer shippingId);

    ServiceResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}