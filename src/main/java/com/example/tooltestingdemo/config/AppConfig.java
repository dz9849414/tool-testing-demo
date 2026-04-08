package com.example.tooltestingdemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
     * 用于JSON处理，支持JDK8日期时间类型
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 JDK8 日期时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳，使用 ISO-8601 格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
