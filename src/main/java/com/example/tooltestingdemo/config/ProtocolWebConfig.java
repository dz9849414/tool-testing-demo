//package com.example.tooltestingdemo.config;
//
//import com.example.tooltestingdemo.interceptor.ProtocolPermInterceptor;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * 协议权限Web配置
// */
//@Configuration
//@RequiredArgsConstructor
//public class ProtocolWebConfig implements WebMvcConfigurer {
//
//    private final ProtocolPermInterceptor protocolPermInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//
//        registry.addInterceptor(protocolPermInterceptor)
//                .addPathPatterns("/api/protocol/**") // 只拦截所有协议测试接口
//                .excludePathPatterns("/api/login");
//
//    }
//}