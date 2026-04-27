package com.example.tooltestingdemo.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户权限分配DTO
 */
@Data
@Schema(description = "用户权限分配请求参数")
public class UserPermissionDTO {
    
    @Schema(description = "用户ID", required = true, example = "123")
    private String userId;
    
    @Schema(description = "权限ID", required = true, example = "p1")
    private String permissionId;
    
    @Schema(description = "权限编码", required = true, example = "system:user:view")
    private String permissionCode;
    
    @Schema(description = "授权类型", example = "DIRECT", allowableValues = {"DIRECT", "INHERIT"})
    private String grantType = "DIRECT";
    
    @Schema(description = "作用域类型", example = "GLOBAL", allowableValues = {"GLOBAL", "ORGANIZATION", "PROJECT"})
    private String scopeType = "GLOBAL";
    
    @Schema(description = "作用域ID", example = "org1")
    private String scopeId;
    
    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status = 1;
    
    @Schema(description = "授权开始时间", example = "2026-04-28T10:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "授权结束时间", example = "2026-04-30T18:00:00")
    private LocalDateTime endTime;
    
    @Schema(description = "是否临时授权", example = "0", allowableValues = {"0", "1"})
    private Integer isTemporary = 0;
    
    @Schema(description = "授权原因", example = "项目紧急需要")
    private String grantReason;
    
    @Schema(description = "授权人ID", example = "admin")
    private String grantUserId;
}