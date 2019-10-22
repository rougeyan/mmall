package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

import javax.servlet.http.HttpSession;

public interface IProducetService {
    ServiceResponse saveOrUpdateProduct(Product product);
    ServiceResponse<String> setSaleStatus(Integer productId,Integer status);
    ServiceResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServiceResponse getProductList(int pageNum,int pageSize);
    ServiceResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);
    ServiceResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServiceResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderby);
}
