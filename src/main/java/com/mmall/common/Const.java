package com.mmall.common;

// 定义常量

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    // 产品列表: 动态排序 圣墟降序
    public interface ProductListOrderBy{
        Set<String> Price_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }


    // 购物车选中状态
    public interface Cart{
        int CHECKED = 1; // 即购物车选中状态
        int UNCHECKED = 0; // 购物车未选中状态;

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";

        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    // 小技巧 不作枚举;
    public interface Role{
        int Role_CUSTOMER = 0; // 普通用户
        int Role_ADMIN = 1; // 管理员
    }

    // 产品状态枚举
    public enum ProductStatusEnum{
        ON_SALE(1,"在线");

        private String value;
        private int code;

        ProductStatusEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
