package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 */
public interface SysUserService {
    
    /**
     * 根据ID查找用户
     */
    SysUser findById(Long id);
    
    /**
     * 根据用户名查找用户
     */
    SysUser findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    SysUser findByEmail(String email);
    
    /**
     * 查找所有用户
     */
    List<SysUser> findAll();
    
    /**
     * 分页查找用户
     */
    Page<SysUser> findAll(Page<SysUser> page);
    
    /**
     * 保存用户
     */
    SysUser save(SysUser user);
    
    /**
     * 更新用户
     */
    SysUser update(SysUser user);
    
    /**
     * 删除用户
     */
    void deleteById(Long id);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态获取用户列表
     */
    List<SysUser> findByStatus(Integer status);
    
    /**
     * 分页根据状态获取用户列表
     */
    Page<SysUser> findByStatus(Page<SysUser> page, Integer status);
    
    /**
     * 根据角色ID查找用户列表
     */
    List<SysUser> findByRoleId(String roleId);
    
    /**
     * 更新用户最后登录信息
     */
    void updateLastLoginInfo(Long userId, String ipAddress);
    
    /**
     * 更新用户状态并记录审批人信息
     */
    void updateUserStatusWithApproval(Long userId, Integer status, Long approverId);
    
    /**
     * 修改用户密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取用户的角色列表
     */
    List<String> getRolesByUserId(Long userId);
    
    /**
     * 为用户分配角色
     */
    void assignRoles(Long userId, List<String> roleIds, Long operatorId);
    
    /**
     * 获取用户的权限列表
     */
    List<String> getPermissionsByUserId(Long userId);

    /**
     * 获取用户的权限列表，按模块分组
     */
    java.util.Map<String, java.util.List<String>> getPermissionsByUserIdGrouped(Long userId);
    
    /**
     * 搜索用户
     */
    List<SysUser> searchUsers(String keyword);
    
    /**
     * 分页搜索用户
     */
    Page<SysUser> searchUsers(Page<SysUser> page, String keyword);

    /**
     * 根据用户名获取权限列表
     */
    List<String> getPermissionsByUsername(String username);
    
    /**
     * 批量分配权限给用户
     * 
     * @param userIds 用户ID列表
     * @param permissions 权限ID列表
     * @param operationType 操作类型：ADD-添加权限，REMOVE-移除权限，REPLACE-替换权限
     * @return 是否成功
     */
    boolean batchAssignPermissions(List<Long> userIds, List<String> permissions, String operationType);
}