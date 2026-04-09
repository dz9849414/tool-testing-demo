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
    SysUser findById(String id);
    
    /**
     * 根据用户名查找用户
     */
    SysUser findByUsername(String username);
    
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
    void deleteById(String id);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据状态查找用户列表
     */
    List<SysUser> findByStatus(Integer status);
    
    /**
     * 根据角色ID查找用户列表
     */
    List<SysUser> findByRoleId(String roleId);
    
    /**
     * 更新用户最后登录信息
     */
    void updateLastLoginInfo(String userId, String ipAddress);
    
    /**
     * 更新用户状态并记录审批人信息
     */
    void updateUserStatusWithApproval(String userId, Integer status, String approverId);
    
    /**
     * 修改用户密码
     */
    boolean changePassword(String userId, String oldPassword, String newPassword);
    
    /**
     * 获取用户的角色列表
     */
    List<String> getRolesByUserId(String userId);
    
    /**
     * 为用户分配角色
     */
    void assignRoles(String userId, List<String> roleIds, String operatorId);
    
    /**
     * 获取用户的权限列表
     */
    List<String> getPermissionsByUserId(String userId);
}