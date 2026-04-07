package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典实体类
 */
@TableName("sys_dict")
@Data
public class SysDict {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "dict_type")
    private String dictType;
    
    @TableField(value = "dict_code")
    private String dictCode;
    
    @TableField(value = "dict_value")
    private String dictValue;
    
    @TableField(value = "description")
    private String description;
    
    @TableField(value = "sort")
    private Integer sort = 0;
    
    @TableField(value = "status")
    private Integer status = 1;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}