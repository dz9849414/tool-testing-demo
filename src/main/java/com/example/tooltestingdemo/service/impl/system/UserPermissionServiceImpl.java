package com.example.tooltestingdemo.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.system.UserPermissionDTO;
import com.example.tooltestingdemo.entity.SysPermission;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.system.SysUserPermission;
import com.example.tooltestingdemo.mapper.SysPermissionMapper;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import com.example.tooltestingdemo.mapper.system.SysUserPermissionMapper;
import com.example.tooltestingdemo.service.system.IUserPermissionService;
import com.example.tooltestingdemo.vo.system.UserPermissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户权限直接分配服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionServiceImpl extends ServiceImpl<SysUserPermissionMapper, SysUserPermission> implements IUserPermissionService {

    private final SysUserPermissionMapper userPermissionMapper;
    private final SysUserMapper userMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public List<UserPermissionVO> getUserPermissions(String userId, String scopeType, String scopeId) {
        try {
            LambdaQueryWrapper<SysUserPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserPermission::getUserId, userId);
            
            if (scopeType != null && !scopeType.trim().isEmpty()) {
                queryWrapper.eq(SysUserPermission::getScopeType, scopeType);
            }
            
            if (scopeId != null && !scopeId.trim().isEmpty()) {
                queryWrapper.eq(SysUserPermission::getScopeId, scopeId);
            }
            
            List<SysUserPermission> userPermissions = userPermissionMapper.selectList(queryWrapper);
            
            return userPermissions.stream().map(this::convertToVO).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取用户权限列表失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public Boolean grantPermission(UserPermissionDTO dto) {
        try {
            // 验证用户存在
            SysUser user = userMapper.selectById(dto.getUserId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证权限存在
            SysPermission permission = permissionMapper.selectById(dto.getPermissionId());
            if (permission == null) {
                throw new RuntimeException("权限不存在");
            }
            
            // 检查是否已存在相同权限分配
            LambdaQueryWrapper<SysUserPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserPermission::getUserId, dto.getUserId())
                       .eq(SysUserPermission::getPermissionId, dto.getPermissionId())
                       .eq(SysUserPermission::getScopeType, dto.getScopeType())
                       .eq(SysUserPermission::getScopeId, dto.getScopeId());
            
            SysUserPermission existing = userPermissionMapper.selectOne(queryWrapper);
            if (existing != null) {
                throw new RuntimeException("该用户已拥有此权限");
            }
            
            // 创建权限分配记录
            SysUserPermission userPermission = new SysUserPermission();
            BeanUtils.copyProperties(dto, userPermission);
            
            // 设置默认值
            userPermission.setPermissionCode(permission.getCode());
            userPermission.setCreateTime(LocalDateTime.now());
            userPermission.setUpdateTime(LocalDateTime.now());
            
            return userPermissionMapper.insert(userPermission) > 0;
            
        } catch (Exception e) {
            log.error("分配用户权限失败: userId={}, permissionId={}", dto.getUserId(), dto.getPermissionId(), e);
            throw new RuntimeException("分配用户权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Boolean batchGrantPermissions(List<UserPermissionDTO> dtos) {
        try {
            for (UserPermissionDTO dto : dtos) {
                Boolean result = grantPermission(dto);
                if (!result) {
                    throw new RuntimeException("批量分配权限失败");
                }
            }
            return true;
            
        } catch (Exception e) {
            log.error("批量分配权限失败", e);
            throw new RuntimeException("批量分配权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Boolean updatePermission(String id, UserPermissionDTO dto) {
        try {
            SysUserPermission userPermission = userPermissionMapper.selectById(id);
            if (userPermission == null) {
                throw new RuntimeException("权限分配记录不存在");
            }
            
            // 更新字段
            userPermission.setStatus(dto.getStatus());
            userPermission.setStartTime(dto.getStartTime());
            userPermission.setEndTime(dto.getEndTime());
            userPermission.setIsTemporary(dto.getIsTemporary());
            userPermission.setGrantReason(dto.getGrantReason());
            userPermission.setUpdateTime(LocalDateTime.now());
            
            return userPermissionMapper.updateById(userPermission) > 0;
            
        } catch (Exception e) {
            log.error("更新用户权限失败: id={}", id, e);
            throw new RuntimeException("更新用户权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Boolean revokePermission(String id) {
        try {
            SysUserPermission userPermission = userPermissionMapper.selectById(id);
            if (userPermission == null) {
                throw new RuntimeException("权限分配记录不存在");
            }
            
            return userPermissionMapper.deleteById(id) > 0;
            
        } catch (Exception e) {
            log.error("撤销用户权限失败: id={}", id, e);
            throw new RuntimeException("撤销用户权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Boolean batchRevokePermissions(List<String> ids) {
        try {
            for (String id : ids) {
                Boolean result = revokePermission(id);
                if (!result) {
                    throw new RuntimeException("批量撤销权限失败");
                }
            }
            return true;
            
        } catch (Exception e) {
            log.error("批量撤销权限失败", e);
            throw new RuntimeException("批量撤销权限失败: " + e.getMessage());
        }
    }

    @Override
    public Boolean hasPermission(String userId, String permissionCode, String scopeType, String scopeId) {
        try {
            LambdaQueryWrapper<SysUserPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserPermission::getUserId, userId)
                       .eq(SysUserPermission::getPermissionCode, permissionCode)
                       .eq(SysUserPermission::getScopeType, scopeType)
                       .eq(SysUserPermission::getScopeId, scopeId)
                       .eq(SysUserPermission::getStatus, 1);
            
            SysUserPermission userPermission = userPermissionMapper.selectOne(queryWrapper);
            
            if (userPermission == null) {
                return false;
            }
            
            // 检查权限是否在有效期内
            LocalDateTime now = LocalDateTime.now();
            if (userPermission.getStartTime() != null && now.isBefore(userPermission.getStartTime())) {
                return false; // 权限尚未生效
            }
            
            if (userPermission.getEndTime() != null && now.isAfter(userPermission.getEndTime())) {
                return false; // 权限已过期
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查用户权限失败: userId={}, permissionCode={}", userId, permissionCode, e);
            return false;
        }
    }

    @Override
    public List<String> getUserEffectivePermissions(String userId) {
        try {
            LambdaQueryWrapper<SysUserPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUserPermission::getUserId, userId)
                       .eq(SysUserPermission::getStatus, 1);
            
            List<SysUserPermission> userPermissions = userPermissionMapper.selectList(queryWrapper);
            
            LocalDateTime now = LocalDateTime.now();
            
            return userPermissions.stream()
                .filter(permission -> {
                    // 检查权限是否在有效期内
                    if (permission.getStartTime() != null && now.isBefore(permission.getStartTime())) {
                        return false;
                    }
                    if (permission.getEndTime() != null && now.isAfter(permission.getEndTime())) {
                        return false;
                    }
                    return true;
                })
                .map(SysUserPermission::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("获取用户有效权限失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Object> getPermissionGrantHistory(String userId, String permissionId, String operationType) {
        // 这里简化实现，实际应该查询权限分配历史表
        // 返回模拟数据
        return List.of(
            new Object() {
                public String operationType = "GRANT";
                public String operatorName = "admin";
                public LocalDateTime operationTime = LocalDateTime.now();
                public String operationReason = "项目需要";
            }
        );
    }

    /**
     * 转换为VO对象
     */
    private UserPermissionVO convertToVO(SysUserPermission userPermission) {
        UserPermissionVO vo = new UserPermissionVO();
        BeanUtils.copyProperties(userPermission, vo);
        
        // 设置用户信息
        SysUser user = userMapper.selectById(userPermission.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        
        // 设置权限信息
        SysPermission permission = permissionMapper.selectById(userPermission.getPermissionId());
        if (permission != null) {
            vo.setPermissionName(permission.getName());
        }
        
        // 设置授权人信息
        if (userPermission.getGrantUserId() != null) {
            SysUser grantUser = userMapper.selectById(userPermission.getGrantUserId());
            if (grantUser != null) {
                vo.setGrantUserName(grantUser.getRealName());
            }
        }
        
        // 检查是否过期
        LocalDateTime now = LocalDateTime.now();
        boolean expired = userPermission.getEndTime() != null && now.isAfter(userPermission.getEndTime());
        vo.setExpired(expired);
        
        // 检查是否有效
        boolean valid = userPermission.getStatus() == 1 && !expired;
        if (userPermission.getStartTime() != null && now.isBefore(userPermission.getStartTime())) {
            valid = false; // 权限尚未生效
        }
        vo.setValid(valid);
        
        return vo;
    }
}