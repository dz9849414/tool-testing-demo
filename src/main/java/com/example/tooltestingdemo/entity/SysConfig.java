package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 */
@TableName("sys_config")
@Data
public class SysConfig {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "config_key")
    private String configKey;
    
    @TableField(value = "config_value")
    private String configValue;
    
    @TableField(value = "config_name")
    private String configName;
    
    @TableField(value = "description")
    private String description;
    
    @TableField(value = "type")
    private String type = "TEXT";
    
    @TableField(value = "is_encrypted")
    private Integer isEncrypted = 0;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(value = "update_user")
    private String updateUser;
}