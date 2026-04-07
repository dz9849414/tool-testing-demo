package com.example.tooltestingdemo.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.tooltestingdemo.entity.*;
import com.example.tooltestingdemo.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化类
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultData();
    }
    
    /**
     * 初始化默认数据
     */
    private void initializeDefaultData() {
        log.info("开始初始化默认数据...");
        
        // 初始化默认角色
        SysRole adminRole = initializeDefaultRole();
        
        // 初始化默认权限
        SysPermission systemPermission = initializeDefaultPermission();
        
        // 初始化默认用户
        SysUser adminUser = initializeDefaultUser();
        
        // 建立用户角色关联
        if (adminUser != null && adminRole != null) {
            initializeUserRoleRelation(adminUser, adminRole);
        }
        
        // 建立角色权限关联
        if (adminRole != null && systemPermission != null) {
            initializeRolePermissionRelation(adminRole, systemPermission);
        }
        
        log.info("默认数据初始化完成");
    }
    
    /**
     * 初始化默认角色
     */
    private SysRole initializeDefaultRole() {
        SysRole existingRole = roleMapper.selectById("admin");
        if (existingRole == null) {
            SysRole adminRole = new SysRole();
            adminRole.setId("admin");
            adminRole.setName("系统管理员");
            adminRole.setDescription("系统管理员，拥有所有权限");
            adminRole.setType("SYSTEM");
            adminRole.setCreateTime(LocalDateTime.now());
            adminRole.setUpdateTime(LocalDateTime.now());
            
            roleMapper.insert(adminRole);
            log.info("创建默认角色: {}", adminRole.getName());
            return adminRole;
        }
        
        log.info("默认角色已存在");
        return existingRole;
    }
    
    /**
     * 初始化默认权限
     */
    private SysPermission initializeDefaultPermission() {
        SysPermission existingPermission = permissionMapper.selectById("p1");
        if (existingPermission == null) {
            SysPermission systemPermission = new SysPermission();
            systemPermission.setId("p1");
            systemPermission.setName("系统管理");
            systemPermission.setCode("system:management");
            systemPermission.setDescription("系统管理模块");
            systemPermission.setModule("system");
            systemPermission.setType("MENU");
            systemPermission.setParentId("0");
            systemPermission.setLevel(1);
            systemPermission.setSort(1);
            systemPermission.setCreateTime(LocalDateTime.now());
            systemPermission.setUpdateTime(LocalDateTime.now());
            
            permissionMapper.insert(systemPermission);
            log.info("创建默认权限: {}", systemPermission.getName());
            return systemPermission;
        }
        
        log.info("默认权限已存在");
        return existingPermission;
    }
    
    /**
     * 初始化默认用户
     */
    private SysUser initializeDefaultUser() {
        SysUser existingUser = userMapper.selectByUsername("admin");
        if (existingUser == null) {
            SysUser adminUser = new SysUser();
            adminUser.setId("admin");
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@example.com");
            adminUser.setRealName("系统管理员");
            adminUser.setStatus(1);
            adminUser.setSource("LOCAL");
            adminUser.setCreateTime(LocalDateTime.now());
            adminUser.setUpdateTime(LocalDateTime.now());
            
            userMapper.insert(adminUser);
            log.info("创建默认管理员用户: {}", adminUser.getUsername());
            return adminUser;
        }
        
        log.info("默认管理员用户已存在");
        return existingUser;
    }
    
    /**
     * 初始化用户角色关联
     */
    private void initializeUserRoleRelation(SysUser user, SysRole role) {
        int count = userRoleMapper.countByUserIdAndRoleId(user.getId(), role.getId());
        if (count == 0) {
            SysUserRole userRole = new SysUserRole();
            userRole.setId("ur_admin");
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRole.setCreateTime(LocalDateTime.now());
            userRole.setCreateUser("system");
            
            userRoleMapper.insert(userRole);
            log.info("建立用户角色关联: {} -> {}", user.getUsername(), role.getName());
        }
    }
    
    /**
     * 初始化角色权限关联
     */
    private void initializeRolePermissionRelation(SysRole role, SysPermission permission) {
        int count = rolePermissionMapper.countByRoleIdAndPermissionId(role.getId(), permission.getId());
        if (count == 0) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setId("rp1");
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setCreateTime(LocalDateTime.now());
            rolePermission.setCreateUser("system");
            
            rolePermissionMapper.insert(rolePermission);
            log.info("建立角色权限关联: {} -> {}", role.getName(), permission.getName());
        }
    }
}