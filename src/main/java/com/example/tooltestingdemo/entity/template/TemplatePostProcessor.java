package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板后置处理器实体类
 */
@Data
@TableName("pdm_tool_template_post_processor")
public class TemplatePostProcessor {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "processor_name")
    private String processorName;

    @TableField(value = "processor_type")
    private String processorType;

    @TableField(value = "extract_type")
    private String extractType;

    @TableField(value = "extract_expression")
    private String extractExpression;

    @TableField(value = "extract_match_no")
    private Integer extractMatchNo;

    @TableField(value = "target_variable")
    private String targetVariable;

    @TableField(value = "variable_scope")
    private String variableScope;

    @TableField(value = "default_value")
    private String defaultValue;

    @TableField(value = "config")
    private String config;

    @TableField(value = "script_content")
    private String scriptContent;

    @TableField(value = "description")
    private String description;

    @TableField(value = "is_enabled")
    private Integer isEnabled;

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
