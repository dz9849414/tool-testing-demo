package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.util.List;

/**
 * 协议权限VO
 */
@Data
public class ProtocolPermissionVO {
    
    /**
     * 可分配的协议权限
     */
    @Data
    public static class AssignablePermission {
        private String protocolCode;
        private String protocolName;
        private String description;
        private String category;
    }
    
    /**
     * 用户权限信息
     */
    @Data
    public static class UserPermissionInfo {
        private Long userId;
        private String username;
        private List<RolePermissionInfo> roles;
        private List<DirectPermissionInfo> directPermissions;
    }
    
    /**
     * 角色权限信息
     */
    @Data
    public static class RolePermissionInfo {
        private Long roleId;
        private String roleName;
        private String roleCode;
        private List<String> protocolCodes;
    }
    
    /**
     * 直接权限信息
     */
    @Data
    public static class DirectPermissionInfo {
        private String protocolCode;
        private String protocolName;
        private String description;
    }
    
    /**
     * 批量分配结果
     */
    @Data
    public static class BatchAssignResult {
        private Integer successCount;
        private Integer failureCount;
        private List<String> failureReasons;
    }
}