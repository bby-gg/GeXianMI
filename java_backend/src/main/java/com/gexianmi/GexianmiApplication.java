package com.gexianmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 葛仙米生长环境智能监测系统启动类。
 *
 * @author GXM-Team
 * @since 2026-07-11
 */
@SpringBootApplication
public class GexianmiApplication {

    /**
     * 应用程序入口，启动 Spring Boot 服务。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GexianmiApplication.class, args);
    }
}
