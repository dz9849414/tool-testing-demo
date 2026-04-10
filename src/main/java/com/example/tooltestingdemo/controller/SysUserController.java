package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {
    
    private final SysUserService userService;
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public ResponseEntity<List<SysUser>> getAllUsers() {
        List<SysUser> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
    
    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public ResponseEntity<Page<SysUser>> getUsersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findAll(pageParam);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<SysUser> getUserById(@PathVariable String id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public ResponseEntity<?> createUser(@RequestBody SysUser user) {
        // 检查是否尝试创建用户名为admin的用户
        if ("admin".equals(user.getUsername())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能创建用户名为admin的用户");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        
        if (user.getEmail() != null && userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("邮箱已存在");
        }
        
        SysUser savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    @PermissionCheck(type = "update")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody SysUser user) {
        user.setId(id);
        
        // 检查是否是admin用户
        if ("admin".equals(id)) {
            // 检查是否尝试修改admin的用户名
            if (user.getUsername() != null && !"admin".equals(user.getUsername())) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "不能更改admin用户名");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            // 检查是否尝试将用户名改为admin
            if ("admin".equals(user.getUsername())) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "不能将用户名改为admin");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
        }
        
        SysUser updatedUser = userService.update(user);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "delete")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        userService.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "用户删除成功");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据状态获取用户列表
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public ResponseEntity<List<SysUser>> getUsersByStatus(@PathVariable Integer status) {
        List<SysUser> users = userService.findByStatus(status);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据角色ID获取用户列表
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public ResponseEntity<List<SysUser>> getUsersByRoleId(@PathVariable String roleId) {
        List<SysUser> users = userService.findByRoleId(roleId);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 审批用户注册
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "approve")
    public ResponseEntity<?> approveUser(@PathVariable String id, @RequestParam Integer status) {
        // 获取当前登录用户（审批人）
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String approverUsername = authentication.getName();
        
        // 获取审批人ID
        com.example.tooltestingdemo.entity.SysUser approver = userService.findByUsername(approverUsername);
        if (approver == null) {
            return ResponseEntity.badRequest().body("审批人不存在");
        }
        
        // 更新用户状态并记录审批人信息
        userService.updateUserStatusWithApproval(id, status, approver.getId());
        
        String message = status == 1 ? "用户审批通过" : "用户审批拒绝";
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 修改用户密码
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<?> changePassword(@PathVariable String id, @RequestBody PasswordChangeRequest request) {
        boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        if (!success) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "旧密码错误");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "密码修改成功");
        response.put("data", null);
        return ResponseEntity.ok(response);
    }
    
    @lombok.Data
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "assignRoles")
    public ResponseEntity<?> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        // 检查是否包含admin角色
        if (roleIds != null && roleIds.contains("admin")) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "不能分配admin角色");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 获取当前登录用户（操作人）
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String operatorUsername = authentication.getName();
        
        // 获取操作人ID
        com.example.tooltestingdemo.entity.SysUser operator = userService.findByUsername(operatorUsername);
        if (operator == null) {
            return ResponseEntity.badRequest().body("操作人不存在");
        }
        
        // 为用户分配角色
        userService.assignRoles(id, roleIds, operator.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "角色分配成功");
        response.put("data", null);
        return ResponseEntity.ok(response);
    }
}