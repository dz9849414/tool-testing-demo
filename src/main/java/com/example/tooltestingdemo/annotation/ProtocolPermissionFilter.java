package com.example.tooltestingdemo.annotation;

import java.lang.annotation.*;

/**
 * 协议权限过滤注解
 * 用于标记需要根据协议权限过滤结果集的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolPermissionFilter {
    
    /**
     * 协议ID字段名（默认为"id"）
     */
    String protocolIdField() default "id";
    
    /**
     * 是否启用权限过滤（默认为true）
     */
    boolean enabled() default true;
}