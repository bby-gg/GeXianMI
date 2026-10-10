package com.gexianmi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置，将 JWT 拦截器注册到 /api/** 路径。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    /**
     * 注册 JWT 拦截器，登录、注册和传感器上报接口不拦截。
     *
     * @param registry 拦截器注册中心
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                // 登录/注册无需 Token；数据上报接口由拦截器内部校验传感器 API 密钥
                .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }
}
