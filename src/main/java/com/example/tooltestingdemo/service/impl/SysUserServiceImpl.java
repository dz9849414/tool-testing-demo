package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    public SysUser findById(Long id) {
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            return null;
        }
        
        // 如果是查询自己的信息，直接返回
        if (currentUser.getId().equals(id)) {
            return userMapper.selectById(id);
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 获取目标用户信息
        SysUser targetUser = userMapper.selectById(id);
        if (targetUser == null) {
            return null;
        }
        
        // 获取目标用户的角色列表
        List<String> targetRoles = userMapper.selectRolesByUserId(targetUser.getId());
        
        // 检查当前用户是否有权限查看目标用户
        if (canViewUser(currentRoles, targetRoles)) {
            return targetUser;
        }
        
        // 没有权限，返回null
        return null;
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
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            return new java.util.ArrayList<>();
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行查询，过滤掉已逻辑删除的用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getIsDeleted, 0);
        List<SysUser> users = userMapper.selectList(queryWrapper);
        
        // 根据角色权限过滤结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : users) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        return filteredUsers;
    }
    
    @Override
    public Page<SysUser> findAll(Page<SysUser> page) {
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            return new Page<>();
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行分页查询，过滤掉已逻辑删除的用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getIsDeleted, 0);
        Page<SysUser> usersPage = userMapper.selectPage(page, queryWrapper);
        
        // 根据角色权限过滤结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : usersPage.getRecords()) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        // 创建新的分页结果
        Page<SysUser> filteredPage = new Page<>(page.getCurrent(), page.getSize(), usersPage.getTotal());
        filteredPage.setRecords(filteredUsers);
        
        return filteredPage;
    }
    
    @Override
    @Transactional
    public SysUser save(SysUser user) {
        // 设置创建人ID（当前登录用户）
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            SysUser currentUser = userMapper.selectByUsername(currentUsername);
            if (currentUser != null) {
                user.setCreateId(currentUser.getId());
            }
        }
        
        // 设置默认值
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(0);
        }
        
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
            existingUser.setStatus(user.getStatus());
            existingUser.setSource(user.getSource());
            
            // 设置更新人ID（当前登录用户）
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String currentUsername = authentication.getName();
                SysUser currentUser = userMapper.selectByUsername(currentUsername);
                if (currentUser != null) {
                    existingUser.setUpdateId(currentUser.getId());
                }
            }
            
            // 如果密码不为空，则更新密码并编码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            userMapper.updateById(existingUser);
            return existingUser;
        }
        return null;
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        // 逻辑删除：设置删除标记和删除信息
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setIsDeleted(1);
            
            // 设置删除人ID（当前登录用户）
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String currentUsername = authentication.getName();
                SysUser currentUser = userMapper.selectByUsername(currentUsername);
                if (currentUser != null) {
                    user.setDeletedBy(currentUser.getId());
                }
            }
            
            // 设置删除时间
            user.setDeletedTime(java.time.LocalDateTime.now());
            
            userMapper.deleteById(user);
        }
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
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            return new java.util.ArrayList<>();
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行查询，过滤掉已逻辑删除的用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, status);
        queryWrapper.eq(SysUser::getIsDeleted, 0);
        List<SysUser> users = userMapper.selectList(queryWrapper);
        
        // 根据角色权限过滤结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : users) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        return filteredUsers;
    }
    
    @Override
    public Page<SysUser> findByStatus(Page<SysUser> page, Integer status) {
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            Page<SysUser> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
            emptyPage.setRecords(new java.util.ArrayList<>());
            return emptyPage;
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行分页查询
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, status);
        Page<SysUser> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 根据角色权限过滤结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : userPage.getRecords()) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        // 创建新的分页结果
        Page<SysUser> filteredPage = new Page<>(page.getCurrent(), page.getSize(), userPage.getTotal());
        filteredPage.setRecords(filteredUsers);
        return filteredPage;
    }
    
    @Override
    public List<SysUser> findByRoleId(String roleId) {
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            return new java.util.ArrayList<>();
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行查询
        List<SysUser> users = userMapper.selectByRoleId(roleId);
        
        // 根据角色权限过滤结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : users) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        return filteredUsers;
    }
    
    @Override
    @Transactional
    public void updateLastLoginInfo(Long userId, String ipAddress) {
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional
    public void updateUserStatusWithApproval(Long userId, Integer status, Long approverId) {
        SysUser user = userMapper.selectById(userId);
        
        user.setStatus(status);
        user.setApproverId(approverId != null ? approverId.toString() : null);
        user.setApproveTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
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
    public List<String> getRolesByUserId(Long userId) {
        return userMapper.selectRolesByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<String> roleIds, Long operatorId) {
        // 先删除用户现有的所有角色关联
        userRoleMapper.deleteByUserId(userId.toString());
        
        // 然后添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setId(java.util.UUID.randomUUID().toString().replace("-", "_"));
                userRole.setUserId(userId.toString());
                userRole.setRoleId(roleId);
                userRole.setCreateTime(java.time.LocalDateTime.now());
                userRole.setCreateUser(operatorId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public List<String> getPermissionsByUserId(Long userId) {
        return userMapper.selectPermissionsByUserId(userId);
    }

    @Override
    public java.util.Map<String, java.util.List<String>> getPermissionsByUserIdGrouped(Long userId) {
        List<String> permissions = getPermissionsByUserId(userId);
        java.util.Map<String, java.util.List<String>> groupedPermissions = new java.util.HashMap<>();

        for (String permission : permissions) {
            // 解析权限编码，获取模块名
            String[] parts = permission.split(":");
            if (parts.length >= 2) {
                String module = parts[0];
                groupedPermissions.computeIfAbsent(module, k -> new java.util.ArrayList<>()).add(permission);
            }
        }

        return groupedPermissions;
    }

    /**
     * 根据用户名获取权限列表
     */
    public List<String> getPermissionsByUsername(String username) {
        SysUser user = findByUsername(username);
        if (user == null) {
            return new java.util.ArrayList<>();
        }
        return getPermissionsByUserId(user.getId());
    }

    @Override
    public List<SysUser> searchUsers(String keyword) {
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            return new java.util.ArrayList<>();
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行搜索
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.likeRight("username", keyword)
                    .or().likeRight("real_name", keyword)
                    .or().likeRight("email", keyword);
        }
        List<SysUser> users = userMapper.selectList(queryWrapper);
        
        // 根据角色权限过滤搜索结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : users) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        return filteredUsers;
    }
    
    @Override
    public Page<SysUser> searchUsers(Page<SysUser> page, String keyword) {
        // 获取当前登录用户
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userMapper.selectByUsername(currentUsername);
        if (currentUser == null) {
            Page<SysUser> emptyPage = new Page<>(page.getCurrent(), page.getSize(), 0);
            emptyPage.setRecords(new java.util.ArrayList<>());
            return emptyPage;
        }
        
        // 获取当前用户的角色列表
        List<String> currentRoles = userMapper.selectRolesByUserId(currentUser.getId());
        
        // 执行分页搜索
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.likeRight("username", keyword)
                    .or().likeRight("real_name", keyword)
                    .or().likeRight("email", keyword);
        }
        Page<SysUser> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 根据角色权限过滤搜索结果
        List<SysUser> filteredUsers = new java.util.ArrayList<>();
        for (SysUser user : userPage.getRecords()) {
            // 总是可以查看自己
            if (user.getId().equals(currentUser.getId())) {
                filteredUsers.add(user);
                continue;
            }
            
            // 获取用户的角色列表
            List<String> userRoles = userMapper.selectRolesByUserId(user.getId());
            
            // 检查当前用户是否有权限查看该用户
            if (canViewUser(currentRoles, userRoles)) {
                filteredUsers.add(user);
            }
        }
        
        // 创建新的分页结果
        Page<SysUser> filteredPage = new Page<>(page.getCurrent(), page.getSize(), userPage.getTotal());
        filteredPage.setRecords(filteredUsers);
        return filteredPage;
    }
    
    /**
     * 检查当前用户是否有权限查看目标用户
     */
    private boolean canViewUser(List<String> currentRoles, List<String> targetRoles) {
        // 如果当前用户是admin，可以查看所有用户
        if (currentRoles != null && currentRoles.contains("admin")) {
            return true;
        }
        
        // 如果当前用户是manager，可以查看普通用户，但不能查看其他manager
        if (currentRoles != null && currentRoles.contains("manager")) {
            // 检查目标用户是否包含manager角色
            if (targetRoles != null && targetRoles.contains("manager")) {
                return false;
            }
            return true;
        }
        
        // 普通用户只能查看自己
        return false;
    }
}