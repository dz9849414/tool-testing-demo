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
    
    /**
     * 权限编码
     */
    String perm() default "";
    
    /**
     * 是否满足其一即可通过
     */
    boolean or() default true;
    
    /**
     * 是否允许当前用户查看自己的信息
     */
    boolean allowCurrentUser() default false;
}