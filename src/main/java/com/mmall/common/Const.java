package com.mmall.common;

// 定义常量

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    // 产品列表: 动态排序 升序降序
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
        ON_SALE(1,"上架"),
        OUT_SALE(0,"下架");

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

    // 订单状态的枚举
    public enum OrderStatusEnum{
        CANCEL(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已支付"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭")
        ;
        OrderStatusEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        // 静态方法:
        public static OrderStatusEnum codeof(int code){
            for (OrderStatusEnum orderStatusEnum:values()) {
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应枚举code");

        }
    }

    // 常量类
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY ="WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS ="TRADE_SUCCESS";

        String RESPONSED_SUCCESS ="success";
        String RESPONSED_FAIL ="fail";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝"),
        ;
        PayPlatformEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付")
        ;
        PaymentTypeEnum(int code,String value) {
            this.value = value;
            this.code = code;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeof(int code){
            for (PaymentTypeEnum paymentTypeEnum:values()) {
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应枚举code");

        }
    }
}
