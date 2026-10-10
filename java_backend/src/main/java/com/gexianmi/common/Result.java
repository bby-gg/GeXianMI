package com.gexianmi.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式，所有接口返回 {code, data, msg} 结构。
 * <p>
 * 前端通过 msg 字段（由 message 字段 JSON 序列化而来）读取提示信息。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码，200 表示成功 */
    private int code;

    /** 响应数据 */
    private T data;

    /** 提示信息，JSON 序列化后字段名为 msg */
    @JsonProperty("msg")
    private String message;

    /**
     * 构建成功响应，包含数据及默认提示信息。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, data, "success");
    }

    /**
     * 构建成功响应，包含数据和自定义提示信息。
     *
     * @param data    响应数据
     * @param message 自定义提示信息
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(200, data, message);
    }

    /**
     * 构建成功响应，仅含提示信息、不含数据。
     *
     * @param message 自定义提示信息
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> Result<T> ok(String message) {
        return new Result<>(200, null, message);
    }

    /**
     * 构建失败响应，使用指定状态码和错误信息。
     *
     * @param code    业务状态码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }

    /**
     * 构建失败响应，使用默认 500 状态码。
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, null, message);
    }

    /**
     * 构建参数校验失败响应，固定返回 400 状态码。
     *
     * @param message 校验错误信息
     * @param <T>     数据类型
     * @return 参数校验失败响应
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, null, message);
    }
}
