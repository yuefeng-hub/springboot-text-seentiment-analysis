package com.example.sentiment.common;

import lombok.Data;

/**
 * 统一响应结果封装
 * code: 200 成功 / 400 业务错误 / 401 未登录 / 500 服务器错误
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> error(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return fail(401, message);
    }
}
