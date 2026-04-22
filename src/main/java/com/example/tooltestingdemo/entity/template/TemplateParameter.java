package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板请求参数实体类（Query/Path参数）
 */
@Data
@TableName("pdm_tool_template_parameter")
public class TemplateParameter {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "param_type")
    private String paramType;

    @TableField(value = "param_name")
    private String paramName;

    @TableField(value = "param_value")
    private String paramValue;

    @TableField(value = "data_type")
    private String dataType;

    @TableField(value = "description")
    private String description;

    @TableField(value = "example_value")
    private String exampleValue;

    @TableField(value = "is_required")
    private Integer isRequired;

    @TableField(value = "is_enabled")
    private Integer isEnabled;

    @TableField(value = "is_variable")
    private Integer isVariable;

    @TableField(value = "variable_name")
    private String variableName;

    @TableField(value = "validation_rules")
    private String validationRules;

    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_id", fill = FieldFill.UPDATE)
    private Long updateId;

    @TableField(value = "update_name", fill = FieldFill.UPDATE)
    private String updateName;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(value = "deleted_by")
    private Long deletedBy;

    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
}