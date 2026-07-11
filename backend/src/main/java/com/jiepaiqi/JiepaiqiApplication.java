package com.jiepaiqi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 节拍器后端应用入口。
 * 负责启动 Spring Boot 模块化单体应用。
 */
@SpringBootApplication
public class JiepaiqiApplication {
    public static void main(String[] args) {
        SpringApplication.run(JiepaiqiApplication.class, args);
    }
}