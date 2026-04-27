package com.example.tooltestingdemo.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户权限分配响应VO
 */
@Data
@Schema(description = "用户权限分配响应结果")
public class UserPermissionVO {
    
    @Schema(description = "关联ID")
    private String id;
    
    @Schema(description = "用户ID")
    private String userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "权限ID")
    private String permissionId;
    
    @Schema(description = "权限编码")
    private String permissionCode;
    
    @Schema(description = "权限名称")
    private String permissionName;
    
    @Schema(description = "授权类型")
    private String grantType;
    
    @Schema(description = "作用域类型")
    private String scopeType;
    
    @Schema(description = "作用域ID")
    private String scopeId;
    
    @Schema(description = "状态")
    private Integer status;
    
    @Schema(description = "授权开始时间")
    private LocalDateTime startTime;
    
    @Schema(description = "授权结束时间")
    private LocalDateTime endTime;
    
    @Schema(description = "是否临时授权")
    private Integer isTemporary;
    
    @Schema(description = "授权原因")
    private String grantReason;
    
    @Schema(description = "授权人ID")
    private String grantUserId;
    
    @Schema(description = "授权人姓名")
    private String grantUserName;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "是否过期")
    private Boolean expired;
    
    @Schema(description = "是否有效")
    private Boolean valid;
}