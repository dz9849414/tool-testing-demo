package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.service.SysRoleService;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.dto.SysRoleDTO;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {
    
    private final SysRoleService roleService;
    private final SysUserService userService;
    
    /**
     * 获取所有角色列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleService.list();
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 分页获取角色列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<Page<SysRole>> getRolesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        Page<SysRole> roles = roleService.page(pageParam);
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<SysRole> getRoleById(@PathVariable String id) {
        SysRole role = roleService.getById(id);
        if (role == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
        }
        return Result.success("获取角色信息成功", role);
    }
    
    /**
     * 根据类型获取角色列表
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<Page<SysRole>> getRolesByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        Page<SysRole> roles = roleService.findByType(pageParam, type);
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 根据用户ID获取角色列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api') or @securityService.isCurrentUser(#userId)")
    public Result<List<SysRole>> getRolesByUserId(@PathVariable String userId) {
        List<SysRole> roles = roleService.findByUserId(userId);
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 创建新角色
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<Boolean> createRole(@RequestBody SysRoleDTO roleDTO) {
        SysRole role = new SysRole();
        try {
            BeanUtils.copyProperties(role, roleDTO);
        } catch (Exception e) {
            return Result.error(ErrorStatus.BAD_REQUEST, "参数转换失败");
        }
        
        // 确保当scopeId为null或者没传时，将角色的scopeId设为null
        if (role.getScopeId() != null && role.getScopeId().isEmpty()) {
            role.setScopeId(null);
        }
        
        // 检查名称和作用域的唯一性
        if (roleService.existsByNameAndScope(role.getName(), role.getScopeId(), null)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "角色名称在当前作用域下已存在");
        }
        
        // 检查id（编码）的唯一性
        if (role.getId() != null && !role.getId().isEmpty()) {
            if (roleService.getById(role.getId()) != null) {
                return Result.error(ErrorStatus.BAD_REQUEST, "角色编码已存在");
            }
        } else {
            // 如果id没传入，系统自动生成
            role.setId("role_" + System.currentTimeMillis());
        }
        
        Boolean savedRole = roleService.save(role);
        return Result.success("创建角色成功", savedRole);
    }
    
    /**
     * 更新角色信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    @PermissionCheck(type = "update")
    public Result<SysRole> updateRole(@PathVariable String id, @RequestBody SysRoleDTO roleDTO) {
        // 检查是否是admin角色
        if ("admin".equals(id)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能修改admin角色");
        }
        
        // 检查角色是否存在
        SysRole existingRole = roleService.getById(id);
        if (existingRole == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
        }
        
        SysRole role = new SysRole();
        role.setId(id);
        
        try {
            BeanUtils.copyProperties(role, roleDTO);
        } catch (Exception e) {
            return Result.error(ErrorStatus.BAD_REQUEST, "参数转换失败");
        }
        
        // 确保当scopeId为null或者没传时，将角色的scopeId设为null
        if (role.getScopeId() != null && role.getScopeId().isEmpty()) {
            role.setScopeId(null);
        }
        
        // 检查角色名称是否在当前作用域下已存在（排除当前角色）
        boolean exists = roleService.existsByNameAndScope(role.getName(), role.getScopeId(), id);
        if (exists) {
            return Result.error(ErrorStatus.BAD_REQUEST, "角色名称在当前作用域下已存在");
        }
        
        boolean updatedRole = roleService.updateRole(role);
        if (updatedRole) {
            return Result.success("更新角色信息成功");
        } else {
            return Result.error(ErrorStatus.BAD_REQUEST, "更新角色信息失败");
        }
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<String> deleteRole(@PathVariable String id) {
        // 检查是否是admin角色
        if ("admin".equals(id)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能删除admin角色");
        }
        
        SysRole role = roleService.getById(id);
        if (role == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
        }
        
        boolean deleted = roleService.deleteRole(id);
        if (!deleted) {
            return Result.error(ErrorStatus.BAD_REQUEST, "删除角色失败");
        }
        return Result.success("角色删除成功");
    }
    
    /**
     * 为角色分配权限
     */
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    @PermissionCheck(type = "assignPermissions")
    public Result<String> assignPermissions(@PathVariable String roleId, @RequestBody List<String> permissionIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能为admin角色分配权限");
        }
        
        // 检查权限列表中是否包含admin权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<String> adminPermissions = new ArrayList<>();
            for (String permissionId : permissionIds) {
                if (permissionId.toLowerCase().contains("admin")) {
                    adminPermissions.add(permissionId);
                }
            }
            if (!adminPermissions.isEmpty()) {
                return Result.error(ErrorStatus.BAD_REQUEST, "不能分配admin权限");
            }
        }
        
        roleService.assignPermissions(roleId, permissionIds);
        return Result.success("权限分配成功");
    }
    
    /**
     * 为角色分配用户
     */
    @PostMapping("/{roleId}/users")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    @PermissionCheck(type = "assignUsersToRole")
    public Result<String> assignUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能为admin角色分配用户");
        }
        
        roleService.assignUsers(roleId, userIds);
        return Result.success("用户分配成功");
    }
    
    /**
     * 从角色中移除权限
     */
    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    @PermissionCheck(type = "removePermissions")
    public Result<String> removePermissions(@PathVariable String roleId, @RequestBody List<String> permissionIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能从admin角色中移除权限");
        }
        
        // 检查权限列表中是否包含admin权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<String> adminPermissions = new ArrayList<>();
            for (String permissionId : permissionIds) {
                if (permissionId.toLowerCase().contains("admin")) {
                    adminPermissions.add(permissionId);
                }
            }
            if (!adminPermissions.isEmpty()) {
                return Result.error(ErrorStatus.BAD_REQUEST, "不能操作admin权限");
            }
        }
        
        roleService.removePermissions(roleId, permissionIds);
        return Result.success("权限移除成功");
    }
    
    /**
     * 从角色中移除用户
     */
    @DeleteMapping("/{roleId}/users")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    @PermissionCheck(type = "removeUsersFromRole")
    public Result<String> removeUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能从admin角色中移除用户");
        }
        
        roleService.removeUsers(roleId, userIds);
        return Result.success("用户移除成功");
    }
    
    /**
     * 根据状态获取角色列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<List<SysRole>> getRolesByStatus(@PathVariable Integer status) {
        List<SysRole> roles = roleService.findByStatus(status);
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 更新角色状态
     */
    @PutMapping("/{roleId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateRoleStatus(@PathVariable String roleId, @RequestParam Integer status) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能修改admin角色的状态");
        }
        
        // 检查状态值是否合法
        if (status != 0 && status != 1) {
            return Result.error(ErrorStatus.BAD_REQUEST, "状态值必须是0（禁用）或1（启用）");
        }
        
        roleService.updateRoleStatus(roleId, status);
        return Result.success(status == 1 ? "角色启用成功" : "角色禁用成功");
    }
    
    /**
     * 检查角色名称是否已存在
     */
    @GetMapping("/check-name")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Boolean>> checkNameExists(
            @RequestParam String name,
            @RequestParam(required = false) String scopeId,
            @RequestParam(required = false) String excludeId) {
        boolean exists = roleService.existsByNameAndScope(name, scopeId, excludeId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success("检查角色名称成功", response);
    }
    
    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysPermission>> getRolePermissions(@PathVariable String roleId) {
        List<SysPermission> permissions = roleService.getPermissionsByRoleId(roleId);
        return Result.success("获取角色权限列表成功", permissions);
    }
    
    /**
     * 批量更新角色状态
     */
    @PostMapping("/batch/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> batchUpdateRoleStatus(@RequestParam List<String> roleIds, @RequestParam Integer status) {
        // 检查状态值是否合法
        if (status != 0 && status != 1) {
            return Result.error(ErrorStatus.BAD_REQUEST, "状态值必须是0（禁用）或1（启用）");
        }
        
        roleService.batchUpdateRoleStatus(roleIds, status);
        return Result.success(status == 1 ? "角色批量启用成功" : "角色批量禁用成功");
    }
    
    /**
     * 查询角色关联的用户列表
     */
    @GetMapping("/{roleId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysUser>> getRoleUsers(@PathVariable String roleId) {
        // 检查角色是否存在
        SysRole role = roleService.getById(roleId);
        if (role == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
        }
        
        // 查询角色关联的用户列表
        List<SysUser> users = userService.findByRoleId(roleId);
        return Result.success("获取角色关联用户列表成功", users);
    }
}