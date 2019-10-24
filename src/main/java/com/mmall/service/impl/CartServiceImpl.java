package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     *
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServiceResponse<CartVo> add(Integer userId, Integer productId, Integer count){
        // 参数校验
        if(productId == null || count ==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }

        // 正常逻辑
        Cart cart = cartMapper.selectCardByUserIdProductId(userId,productId);
        if(cart == null){
            // 若购物车表搜不出这个产品 需要新增一个这样的产品
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);

            cartMapper.insert(cartItem);
        }else{
            // 产品已经在购物车里 数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);

            cartMapper.updateByPrimaryKeySelective(cart);
        }

        // 购物车需要与库存联动 例如进行数量的控制 等等;
        // 超过库存提示啊什么的;
        // 并且购物车里面也需要有Value Object

        return this.list(userId);
    }

    /**
     * 这是一个通用方法(贯穿整个购物车) :
     *
     *  // 目的是获取当前用户 即时的购物车产品列表;
     *
     * // 获取当前用户的购物车列表 是一个 由 CartProductVo 和其他属性组成的 CartVo对象;
     * // 虽然我们修改的其中一项/ 但是 我们返回的却是整个购物车列表;
     *
     * // 表之前的内部通讯 有可能这个购物车是大于库存的(即其他用户买的,此客户还没有进入购物车查看购物车保留的还是之前的数据);
     *
     * @param userId
     * @return CartVo 是该user的 购物车列表;
     */
    private CartVo getCarVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        // 查询一个user的购车列表; 是cart => pojo的
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        List<CartProductVo>  cartProductVoList = Lists.newArrayList();

        // 数字在计算的时候避免精度丢失
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList)){
            // 如果不是空的情况 遍历它

            for(Cart cartItem:cartList){
                // 给CartProductVo 赋值;
                CartProductVo cartProductVo = new CartProductVo();

                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                // 搜一下该具体一项产品的信息
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                // 继续 组装cartProductVo;
                if(product !=null){
                    cartProductVo.setProductMainImg(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubTitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    // 判定库存
                    int buyLimitCount = 0;
                    // 库存 管理
                    // 如果产品的库存大于 具体产品的数量
                    if(product.getStock() >= cartItem.getQuantity()){
                        // 库存充足的情况下

                        buyLimitCount = cartItem.getQuantity();

                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        // 超出库存
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        // 购物车更新有效库存
                        Cart cartForQuantity =new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        // 更新购物车表中有效的 产品数量;
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    // 重新设置数量
                    cartProductVo.setQuantity(buyLimitCount);
                    // 当前产品计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity().doubleValue()));

                    // 勾选
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                // 当项价格 加入到 购物车总价中;
                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    // 如果已经勾选, 增加到整个购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }

        }

        // 组装cartVo
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        // 判定是否全部勾选
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImgesHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }


    /**
     * 判定是否全部已选购物车
     * @param userId
     * @return
     */
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }

        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

    // 更新购物车
    public ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count){
        // 参数校验
        if(productId == null || count ==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }

        // 正常逻辑
        Cart cart = cartMapper.selectCardByUserIdProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
        }
        // 更新到购物车列表中
        cartMapper.updateByPrimaryKeySelective(cart);

        return this.list(userId);

    }
    public ServiceResponse<CartVo> deleteProduct(Integer userId,String productIds){
        // 参数校验
        if(productIds == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }
        /**
         * 使用guava 的快捷方法
         * 笨方法: ids 转数组  => 遍历数据 添加到集合当中
         */
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUEMENT.getCode(),ResponseCode.ILLEGAL_ARGUEMENT.getDesc());
        }
        // 删除
        cartMapper.deleteByUserIdProductIds(userId,productList);

        return this.list(userId);

    }


    /**
     * 查看购物车
     */
    public ServiceResponse<CartVo> list(Integer userId){
        CartVo cartVo = this.getCarVoLimit(userId);

        return ServiceResponse.createBySuccess(cartVo);
    }


    /**
     * 全选 全部反选
     * @param userId
     * @param checked
     * @return
     */
    public ServiceResponse<CartVo> selectOrUnSelect(Integer userId,Integer checked, Integer productId){
        cartMapper.checkedOrUncheckedProduct(userId,checked,productId);
        return this.list(userId);
    }


    public ServiceResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServiceResponse.createBySuccess(0);
        }
        return ServiceResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }
}
