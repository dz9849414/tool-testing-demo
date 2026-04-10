package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.SysUserRole;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import com.example.tooltestingdemo.mapper.SysUserRoleMapper;
import com.example.tooltestingdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {
    
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public SysUser findById(String id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public SysUser findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    @Override
    public SysUser findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }
    
    @Override
    public List<SysUser> findAll() {
        return userMapper.selectList(new QueryWrapper<>());
    }
    
    @Override
    public Page<SysUser> findAll(Page<SysUser> page) {
        return userMapper.selectPage(page, new QueryWrapper<>());
    }
    
    @Override
    @Transactional
    public SysUser save(SysUser user) {
        userMapper.insert(user);
        return user;
    }
    
    @Override
    @Transactional
    public SysUser update(SysUser user) {
        SysUser existingUser = userMapper.selectById(user.getId());
        if (existingUser != null) {
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setRealName(user.getRealName());
            existingUser.setOrganizationId(user.getOrganizationId());
            existingUser.setSource(user.getSource());
            userMapper.updateById(existingUser);
            return existingUser;
        }
        return null;
    }
    
    @Override
    @Transactional
    public void deleteById(String id) {
        userMapper.deleteById(id);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userMapper.countByEmail(email) > 0;
    }
    
    @Override
    public List<SysUser> findByStatus(Integer status) {
        return userMapper.selectByStatus(status);
    }
    
    @Override
    public List<SysUser> findByRoleId(String roleId) {
        return userMapper.selectByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void updateLastLoginInfo(String userId, String ipAddress) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional
    public void updateUserStatusWithApproval(String userId, Integer status, String approverId) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            user.setApproverId(approverId);
            user.setApproveTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return true;
    }

    @Override
    public List<String> getRolesByUserId(String userId) {
        return userMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds, String operatorId) {
        // 先删除用户现有的所有角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 然后添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setId(java.util.UUID.randomUUID().toString().replace("-", "_"));
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(java.time.LocalDateTime.now());
                userRole.setCreateUser(operatorId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public List<String> getPermissionsByUserId(String userId) {
        return userMapper.selectPermissionsByUserId(userId);
    }
}