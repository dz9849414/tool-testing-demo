package com.example.tooltestingdemo.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.tooltestingdemo.security.CustomUserDetails;
import com.example.tooltestingdemo.service.SecurityService;

import java.util.List;

/**
 * 安全工具类（静态调用，绝对安全，不影响任何原有功能）
 */
public final class SecurityUtils {

    // 1. 私有化构造，禁止实例化
    private SecurityUtils() {}

    // ====================== 核心获取用户 ======================

    /**
     * 获取当前登录用户详情
     */
    public static CustomUserDetails getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        return (principal instanceof CustomUserDetails) ? (CustomUserDetails) principal : null;
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getUserId() {
        CustomUserDetails user = getUser();
        return user == null ? null : user.getUserId();
    }

    /**
     * 获取当前登录用户名
     */
    public static String getUsername() {
        CustomUserDetails user = getUser();
        return user == null ? null : user.getUsername();
    }

    /**
     * 获取当前登录用户角色列表
     */
    public static List<String> getRoles() {
        CustomUserDetails user = getUser();
        return user == null ? null : user.getRoles();
    }

    // ====================== 登录状态判断 ======================

    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn() {
        return getUserId() != null;
    }

    /**
     * 检查是否为当前用户
     */
    public static boolean isCurrentUser(String userId) {
        Long currentUserId = getUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * 检查是否为管理员
     */
    public static boolean isAdmin() {
        Long userId = getUserId();
        return userId != null && "admin".equals(userId);
    }

    // ====================== 权限判断（从 Spring Bean 中获取） ======================

    /**
     * 检查是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        if (!isLoggedIn()) return false;
        SecurityService securityService = SpringContextUtils.getBean(SecurityService.class);
        return securityService != null && securityService.hasPermission(permission);
    }

    /**
     * 检查是否有任意一个权限
     */
    public static boolean hasAnyPermission(String... permissions) {
        if (!isLoggedIn()) return false;
        SecurityService securityService = SpringContextUtils.getBean(SecurityService.class);
        return securityService != null && securityService.hasAnyPermission(permissions);
    }

    /**
     * 检查是否有所有权限
     */
    public static boolean hasAllPermissions(String... permissions) {
        if (!isLoggedIn()) return false;
        SecurityService securityService = SpringContextUtils.getBean(SecurityService.class);
        return securityService != null && securityService.hasAllPermissions(permissions);
    }

    /**
     * 检查是否有指定角色
     */
    public static boolean hasRole(String role) {
        if (!isLoggedIn()) return false;
        SecurityService securityService = SpringContextUtils.getBean(SecurityService.class);
        return securityService != null && securityService.hasRole(role);
    }

    // ====================== 便捷方法 ======================

    /**
     * 获取当前用户ID，如果未登录返回默认值
     */
    public static Long getUserIdOrDefault(Long defaultValue) {
        Long userId = getUserId();
        return userId != null ? userId : defaultValue;
    }


    /**
     * 获取当前用户名，如果未登录返回默认值
     */
    public static String getUsernameOrDefault(String defaultValue) {
        String username = getUsername();
        return username != null ? username : defaultValue;
    }
}