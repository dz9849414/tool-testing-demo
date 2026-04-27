package com.example.tooltestingdemo.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户权限直接分配实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pdm_tool_sys_user_permission")
public class SysUserPermission {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 权限ID
     */
    @TableField("permission_id")
    private String permissionId;
    
    /**
     * 权限编码（冗余字段，便于查询）
     */
    @TableField("permission_code")
    private String permissionCode;
    
    /**
     * 授权类型：DIRECT-直接授权，INHERIT-继承授权
     */
    @TableField("grant_type")
    private String grantType;
    
    /**
     * 作用域类型：GLOBAL-全局，ORGANIZATION-组织内，PROJECT-项目内
     */
    @TableField("scope_type")
    private String scopeType;
    
    /**
     * 作用域ID（组织ID或项目ID）
     */
    @TableField("scope_id")
    private String scopeId;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 授权开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;
    
    /**
     * 授权结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;
    
    /**
     * 是否临时授权：0-永久，1-临时
     */
    @TableField("is_temporary")
    private Integer isTemporary;
    
    /**
     * 授权原因
     */
    @TableField("grant_reason")
    private String grantReason;
    
    /**
     * 授权人ID
     */
    @TableField("grant_user_id")
    private String grantUserId;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    @TableField("create_user")
    private String createUser;
    
    /**
     * 更新人
     */
    @TableField("update_user")
    private String updateUser;
}