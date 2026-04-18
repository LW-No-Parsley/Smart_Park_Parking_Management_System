package com.syan.smart_park.common;

import com.syan.smart_park.common.exception.ReturnCode;
import lombok.Data;

@Data
public class R<T> {

    private Integer code;        // 状态码
    private Boolean status;      // 请求状态：true-成功, false-失败
    private String message;      // 提示信息
    private T data;              // 数据
    private long timestamp;      // 接口请求时间

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    // 基础成功方法
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(ReturnCode.RC200.getCode());
        r.setStatus(true);
        r.setMessage(ReturnCode.RC200.getMessage());
        r.setData(data);
        return r;
    }

    // 成功，无数据
    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.setCode(ReturnCode.RC200.getCode());
        r.setStatus(true);
        r.setMessage(ReturnCode.RC200.getMessage());
        r.setData((T) "");
        return r;
    }

    // 成功，自定义消息
    public static <T> R<T> success(String message) {
        R<T> r = new R<>();
        r.setCode(ReturnCode.RC200.getCode());
        r.setStatus(true);
        r.setMessage(message);
        r.setData((T) "");
        return r;
    }

    // 基础错误方法
    public static <T> R<T> error(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setStatus(false);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    // 使用ReturnCode枚举
    public static <T> R<T> error(ReturnCode returnCode) {
        return error(returnCode.getCode(), returnCode.getMessage());
    }

    // 使用ReturnCode枚举，自定义消息
    public static <T> R<T> error(ReturnCode returnCode, String customMessage) {
        return error(returnCode.getCode(), customMessage);
    }

    // 快捷错误方法
    public static <T> R<T> badRequest() {
        return error(ReturnCode.RC400);
    }

    public static <T> R<T> badRequest(String message) {
        return error(ReturnCode.RC400, message);
    }

    public static <T> R<T> unauthorized() {
        return error(ReturnCode.RC401);
    }

    public static <T> R<T> forbidden() {
        return error(ReturnCode.RC403);
    }

    public static <T> R<T> notFound() {
        return error(ReturnCode.RC404);
    }

    public static <T> R<T> serverError() {
        return error(ReturnCode.RC500);
    }

    public static <T> R<T> serverError(String message) {
        return error(ReturnCode.RC500, message);
    }

    // 业务相关快捷方法
    public static <T> R<T> userNotFound() {
        return error(ReturnCode.RC600);
    }

    public static <T> R<T> parkingLotFull() {
        return error(ReturnCode.RC1001);
    }

    public static <T> R<T> paymentFailed() {
        return error(ReturnCode.RC1009);
    }
}