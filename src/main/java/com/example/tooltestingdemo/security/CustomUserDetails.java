package com.example.tooltestingdemo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

/**
 * 自定义用户详情实现
 * 扩展了Spring Security的User类，添加了用户ID和角色列表字段
 */
public class CustomUserDetails extends User {

    private final Long userId;
    private final List<String> roles;

    public CustomUserDetails(Long userId, String username, String password, List<String> roles, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.roles = roles;
    }

    public CustomUserDetails(Long userId, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, List<String> roles, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.roles = roles;
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * 获取用户角色列表
     */
    public List<String> getRoles() {
        return roles;
    }
}