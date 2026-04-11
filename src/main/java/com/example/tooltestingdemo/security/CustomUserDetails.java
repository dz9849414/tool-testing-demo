package com.example.tooltestingdemo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义用户详情实现
 * 扩展了Spring Security的User类，添加了用户ID字段
 */
public class CustomUserDetails extends User {

    private final String userId;

    public CustomUserDetails(String userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public CustomUserDetails(String userId, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }

    /**
     * 获取用户ID
     */
    public String getUserId() {
        return userId;
    }
}