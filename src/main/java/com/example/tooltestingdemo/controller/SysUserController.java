package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.common.ErrorStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.tooltestingdemo.dto.SysUserUpdateDTO;
import org.apache.commons.beanutils.BeanUtils;
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
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 获取所有用户列表
     */
    @GetMapping
    @PermissionCheck(perm = "system:user:api",type = "view" , or = true)
    public Result<Page<SysUser>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findAll(pageParam);
        return Result.success("获取用户列表成功", users);
    }
    
    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @PermissionCheck(type = "view", perm = "system:user:api", or = true)
    public Result<Page<SysUser>> getUsersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findAll(pageParam);
        return Result.success("获取用户列表成功", users);
    }
    
    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    @PermissionCheck(type = "view", perm = "system:user:api", or = true, allowCurrentUser = true)
    public Result<SysUser> getUserById(@PathVariable String id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
        }
        return Result.success("获取用户信息成功", user);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public Result<SysUser> createUser(@RequestBody SysUser user) {
        // 检查是否尝试创建用户名为admin的用户
        if ("admin".equals(user.getUsername())) {
            return Result.error(400, "不能创建用户名为admin的用户");
        }
        
        if (userService.existsByUsername(user.getUsername())) {
            return Result.error(400, "用户名已存在");
        }
        
        if (user.getEmail() != null && userService.existsByEmail(user.getEmail())) {
            return Result.error(400, "邮箱已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        SysUser savedUser = userService.save(user);
        return Result.success("创建用户成功", savedUser);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    @PermissionCheck(type = "update")
    public Result<SysUser> updateUser(@PathVariable String id, @RequestBody SysUserUpdateDTO userDTO) {
        SysUser user = new SysUser();
        user.setId(id);
        
        // 检查是否是admin用户
        if ("admin".equals(id)) {
            // 检查是否尝试修改admin的用户名
            if (userDTO.getUsername() != null && !"admin".equals(userDTO.getUsername())) {
                return Result.error(400, "不能更改admin用户名");
            }
        } else {
            // 检查是否尝试将用户名改为admin
            if (userDTO.getUsername() != null && "admin".equals(userDTO.getUsername())) {
                return Result.error(400, "不能将用户名改为admin");
            }
        }
        
        // 检查邮箱是否已存在（排除当前用户）
        if (userDTO.getEmail() != null) {
            SysUser existingUser = userService.findByEmail(userDTO.getEmail());
            if (existingUser != null && !existingUser.getId().equals(id)) {
                return Result.error(400, "邮箱已存在");
            }
        }
        
        // 设置字段
        try {
            BeanUtils.copyProperties(user, userDTO);
        } catch (Exception e) {
            return Result.error(400, "参数转换失败");
        }
        
        SysUser updatedUser = userService.update(user);
        if (updatedUser == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("更新用户信息成功", updatedUser);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "delete")
    public Result<String> deleteUser(@PathVariable String id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
        }
        
        userService.deleteById(id);
        
        return Result.success("用户删除成功");
    }
    
    /**
     * 根据状态获取用户列表
     */
    @GetMapping("/status/{status}")
    @PermissionCheck(type = "view", perm = "system:user:api", or = true)
    public Result<Page<SysUser>> getUsersByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findByStatus(pageParam, status);
        return Result.success("获取用户列表成功", users);
    }
    
    /** 
      * 根据角色ID获取用户列表 
      */ 
     @GetMapping("/role/{roleId}") 
     @PreAuthorize("hasRole('ADMIN') or @permissionCheckAspect.checkPermission('system:user:api')") 
     public Result<List<SysUser>> getUsersByRoleId(@PathVariable String roleId) { 
         List<SysUser> users = userService.findByRoleId(roleId); 
         return Result.success("获取用户列表成功", users); 
     }
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success("检查用户名成功", response);
    }
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email")
    public Result<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success("检查邮箱成功", response);
    }
    
    /**
     * 搜索用户
     * 功能描述：系统支持通过用户名、姓名或邮箱关键词快速查找用户
     * 输入：搜索关键词
     * 输出：匹配的用户列表（含状态、角色概览）
     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public Result<Page<SysUser>> searchUsers(
            @RequestParam String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.searchUsers(pageParam, search);
        return Result.success("搜索用户成功", users);
    }

    /**
     * 审批用户注册
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "approve")
    public Result<String> approveUser(@PathVariable String id, @RequestParam Integer status) {
        // 获取当前登录用户（审批人）
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String approverUsername = authentication.getName();
        
        // 获取审批人ID
        com.example.tooltestingdemo.entity.SysUser approver = userService.findByUsername(approverUsername);
        if (approver == null) {
            return Result.error(400, "审批人不存在");
        }
        
        // 更新用户状态并记录审批人信息
        userService.updateUserStatusWithApproval(id, status, approver.getId());
        
        String message = status == 1 ? "用户审批通过" : "用户审批拒绝";
        return Result.success(message);
    }
    
    /**
     * 修改用户密码
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public Result<String> changePassword(@PathVariable String id, @RequestBody PasswordChangeRequest request) {
        boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        if (!success) {
            return Result.error(400, "旧密码错误");
        }
        
        return Result.success("密码修改成功");
    }
    
    @Data
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
    }

    /**
     * 获取用户的权限列表，按模块分组
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public Result<java.util.Map<String, java.util.List<String>>> getUserPermissions(@PathVariable String id) {
        java.util.Map<String, java.util.List<String>> permissions = userService.getPermissionsByUserIdGrouped(id);
        return Result.success("获取权限列表成功", permissions);
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "assignRoles")
    public Result<String> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        // 检查是否包含admin角色
        if (roleIds != null && roleIds.contains("admin")) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能分配admin角色");
        }
        
        // 获取当前登录用户（操作人）
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String operatorUsername = authentication.getName();
        
        // 获取操作人ID
        com.example.tooltestingdemo.entity.SysUser operator = userService.findByUsername(operatorUsername);
        if (operator == null) {
            return Result.error(ErrorStatus.BAD_REQUEST, "操作人不存在");
        }
        
        // 为用户分配角色
        userService.assignRoles(id, roleIds, operator.getId());
        
        return Result.success("角色分配成功");
    }
    
    /**
     * 更新用户状态（启用/禁用/锁定）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PermissionCheck(type = "update")
    public Result<String> updateUserStatus(@PathVariable String id, @RequestParam Integer status) {
        // 检查是否是admin用户
        if ("admin".equals(id)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能修改admin用户");
        }
        
        // 检查状态值是否合法
        if (status != 0 && status != 1 && status != 2) {
            return Result.error(ErrorStatus.BAD_REQUEST, "状态值不合法，0-禁用，1-启用，2-锁定");
        }
        
        // 更新用户状态
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        
        user.setStatus(status);
        userService.update(user);
        
        // TODO: 若禁用则使当前会话失效
        // 这里需要实现会话失效的逻辑，例如清除Redis中的token等
        
        String message = "";
        switch (status) {
            case 0:
                message = "用户禁用成功";
                break;
            case 1:
                message = "用户启用成功";
                break;
            case 2:
                message = "用户锁定成功";
                break;
        }
        
        return Result.success(message);
    }
}