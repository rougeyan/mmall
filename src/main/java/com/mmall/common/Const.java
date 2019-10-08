package com.mmall.common;

// 定义常量

public class Const {
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";


    // 小技巧 不作枚举;
    public interface Role{
        int Role_CUSTOMER = 0; // 普通用户
        int Role_ADMIN = 1; // 管理员
    }
}
