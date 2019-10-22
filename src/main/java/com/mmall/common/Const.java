package com.mmall.common;

// 定义常量

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy{
        Set<String> Price_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
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
