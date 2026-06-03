package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.dto.PasswordUpdateDTO;
import com.example.tooltestingdemo.dto.UserBatchPermissionDTO;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.mapper.SysPermissionMapper;
import com.example.tooltestingdemo.service.SysUserService;
import com.example.tooltestingdemo.util.ExcelUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.common.ErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.tooltestingdemo.dto.SysUserCreateDTO;
import com.example.tooltestingdemo.dto.SysUserUpdateDTO;
import com.example.tooltestingdemo.enums.RoleEnum;
import com.example.tooltestingdemo.vo.SysUserVO;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {
    
    private final SysUserService userService;
    private final SysPermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        SysUser user = userService.findByUsername(username);

        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", "用户不存在");
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        }

        // 隐藏密码信息
        user.setPassword(null);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", user);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有用户列表（支持多条件模糊搜索）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:api')")
    public Result<Page<SysUserVO>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.searchUsers(pageParam, username, phone, status, beginTime, endTime);
        
        // 转换为VO
        Page<SysUserVO> voPage = new Page<>(users.getCurrent(), users.getSize(), users.getTotal());
        voPage.setRecords(users.getRecords().stream().map(user -> {
            SysUserVO userVO = new SysUserVO();
            try {
                BeanUtils.copyProperties(userVO, user);
            } catch (Exception e) {
                throw new RuntimeException("用户数据转换失败");
            }
            return userVO;
        }).collect(Collectors.toList()));
        
        return Result.success("获取用户列表成功", voPage);
    }
    
    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:api')")
    public Result<Page<SysUserVO>> getUsersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findAll(pageParam);
        
        // 转换为VO
        Page<SysUserVO> voPage = new Page<>(users.getCurrent(), users.getSize(), users.getTotal());
        voPage.setRecords(users.getRecords().stream().map(user -> {
            SysUserVO userVO = new SysUserVO();
            try {
                BeanUtils.copyProperties(userVO, user);
            } catch (Exception e) {
                throw new RuntimeException("用户数据转换失败");
            }
            return userVO;
        }).collect(Collectors.toList()));
        
        return Result.success("获取用户列表成功", voPage);
    }
    
    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public Result<SysUserVO> getUserById(@PathVariable Long id) {
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
        }
        
        // 转换为VO
        SysUserVO userVO = new SysUserVO();
        try {
            BeanUtils.copyProperties(userVO, user);
        } catch (Exception e) {
            return Result.error(400, "用户数据转换失败");
        }
        
        return Result.success("获取用户信息成功", userVO);
    }
    
    /**
     * 创建新用户
     */
    @PostMapping
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public Result<SysUser> createUser(@RequestBody SysUserCreateDTO userDTO) {
        // 检查是否尝试创建用户名为admin的用户
        if (RoleEnum.ADMIN.getCode().equals(userDTO.getUsername())) {
            return Result.error(400, "不能创建用户名为admin的用户");
        }
        
        if (userService.existsByUsername(userDTO.getUsername())) {
            return Result.error(400, "用户名已存在");
        }
        
        if (userDTO.getEmail() != null && userService.existsByEmail(userDTO.getEmail())) {
            return Result.error(400, "邮箱已存在");
        }

        SysUser user = new SysUser();
        try {
            BeanUtils.copyProperties(user, userDTO);
        } catch (Exception e) {
            return Result.error(400, "参数转换失败");
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
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody SysUserUpdateDTO userDTO) {
        SysUser user = new SysUser();
        user.setId(id);
        
        // 检查是否是admin用户
        if (RoleEnum.ADMIN.getCode().equals(id.toString())) {
            // 检查是否尝试修改admin的用户名
            if (userDTO.getUsername() != null && !RoleEnum.ADMIN.getCode().equals(userDTO.getUsername())) {
                return Result.error(400, "不能更改admin用户名");
            }
        } else {
            // 检查是否尝试将用户名改为admin
            if (userDTO.getUsername() != null && RoleEnum.ADMIN.getCode().equals(userDTO.getUsername())) {
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
    public Result<String> deleteUser(@PathVariable Long id) {
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
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public Result<Page<SysUserVO>> getUsersByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.findByStatus(pageParam, status);
        
        // 转换为VO
        Page<SysUserVO> voPage = new Page<>(users.getCurrent(), users.getSize(), users.getTotal());
        voPage.setRecords(users.getRecords().stream().map(user -> {
            SysUserVO userVO = new SysUserVO();
            try {
                BeanUtils.copyProperties(userVO, user);
            } catch (Exception e) {
                throw new RuntimeException("用户数据转换失败");
            }
            return userVO;
        }).collect(Collectors.toList()));
        
        return Result.success("获取用户列表成功", voPage);
    }
    
    /**
     * 根据角色ID获取用户列表
     * 
     * @param roleId 角色ID
     * @param username 用户名关键词（可选，支持模糊搜索）
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    public Result<List<SysUserVO>> getUsersByRoleId(
            @PathVariable String roleId,
            @RequestParam(required = false) String username) {
        List<SysUser> users = userService.findByRoleId(roleId, username);
        
        // 转换为VO
        List<SysUserVO> userVOs = users.stream().map(user -> {
            SysUserVO userVO = new SysUserVO();
            try {
                BeanUtils.copyProperties(userVO, user);
            } catch (Exception e) {
                throw new RuntimeException("用户数据转换失败");
            }
            return userVO;
        }).collect(Collectors.toList());
        
        return Result.success("获取用户列表成功", userVOs);
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
    public Result<Page<SysUserVO>> searchUsers(
            @RequestParam String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        Page<SysUser> users = userService.searchUsers(pageParam, search);
        
        // 转换为VO
        Page<SysUserVO> voPage = new Page<>(users.getCurrent(), users.getSize(), users.getTotal());
        voPage.setRecords(users.getRecords().stream().map(user -> {
            SysUserVO userVO = new SysUserVO();
            try {
                BeanUtils.copyProperties(userVO, user);
            } catch (Exception e) {
                throw new RuntimeException("用户数据转换失败");
            }
            return userVO;
        }).collect(Collectors.toList()));
        
        return Result.success("搜索用户成功", voPage);
    }

    /**
     * 审批用户注册
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "approve")
    public Result<String> approveUser(@PathVariable Long id, @RequestParam Integer status) {
        // 获取当前登录用户（审批人）
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String approverUsername = authentication.getName();
        
        // 获取审批人ID
        com.example.tooltestingdemo.entity.SysUser approver = userService.findByUsername(approverUsername);
        if (approver == null) {
            return Result.error(400, "审批人不存在");
        }
        
        // 检查用户是否存在
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
        }
        
        // 更新用户状态并记录审批人信息
        userService.updateUserStatusWithApproval(id, status, approver.getId());
        
        String message = status == 1 ? "用户审批通过" : "用户审批拒绝";
        return Result.success(message);
    }
    
    /**
     * 修改用户密码（需要旧密码）
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public Result<String> changePassword(@PathVariable Long id, @RequestBody PasswordChangeRequest request) {
        boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        if (!success) {
            return Result.error(400, "旧密码错误");
        }
        
        return Result.success("密码修改成功");
    }
    
    /**
     * 更新用户密码（只需要新密码）
     */
    @PutMapping("/{id}/password/update")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:api')")
    public Result<String> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateDTO request) {
        boolean success = userService.updatePassword(id, request.getNewPassword());
        if (!success) {
            return Result.error(400, "用户不存在");
        }
        
        return Result.success("密码更新成功");
    }
    
    @Data
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
    }

    /**
     * 获取用户的权限列表，按模块分组
     * 
     * @param id 用户ID
     * @param moduleType 模块类型：不传返回所有权限，传2只返回协议模块权限
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("@securityService.hasPermission('system:user:api') or @securityService.isCurrentUser(#id)")
    public Result<java.util.Map<String, java.util.List<String>>> getUserPermissions(
            @PathVariable Long id,
            @RequestParam(required = false) Integer moduleType) {
        java.util.Map<String, java.util.List<String>> permissions = userService.getPermissionsByUserIdGrouped(id, moduleType);
        return Result.success("获取权限列表成功", permissions);
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "assignRoles")
    public Result<String> assignRoles(@PathVariable Long id, @RequestBody List<String> roleIds) {
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
    @PreAuthorize("@securityService.hasPermission('system:user:api')")
    @PermissionCheck(type = "update")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
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
    
    /**
     * 批量分配权限给用户
     * 
     * 请求示例：
     * {
     *   "userIds": ["2047291066211233794"],
     *   "permissions": ["report_api_1", "report_api_12"]
     * }
     */
    @PostMapping("/batch/permissions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:api')")
    public Result<String> batchAssignPermissions(@RequestBody UserBatchPermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            if (dto.getPermissions() == null || dto.getPermissions().isEmpty()) {
                return Result.error("权限ID列表不能为空");
            }
            
            // 检查用户是否存在
            for (Long userId : dto.getUserIds()) {
                SysUser user = userService.findById(userId);
                if (user == null) {
                    return Result.error("用户不存在，用户ID: " + userId);
                }
            }
            
            // 调用服务层进行批量权限分配
            boolean success = userService.batchAssignPermissions(dto.getUserIds(), dto.getPermissions(), dto.getOperationType());
            
            if (success) {
                return Result.success("批量分配权限成功");
            } else {
                return Result.error("批量分配权限失败");
            }
        } catch (Exception e) {
            return Result.error("批量分配权限异常: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户权限
     * 
     * 请求示例：
     * DELETE /api/users/2/permissions
     * Content-Type: application/json
     * 
     * ["protocol_m15", "protocol_m18", "report_p1"]
     */
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:remove')")
    public Result<String> removeUserPermissions(
            @PathVariable Long id,
            @RequestBody List<String> permissionCodes) {
        try {
            // 检查用户是否存在
            SysUser user = userService.findById(id);
            if (user == null) {
                return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
            }
            
            // 检查是否是admin用户
            if ("admin".equals(user.getUsername())) {
                return Result.error(ErrorStatus.BAD_REQUEST, "不能删除admin用户的权限");
            }
            
            // 检查权限列表中是否包含admin权限
            if (permissionCodes != null && !permissionCodes.isEmpty()) {
                List<String> adminPermissions = new ArrayList<>();
                for (String permissionCode : permissionCodes) {
                    if (permissionCode.toLowerCase().contains("admin")) {
                        adminPermissions.add(permissionCode);
                    }
                }
                if (!adminPermissions.isEmpty()) {
                    return Result.error(ErrorStatus.BAD_REQUEST, "不能删除admin权限");
                }
            }
            
            if (permissionCodes == null || permissionCodes.isEmpty()) {
                return Result.error("权限列表不能为空");
            }
            
            // 调用服务层删除用户权限
            boolean success = userService.removeUserPermissions(id, permissionCodes);
            
            if (success) {
                return Result.success("删除用户权限成功");
            } else {
                return Result.error("删除用户权限失败");
            }
        } catch (Exception e) {
            return Result.error("删除用户权限异常: " + e.getMessage());
        }
    }
    
    /**
     * 导出用户权限配置
     * 
     * @param id 用户ID
     * @param format 导出格式：json或excel，默认json
     */
    @GetMapping("/{id}/permissions/export")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:export') or @securityService.isCurrentUser(#id)")
    public Object exportUserPermissions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "json") String format) {
        // 检查用户是否存在
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
        }
        
        // 获取用户的所有权限（包括直接分配和继承的）
        java.util.Map<String, java.util.List<String>> permissions = userService.getPermissionsByUserIdGrouped(id);
        
        if ("excel".equalsIgnoreCase(format)) {
            // 导出Excel格式
            byte[] excelBytes = ExcelUtils.exportUserPermissionsToExcel(
                    "用户权限配置", 
                    id.toString(), 
                    user.getUsername(), 
                    user.getRealName(), 
                    permissions);
            
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"user_permissions_" + id + ".xlsx\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } else {
            // 导出JSON格式
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("userId", id);
            exportData.put("username", user.getUsername());
            exportData.put("realName", user.getRealName());
            exportData.put("permissions", permissions);
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            
            return Result.success("导出用户权限成功", exportData);
        }
    }
    
    /**
     * 批量导出用户权限配置
     * 
     * @param userIds 用户ID列表（逗号分隔）
     * @param format 导出格式：json或excel，默认json
     */
    @GetMapping("/permissions/batch-export")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:export')")
    public Object batchExportUserPermissions(
            @RequestParam List<Long> userIds,
            @RequestParam(defaultValue = "json") String format) {
        
        if (userIds == null || userIds.isEmpty()) {
            return Result.error(ErrorStatus.BAD_REQUEST, "用户ID列表不能为空");
        }
        
        List<Map<String, Object>> allUserPermissions = new ArrayList<>();
        
        for (Long userId : userIds) {
            SysUser user = userService.findById(userId);
            if (user == null) {
                continue;
            }
            
            Map<String, List<String>> permissions = userService.getPermissionsByUserIdGrouped(userId);
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("username", user.getUsername());
            userData.put("realName", user.getRealName());
            userData.put("permissions", permissions);
            allUserPermissions.add(userData);
        }
        
        if ("excel".equalsIgnoreCase(format)) {
            byte[] excelBytes = ExcelUtils.exportBatchUserPermissionsToExcel("批量用户权限配置", allUserPermissions);
            
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"batch_user_permissions.xlsx\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } else {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("users", allUserPermissions);
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            
            return Result.success("批量导出用户权限成功", exportData);
        }
    }
    
    /**
     * 导入用户权限配置（JSON格式）
     * 
     * @param id 用户ID
     * @param permissionCodes 权限编码列表
     * @param mode 导入模式：OVERWRITE-覆盖导入（清空现有权限），APPEND-增量导入（保留现有权限，追加新权限），默认OVERWRITE
     */
    @PostMapping("/{id}/permissions/import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:import')")
    public Result<String> importUserPermissions(
            @PathVariable Long id, 
            @RequestBody List<String> permissionCodes,
            @RequestParam(defaultValue = "OVERWRITE") String mode) {
        return doImportUserPermissions(id, permissionCodes, mode);
    }
    
    /**
     * 导入用户权限配置（Excel格式）
     * 
     * @param id 用户ID
     * @param file Excel文件
     * @param mode 导入模式：OVERWRITE-覆盖导入（清空现有权限），APPEND-增量导入（保留现有权限，追加新权限），默认OVERWRITE
     */
    @PostMapping("/{id}/permissions/excel-import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:import')")
    public Result<String> importUserPermissionsFromExcel(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "OVERWRITE") String mode) {
        
        if (file.isEmpty()) {
            return Result.error(ErrorStatus.BAD_REQUEST, "请选择要上传的Excel文件");
        }
        
        try {
            // 从Excel文件中读取权限编码（使用用户权限专用的导入方法）
            List<String> permissionCodes = ExcelUtils.importUserPermissionsFromExcel(file.getInputStream());
            
            if (permissionCodes.isEmpty()) {
                return Result.error(ErrorStatus.BAD_REQUEST, "Excel文件中没有有效数据");
            }
            
            return doImportUserPermissions(id, permissionCodes, mode);
        } catch (Exception e) {
            return Result.error(ErrorStatus.BAD_REQUEST, "导入Excel失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行用户权限导入逻辑
     */
    private Result<String> doImportUserPermissions(Long id, List<String> permissionCodes, String mode) {
        // 检查用户是否存在
        SysUser user = userService.findById(id);
        if (user == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "用户不存在");
        }
        
        // 检查是否是admin用户
        if ("admin".equals(user.getUsername())) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能为admin用户导入权限");
        }
        
        // 检查权限列表中是否包含admin权限
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            List<String> adminPermissions = new ArrayList<>();
            for (String permissionCode : permissionCodes) {
                if (permissionCode.toLowerCase().contains("admin")) {
                    adminPermissions.add(permissionCode);
                }
            }
            if (!adminPermissions.isEmpty()) {
                return Result.error(ErrorStatus.BAD_REQUEST, "不能导入admin权限");
            }
        }
        
        // 根据导入模式执行不同操作
        if ("APPEND".equalsIgnoreCase(mode)) {
            // 增量导入：保留现有权限，追加新权限（去重）
            java.util.Map<String, java.util.List<String>> existingPermissions = userService.getPermissionsByUserIdGrouped(id);
            java.util.Set<String> existingPermissionCodes = new java.util.HashSet<>();
            for (java.util.List<String> perms : existingPermissions.values()) {
                existingPermissionCodes.addAll(perms);
            }
            
            // 过滤掉已存在的权限
            List<String> newPermissionCodes = permissionCodes.stream()
                    .filter(p -> !existingPermissionCodes.contains(p))
                    .collect(java.util.stream.Collectors.toList());
            
            if (!newPermissionCodes.isEmpty()) {
                userService.batchAssignDirectPermissions(id, newPermissionCodes);
            }
            return Result.success("增量导入用户权限成功");
        } else {
            // 覆盖导入：先清除用户直接分配的权限，再导入新权限
            userService.removeAllDirectPermissions(id);
            userService.batchAssignDirectPermissions(id, permissionCodes);
            return Result.success("覆盖导入用户权限成功");
        }
    }
}