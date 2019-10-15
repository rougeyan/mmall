package com.mmall.common;

/**
 * 定义 返回的Code 枚举
 */
public enum ResponseCode {
    /**
     * 枚举构造器
     */
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUEMENT(2,"ILLEGAL_ARGUEMENT");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
