package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体类
 */
@TableName("pdm_tool_sys_role")
@Data
public class SysRole {
    
    @TableId(value = "id", type = IdType.INPUT)
    private String id;
    
    @TableField(value = "name")
    private String name;
    
    @TableField(value = "description")
    private String description;
    
    @TableField(value = "type")
    private String type = "SYSTEM";
    
    @TableField(value = "scope_id")
    private String scopeId;
    
    @TableField(value = "status")
    private Integer status = 1;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}