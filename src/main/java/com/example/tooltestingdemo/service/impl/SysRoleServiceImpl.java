package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.entity.SysRolePermission;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysUserRole;
import com.example.tooltestingdemo.mapper.SysRoleMapper;
import com.example.tooltestingdemo.mapper.SysRolePermissionMapper;
import com.example.tooltestingdemo.mapper.SysUserRoleMapper;
import com.example.tooltestingdemo.mapper.SysPermissionMapper;
import com.example.tooltestingdemo.service.SysRoleService;
import com.example.tooltestingdemo.util.IdGenerator;
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
    private final SysPermissionMapper permissionMapper;
    
    @Override
    public SysRole findByName(String name) {
        return roleMapper.selectByName(name);
    }
    
    @Override
    public List<SysRole> findByType(String type) {
        return roleMapper.selectByType(type);
    }
    
    @Override
    public Page<SysRole> findByType(Page<SysRole> page, String type) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getType, type);
        return roleMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public List<SysRole> findByScopeId(String scopeId) {
        return roleMapper.selectByScopeId(scopeId);
    }
    
    @Override
    public SysRole findByNameAndScopeId(String name, String scopeId) {
        List<SysRole> roles = roleMapper.selectByNameAndScopeId(name, scopeId);
        return roles != null && !roles.isEmpty() ? roles.get(0) : null;
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
    public boolean existsByNameAndScope(String name, String scopeId, String excludeId) {
        List<SysRole> roles = roleMapper.selectByNameAndScopeId(name, scopeId);
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        
        // 检查是否存在排除ID之外的角色
        for (SysRole role : roles) {
            if (excludeId == null || !role.getId().equals(excludeId)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public void assignPermissions(String roleId, List<String> permissionIds) {
        // 先删除已有的权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 创建新的权限关联
        for (String permissionId : permissionIds) {
            SysRolePermission rolePermission = new SysRolePermission();
            rolePermission.setId("rp_" + IdGenerator.generateSnowflakeId());
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
            userRole.setId("ur_" + IdGenerator.generateSnowflakeId());
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            userRole.setCreateUser(1L);
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
            super.updateById(role);
        }
    }
    
    @Override
    public void disableRole(String roleId) {
        SysRole role = getById(roleId);
        if (role != null) {
            role.setStatus(0);
            super.updateById(role);
        }
    }
    
    @Override
    public void updateRoleStatus(String roleId, Integer status) {
        SysRole role = getById(roleId);
        if (role != null) {
            role.setStatus(status);
            super.updateById(role);
        }
    }
    
    @Override
    public List<SysPermission> getPermissionsByRoleId(String roleId) {
        return permissionMapper.selectByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void batchUpdateRoleStatus(List<String> roleIds, Integer status) {
        for (String roleId : roleIds) {
            // 跳过admin角色
            if ("admin".equals(roleId)) {
                continue;
            }
            
            SysRole role = getById(roleId);
            if (role != null) {
                role.setStatus(status);
                super.updateById(role);
            }
        }
    }
    
    public boolean updateRole(SysRole role) {
        if (role == null) {
            return false;
        }
        
        // 使用UpdateWrapper构建更新条件，确保即使scopeId为null也能更新
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SysRole> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("id", role.getId())
                .set("name", role.getName())
                .set("description", role.getDescription())
                .set("type", role.getType())
                .set("scope_id", role.getScopeId())
                .set("status", role.getStatus())
                .set("update_time", java.time.LocalDateTime.now());
        
        return baseMapper.update(null, updateWrapper) > 0;
    }
    
    @Transactional
    public boolean deleteRole(String id) {
        // 先删除角色与用户的关联
        userRoleMapper.deleteByRoleId(id);
        
        // 再删除角色与权限的关联
        rolePermissionMapper.deleteByRoleId(id);
        
        // 最后删除角色本身
        return baseMapper.deleteById(id) > 0;
    }
}