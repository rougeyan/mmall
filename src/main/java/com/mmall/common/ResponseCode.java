package com.mmall.common;

/**
 * 定义 返回的Code 枚举
 */
public enum ResponseCode {
    /**
     * 枚举构造器
     *
     * 统一格式：A-BB-CCC，6位长度整形int。
     *
     * A：代表错误级别，1表示系统级错误，2表示服务级错误。
     *
     * BB：代表错误项目或者模块号，从00开始。
     *
     * CCC：具体错误编号，自增，从001开始。
     *
     */
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"用户未登录,"),
    NEED_LOGIN_SHOWLOGINDIALOG(100005,"用户未登录,打开登录弹窗"),
    NEED_LOGIN_REDIRECT(100006,"用户未登录,重定向login"),
    // 非法参数
    ILLEGAL_ARGUEMENT(2,"ILLEGAL_ARGUEMENT"),
    ILLEGAL_ARGUEMENT_TOKEN(100003,"非法token参数"),
    ILLEGAL_ARGUEMENT_PARAMS(100004,"传参错误"),

    // 全局登录拦截
    GLOBAL_INTERRUPT_LOGIN(100001,"统一拦截,用户未登录"),
    GLOBAL_INTERRUPT_ADMIN(100002,"统一拦截,没有管理员权限");



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
