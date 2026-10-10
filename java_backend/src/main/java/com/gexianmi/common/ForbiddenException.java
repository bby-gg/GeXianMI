package com.gexianmi.common;

/**
 * 越权/无权限访问异常。
 * <p>
 * 由 {@link GlobalExceptionHandler} 统一映射为 403 响应，
 * 与「参数错误 400」「未登录 401」「服务器错误 500」区分开。
 * </p>
 *
 * @author GXM-Team
 * @since 2026-10-10
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
