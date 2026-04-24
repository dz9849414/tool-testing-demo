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
    public Page<SysRole> getRolesByPageWithSearch(Page<SysRole> page, String name) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果提供了名称参数，进行模糊查询
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(SysRole::getName, name.trim());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysRole::getCreateTime);
        
        // 执行分页查询
        return this.page(page, queryWrapper);
    }
    
    @Override
    public Page<SysRole> getRolesByPageWithSearch(Page<SysRole> page, String name, String description, Integer status, String sortField, String sortOrder) {
        return getRolesByPageWithSearch(page, name, description, status, null, null, sortField, sortOrder);
    }
    
    @Override
    public Page<SysRole> getRolesByPageWithSearch(Page<SysRole> page, String name, String description, Integer status, java.time.LocalDate beginTime, java.time.LocalDate endTime, String sortField, String sortOrder) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        
        // 名称模糊查询
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(SysRole::getName, name.trim());
        }
        
        // 描述模糊查询
        if (description != null && !description.trim().isEmpty()) {
            queryWrapper.like(SysRole::getDescription, description.trim());
        }
        
        // 状态精确查询
        if (status != null) {
            queryWrapper.eq(SysRole::getStatus, status);
        }
        
        // 日期范围查询
        applyDateRangeFilter(queryWrapper, beginTime, endTime);
        
        // 排序处理
        applySorting(queryWrapper, sortField, sortOrder);
        
        // 执行分页查询
        return this.page(page, queryWrapper);
    }
    
    /**
     * 应用日期范围过滤
     */
    private void applyDateRangeFilter(LambdaQueryWrapper<SysRole> queryWrapper, java.time.LocalDate beginTime, java.time.LocalDate endTime) {
        if (beginTime != null && endTime != null) {
            // 开始时间和结束时间都存在
            queryWrapper.between(SysRole::getCreateTime, 
                beginTime.atStartOfDay(), 
                endTime.atTime(23, 59, 59));
        } else if (beginTime != null) {
            // 只有开始时间
            queryWrapper.ge(SysRole::getCreateTime, beginTime.atStartOfDay());
        } else if (endTime != null) {
            // 只有结束时间
            queryWrapper.le(SysRole::getCreateTime, endTime.atTime(23, 59, 59));
        }
    }
    
    /**
     * 应用排序规则
     */
    private void applySorting(LambdaQueryWrapper<SysRole> queryWrapper, String sortField, String sortOrder) {
        if (sortField == null || sortField.trim().isEmpty()) {
            sortField = "createTime";
        }
        
        if (sortOrder == null || sortOrder.trim().isEmpty()) {
            sortOrder = "desc";
        }
        
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        
        switch (sortField.toLowerCase()) {
            case "name":
                if (isAsc) {
                    queryWrapper.orderByAsc(SysRole::getName);
                } else {
                    queryWrapper.orderByDesc(SysRole::getName);
                }
                break;
            case "type":
                if (isAsc) {
                    queryWrapper.orderByAsc(SysRole::getType);
                } else {
                    queryWrapper.orderByDesc(SysRole::getType);
                }
                break;
            case "status":
                if (isAsc) {
                    queryWrapper.orderByAsc(SysRole::getStatus);
                } else {
                    queryWrapper.orderByDesc(SysRole::getStatus);
                }
                break;
            case "createtime":
            case "create_time":
            default:
                if (isAsc) {
                    queryWrapper.orderByAsc(SysRole::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(SysRole::getCreateTime);
                }
                break;
        }
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
    
    @Override
    public Long getMaxRoleId() {
        return roleMapper.selectMaxId();
    }
    
    @Override
    @Transactional
    public boolean batchAssignPermissions(List<String> roleIds, List<String> permissions, String operationType) {
        try {
            // 批量处理每个角色
            for (String roleId : roleIds) {
                // 跳过admin角色
                if ("admin".equals(roleId)) {
                    continue;
                }
                
                // 获取角色现有的权限
                List<String> existingPermissions = getExistingPermissionsByRoleId(roleId);
                
                // 根据操作类型处理权限
                List<String> newPermissions;
                switch (operationType.toUpperCase()) {
                    case "ADD":
                        // 添加权限：合并现有权限和新权限，去重
                        newPermissions = new java.util.ArrayList<>(existingPermissions);
                        for (String permission : permissions) {
                            if (!newPermissions.contains(permission)) {
                                newPermissions.add(permission);
                            }
                        }
                        // 使用已有的assignPermissions方法
                        assignPermissions(roleId, newPermissions);
                        break;
                        
                    case "REMOVE":
                        // 移除权限：从现有权限中移除指定的权限
                        newPermissions = new java.util.ArrayList<>(existingPermissions);
                        newPermissions.removeAll(permissions);
                        // 使用已有的assignPermissions方法
                        assignPermissions(roleId, newPermissions);
                        break;
                        
                    case "REPLACE":
                        // 替换权限：直接用新权限替换现有权限
                        // 使用已有的assignPermissions方法
                        assignPermissions(roleId, permissions);
                        break;
                        
                    default:
                        throw new IllegalArgumentException("不支持的操作类型: " + operationType);
                }
            }
            
            return true;
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("批量分配权限失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取角色现有的权限ID列表
     */
    private List<String> getExistingPermissionsByRoleId(String roleId) {
        List<SysPermission> permissions = getPermissionsByRoleId(roleId);
        List<String> permissionIds = new java.util.ArrayList<>();
        for (SysPermission permission : permissions) {
            permissionIds.add(permission.getId());
        }
        return permissionIds;
    }
}