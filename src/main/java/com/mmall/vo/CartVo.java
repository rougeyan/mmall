package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {
    List<CartProductVo> cartProductVoList; // 购车列表
    private BigDecimal cartTotalPrice; // 购物车所有产品总价
    private Boolean allChecked; // 是否产品均勾选
    private String ImgesHost;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImgesHost() {
        return ImgesHost;
    }

    public void setImgesHost(String imgesHost) {
        ImgesHost = imgesHost;
    }
}
