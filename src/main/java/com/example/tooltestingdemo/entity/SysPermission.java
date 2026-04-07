package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体类
 */
@TableName("sys_permission")
@Data
public class SysPermission {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "name")
    private String name;
    
    @TableField(value = "code")
    private String code;
    
    @TableField(value = "description")
    private String description;
    
    @TableField(value = "module")
    private String module;
    
    @TableField(value = "type")
    private String type = "MENU";
    
    @TableField(value = "parent_id")
    private String parentId = "0";
    
    @TableField(value = "level")
    private Integer level = 1;
    
    @TableField(value = "sort")
    private Integer sort = 0;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}