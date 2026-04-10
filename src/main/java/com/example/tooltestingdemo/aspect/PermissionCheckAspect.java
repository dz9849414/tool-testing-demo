package com.example.tooltestingdemo.aspect;

import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.SysUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限检查切面
 */
@Aspect
@Component
public class PermissionCheckAspect {
    
    @Autowired
    private SysUserService userService;
    
    @Autowired
    private SecurityService securityService;
    
    /**
     * 定义切入点
     */
    @Pointcut("@annotation(com.example.tooltestingdemo.annotation.PermissionCheck)")
    public void permissionCheckPointcut() {
    }
    
    /**
     * 环绕通知，处理权限检查
     */
    @Around("permissionCheckPointcut() && @annotation(permissionCheck)")
    public Object around(ProceedingJoinPoint joinPoint, PermissionCheck permissionCheck) throws Throwable {
        String type = permissionCheck.type();
        String targetUserIdParam = permissionCheck.targetUserIdParam();
        String roleIdsParam = permissionCheck.roleIdsParam();
        
        // 获取方法参数
        Map<String, Object> params = getMethodParams(joinPoint);
        
        // 检查权限
        ResponseEntity<?> errorResponse = checkPermission(type, params, targetUserIdParam, roleIdsParam);
        if (errorResponse != null) {
            return errorResponse;
        }
        
        // 执行原方法
        return joinPoint.proceed();
    }
    
    /**
     * 获取方法参数
     */
    private Map<String, Object> getMethodParams(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();
        // 这里简化处理，实际项目中可以通过反射获取方法参数名
        // 或者使用Spring的MethodParameterNameDiscoverer
        Object[] args = joinPoint.getArgs();
        // 假设参数顺序为：id, roleIds, 等
        // 实际项目中需要根据具体方法调整
        if (args.length > 0) {
            params.put("id", args[0]);
        }
        if (args.length > 1) {
            params.put("roleIds", args[1]);
        }
        return params;
    }
    
    /**
     * 检查权限
     */
    private ResponseEntity<?> checkPermission(String type, Map<String, Object> params, String targetUserIdParam, String roleIdsParam) {
        switch (type) {
            case "update":
            case "delete":
            case "approve":
                return checkUpdatePermission(params.get(targetUserIdParam).toString());
            case "assignRoles":
                String targetUserId = params.get(targetUserIdParam).toString();
                List<String> roleIds = (List<String>) params.get(roleIdsParam);
                ResponseEntity<?> updateCheck = checkUpdatePermission(targetUserId);
                if (updateCheck != null) {
                    return updateCheck;
                }
                return checkRolePermission(roleIds);
            default:
                return null;
        }
    }
    
    /**
     * 检查更新权限，防止越级更新
     */
    private ResponseEntity<?> checkUpdatePermission(String targetUserId) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userService.findByUsername(currentUsername);
        if (currentUser == null) {
            return createErrorResponse("用户不存在");
        }

        // 如果是当前用户自己，允许操作
        if (currentUser.getId().equals(targetUserId)) {
            return null;
        }

        // 获取目标用户信息
        com.example.tooltestingdemo.entity.SysUser targetUser = userService.findById(targetUserId);
        if (targetUser == null) {
            return createErrorResponse("目标用户不存在");
        }

        // 获取当前用户的角色列表
        List<String> currentRoles = userService.getRolesByUserId(currentUser.getId());
        // 获取目标用户的角色列表
        List<String> targetRoles = userService.getRolesByUserId(targetUser.getId());

        // 检查当前用户是否有管理员角色
        boolean isAdmin = currentRoles != null && currentRoles.contains("admin");
        if (isAdmin) {
            // 管理员可以操作所有用户
            return null;
        }

        // 检查当前用户是否有经理角色
        boolean isManager = currentRoles != null && currentRoles.contains("manager");
        if (isManager) {
            // 经理可以操作普通用户，但不能操作管理员或其他经理
            if (targetRoles != null) {
                for (String role : targetRoles) {
                    if ("admin".equals(role) || "manager".equals(role)) {
                        return createErrorResponse("无权限操作该用户");
                    }
                }
            }
            return null;
        }

        // 普通用户只能操作自己
        return createErrorResponse("无权限操作该用户");
    }
    
    /**
     * 检查角色分配权限，防止分配高于自己权限的角色
     */
    private ResponseEntity<?> checkRolePermission(List<String> roleIds) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userService.findByUsername(currentUsername);
        if (currentUser == null) {
            return createErrorResponse("用户不存在");
        }

        // 获取当前用户的角色列表
        List<String> currentRoles = userService.getRolesByUserId(currentUser.getId());
        if (currentRoles == null || currentRoles.isEmpty()) {
            return createErrorResponse("无权限分配角色");
        }

        // 检查当前用户是否有管理员角色
        boolean isAdmin = currentRoles.contains("admin");
        if (isAdmin) {
            // 管理员可以分配所有角色
            return null;
        }

        // 检查当前用户是否有经理角色
        boolean isManager = currentRoles.contains("manager");
        if (isManager) {
            // 经理只能分配普通用户角色
            for (String roleId : roleIds) {
                if ("admin".equals(roleId) || "manager".equals(roleId)) {
                    return createErrorResponse("不能分配高于自己权限的角色");
                }
            }
            return null;
        }

        // 普通用户不能分配角色
        return createErrorResponse("无权限分配角色");
    }
    
    /**
     * 创建错误响应
     */
    private ResponseEntity<?> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}