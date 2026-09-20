package com.example.sentiment.config;

import com.example.sentiment.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理：参数校验异常 + 业务异常 + HTTP 异常统一返回 Result
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** @RequestBody 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String msg = error == null ? "参数校验失败" : error.getDefaultMessage();
        return Result.fail(400, msg);
    }

    /** 表单绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String msg = error == null ? "参数校验失败" : error.getDefaultMessage();
        return Result.fail(400, msg);
    }

    /** 方法参数校验失败（@Validated） */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数校验失败");
        return Result.fail(400, msg);
    }

    /** 请求体 JSON 无法解析（如格式错误、缺字段） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.fail(400, "请求体格式错误，请检查 JSON 与 Content-Type: application/json");
    }

    /** 请求方法不支持（如用 GET 调 POST 接口） */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(405, "请求方法不支持，请使用 " + (e.getSupportedHttpMethods() == null ? "正确方法" : e.getSupportedHttpMethods()));
    }

    /** Content-Type 不支持 */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return Result.fail(415, "不支持的 Content-Type，请使用 application/json");
    }

    /** 路径不存在 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandler(NoHandlerFoundException e) {
        return Result.fail(404, "接口不存在: " + e.getRequestURL());
    }

    /** 业务异常（如"用户名已存在""用户名或密码错误"） */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleBusiness(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /** 兜底异常：开发阶段返回真实错误信息便于排查，上线前可改回通用提示 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        String detail = e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getClass().getSimpleName() + ": " + e.getMessage();
        return Result.error("系统异常 -> " + detail);
    }
}
