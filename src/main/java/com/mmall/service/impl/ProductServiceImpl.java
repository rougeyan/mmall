package com.mmall.service.impl;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IProducetService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iProducetService")
public class ProductServiceImpl implements IProducetService {

    @Autowired
    private ProductMapper productMapper;

    public ServiceResponse saveOrUpdateProduct(Product product){
        if(product !=null){
            // 判定子图是不是空
            if(StringUtils.isNoneBlank(product.getSubImages())){
                // 把子图的第一个设置为主图
                String[] subImageArray = product.getSubImages().split(",");
                if(subImageArray.length>0){
                    product.setMainImage(subImageArray[0]);
                }
            }
            // 是更新的话
            if(product.getId() != null){
                int rowCount =  productMapper.updateByPrimaryKeySelective(product);
                if(rowCount>0){
                    return ServiceResponse.createBySuccess("更新产品成功");
                }
                return ServiceResponse.createByErrorMessage("更新产品失败");
            }else{
                int rowCount = productMapper.insert(product);
                if(rowCount>0){
                    return ServiceResponse.createBySuccess("新增产品成功");
                }
                return ServiceResponse.createByErrorMessage("新增产品失败");
            }
        }
        return ServiceResponse.createByErrorMessage("新增或者更新产品参数不正确");
    }

    /**
     * 设置产品上下架;
     * @param productId
     * @param status
     * @return
     */
    public ServiceResponse<String> setSaleStatus(Integer productId,Integer status){
        if(productId == null || status == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount>0){
            return ServiceResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServiceResponse.createByErrorMessage("修改产品销售状态失败");
    }

    public ServiceResponse<Object> manageProductDetail(Integer productId){
        if(productId == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServiceResponse.createByErrorMessage("产品已下架或者删除");
        }

        // vo 对象 value object
        // pojo -> business object ->vo view object
        return null;
    }



}
