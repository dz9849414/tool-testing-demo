package com.example.tooltestingdemo.controller.system;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.system.BatchRemoveUserPermissionDTO;
import com.example.tooltestingdemo.dto.system.UserPermissionDTO;
import com.example.tooltestingdemo.service.system.IUserPermissionService;
import com.example.tooltestingdemo.vo.system.UserPermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户权限直接分配控制器
 */
@RestController
@RequestMapping("/api/system/user-permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户权限直接分配管理")
public class UserPermissionController {

    private final IUserPermissionService userPermissionService;

    @GetMapping
    @Operation(summary = "获取用户权限列表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:view')")
    public Result<List<UserPermissionVO>> getUserPermissions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String scopeId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            
            List<UserPermissionVO> permissions = userPermissionService.getUserPermissions(userId, scopeType, scopeId);
            return Result.success(permissions);
            
        } catch (Exception e) {
            return Result.error("获取用户权限列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "为用户分配权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:grant')")
    public Result<Boolean> grantPermission(@RequestBody UserPermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (dto.getPermissionId() == null || dto.getPermissionId().trim().isEmpty()) {
                return Result.error("权限ID不能为空");
            }
            if (dto.getPermissionCode() == null || dto.getPermissionCode().trim().isEmpty()) {
                return Result.error("权限编码不能为空");
            }
            
            Boolean result = userPermissionService.grantPermission(dto);
            return result ? Result.success(true) : Result.error("分配权限失败");
            
        } catch (Exception e) {
            return Result.error("分配权限失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch")
    @Operation(summary = "批量分配权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:batch')")
    public Result<Boolean> batchGrantPermissions(@RequestBody List<UserPermissionDTO> dtos) {
        try {
            if (dtos == null || dtos.isEmpty()) {
                return Result.error("权限列表不能为空");
            }
            
            // 参数校验
            for (UserPermissionDTO dto : dtos) {
                if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                    return Result.error("用户ID不能为空");
                }
                if (dto.getPermissionId() == null || dto.getPermissionId().trim().isEmpty()) {
                    return Result.error("权限ID不能为空");
                }
            }
            
            Boolean result = userPermissionService.batchGrantPermissions(dtos);
            return result ? Result.success(true) : Result.error("批量分配权限失败");
            
        } catch (Exception e) {
            return Result.error("批量分配权限失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:grant')")
    public Result<Boolean> updatePermission(@PathVariable String id, @RequestBody UserPermissionDTO dto) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Result.error("权限分配ID不能为空");
            }
            
            Boolean result = userPermissionService.updatePermission(id, dto);
            return result ? Result.success(true) : Result.error("更新权限失败");
            
        } catch (Exception e) {
            return Result.error("更新权限失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "撤销用户权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:revoke')")
    public Result<Boolean> revokePermission(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Result.error("权限分配ID不能为空");
            }
            
            Boolean result = userPermissionService.revokePermission(id);
            return result ? Result.success(true) : Result.error("撤销权限失败");
            
        } catch (Exception e) {
            return Result.error("撤销权限失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量撤销权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:revoke')")
    public Result<Boolean> batchRevokePermissions(@RequestBody List<String> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("权限分配ID列表不能为空");
            }
            
            Boolean result = userPermissionService.batchRevokePermissions(ids);
            return result ? Result.success(true) : Result.error("批量撤销权限失败");
            
        } catch (Exception e) {
            return Result.error("批量撤销权限失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-remove")
    @Operation(summary = "批量移除用户直接权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:remove')")
    public Result<Boolean> batchRemoveUserPermissions(@RequestBody BatchRemoveUserPermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
                return Result.error("权限ID列表不能为空");
            }
            
            // 验证用户和权限数量限制
            if (dto.getUserIds().size() > 100) {
                return Result.error("单次操作用户数量不能超过100个");
            }
            
            if (dto.getPermissionIds().size() > 100) {
                return Result.error("单次操作权限数量不能超过100个");
            }
            
            Boolean result = userPermissionService.batchRemoveUserPermissions(dto);
            return result ? Result.success( "批量移除用户直接权限成功") : Result.error("批量移除用户直接权限失败");
            
        } catch (Exception e) {
            log.error("批量移除用户直接权限失败", e);
            return Result.error("批量移除用户直接权限失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-remove/validate")
    @Operation(summary = "验证批量移除用户直接权限的可行性")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:remove')")
    public Result<Object> validateBatchRemoveUserPermissions(@RequestBody BatchRemoveUserPermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
                return Result.error("权限ID列表不能为空");
            }
            
            // 统计可移除的权限关联数量
            int removableCount = 0;
            StringBuilder validationDetails = new StringBuilder();
            
            for (String userId : dto.getUserIds()) {
                for (String permissionId : dto.getPermissionIds()) {
                    // 检查用户是否拥有该直接权限
                    Boolean hasPermission = userPermissionService.hasPermission(userId, permissionId, 
                            dto.getScopeType(), dto.getScopeId());
                    if (hasPermission) {
                        removableCount++;
                        validationDetails.append(String.format("用户[%s]拥有权限[%s]（作用域：%s/%s）\n", 
                                userId, permissionId, dto.getScopeType(), dto.getScopeId()));
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
                    String.format("可移除 %d 个用户直接权限关联", finalRemovableCount) : "没有可移除的用户直接权限关联";
                public String details = finalValidationDetails;
            });
            
        } catch (Exception e) {
            return Result.error("验证批量移除用户直接权限失败：" + e.getMessage());
        }
    }

    @GetMapping("/check")
    @Operation(summary = "检查用户是否拥有权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:view')")
    public Result<Boolean> hasPermission(
            @RequestParam String userId,
            @RequestParam String permissionCode,
            @RequestParam(defaultValue = "GLOBAL") String scopeType,
            @RequestParam(required = false) String scopeId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (permissionCode == null || permissionCode.trim().isEmpty()) {
                return Result.error("权限编码不能为空");
            }
            
            Boolean hasPermission = userPermissionService.hasPermission(userId, permissionCode, scopeType, scopeId);
            return Result.success(hasPermission);
            
        } catch (Exception e) {
            return Result.error("检查权限失败：" + e.getMessage());
        }
    }

    @GetMapping("/{userId}/effective")
    @Operation(summary = "获取用户所有有效权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:view')")
    public Result<List<String>> getUserEffectivePermissions(@PathVariable String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            
            List<String> permissions = userPermissionService.getUserEffectivePermissions(userId);
            return Result.success(permissions);
            
        } catch (Exception e) {
            return Result.error("获取用户有效权限失败：" + e.getMessage());
        }
    }

    @GetMapping("/history")
    @Operation(summary = "获取权限分配历史")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:user:permission:view')")
    public Result<List<Object>> getPermissionGrantHistory(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String permissionId,
            @RequestParam(required = false) String operationType) {
        try {
            List<Object> history = userPermissionService.getPermissionGrantHistory(userId, permissionId, operationType);
            return Result.success(history);
            
        } catch (Exception e) {
            return Result.error("获取权限分配历史失败：" + e.getMessage());
        }
    }
}