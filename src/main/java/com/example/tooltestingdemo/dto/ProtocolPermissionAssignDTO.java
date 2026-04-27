package com.example.tooltestingdemo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 协议权限分配DTO
 */
@Data
public class ProtocolPermissionAssignDTO {
    
    /**
     * 批量分配角色给用户
     */
    @Data
    public static class BatchAssignRoleToUserDTO {
        @NotEmpty(message = "用户ID列表不能为空")
        private List<Long> userIds;
        
        @NotEmpty(message = "角色ID列表不能为空")
        private List<Long> roleIds;
        
        private String description;
    }
    
    /**
     * 批量分配权限给角色
     */
    @Data
    public static class BatchAssignPermissionToRoleDTO {
        @NotEmpty(message = "角色ID列表不能为空")
        private List<Long> roleIds;
        
        @NotEmpty(message = "协议代码列表不能为空")
        private List<String> protocolCodes;
        
        private String description;
    }
    
    /**
     * 批量直接分配权限给用户
     */
    @Data
    public static class BatchAssignPermissionToUserDTO {
        @NotEmpty(message = "用户ID列表不能为空")
        private List<Long> userIds;
        
        @NotEmpty(message = "协议代码列表不能为空")
        private List<String> protocolCodes;
        
        private String description;
    }
    
    /**
     * 移除用户角色
     */
    @Data
    public static class RemoveRoleFromUserDTO {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "角色ID不能为空")
        private Long roleId;
    }
    
    /**
     * 移除角色权限
     */
    @Data
    public static class RemovePermissionFromRoleDTO {
        @NotNull(message = "角色ID不能为空")
        private Long roleId;
        
        @NotNull(message = "协议代码不能为空")
        private String protocolCode;
    }
    
    /**
     * 移除用户直接权限
     */
    @Data
    public static class RemovePermissionFromUserDTO {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "协议代码不能为空")
        private String protocolCode;
    }
}