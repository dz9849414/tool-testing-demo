package com.example.tooltestingdemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用配置类
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/config/AppConfig.java
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate Bean
     * 用于发送HTTP请求
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper Bean
     * 用于JSON处理
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
