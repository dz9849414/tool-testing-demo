package com.example.tooltestingdemo.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量移除用户直接权限DTO
 */
@Data
@Schema(description = "批量移除用户直接权限请求参数")
public class BatchRemoveUserPermissionDTO {
    
    @Schema(description = "用户ID列表", required = true)
    private List<String> userIds;
    
    @Schema(description = "权限ID列表", required = true)
    private List<String> permissionIds;
    
    @Schema(description = "作用域类型", example = "GLOBAL")
    private String scopeType = "GLOBAL";
    
    @Schema(description = "作用域ID")
    private String scopeId;
    
    @Schema(description = "移除原因")
    private String removeReason;
    
    @Schema(description = "操作人ID")
    private String operatorId;
}