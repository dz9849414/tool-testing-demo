package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.dto.RoleBatchPermissionDTO;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.mapper.SysPermissionMapper;
import com.example.tooltestingdemo.service.SysRoleService;
import com.example.tooltestingdemo.service.SysUserService;
import com.example.tooltestingdemo.util.ExcelUtils;
import lombok.RequiredArgsConstructor;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.dto.SysRoleDTO;
import com.example.tooltestingdemo.enums.RoleEnum;
import com.example.tooltestingdemo.util.IdGenerator;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

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
    private final SysPermissionMapper permissionMapper;
    
    /**
     * 分页获取角色列表（支持模糊查询、状态筛选、日期范围和排序）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<Page<SysRole>> getRolesByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,
            @RequestParam(defaultValue = "createTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        Page<SysRole> pageParam = new Page<>(pageNum, pageSize);
        Page<SysRole> roles = roleService.getRolesByPageWithSearch(pageParam, name, description, status, beginTime, endTime, sortField, sortOrder);
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 分页获取角色列表（支持模糊查询）
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
            // 如果id没传入，系统自动生成：获取当前最大ID + 1
            Long maxId = roleService.getMaxRoleId();
            role.setId(String.valueOf(maxId + 1));
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
        if (RoleEnum.ADMIN.getCode().equals(id)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能修改admin角色");
        }
        
        // 检查角色是否存在
        SysRole existingRole = roleService.getById(id);
        if (existingRole == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
        }
        
        SysRole role = new SysRole();
        roleDTO.setId(id);
        
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
     * 批量分配权限给角色
     * 
     * 请求示例：
     * {
     *   "roleIds": ["1009"],
     *   "permissions": ["protocol_m3", "protocol_m6"]
     * }
     */
    @PostMapping("/batch/permissions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:api')")
    public Result<String> batchAssignPermissions(@RequestBody RoleBatchPermissionDTO dto) {
        try {
            // 参数校验
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                return Result.error("角色ID列表不能为空");
            }
            
            if (dto.getPermissions() == null || dto.getPermissions().isEmpty()) {
                return Result.error("权限ID列表不能为空");
            }
            
            // 检查角色是否存在
            for (String roleId : dto.getRoleIds()) {
                // 检查是否是admin角色
                if ("admin".equals(roleId)) {
                    return Result.error(ErrorStatus.BAD_REQUEST, "不能为admin角色分配权限");
                }
                
                SysRole role = roleService.getById(roleId);
                if (role == null) {
                    return Result.error("角色不存在，角色ID: " + roleId);
                }
            }
            
            // 检查权限列表中是否包含admin权限
            if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
                List<String> adminPermissions = new ArrayList<>();
                for (String permissionId : dto.getPermissions()) {
                    if (permissionId.toLowerCase().contains("admin")) {
                        adminPermissions.add(permissionId);
                    }
                }
                if (!adminPermissions.isEmpty()) {
                    return Result.error(ErrorStatus.BAD_REQUEST, "不能分配admin权限");
                }
            }
            
            // 调用服务层进行批量权限分配
            boolean success = roleService.batchAssignPermissions(dto.getRoleIds(), dto.getPermissions(), dto.getOperationType());
            
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
        
        // 如果要禁用角色且包含admin，直接返回错误
        if (status == 0 && roleIds.contains("admin")) {
            return Result.error("admin角色为系统内置角色，不能被禁用");
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
        
        // 查询角色关联的用户列表（不传username参数，保持原有功能）
        List<SysUser> users = userService.findByRoleId(roleId, null);
        return Result.success("获取角色关联用户列表成功", users);
    }
    
    /**
     * 导出角色权限配置
     * 
     * @param roleId 角色ID
     * @param format 导出格式：json或excel，默认json
     */
    @GetMapping("/{roleId}/permissions/export")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:permission:export')")
    public Object exportRolePermissions(
            @PathVariable String roleId,
            @RequestParam(defaultValue = "json") String format) {
        // 检查角色是否存在
        SysRole role = roleService.getById(roleId);
        if (role == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
        }
        
        // 获取角色的所有权限
        List<SysPermission> permissions = roleService.getPermissionsByRoleId(roleId);
        
        if ("excel".equalsIgnoreCase(format)) {
            // 导出Excel格式
            List<Map<String, Object>> permissionList = new ArrayList<>();
            for (SysPermission permission : permissions) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", permission.getId());
                map.put("name", permission.getName());
                map.put("code", permission.getCode());
                map.put("description", permission.getDescription());
                map.put("module", permission.getModule());
                permissionList.add(map);
            }
            
            byte[] excelBytes = ExcelUtils.exportRolePermissionsToExcel(
                    "角色权限配置", roleId, role.getName(), permissionList);
            
            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"role_permissions_" + roleId + ".xlsx\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);
        } else {
            // 导出JSON格式
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("roleId", roleId);
            exportData.put("roleName", role.getName());
            exportData.put("permissions", permissions);
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            
            return Result.success("导出角色权限成功", exportData);
        }
    }
    
    /**
     * 导入角色权限配置（JSON格式）
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @param mode 导入模式：OVERWRITE-覆盖导入（清空现有权限），APPEND-增量导入（保留现有权限，追加新权限），默认OVERWRITE
     */
    @PostMapping("/{roleId}/permissions/import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:permission:import')")
    public Result<String> importRolePermissions(
            @PathVariable String roleId, 
            @RequestBody List<String> permissionIds,
            @RequestParam(defaultValue = "OVERWRITE") String mode) {
        return doImportRolePermissions(roleId, permissionIds, mode);
    }
    
    /**
     * 导入角色权限配置（Excel格式）
     * 
     * @param roleId 角色ID
     * @param file Excel文件
     * @param mode 导入模式：OVERWRITE-覆盖导入（清空现有权限），APPEND-增量导入（保留现有权限，追加新权限），默认OVERWRITE
     */
    @PostMapping("/{roleId}/permissions/excel-import")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:role:permission:import')")
    public Result<String> importRolePermissionsFromExcel(
            @PathVariable String roleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "OVERWRITE") String mode) {
        
        if (file.isEmpty()) {
            return Result.error(ErrorStatus.BAD_REQUEST, "请选择要上传的Excel文件");
        }
        
        try {
            // 从Excel文件中读取权限编码
            List<String> permissionCodes = ExcelUtils.importPermissionsFromExcel(file.getInputStream());
            
            if (permissionCodes.isEmpty()) {
                return Result.error(ErrorStatus.BAD_REQUEST, "Excel文件中没有有效数据");
            }
            
            // 根据权限编码获取权限ID
            List<String> permissionIds = new ArrayList<>();
            for (String code : permissionCodes) {
                SysPermission permission = permissionMapper.selectByCode(code);
                if (permission != null) {
                    permissionIds.add(permission.getId());
                }
            }
            
            return doImportRolePermissions(roleId, permissionIds, mode);
        } catch (Exception e) {
            return Result.error(ErrorStatus.BAD_REQUEST, "导入Excel失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行角色权限导入逻辑
     */
    private Result<String> doImportRolePermissions(String roleId, List<String> permissionIds, String mode) {
        // 检查是否是admin角色
        if ("admin".equals(roleId)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "不能为admin角色导入权限");
        }
        
        // 检查角色是否存在
        SysRole role = roleService.getById(roleId);
        if (role == null) {
            return Result.error(ErrorStatus.NOT_FOUND, "角色不存在");
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
                return Result.error(ErrorStatus.BAD_REQUEST, "不能导入admin权限");
            }
        }
        
        // 根据导入模式执行不同操作
        if ("APPEND".equalsIgnoreCase(mode)) {
            // 增量导入：保留现有权限，追加新权限（去重）
            List<SysPermission> existingPermissions = roleService.getPermissionsByRoleId(roleId);
            List<String> existingPermissionIds = existingPermissions.stream()
                    .map(SysPermission::getId)
                    .collect(java.util.stream.Collectors.toList());
            
            // 过滤掉已存在的权限
            List<String> newPermissionIds = permissionIds.stream()
                    .filter(p -> !existingPermissionIds.contains(p))
                    .collect(java.util.stream.Collectors.toList());
            
            if (!newPermissionIds.isEmpty()) {
                // 使用批量添加模式
                roleService.batchAssignPermissions(java.util.Arrays.asList(roleId), newPermissionIds, "ADD");
            }
            return Result.success("增量导入角色权限成功");
        } else {
            // 覆盖导入：先清除角色现有权限，再导入新权限
            roleService.removeAllPermissions(roleId);
            roleService.assignPermissions(roleId, permissionIds);
            return Result.success("覆盖导入角色权限成功");
        }
    }
}