package com.example.tooltestingdemo.aspect;

import com.example.tooltestingdemo.annotation.PermissionCheck;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.enums.RoleEnum;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.SysUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

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
        String perm = permissionCheck.perm();
        boolean or = permissionCheck.or();
        boolean allowCurrentUser = permissionCheck.allowCurrentUser();
        
        // 获取方法参数
        java.util.Map<String, Object> params = getMethodParams(joinPoint);
        
        // 检查是否是当前用户查看自己的信息
        if (allowCurrentUser && isCurrentUser(params.get(targetUserIdParam))) {
            // 当前用户查看自己的信息，允许通过
            return joinPoint.proceed();
        }
        
        // 检查类型权限
        Result<?> typeErrorResponse = checkPermission(type, params, targetUserIdParam, roleIdsParam);
        
        // 检查编码权限
        Result<?> permErrorResponse = null;
        if (!perm.isEmpty()) {
            permErrorResponse = checkPermissionByCode(perm);
        }
        
        // 对于view类型，如果指定了perm，需要检查权限编码
        if ("view".equals(type) && !perm.isEmpty()) {
            if (permErrorResponse != null) {
                return permErrorResponse;
            }
        } else {
            // 根据or参数决定权限检查逻辑
            if (or) {
                // 满足其一即可通过
                if (typeErrorResponse != null && permErrorResponse != null) {
                    return typeErrorResponse; // 返回类型检查的错误信息
                }
            } else {
                // 需要同时满足
                if (typeErrorResponse != null) {
                    return typeErrorResponse;
                }
                if (permErrorResponse != null) {
                    return permErrorResponse;
                }
            }
        }
        
        // 执行原方法
        return joinPoint.proceed();
    }
    
    /**
     * 检查是否是当前用户
     */
    private boolean isCurrentUser(Object targetUserId) {
        if (targetUserId == null) {
            return false;
        }
        
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userService.findByUsername(currentUsername);
        if (currentUser == null) {
            return false;
        }
        
        // 检查目标用户ID是否与当前用户ID相同
        return currentUser.getId().equals(targetUserId.toString());
    }
    
    /**
     * 根据权限编码检查权限
     */
    private Result<?> checkPermissionByCode(String perm) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // 获取当前用户信息
        com.example.tooltestingdemo.entity.SysUser currentUser = userService.findByUsername(currentUsername);
        if (currentUser == null) {
            return createErrorResponse("用户不存在");
        }

        // 获取当前用户的权限列表
        List<String> permissions = userService.getPermissionsByUserId(currentUser.getId());
        if (permissions == null || !permissions.contains(perm)) {
            return createErrorResponse("无权限访问该接口");
        }

        return null;
    }
    
    /**
     * 获取方法参数
     */
    private java.util.Map<String, Object> getMethodParams(ProceedingJoinPoint joinPoint) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
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
    private Result<?> checkPermission(String type, java.util.Map<String, Object> params, String targetUserIdParam, String roleIdsParam) {
        switch (type) {
            case "view":
                // 对于view类型，如果指定了perm，则跳过角色检查，只检查权限编码
                // 因为权限编码检查已经在checkPermissionByCode中处理了
                return null;
            case "update":
            case "delete":
            case "approve":
                return checkUpdatePermission(params.get(targetUserIdParam).toString());
            case "assignRoles":
                String targetUserId = params.get(targetUserIdParam).toString();
                List<String> roleIds = (List<String>) params.get(roleIdsParam);
                Result<?> updateCheck = checkUpdatePermission(targetUserId);
                if (updateCheck != null) {
                    return updateCheck;
                }
                return checkRolePermission(roleIds);
            case "assignPermissions":
            case "removePermissions":
                return checkPermissionManagementPermission();
            case "assignUsersToRole":
            case "removeUsersFromRole":
                return checkRoleManagementPermission();
            default:
                return null;
        }
    }
    
    /**
     * 检查查看权限
     */
    private Result<?> checkViewPermission() {
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
            return createErrorResponse("无权限查看用户列表");
        }

        // 检查当前用户是否有管理员或经理角色
        boolean isAdmin = currentRoles.contains(RoleEnum.ADMIN.getCode());
        boolean isManager = currentRoles.contains(RoleEnum.MANAGER.getCode());
        if (isAdmin || isManager) {
            // 管理员和经理可以查看用户列表
            return null;
        }

        // 普通用户不能查看用户列表
        return createErrorResponse("无权限查看用户列表");
    }
    
    /**
     * 检查权限管理权限
     */
    private Result<?> checkPermissionManagementPermission() {
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
            return createErrorResponse("无权限管理权限");
        }

        // 检查当前用户是否有管理员角色
        boolean isAdmin = currentRoles.contains(RoleEnum.ADMIN.getCode());
        if (isAdmin) {
            // 管理员可以管理所有权限
            return null;
        }

        // 非管理员不能管理权限
        return createErrorResponse("只有管理员可以管理权限");
    }
    
    /**
     * 检查角色管理权限
     */
    private Result<?> checkRoleManagementPermission() {
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
            return createErrorResponse("无权限管理角色");
        }

        // 检查当前用户是否有管理员或经理角色
        boolean isAdmin = currentRoles.contains(RoleEnum.ADMIN.getCode());
        boolean isManager = currentRoles.contains(RoleEnum.MANAGER.getCode());
        if (isAdmin || isManager) {
            // 管理员和经理可以管理角色
            return null;
        }

        // 普通用户不能管理角色
        return createErrorResponse("无权限管理角色");
    }
    
    /**
     * 检查更新权限，防止越级更新
     */
    private Result<?> checkUpdatePermission(String targetUserId) {
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
        boolean isAdmin = currentRoles != null && currentRoles.contains(RoleEnum.ADMIN.getCode());
        if (isAdmin) {
            // 管理员可以操作所有用户
            return null;
        }

        // 检查当前用户是否有经理角色
        boolean isManager = currentRoles != null && currentRoles.contains(RoleEnum.MANAGER.getCode());
        if (isManager) {
            // 经理可以操作普通用户，但不能操作管理员或其他经理
            if (targetRoles != null) {
                for (String role : targetRoles) {
                    if (RoleEnum.ADMIN.getCode().equals(role) || RoleEnum.MANAGER.getCode().equals(role)) {
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
    private Result<?> checkRolePermission(List<String> roleIds) {
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
        boolean isAdmin = currentRoles.contains(RoleEnum.ADMIN.getCode());
        if (isAdmin) {
            // 管理员可以分配所有角色
            return null;
        }

        // 检查当前用户是否有经理角色
        boolean isManager = currentRoles.contains(RoleEnum.MANAGER.getCode());
        if (isManager) {
            // 经理只能分配普通用户角色
            for (String roleId : roleIds) {
                if (RoleEnum.ADMIN.getCode().equals(roleId) || RoleEnum.MANAGER.getCode().equals(roleId)) {
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
    private Result<?> createErrorResponse(String message) {
        return Result.error(ErrorStatus.FORBIDDEN, message);
    }
}