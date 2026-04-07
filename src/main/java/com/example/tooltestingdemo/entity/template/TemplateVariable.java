package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板变量定义实体类
 */
@Data
@TableName("template_variable")
public class TemplateVariable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "variable_name")
    private String variableName;

    @TableField(value = "variable_type")
    private String variableType;

    @TableField(value = "default_value")
    private String defaultValue;

    @TableField(value = "current_value")
    private String currentValue;

    @TableField(value = "description")
    private String description;

    @TableField(value = "example_value")
    private String exampleValue;

    @TableField(value = "is_required")
    private Integer isRequired;

    @TableField(value = "is_editable")
    private Integer isEditable;

    @TableField(value = "is_persistent")
    private Integer isPersistent;

    @TableField(value = "source_type")
    private String sourceType;

    @TableField(value = "source_config")
    private String sourceConfig;

    @TableField(value = "validation_rules")
    private String validationRules;

    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
