package com.mmall.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.Test;

import java.io.Serializable;

/**
 * 统一定义ResponseCode类
 * 返回各 Response
 */
//@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

// 保证 序列化json 的时候 如果是null对象 key也消失;
public class ServiceResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    /**
     * 使用 private 私有构造器
     */
    private ServiceResponse(int status) {
        this.status = status;
    }

    private ServiceResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServiceResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServiceResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

//    public static void main(String[] args) {
//        ServiceResponse sr1 = new ServiceResponse(1,new Object());
//        ServiceResponse sr2 = new ServiceResponse(1,"abc");
//        System.out.println("console");
//    }
    @JsonIgnore
    // 使之不在json序列化当中
    public boolean isSuccess(){
        return this.status ==  ResponseCode.SUCCESS.getCode();
    }

    // 注意缺失了任意一个get[属性的方法]都会导致 最后出来的序列化数据拿不到;
    public T getData(){
        return data;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    // ============  成功  ============

    // 返回status
    public static <T> ServiceResponse<T> createBySuccess(){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    // 返回msg
    public static <T> ServiceResponse<T> createBySuccessMessage(String msg){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    // 返回data
    public static <T> ServiceResponse<T> createBySuccess(T data){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }

    // 返回data 带msg,跟data;
    public static <T> ServiceResponse<T> createBySuccess(String msg,T data){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    // ============  失败  ============
    public static <T> ServiceResponse<T> createByError(){
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <T> ServiceResponse<T> createByErrorMessage(String errorMessage){
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    // ============  需要Login  ============

    public static <T> ServiceResponse<T> createByErrorCodeMessage(int errorcode, String errorMessage){
        return new ServiceResponse<T>(errorcode,errorMessage);
    }
}
