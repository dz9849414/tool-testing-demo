package com.example.tooltestingdemo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionCheck {
    /**
     * 权限类型：update, delete, approve, assignRoles
     */
    String type();
    
    /**
     * 目标用户ID参数名称
     */
    String targetUserIdParam() default "id";
    
    /**
     * 角色ID列表参数名称（仅用于assignRoles类型）
     */
    String roleIdsParam() default "roleIds";
}