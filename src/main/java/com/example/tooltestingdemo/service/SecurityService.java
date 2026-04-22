package com.example.tooltestingdemo.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.tooltestingdemo.security.CustomUserDetails;

import java.util.Collection;

/**
 * 安全服务类
 */
@Service
public class SecurityService {
    
    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        return null;
    }
    
    /**
     * 检查是否为当前用户
     */
    public boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }
    
    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
    
    /**
     * 检查用户是否已登录
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * 检查当前用户是否是admin
     */
    public boolean isAdmin() {
        Long currentUserId = getCurrentUserId();
        return 1L == currentUserId;
    }
    
    /**
     * 检查用户是否拥有指定权限或其父级权限
     */
    public boolean hasPermission(String permission) {
        // 如果是admin用户，直接返回true
        if (isAdmin()) {
            return true;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            // 精确匹配
            if (auth.equals(permission)) {
                return true;
            }
            // 父级权限匹配（例如：system:user 包含 system:user:api）
            if (permission.startsWith(auth + ":")) {
                return true;
            }
        }
        return false;
    }

    // ====================== 权限检查方法 ======================

    /**
     * 检查是否有任意一个权限
     */
    public boolean hasAnyPermission(String... permissions) {
        if (!isAuthenticated()) return false;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        
        for (String permission : permissions) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(permission))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        if (!isAuthenticated()) return false;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        
        for (String permission : permissions) {
            if (authentication.getAuthorities().stream()
                    .noneMatch(authority -> authority.getAuthority().equals(permission))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String role) {
        if (!isAuthenticated()) return false;
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;
        
        // 检查角色，通常角色以"ROLE_"前缀
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix));
    }
    
    /**
     * 获取当前登录用户的角色ID
     */
    public String getCurrentUserRoleId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            // 获取用户的第一个角色ID（如果有多个角色，这里只取第一个）
            if (!userDetails.getRoles().isEmpty()) {
                return userDetails.getRoles().get(0);
            }
        }
        return null;
    }
}