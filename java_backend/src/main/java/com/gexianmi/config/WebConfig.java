package com.gexianmi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域 CORS 配置，允许前端跨域访问后端接口。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置 CORS 跨域映射规则，所有 /api/ 开头的接口允许跨域访问。
     *
     * @param registry CORS 注册中心
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 开发阶段允许所有来源，生产环境需收紧为具体域名
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                // 预检请求缓存 1 小时，减少重复 OPTIONS 请求
                .maxAge(3600);
    }
}
