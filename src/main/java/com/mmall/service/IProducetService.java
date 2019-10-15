package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;

import javax.servlet.http.HttpSession;

public interface IProducetService {
    ServiceResponse saveOrUpdateProduct(Product product);
    ServiceResponse<String> setSaleStatus(Integer productId,Integer status);
}
