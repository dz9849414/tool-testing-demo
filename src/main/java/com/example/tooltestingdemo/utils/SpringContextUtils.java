package com.example.tooltestingdemo.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * 获取Spring Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 获取Spring Bean（按名称）
     */
    public static Object getBean(String name) {
        return context.getBean(name);
    }

    /**
     * 检查Bean是否存在
     */
    public static boolean containsBean(String name) {
        return context.containsBean(name);
    }
}