package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.entity.SysPermission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface SysPermissionService extends IService<SysPermission> {
    
    /**
     * 获取所有权限列表
     * @param moduleType 模块类型筛选：null-查除了协议模块的范围，2-只查协议模块的
     */
    List<SysPermission> getAllPermissions(Integer moduleType);
    
    /**
     * 分页获取权限列表
     */
    Page<SysPermission> getPermissionsByPage(Page<SysPermission> page, String name, String code, String module, String type);
    
    /**
     * 根据ID获取权限信息
     */
    SysPermission getPermissionById(String id);
    
    /**
     * 根据模块获取权限列表
     */
    List<SysPermission> getPermissionsByModule(String module);
    
    /**
     * 根据类型获取权限列表
     */
    List<SysPermission> getPermissionsByType(String type);
    
    /**
     * 根据权限代码获取权限
     */
    SysPermission getPermissionByCode(String code);
    
    /**
     * 检查权限代码是否存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查权限代码是否存在（排除指定ID）
     */
    boolean existsByCode(String code, String excludeId);
}