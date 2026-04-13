package com.example.tooltestingdemo.security;

import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.enums.RoleEnum;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义用户详情服务
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final SysUserService userService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 根据用户角色和权限获取权限集合
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 获取用户的角色列表
        List<String> roles = userService.getRolesByUserId(user.getId());
        
        if (roles != null && !roles.isEmpty()) {
            for (String roleCode : roles) {
                // 根据角色代码获取角色枚举
                RoleEnum role = RoleEnum.getByCode(roleCode);
                // 添加角色权限
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode().toUpperCase()));
            }
        } else {
            // 默认角色
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            // 如果没有角色，添加默认角色到角色列表
            roles = new ArrayList<>();
            roles.add("user");
        }
        
        // 获取用户的权限列表
        List<String> permissions = userService.getPermissionsByUserId(user.getId());
        if (permissions != null && !permissions.isEmpty()) {
            for (String permission : permissions) {
                // 添加权限
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }
        
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                roles,
                authorities
        );
    }
}