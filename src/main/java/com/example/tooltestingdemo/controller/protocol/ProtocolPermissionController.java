package com.example.tooltestingdemo.controller.protocol;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO;
import com.example.tooltestingdemo.service.ProtocolPermService;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.vo.ProtocolPermissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 协议权限分配控制器
 */
@RestController
@RequestMapping("/api/protocol/permission")
@RequiredArgsConstructor
@Tag(name = "协议权限分配管理")
@Validated
public class ProtocolPermissionController {

    private final ProtocolPermService protocolPermService;
    private final IProtocolConfigService iProtocolConfigService;

    /**
     * 查询所有可分配的协议权限
     */
    @GetMapping("/assignable")
    @Operation(summary = "查询所有可分配的协议权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:view')")
    public Result<List<ProtocolPermissionVO.AssignablePermission>> getAssignablePermissions() {
        List<ProtocolPermissionVO.AssignablePermission> permissions = iProtocolConfigService.getAssignableProtocols();
        return Result.success(permissions);
    }

    /**
     * 查询用户权限信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户权限信息")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:view')")
    public Result<ProtocolPermissionVO.UserPermissionInfo> getUserPermissionInfo(@PathVariable Long userId) {
        ProtocolPermissionVO.UserPermissionInfo userPermissionInfo = protocolPermService.getUserPermissionInfo(userId);
        return Result.success(userPermissionInfo);
    }

    /**
     * 批量分配角色给用户
     */
    @PostMapping("/assign/role-to-user")
    @Operation(summary = "批量分配角色给用户")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:assign')")
    public Result<ProtocolPermissionVO.BatchAssignResult> batchAssignRoleToUser(
            @RequestBody @Valid ProtocolPermissionAssignDTO.BatchAssignRoleToUserDTO dto) {
        ProtocolPermissionVO.BatchAssignResult result = protocolPermService.batchAssignRoleToUser(dto);
        return Result.success("分配成功", result);
    }

    /**
     * 批量分配权限给角色
     */
    @PostMapping("/assign/permission-to-role")
    @Operation(summary = "批量分配权限给角色")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:assign')")
    public Result<ProtocolPermissionVO.BatchAssignResult> batchAssignPermissionToRole(
            @RequestBody @Valid ProtocolPermissionAssignDTO.BatchAssignPermissionToRoleDTO dto) {
        ProtocolPermissionVO.BatchAssignResult result = protocolPermService.batchAssignPermissionToRole(dto);
        return Result.success("分配成功", result);
    }

    /**
     * 批量直接分配权限给用户
     */
    @PostMapping("/assign/permission-to-user")
    @Operation(summary = "批量直接分配权限给用户")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:assign')")
    public Result<ProtocolPermissionVO.BatchAssignResult> batchAssignPermissionToUser(
            @RequestBody @Valid ProtocolPermissionAssignDTO.BatchAssignPermissionToUserDTO dto) {
        ProtocolPermissionVO.BatchAssignResult result = protocolPermService.batchAssignPermissionToUser(dto);
        return Result.success("分配成功", result);
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping("/remove/role-from-user")
    @Operation(summary = "移除用户角色")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:remove')")
    public Result<Void> removeRoleFromUser(@RequestBody @Valid ProtocolPermissionAssignDTO.RemoveRoleFromUserDTO dto) {
        protocolPermService.removeRoleFromUser(dto);
        return Result.success("移除成功");
    }

    /**
     * 移除角色权限
     */
    @DeleteMapping("/remove/permission-from-role")
    @Operation(summary = "移除角色权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:remove')")
    public Result<Void> removePermissionFromRole(@RequestBody @Valid ProtocolPermissionAssignDTO.RemovePermissionFromRoleDTO dto) {
        protocolPermService.removePermissionFromRole(dto);
        return Result.success("移除成功");
    }

    /**
     * 移除用户直接权限
     */
    @DeleteMapping("/remove/permission-from-user")
    @Operation(summary = "移除用户直接权限")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:remove')")
    public Result<Void> removePermissionFromUser(@RequestBody @Valid ProtocolPermissionAssignDTO.RemovePermissionFromUserDTO dto) {
        protocolPermService.removePermissionFromUser(dto);
        return Result.success("移除成功");
    }

    /**
     * 刷新协议权限缓存
     */
    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新协议权限缓存")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('protocol:permission:manage')")
    public Result<Void> refreshPermissionCache() {
        protocolPermService.refreshCache();
        return Result.success("缓存刷新成功");
    }
}