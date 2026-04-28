package com.example.tooltestingdemo.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tooltestingdemo.dto.system.BatchRemovePermissionDTO;
import com.example.tooltestingdemo.dto.system.BatchRemoveUserRoleDTO;
import com.example.tooltestingdemo.entity.SysRole;
import com.example.tooltestingdemo.entity.SysRolePermission;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.SysUserRole;
import com.example.tooltestingdemo.mapper.SysRoleMapper;
import com.example.tooltestingdemo.mapper.SysRolePermissionMapper;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import com.example.tooltestingdemo.mapper.SysUserRoleMapper;
import com.example.tooltestingdemo.service.system.IRolePermissionService;
import com.example.tooltestingdemo.vo.system.BatchRemoveResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色权限管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionServiceImpl implements IRolePermissionService {

    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public Boolean batchRemovePermissionsFromRole(BatchRemovePermissionDTO dto) {
        try {
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                throw new RuntimeException("角色ID列表不能为空");
            }
            
            if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
                throw new RuntimeException("权限ID列表不能为空");
            }
            
            int totalRemoved = 0;
            
            // 遍历所有角色和权限组合
            for (String roleId : dto.getRoleIds()) {
                // 验证角色存在
                SysRole role = roleMapper.selectById(roleId);
                if (role == null) {
                    log.warn("角色不存在: roleId={}", roleId);
                    continue;
                }
                
                for (String permissionId : dto.getPermissionIds()) {
                    try {
                        // 检查角色是否拥有该权限
                        LambdaQueryWrapper<SysRolePermission> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(SysRolePermission::getRoleId, roleId)
                                   .eq(SysRolePermission::getPermissionId, permissionId);
                        
                        SysRolePermission rolePermission = rolePermissionMapper.selectOne(queryWrapper);
                        if (rolePermission != null) {
                            // 移除权限
                            int deleted = rolePermissionMapper.delete(queryWrapper);
                            if (deleted > 0) {
                                totalRemoved++;
                                log.info("成功从角色移除权限: roleId={}, permissionId={}", roleId, permissionId);
                                
                                // 记录操作历史（可选）
                                recordPermissionRemoveHistory(roleId, permissionId, dto.getRemoveReason(), dto.getOperatorId());
                            }
                        } else {
                            log.info("角色未拥有该权限，无需移除: roleId={}, permissionId={}", roleId, permissionId);
                        }
                        
                    } catch (Exception e) {
                        log.error("移除角色权限失败: roleId={}, permissionId={}", roleId, permissionId, e);
                        // 继续处理其他权限，不中断整个批量操作
                    }
                }
            }
            
            log.info("批量移除权限完成: 共移除 {} 个权限关联", totalRemoved);
            return totalRemoved > 0;
            
        } catch (Exception e) {
            log.error("批量移除权限失败", e);
            throw new RuntimeException("批量移除权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public BatchRemoveResult batchRemoveRolesFromUser(BatchRemoveUserRoleDTO dto) {
        BatchRemoveResult result = new BatchRemoveResult();
        List<String> failureReasons = new ArrayList<>();
        int totalRemoved = 0;
        int totalProcessed = 0;
        
        try {
            if (dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
                throw new RuntimeException("用户ID列表不能为空");
            }
            
            if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
                throw new RuntimeException("角色ID列表不能为空");
            }
            
            // 遍历所有用户和角色组合
            for (String userId : dto.getUserIds()) {
                // 验证用户存在
                SysUser user = userMapper.selectById(userId);
                if (user == null) {
                    String reason = String.format("用户不存在: userId=%s", userId);
                    failureReasons.add(reason);
                    log.warn(reason);
                    continue;
                }
                
                for (String roleId : dto.getRoleIds()) {
                    totalProcessed++;
                    try {
                        // 检查用户是否拥有该角色
                        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(SysUserRole::getUserId, userId)
                                   .eq(SysUserRole::getRoleId, roleId);
                        
                        SysUserRole userRole = userRoleMapper.selectOne(queryWrapper);
                        if (userRole != null) {
                            // 移除角色
                            int deleted = userRoleMapper.delete(queryWrapper);
                            if (deleted > 0) {
                                totalRemoved++;
                                log.info("成功从用户移除角色: userId={}, roleId={}", userId, roleId);
                                
                                // 记录操作历史（可选）
                                recordRoleRemoveHistory(userId, roleId, dto.getRemoveReason(), dto.getOperatorId());
                            } else {
                                String reason = String.format("删除数据库记录失败: userId=%s, roleId=%s", userId, roleId);
                                failureReasons.add(reason);
                                log.error(reason);
                            }
                        } else {
                            String reason = String.format("用户未拥有该角色: userId=%s, roleId=%s", userId, roleId);
                            failureReasons.add(reason);
                            log.info(reason);
                        }
                        
                    } catch (Exception e) {
                        String reason = String.format("移除用户角色失败: userId=%s, roleId=%s, error=%s", 
                                userId, roleId, e.getMessage());
                        failureReasons.add(reason);
                        log.error(reason, e);
                    }
                }
            }
            
            // 设置结果
            result.setSuccess(totalRemoved > 0);
            result.setRemovedCount(totalRemoved);
            result.setProcessedCount(totalProcessed);
            result.setFailureReasons(failureReasons);
            result.setMessage(String.format("批量移除角色完成: 共处理 %d 个用户角色关联，成功移除 %d 个", 
                    totalProcessed, totalRemoved));
            
            log.info("批量移除角色完成: {}", result.getMessage());
            return result;
            
        } catch (Exception e) {
            log.error("批量移除角色失败", e);
            result.setSuccess(false);
            result.setRemovedCount(0);
            result.setProcessedCount(totalProcessed);
            result.setFailureReasons(List.of("批量操作失败: " + e.getMessage()));
            result.setMessage("批量移除角色失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Boolean roleHasPermission(String roleId, String permissionId) {
        try {
            LambdaQueryWrapper<SysRolePermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysRolePermission::getRoleId, roleId)
                       .eq(SysRolePermission::getPermissionId, permissionId);
            
            SysRolePermission rolePermission = rolePermissionMapper.selectOne(queryWrapper);
            return rolePermission != null;
            
        } catch (Exception e) {
            log.error("检查角色权限失败: roleId={}, permissionId={}", roleId, permissionId, e);
            return false;
        }
    }

    @Override
    public Boolean userHasRole(String userId, String roleId) {
        try {
            LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserRole::getUserId, userId)
                       .eq(SysUserRole::getRoleId, roleId);
            
            SysUserRole userRole = userRoleMapper.selectOne(queryWrapper);
            return userRole != null;
            
        } catch (Exception e) {
            log.error("检查用户角色失败: userId={}, roleId={}", userId, roleId, e);
            return false;
        }
    }

    /**
     * 记录权限移除历史（可选实现）
     */
    private void recordPermissionRemoveHistory(String roleId, String permissionId, String reason, String operatorId) {
        // 这里可以记录到操作日志表或权限分配历史表
        log.info("记录权限移除历史: roleId={}, permissionId={}, reason={}, operator={}", 
                roleId, permissionId, reason, operatorId);
        
        // 实际实现可以调用日志服务记录详细操作历史
        // operationLogService.logOperation("REMOVE_PERMISSION_FROM_ROLE", ...);
    }

    /**
     * 记录角色移除历史（可选实现）
     */
    private void recordRoleRemoveHistory(String userId, String roleId, String reason, String operatorId) {
        // 这里可以记录到操作日志表或用户角色历史表
        log.info("记录角色移除历史: userId={}, roleId={}, reason={}, operator={}", 
                userId, roleId, reason, operatorId);
        
        // 实际实现可以调用日志服务记录详细操作历史
        // operationLogService.logOperation("REMOVE_ROLE_FROM_USER", ...);
    }
}