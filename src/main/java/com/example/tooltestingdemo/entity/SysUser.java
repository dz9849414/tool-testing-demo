package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@TableName("pdm_tool_sys_user")
@Data
public class SysUser {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    @TableField(value = "username")
    private String username;
    
    @TableField(value = "password")
    private String password;
    
    @TableField(value = "email")
    private String email;
    
    @TableField(value = "phone")
    private String phone;
    
    @TableField(value = "real_name")
    private String realName;
    
    @TableField(value = "status")
    private Integer status = 1;
    
    @TableField(value = "create_id")
    private Long createId;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_id")
    private Long updateId;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted = 0;
    
    @TableField(value = "deleted_by")
    private Long deletedBy;
    
    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
    
    @TableField(value = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    @TableField(value = "last_login_ip")
    private String lastLoginIp;
    
    @TableField(value = "source")
    private String source = "LOCAL";
    
    @TableField(value = "organization_id")
    private String organizationId;
    
    @TableField(value = "approver_id")
    private String approverId;
    
    @TableField(value = "approve_time")
    private LocalDateTime approveTime;
}