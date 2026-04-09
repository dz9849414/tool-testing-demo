package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.entity.SysRolePermission;
import com.example.tooltestingdemo.entity.SysUserRole;
import com.example.tooltestingdemo.mapper.SysRoleMapper;
import com.example.tooltestingdemo.mapper.SysRolePermissionMapper;
import com.example.tooltestingdemo.mapper.SysUserRoleMapper;
import com.example.tooltestingdemo.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    
    @Override
    public SysRole findByName(String name) {
        return roleMapper.selectByName(name);
    }
    
    @Override
    public List<SysRole> findByType(String type) {
        return roleMapper.selectByType(type);
    }
    
    @Override
    public List<SysRole> findByScopeId(String scopeId) {
        return roleMapper.selectByScopeId(scopeId);
    }
    
    @Override
    public SysRole findByNameAndScopeId(String name, String scopeId) {
        return roleMapper.selectByNameAndScopeId(name, scopeId);
    }
    
    @Override
    public List<SysRole> findByUserId(String userId) {
        return roleMapper.selectByUserId(userId);
    }
    
    @Override
    public boolean existsByName(String name) {
        return roleMapper.selectByName(name) != null;
    }
    
    @Override
    public boolean existsByName(String name, String excludeId) {
        SysRole role = roleMapper.selectByName(name);
        return role != null && !role.getId().equals(excludeId);
    }
    
    @Override
    @Transactional
    public void assignPermissions(String roleId, List<String> permissionIds) {
        // 先删除已有的权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 创建新的权限关联
        for (String permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setId("rp_" + System.currentTimeMillis());
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreateTime(LocalDateTime.now());
            rolePermission.setCreateUser("system");
            rolePermissionMapper.insert(rolePermission);
        }
    }
    
    @Override
    @Transactional
    public void assignUsers(String roleId, List<String> userIds) {
        // 先删除已有的用户关联
        userRoleMapper.deleteByRoleId(roleId);
        
        // 创建新的用户关联
        for (String userId : userIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setId("ur_" + System.currentTimeMillis());
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            userRole.setCreateUser("system");
            userRoleMapper.insert(userRole);
        }
    }
    
    @Override
    @Transactional
    public void removePermissions(String roleId, List<String> permissionIds) {
        for (String permissionId : permissionIds) {
            SysRolePermission rolePermission = rolePermissionMapper.selectByRoleIdAndPermissionId(roleId, permissionId);
            if (rolePermission != null) {
                rolePermissionMapper.deleteById(rolePermission.getId());
            }
        }
    }
    
    @Override
    @Transactional
    public void removeUsers(String roleId, List<String> userIds) {
        for (String userId : userIds) {
            SysUserRole userRole = userRoleMapper.selectByUserIdAndRoleId(userId, roleId);
            if (userRole != null) {
                userRoleMapper.deleteById(userRole.getId());
            }
        }
    }
    
    @Override
    public List<SysRole> findByStatus(Integer status) {
        return roleMapper.selectByStatus(status);
    }
    
    @Override
    public void enableRole(String roleId) {
        SysRole role = getById(roleId);
        if (role != null) {
            role.setStatus(1);
            updateById(role);
        }
    }
    
    @Override
    public void disableRole(String roleId) {
        SysRole role = getById(roleId);
        if (role != null) {
            role.setStatus(0);
            updateById(role);
        }
    }
}