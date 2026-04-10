package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    
    /**
     * 获取所有角色列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleService.list();
        return ResponseEntity.ok(roles);
    }
    
    /**
     * 分页获取角色列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SysRole>> getRolesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysRole> pageParam = new Page<>(page, size);
        Page<SysRole> roles = roleService.page(pageParam);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SysRole> getRoleById(@PathVariable String id) {
        SysRole role = roleService.getById(id);
        if (role == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(role);
    }
    
    /**
     * 根据类型获取角色列表
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SysRole>> getRolesByType(@PathVariable String type) {
        List<SysRole> roles = roleService.findByType(type);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * 根据用户ID获取角色列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<List<SysRole>> getRolesByUserId(@PathVariable String userId) {
        List<SysRole> roles = roleService.findByUserId(userId);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * 创建新角色
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRole(@RequestBody SysRole role) {
        if (roleService.existsByName(role.getName())) {
            return ResponseEntity.badRequest().body("角色名称已存在");
        }
        
        Boolean savedRole = roleService.save(role);
        return ResponseEntity.ok(savedRole);
    }
    
    /**
     * 更新角色信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable String id, @RequestBody SysRole role) {
        // 检查是否是admin角色
        if ("admin".equals(id)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能修改admin角色");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        role.setId(id);
        
        if (roleService.existsByName(role.getName(), id)) {
            return ResponseEntity.badRequest().body("角色名称已存在");
        }
        
        boolean updated = roleService.updateById(role);
        if (!updated) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roleService.getById(id));
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable String id) {
        // 检查是否是admin角色
        if ("admin".equals(id)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能删除admin角色");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        SysRole role = roleService.getById(id);
        if (role == null) {
            return ResponseEntity.notFound().build();
        }
        
        boolean deleted = roleService.removeById(id);
        if (!deleted) {
            return ResponseEntity.badRequest().body("删除角色失败");
        }
        return ResponseEntity.ok().build();
    }
    
    /**
     * 为角色分配权限
     */
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignPermissions(@PathVariable String roleId, @RequestBody List<String> permissionIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能为admin角色分配权限");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        roleService.assignPermissions(roleId, permissionIds);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "权限分配成功");
        response.put("data", null);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 为角色分配用户
     */
    @PostMapping("/{roleId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能为admin角色分配用户");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        roleService.assignUsers(roleId, userIds);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 从角色中移除权限
     */
    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removePermissions(@PathVariable String roleId, @RequestBody List<String> permissionIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能从admin角色中移除权限");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        roleService.removePermissions(roleId, permissionIds);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 从角色中移除用户
     */
    @DeleteMapping("/{roleId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能从admin角色中移除用户");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        roleService.removeUsers(roleId, userIds);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 根据状态获取角色列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SysRole>> getRolesByStatus(@PathVariable Integer status) {
        List<SysRole> roles = roleService.findByStatus(status);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * 启用角色
     */
    @PutMapping("/{roleId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableRole(@PathVariable String roleId) {
        roleService.enableRole(roleId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 禁用角色
     */
    @PutMapping("/{roleId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableRole(@PathVariable String roleId) {
        roleService.disableRole(roleId);
        return ResponseEntity.ok().build();
    }
}