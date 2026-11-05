package com.medical.medical_system.common;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一响应结构
 * <p>
 * 让前端/Postman 拿到的返回体结构恒定：code 判断成败，message 说明原因，data 承载业务数据。
 * 避免"成功返回对象、失败返回字符串"这种前端难处理的形式。
 */
@Data
public class Result<T> {

    /** 业务状态码：200 成功，400 参数/业务校验失败，404 资源不存在，409 唯一约束冲突，500 服务端异常 */
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null);
    }
}
