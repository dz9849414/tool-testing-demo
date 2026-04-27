package com.example.tooltestingdemo.service;

import com.example.tooltestingdemo.entity.protocol.SysRouteProtocol;
import com.example.tooltestingdemo.entity.protocol.UserProtocolRoleRel;
import com.example.tooltestingdemo.exception.BusinessException;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.mapper.RouteProtocolMapper;
import com.example.tooltestingdemo.mapper.RoleProtocolRelMapper;
import com.example.tooltestingdemo.mapper.UserProtocolRelMapper;
import com.example.tooltestingdemo.mapper.UserProtocolRoleRelMapper;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 协议权限服务（全局共用上下文，不依赖登录用户）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProtocolPermService {
    
    private final RouteProtocolMapper routeProtocolMapper;
    private final RoleProtocolRelMapper relMapper;
    private final UserProtocolRelMapper userProtocolRelMapper;
    private final UserProtocolRoleRelMapper userProtocolRoleRelMapper;
    private final SysUserMapper sysUserMapper;
    
    // 全局缓存：路由URI -> 协议代码映射
    private Map<String, String> routeProtocolCache = new HashMap<>();
    
    // 全局缓存：角色ID -> 允许的协议集合
    private Map<Long, Set<String>> roleProtocolCache = new HashMap<>();
    
    // 全局缓存：用户ID -> 允许的协议集合
    private Map<Long, Set<String>> userProtocolCache = new HashMap<>();
    
    /**
     * 系统启动时加载协议权限配置到全局缓存
     */
    @PostConstruct
    public void initProtocolCache() {
        log.info("开始加载协议权限全局配置...");
        
        // 1. 加载路由协议绑定配置
        loadRouteProtocolConfig();
        
        // 2. 加载角色协议权限配置
        loadRoleProtocolConfig();
        
        // 3. 加载用户协议权限配置
        loadUserProtocolConfig();
        
        log.info("协议权限全局配置加载完成，路由绑定: {} 条，角色权限: {} 个，用户权限: {} 个", 
                routeProtocolCache.size(), roleProtocolCache.size(), userProtocolCache.size());
    }
    
    /**
     * 加载路由协议绑定配置到全局缓存
     */
    private void loadRouteProtocolConfig() {
        // 查询所有启用的路由协议绑定
        List<SysRouteProtocol> routeProtocols = routeProtocolMapper.selectAllEnabledRoutes();
        
        routeProtocolCache.clear();
        for (SysRouteProtocol route : routeProtocols) {
            routeProtocolCache.put(route.getRequestUri(), route.getProtocolCode());
        }
    }
    
    /**
     * 加载角色协议权限配置到全局缓存
     */
    private void loadRoleProtocolConfig() {
        try {
            // 查询所有启用的角色协议权限
            List<Map<String, Object>> roleProtocols = relMapper.selectAllEnabledRoleProtocols();
            
            roleProtocolCache.clear();
            for (Map<String, Object> roleProtocol : roleProtocols) {
                // 使用数据库列名获取字段值
                Long roleId = ((Number) roleProtocol.get("role_id")).longValue();
                String protocolCode = (String) roleProtocol.get("protocol_code");
                
                roleProtocolCache
                    .computeIfAbsent(roleId, k -> new HashSet<>())
                    .add(protocolCode);
            }
        } catch (Exception e) {
            log.warn("加载角色协议权限配置失败，可能是数据库表不存在，将使用空配置: {}", e.getMessage());
            roleProtocolCache.clear();
        }
    }
    
    /**
     * 加载用户协议权限配置到全局缓存
     */
    private void loadUserProtocolConfig() {
        try {
            // 查询所有启用的用户协议权限
            List<Map<String, Object>> userProtocols = userProtocolRelMapper.selectAllEnabledUserProtocols();
            
            userProtocolCache.clear();
            for (Map<String, Object> userProtocol : userProtocols) {
                // 使用数据库列名获取字段值
                Long userId = ((Number) userProtocol.get("user_id")).longValue();
                String protocolCode = (String) userProtocol.get("protocol_code");
                
                userProtocolCache
                    .computeIfAbsent(userId, k -> new HashSet<>())
                    .add(protocolCode);
            }
        } catch (Exception e) {
            log.warn("加载用户协议权限配置失败，可能是数据库表不存在，将使用空配置: {}", e.getMessage());
            userProtocolCache.clear();
        }
    }
    
    /**
     * 1. 根据当前请求URI查询绑定协议（从全局缓存）
     */
    public String getProtocolByUri(String uri) {
        return routeProtocolCache.get(uri);
    }
    
    /**
     * 2. 根据角色ID查询已授权协议集合（从全局缓存）
     */
    public Set<String> getAllowProtocolCodes(Long roleId) {
        return roleProtocolCache.getOrDefault(roleId, new HashSet<>());
    }
    
    /**
     * 3. 根据用户ID查询已授权协议集合（从全局缓存）
     */
    public Set<String> getAllowProtocolCodesByUserId(Long userId) {
        return userProtocolCache.getOrDefault(userId, new HashSet<>());
    }
    
    /**
     * 3. 统一校验方法（基于全局共用上下文）
     */
    public void checkPerm(String uri, Long roleId) {
        // 当前接口绑定协议
        String protocolCode = getProtocolByUri(uri);
        if (protocolCode == null) {
            // 非协议测试接口，直接放行
            return;
        }
        
        // 当前角色允许的协议
        Set<String> allowSet = getAllowProtocolCodes(roleId);
        if (!allowSet.contains(protocolCode)) {
            throw new RuntimeException("无【" + protocolCode + "】协议操作权限");
        }
    }
    
    /**
     * 4. 基于用户ID的权限校验方法（使用新的协议权限表）
     */
    public void checkPermByUserId(String uri, Long userId) {
        // 当前接口绑定协议
        String protocolCode = getProtocolByUri(uri);
        if (protocolCode == null) {
            // 非协议测试接口，直接放行
            return;
        }
        
        // 从用户表直接获取角色ID
        Long roleId = sysUserMapper.selectRoleIdByUserId(userId);
        if (roleId == null) {
            throw new RuntimeException("用户未分配角色");
        }
        
        // 当前用户允许的协议（从新的协议权限表获取）
        Set<String> allowSet = getAllowProtocolCodes(roleId);
        if (!allowSet.contains(protocolCode)) {
            throw new RuntimeException("无【" + protocolCode + "】协议操作权限");
        }
    }
    
    /**
     * 刷新协议权限缓存
     */
    public void refreshCache() {
        log.info("刷新协议权限全局缓存...");
        loadRouteProtocolConfig();
        loadRoleProtocolConfig();
        loadUserProtocolConfig();
        log.info("协议权限全局缓存刷新完成，路由绑定: {} 条，角色权限: {} 个，用户权限: {} 个", 
                routeProtocolCache.size(), roleProtocolCache.size(), userProtocolCache.size());
    }
    
    // ================================================
    // 新增的权限分配方法
    // ================================================
    
    /**
     * 查询所有可分配的协议权限
     */
    public List<com.example.tooltestingdemo.vo.ProtocolPermissionVO.AssignablePermission> getAssignableProtocols() {
        // 这里需要调用协议类型服务获取所有可用的协议
        // 暂时返回空列表，需要与IProtocolTypeService集成
        return java.util.Collections.emptyList();
    }
    
    /**
     * 查询用户权限信息
     */
    public com.example.tooltestingdemo.vo.ProtocolPermissionVO.UserPermissionInfo getUserPermissionInfo(Long userId) {
        // 这里需要查询用户的角色信息和直接权限信息
        // 暂时返回空对象，需要实现具体的查询逻辑
        return new com.example.tooltestingdemo.vo.ProtocolPermissionVO.UserPermissionInfo();
    }
    
    /**
     * 批量分配角色给用户
     */
    public com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult batchAssignRoleToUser(
            com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO.BatchAssignRoleToUserDTO dto) {
        com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult result = new com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult();
        java.util.List<String> failureReasons = new java.util.ArrayList<>();
        int successCount = 0;
        
        // 多对多批量分配：每个用户分配每个角色
        for (Long userId : dto.getUserIds()) {
            for (Long roleId : dto.getRoleIds()) {
                try {
                    // 检查是否已存在关联
                    if (!userProtocolRoleRelMapper.existsByUserIdAndRoleId(userId, roleId)) {
                        UserProtocolRoleRel rel = new UserProtocolRoleRel();
                        rel.setUserId(userId);
                        rel.setRoleId(roleId);
                        rel.setDescription(dto.getDescription());
                        rel.setStatus(1);
                        rel.setCreateTime(java.time.LocalDateTime.now());
                        rel.setUpdateTime(java.time.LocalDateTime.now());
                        
                        userProtocolRoleRelMapper.insert(rel);
                        successCount++;
                    } else {
                        failureReasons.add(String.format("用户%d已拥有角色%d", userId, roleId));
                    }
                } catch (Exception e) {
                    failureReasons.add(String.format("用户%d分配角色%d失败: %s", userId, roleId, e.getMessage()));
                }
            }
        }
        
        result.setSuccessCount(successCount);
        result.setFailureCount(failureReasons.size());
        result.setFailureReasons(failureReasons);
        
        // 刷新缓存
        refreshCache();
        
        return result;
    }
    
    /**
     * 批量分配权限给角色
     */
    public com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult batchAssignPermissionToRole(
            com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO.BatchAssignPermissionToRoleDTO dto) {
        com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult result = new com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult();
        java.util.List<String> failureReasons = new java.util.ArrayList<>();
        int successCount = 0;
        
        // 多对多批量分配：每个角色分配每个协议权限
        for (Long roleId : dto.getRoleIds()) {
            for (String protocolCode : dto.getProtocolCodes()) {
                try {
                    // 检查是否已存在关联
                    if (!relMapper.existsByRoleIdAndProtocolCode(roleId, protocolCode)) {
                        // 创建角色协议关联记录
                        relMapper.insertRoleProtocolRel(roleId, protocolCode, dto.getDescription(), 1, 
                                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
                        successCount++;
                    } else {
                        failureReasons.add(String.format("角色%d已拥有协议权限%s", roleId, protocolCode));
                    }
                } catch (Exception e) {
                    failureReasons.add(String.format("角色%d分配协议权限%s失败: %s", roleId, protocolCode, e.getMessage()));
                }
            }
        }
        
        result.setSuccessCount(successCount);
        result.setFailureCount(failureReasons.size());
        result.setFailureReasons(failureReasons);
        
        // 刷新缓存
        refreshCache();
        
        return result;
    }
    
    /**
     * 批量直接分配权限给用户
     */
    public com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult batchAssignPermissionToUser(
            com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO.BatchAssignPermissionToUserDTO dto) {
        com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult result = new com.example.tooltestingdemo.vo.ProtocolPermissionVO.BatchAssignResult();
        java.util.List<String> failureReasons = new java.util.ArrayList<>();
        int successCount = 0;
        
        // 多对多批量分配：每个用户直接分配每个协议权限
        for (Long userId : dto.getUserIds()) {
            for (String protocolCode : dto.getProtocolCodes()) {
                try {
                    // 检查是否已存在关联
                    if (!userProtocolRelMapper.existsByUserIdAndProtocolCode(userId, protocolCode)) {
                        // 创建用户协议关联记录
                        userProtocolRelMapper.insertUserProtocolRel(userId, protocolCode, dto.getDescription(), 1, 
                                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
                        successCount++;
                    } else {
                        failureReasons.add(String.format("用户%d已拥有直接协议权限%s", userId, protocolCode));
                    }
                } catch (Exception e) {
                    failureReasons.add(String.format("用户%d分配直接协议权限%s失败: %s", userId, protocolCode, e.getMessage()));
                }
            }
        }
        
        result.setSuccessCount(successCount);
        result.setFailureCount(failureReasons.size());
        result.setFailureReasons(failureReasons);
        
        // 刷新缓存
        refreshCache();
        
        return result;
    }
    
    /**
     * 移除用户角色
     */
    public void removeRoleFromUser(com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO.RemoveRoleFromUserDTO dto) {
        // 检查关联是否存在
        if (!userProtocolRoleRelMapper.existsByUserIdAndRoleId(dto.getUserId(), dto.getRoleId())) {
            throw new BusinessException(ErrorStatus.NOT_FOUND, String.format("用户%d与角色%d的关联不存在", dto.getUserId(), dto.getRoleId()));
        }
        
        // 删除用户角色关联
        int deleted = userProtocolRoleRelMapper.deleteByUserIdAndRoleId(dto.getUserId(), dto.getRoleId());
        if (deleted <= 0) {
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR, String.format("移除用户%d角色%d失败", dto.getUserId(), dto.getRoleId()));
        }
        
        log.info("成功移除用户角色: userId={}, roleId={}", dto.getUserId(), dto.getRoleId());
        
        // 刷新缓存
        refreshCache();
    }
    
    /**
     * 移除角色权限
     */
    public void removePermissionFromRole(com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO.RemovePermissionFromRoleDTO dto) {
        // 检查关联是否存在
        if (!relMapper.existsByRoleIdAndProtocolCode(dto.getRoleId(), dto.getProtocolCode())) {
            throw new BusinessException(ErrorStatus.NOT_FOUND, String.format("角色%d与协议权限%s的关联不存在", dto.getRoleId(), dto.getProtocolCode()));
        }
        
        // 删除角色协议关联
        int deleted = relMapper.deleteByRoleIdAndProtocolCode(dto.getRoleId(), dto.getProtocolCode());
        if (deleted <= 0) {
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR, String.format("移除角色%d协议权限%s失败", dto.getRoleId(), dto.getProtocolCode()));
        }
        
        log.info("成功移除角色权限: roleId={}, protocolCode={}", dto.getRoleId(), dto.getProtocolCode());
        
        // 刷新缓存
        refreshCache();
    }
    
    /**
     * 移除用户直接权限
     */
    public void removePermissionFromUser(com.example.tooltestingdemo.dto.ProtocolPermissionAssignDTO.RemovePermissionFromUserDTO dto) {
        // 检查关联是否存在
        if (!userProtocolRelMapper.existsByUserIdAndProtocolCode(dto.getUserId(), dto.getProtocolCode())) {
            throw new BusinessException(ErrorStatus.NOT_FOUND, String.format("用户%d与直接协议权限%s的关联不存在", dto.getUserId(), dto.getProtocolCode()));
        }
        
        // 删除用户协议关联
        int deleted = userProtocolRelMapper.deleteByUserIdAndProtocolCode(dto.getUserId(), dto.getProtocolCode());
        if (deleted <= 0) {
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR, String.format("移除用户%d直接协议权限%s失败", dto.getUserId(), dto.getProtocolCode()));
        }
        
        log.info("成功移除用户直接权限: userId={}, protocolCode={}", dto.getUserId(), dto.getProtocolCode());
        
        // 刷新缓存
        refreshCache();
    }
}