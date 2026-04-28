package com.example.tooltestingdemo.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量移除用户角色DTO
 */
@Data
@Schema(description = "批量移除用户角色请求参数")
public class BatchRemoveUserRoleDTO {
    
    @Schema(description = "用户ID列表", required = true)
    private List<String> userIds;
    
    @Schema(description = "角色ID列表", required = true)
    private List<String> roleIds;
    
    @Schema(description = "移除原因")
    private String removeReason;
    
    @Schema(description = "操作人ID")
    private String operatorId;
}