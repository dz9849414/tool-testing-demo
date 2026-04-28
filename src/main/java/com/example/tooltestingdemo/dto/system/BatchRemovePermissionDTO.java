package com.example.tooltestingdemo.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量移除权限DTO
 */
@Data
@Schema(description = "批量移除权限请求参数")
public class BatchRemovePermissionDTO {
    
    @Schema(description = "角色ID列表", required = true)
    private List<String> roleIds;
    
    @Schema(description = "权限ID列表", required = true)
    private List<String> permissionIds;
    
    @Schema(description = "移除原因")
    private String removeReason;
    
    @Schema(description = "操作人ID")
    private String operatorId;
}