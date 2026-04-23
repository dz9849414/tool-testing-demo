package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {
    
    /**
     * 根据角色名称查找角色
     */
    SysRole findByName(String name);
    
    /**
     * 根据类型获取角色列表
     */
    List<SysRole> findByType(String type);
    
    /**
     * 根据类型分页查询角色
     */
    Page<SysRole> findByType(Page<SysRole> page, String type);

    /**
     * 分页查询角色列表（支持模糊查询）
     */
    Page<SysRole> getRolesByPageWithSearch(Page<SysRole> page, String name);
    
    /**
     * 根据作用域ID查找角色列表
     */
    List<SysRole> findByScopeId(String scopeId);
    
    /**
     * 根据角色名称和作用域ID查找角色
     */
    SysRole findByNameAndScopeId(String name, String scopeId);
    
    /**
     * 根据用户ID查找角色列表
     */
    List<SysRole> findByUserId(String userId);
    
    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查角色名称是否存在（排除指定ID）
     */
    boolean existsByName(String name, String excludeId);
    
    /**
     * 检查角色名称在指定作用域下是否已存在（排除指定ID）
     */
    boolean existsByNameAndScope(String name, String scopeId, String excludeId);
    
    /**
     * 为角色分配权限
     */
    void assignPermissions(String roleId, List<String> permissionIds);
    
    /**
     * 为用户分配角色
     */
    void assignUsers(String roleId, List<String> userIds);
    
    /**
     * 从角色中移除权限
     */
    void removePermissions(String roleId, List<String> permissionIds);
    
    /**
     * 从角色中移除用户
     */
    void removeUsers(String roleId, List<String> userIds);
    
    /**
     * 根据角色状态查找角色列表
     */
    List<SysRole> findByStatus(Integer status);
    
    /**
     * 启用角色
     */
    void enableRole(String roleId);
    
    /**
     * 禁用角色
     */
    void disableRole(String roleId);
    
    /**
     * 更新角色状态
     */
    void updateRoleStatus(String roleId, Integer status);
    
    /**
     * 更新角色
     */
    boolean updateRole(SysRole role);
    
    /**
     * 删除角色
     */
    boolean deleteRole(String id);
    
    /**
     * 获取角色的权限列表
     */
    List<SysPermission> getPermissionsByRoleId(String roleId);
    
    /**
     * 批量更新角色状态
     */
    void batchUpdateRoleStatus(List<String> roleIds, Integer status);
}