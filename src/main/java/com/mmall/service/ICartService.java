package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.vo.CartVo;

public interface ICartService {
    ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    ServiceResponse<CartVo> deleteProduct(Integer userId,String productIds);

    ServiceResponse<CartVo> list(Integer userId);

    ServiceResponse<CartVo> selectOrUnSelect(Integer userId,Integer checked, Integer productId);

    ServiceResponse<Integer> getCartProductCount(Integer userId);
}
