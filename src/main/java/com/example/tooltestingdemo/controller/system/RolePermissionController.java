package com.example.tooltestingdemo.controller.system;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.system.BatchRemovePermissionDTO;
import com.example.tooltestingdemo.dto.system.BatchRemoveUserRoleDTO;
import com.example.tooltestingdemo.service.system.IRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 角色权限管理控制器
 */
@RestController
@RequestMapping("/api/system/role-permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "角色权限批量管理")
public class RolePermissionController {

    private final IRolePermissionService rolePermissionService;

    @PostMapping("/batch-remove-permissions")
    @Operation(summary = "批量从角色移除权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:permission:remove')")
    public Result<Boolean> batchRemovePermissionsFromRole(@RequestBody BatchRemovePermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                return Result.error("角色ID列表不能为空");
            }
            
            if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
                return Result.error("权限ID列表不能为空");
            }
            
            // 验证角色和权限数量限制
            if (dto.getRoleIds().size() > 100) {
                return Result.error("单次操作角色数量不能超过100个");
            }
            
            if (dto.getPermissionIds().size() > 100) {
                return Result.error("单次操作权限数量不能超过100个");
            }
            
            Boolean result = rolePermissionService.batchRemovePermissionsFromRole(dto);
            return result ? Result.success("批量移除权限成功") : Result.error("批量移除权限失败");
            
        } catch (Exception e) {
            log.error("批量移除权限失败", e);
            return Result.error("批量移除权限失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-remove-roles")
    @Operation(summary = "批量从用户移除角色")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:role:remove')")
    public Result<Boolean> batchRemoveRolesFromUser(@RequestBody BatchRemoveUserRoleDTO dto) {
        try {
            // 参数校验
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                return Result.error("角色ID列表不能为空");
            }
            
            // 验证用户和角色数量限制
            if (dto.getUserIds().size() > 100) {
                return Result.error("单次操作用户数量不能超过100个");
            }
            
            if (dto.getRoleIds().size() > 100) {
                return Result.error("单次操作角色数量不能超过100个");
            }
            
            Boolean result = rolePermissionService.batchRemoveRolesFromUser(dto);
            return result ? Result.success( "批量移除角色成功") : Result.error("批量移除角色失败");
            
        } catch (Exception e) {
            log.error("批量移除角色失败", e);
            return Result.error("批量移除角色失败：" + e.getMessage());
        }
    }

    @GetMapping("/check/role-permission")
    @Operation(summary = "检查角色是否拥有权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:permission:view')")
    public Result<Boolean> roleHasPermission(
            @RequestParam String roleId,
            @RequestParam String permissionId) {
        try {
            if (roleId == null || roleId.trim().isEmpty()) {
                return Result.error("角色ID不能为空");
            }
            
            if (permissionId == null || permissionId.trim().isEmpty()) {
                return Result.error("权限ID不能为空");
            }
            
            Boolean hasPermission = rolePermissionService.roleHasPermission(roleId, permissionId);
            return Result.success(hasPermission);
            
        } catch (Exception e) {
            return Result.error("检查角色权限失败：" + e.getMessage());
        }
    }

    @GetMapping("/check/user-role")
    @Operation(summary = "检查用户是否拥有角色")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:role:view')")
    public Result<Boolean> userHasRole(
            @RequestParam String userId,
            @RequestParam String roleId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            
            if (roleId == null || roleId.trim().isEmpty()) {
                return Result.error("角色ID不能为空");
            }
            
            Boolean hasRole = rolePermissionService.userHasRole(userId, roleId);
            return Result.success(hasRole);
            
        } catch (Exception e) {
            return Result.error("检查用户角色失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-remove-permissions/validate")
    @Operation(summary = "验证批量移除权限的可行性")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:permission:remove')")
    public Result<Object> validateBatchRemovePermissions(@RequestBody BatchRemovePermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                return Result.error("角色ID列表不能为空");
            }
            
            if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
                return Result.error("权限ID列表不能为空");
            }
            
            // 统计可移除的权限关联数量
            int removableCount = 0;
            StringBuilder validationDetails = new StringBuilder();
            
            for (String roleId : dto.getRoleIds()) {
                for (String permissionId : dto.getPermissionIds()) {
                    Boolean hasPermission = rolePermissionService.roleHasPermission(roleId, permissionId);
                    if (hasPermission) {
                        removableCount++;
                        validationDetails.append(String.format("角色[%s]拥有权限[%s]\n", roleId, permissionId));
                    }
                }
            }
            
            // 返回验证结果
            final int finalRemovableCount = removableCount;
            final String finalValidationDetails = validationDetails.toString();
            
            return Result.success(new Object() {
                public Boolean valid = finalRemovableCount > 0;
                public Integer removableCount = finalRemovableCount;
                public String message = finalRemovableCount > 0 ? 
                    String.format("可移除 %d 个权限关联", finalRemovableCount) : "没有可移除的权限关联";
                public String details = finalValidationDetails;
            });
            
        } catch (Exception e) {
            return Result.error("验证批量移除权限失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-remove-roles/validate")
    @Operation(summary = "验证批量移除角色的可行性")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:role:remove')")
    public Result<Object> validateBatchRemoveRoles(@RequestBody BatchRemoveUserRoleDTO dto) {
        try {
            // 参数校验
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                return Result.error("角色ID列表不能为空");
            }
            
            // 统计可移除的角色关联数量
            int removableCount = 0;
            StringBuilder validationDetails = new StringBuilder();
            
            for (String userId : dto.getUserIds()) {
                for (String roleId : dto.getRoleIds()) {
                    Boolean hasRole = rolePermissionService.userHasRole(userId, roleId);
                    if (hasRole) {
                        removableCount++;
                        validationDetails.append(String.format("用户[%s]拥有角色[%s]\n", userId, roleId));
                    }
                }
            }
            
            // 返回验证结果
            final int finalRemovableCount = removableCount;
            final String finalValidationDetails = validationDetails.toString();
            
            return Result.success(new Object() {
                public Boolean valid = finalRemovableCount > 0;
                public Integer removableCount = finalRemovableCount;
                public String message = finalRemovableCount > 0 ? 
                    String.format("可移除 %d 个角色关联", finalRemovableCount) : "没有可移除的角色关联";
                public String details = finalValidationDetails;
            });
            
        } catch (Exception e) {
            return Result.error("验证批量移除角色失败：" + e.getMessage());
        }
    }
}