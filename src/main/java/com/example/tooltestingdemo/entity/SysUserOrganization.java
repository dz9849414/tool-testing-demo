package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户组织关联实体类
 */
@TableName("sys_user_organization")
@Data
public class SysUserOrganization {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "user_id")
    private String userId;
    
    @TableField(value = "org_id")
    private String orgId;
    
    @TableField(value = "is_primary")
    private Integer isPrimary = 0;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}