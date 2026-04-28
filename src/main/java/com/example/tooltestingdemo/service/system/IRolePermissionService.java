package com.example.tooltestingdemo.service.system;

import com.example.tooltestingdemo.dto.system.BatchRemovePermissionDTO;
import com.example.tooltestingdemo.dto.system.BatchRemoveUserRoleDTO;

/**
 * 角色权限管理服务接口
 */
public interface IRolePermissionService {
    
    /**
     * 批量从角色移除权限
     */
    Boolean batchRemovePermissionsFromRole(BatchRemovePermissionDTO dto);
    
    /**
     * 批量从用户移除角色
     */
    Boolean batchRemoveRolesFromUser(BatchRemoveUserRoleDTO dto);
    
    /**
     * 检查角色是否拥有权限
     */
    Boolean roleHasPermission(String roleId, String permissionId);
    
    /**
     * 检查用户是否拥有角色
     */
    Boolean userHasRole(String userId, String roleId);
}