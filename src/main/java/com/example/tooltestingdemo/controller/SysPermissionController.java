package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class SysPermissionController {
    
    private final SysPermissionService permissionService;
    
    /**
     * 获取所有权限列表（包含总条目数）
     * @param moduleType 模块类型筛选：不传值-查除了协议模块的范围，传2-只查协议模块的
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:permission:api')")
    public Result<Object> getAllPermissions(@RequestParam(required = false) Integer moduleType) {
        List<SysPermission> permissions = permissionService.getAllPermissions(moduleType);
        
        // 创建包含总条目数的响应对象
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("list", permissions);
        response.put("total", permissions.size());
        
        return Result.success("获取权限列表成功", response);
    }
    
    /**
     * 分页获取权限列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:permission:api')")
    public Result<Page<SysPermission>> getPermissionsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type) {
        Page<SysPermission> pageParam = new Page<>(pageNum, pageSize);
        Page<SysPermission> permissions = permissionService.getPermissionsByPage(pageParam, name, code, module, type);
        return Result.success("获取权限列表成功", permissions);
    }
    
    /**
     * 根据ID获取权限信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:permission:api')")
    public Result<SysPermission> getPermissionById(@PathVariable String id) {
        SysPermission permission = permissionService.getPermissionById(id);
        if (permission == null) {
            return Result.error(404, "权限不存在");
        }
        return Result.success("获取权限信息成功", permission);
    }
    
    /**
     * 根据模块获取权限列表
     */
    @GetMapping("/module/{module}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:permission:api')")
    public Result<List<SysPermission>> getPermissionsByModule(@PathVariable String module) {
        List<SysPermission> permissions = permissionService.getPermissionsByModule(module);
        return Result.success("获取权限列表成功", permissions);
    }
    
    /**
     * 根据类型获取权限列表
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:permission:api')")
    public Result<List<SysPermission>> getPermissionsByType(@PathVariable String type) {
        List<SysPermission> permissions = permissionService.getPermissionsByType(type);
        return Result.success("获取权限列表成功", permissions);
    }
}