package com.example.tooltestingdemo.aspect;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.annotation.ProtocolPermissionFilter;
import com.example.tooltestingdemo.mapper.RoleProtocolRelMapper;
import com.example.tooltestingdemo.mapper.UserProtocolRelMapper;
import com.example.tooltestingdemo.mapper.UserProtocolRoleRelMapper;
import com.example.tooltestingdemo.entity.protocol.UserProtocolRoleRel;
import com.example.tooltestingdemo.service.SecurityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 协议权限过滤切面
 * 用于根据用户协议权限自动过滤查询结果
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ProtocolPermissionAspect {
    
    private final SecurityService securityService;
    private final UserProtocolRelMapper userProtocolRelMapper;
    private final RoleProtocolRelMapper relMapper;
    private final UserProtocolRoleRelMapper userProtocolRoleRelMapper;
    
    /**
     * 环绕通知：处理协议权限过滤
     */
    @Around("@annotation(protocolPermissionFilter)")
    public Object filterByProtocolPermission(ProceedingJoinPoint joinPoint, 
                                           ProtocolPermissionFilter protocolPermissionFilter) throws Throwable {
        
        // 如果不启用权限过滤，直接执行原方法
        if (!protocolPermissionFilter.enabled()) {
            return joinPoint.proceed();
        }
        
        // 执行原方法获取结果
        Object result = joinPoint.proceed();
        
        // 获取当前用户ID
        Long currentUserId = securityService.getCurrentUserId();
        if (currentUserId == null) {
            log.warn("无法获取当前用户ID，跳过权限过滤");
            return result;
        }
        
        // 获取用户有权限的协议ID列表
        Set<Long> allowedProtocolIds = getUserAllowedProtocolIds(currentUserId);
        
        // 如果用户没有任何权限，返回空结果
        if (allowedProtocolIds.isEmpty()) {
            return createEmptyResult(result);
        }
        
        // 根据结果类型进行过滤
        return filterResultByPermission(result, allowedProtocolIds, protocolPermissionFilter.protocolIdField());
    }
    
    /**
     * 根据结果类型进行权限过滤
     */
    private Object filterResultByPermission(Object result, Set<Long> allowedProtocolIds, String protocolIdField) {
        if (result instanceof IPage) {
            return filterPageResult((IPage<?>) result, allowedProtocolIds, protocolIdField);
        } else if (result instanceof List) {
            return filterListResult((List<?>) result, allowedProtocolIds, protocolIdField);
        } else if (result instanceof Collection) {
            return filterCollectionResult((Collection<?>) result, allowedProtocolIds, protocolIdField);
        }
        
        // 其他类型直接返回
        return result;
    }
    
    /**
     * 过滤分页结果
     */
    private <T> IPage<T> filterPageResult(IPage<T> page, Set<Long> allowedProtocolIds, String protocolIdField) {
        List<T> filteredRecords = page.getRecords().stream()
                .filter(record -> hasPermission(record, allowedProtocolIds, protocolIdField))
                .collect(Collectors.toList());
        
        // 创建新的分页对象
        IPage<T> filteredPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
            page.getCurrent(), page.getSize(), filteredRecords.size()
        );
        filteredPage.setRecords(filteredRecords);
        
        return filteredPage;
    }
    
    /**
     * 过滤列表结果
     */
    private <T> List<T> filterListResult(List<T> list, Set<Long> allowedProtocolIds, String protocolIdField) {
        return list.stream()
                .filter(record -> hasPermission(record, allowedProtocolIds, protocolIdField))
                .collect(Collectors.toList());
    }
    
    /**
     * 过滤集合结果
     */
    private <T> Collection<T> filterCollectionResult(Collection<T> collection, Set<Long> allowedProtocolIds, String protocolIdField) {
        return collection.stream()
                .filter(record -> hasPermission(record, allowedProtocolIds, protocolIdField))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查记录是否有权限
     */
    private boolean hasPermission(Object record, Set<Long> allowedProtocolIds, String protocolIdField) {
        try {
            // 使用反射获取协议ID字段的值
            java.lang.reflect.Field field = record.getClass().getDeclaredField(protocolIdField);
            field.setAccessible(true);
            Object value = field.get(record);
            
            if (value instanceof Long) {
                return allowedProtocolIds.contains((Long) value);
            } else if (value instanceof Integer) {
                return allowedProtocolIds.contains(((Integer) value).longValue());
            } else if (value instanceof String) {
                try {
                    Long protocolId = Long.valueOf((String) value);
                    return allowedProtocolIds.contains(protocolId);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.warn("获取记录协议ID失败: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 获取用户有权限的协议ID列表
     */
    private Set<Long> getUserAllowedProtocolIds(Long userId) {
        Set<Long> allowedProtocolIds = new HashSet<>();
        
        try {
            // 1. 获取用户直接分配的协议权限
            List<Map<String, Object>> userProtocols = userProtocolRelMapper.selectAllEnabledUserProtocols();
            for (Map<String, Object> userProtocol : userProtocols) {
                Long protocolUserId = ((Number) userProtocol.get("user_id")).longValue();
                if (userId.equals(protocolUserId)) {
                    Long protocolId = ((Number) userProtocol.get("protocol_id")).longValue();
                    allowedProtocolIds.add(protocolId);
                }
            }
            
            // 2. 获取用户通过角色分配的协议权限
            List<Map<String, Object>> roleProtocols = relMapper.selectAllEnabledRoleProtocols();
            Set<Long> userRoleIds = getUserRoleIds(userId);
            
            for (Map<String, Object> roleProtocol : roleProtocols) {
                Long roleId = ((Number) roleProtocol.get("role_id")).longValue();
                if (userRoleIds.contains(roleId)) {
                    Long protocolId = ((Number) roleProtocol.get("protocol_id")).longValue();
                    allowedProtocolIds.add(protocolId);
                }
            }
            
        } catch (Exception e) {
            log.warn("获取用户协议权限失败: {}", e.getMessage());
        }
        
        return allowedProtocolIds;
    }
    
    /**
     * 获取用户关联的角色ID列表
     */
    private Set<Long> getUserRoleIds(Long userId) {
        Set<Long> roleIds = new HashSet<>();
        
        try {
            List<UserProtocolRoleRel> userRoleRels = userProtocolRoleRelMapper.selectList(
                new LambdaQueryWrapper<UserProtocolRoleRel>()
                    .eq(UserProtocolRoleRel::getUserId, userId)
                    .eq(UserProtocolRoleRel::getStatus, 1)
            );
            
            for (UserProtocolRoleRel rel : userRoleRels) {
                roleIds.add(rel.getRoleId());
            }
            
        } catch (Exception e) {
            log.warn("获取用户角色关联失败: {}", e.getMessage());
        }
        
        return roleIds;
    }
    
    /**
     * 创建空结果
     */
    private Object createEmptyResult(Object originalResult) {
        if (originalResult instanceof IPage) {
            IPage<?> page = (IPage<?>) originalResult;
            IPage<?> emptyPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page.getCurrent(), page.getSize(), 0
            );
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        } else if (originalResult instanceof List) {
            return Collections.emptyList();
        } else if (originalResult instanceof Collection) {
            return Collections.emptyList();
        }
        
        return originalResult;
    }
}