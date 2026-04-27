package com.example.tooltestingdemo.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.system.UserPermissionDTO;
import com.example.tooltestingdemo.entity.system.SysUserPermission;
import com.example.tooltestingdemo.vo.system.UserPermissionVO;

import java.util.List;

/**
 * 用户权限直接分配服务接口
 */
public interface IUserPermissionService extends IService<SysUserPermission> {
    
    /**
     * 获取用户权限列表
     */
    List<UserPermissionVO> getUserPermissions(String userId, String scopeType, String scopeId);
    
    /**
     * 为用户分配权限
     */
    Boolean grantPermission(UserPermissionDTO dto);
    
    /**
     * 批量分配权限
     */
    Boolean batchGrantPermissions(List<UserPermissionDTO> dtos);
    
    /**
     * 更新用户权限
     */
    Boolean updatePermission(String id, UserPermissionDTO dto);
    
    /**
     * 撤销用户权限
     */
    Boolean revokePermission(String id);
    
    /**
     * 批量撤销权限
     */
    Boolean batchRevokePermissions(List<String> ids);
    
    /**
     * 检查用户是否拥有权限
     */
    Boolean hasPermission(String userId, String permissionCode, String scopeType, String scopeId);
    
    /**
     * 获取用户所有有效权限
     */
    List<String> getUserEffectivePermissions(String userId);
    
    /**
     * 获取权限分配历史
     */
    List<Object> getPermissionGrantHistory(String userId, String permissionId, String operationType);
}