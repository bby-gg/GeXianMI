package com.gexianmi.config;

import java.lang.annotation.*;

/**
 * 标记需要管理员权限的接口方法。
 * JwtInterceptor 检测到此注解时校验管理员角色，非管理员返回 403。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}
