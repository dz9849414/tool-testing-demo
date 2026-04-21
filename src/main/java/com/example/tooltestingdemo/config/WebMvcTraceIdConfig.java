package com.example.tooltestingdemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 自动注入链路追踪ID
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({TraceIdProperties.class, MethodTraceProperties.class})
public class WebMvcTraceIdConfig implements WebMvcConfigurer {

    private final TraceIdInterceptor traceIdInterceptor;
    private final TraceIdProperties traceIdProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (traceIdProperties.isEnabled()) {
            registry.addInterceptor(traceIdInterceptor).addPathPatterns("/**");
        }
    }
}
