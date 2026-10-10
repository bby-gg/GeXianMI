package com.gexianmi.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，统一将各类异常转换为 {code, msg, data} 格式的响应。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败的异常，返回 400 状态码及字段校验错误信息。
     *
     * @param e 方法参数校验异常
     * @return 包含错误信息的统一响应体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    /**
     * 处理业务运行时异常，返回 400 状态码及异常消息。
     *
     * @param e 运行时异常
     * @return 包含错误信息的统一响应体
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleRuntime(RuntimeException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 兜底异常处理，捕获所有未预期的异常，返回 500 状态码。
     *
     * @param e 未预期的异常
     * @return 包含服务器内部错误信息的统一响应体
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}
