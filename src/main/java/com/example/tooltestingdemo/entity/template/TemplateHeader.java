package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板请求头实体类
 */
@Data
@TableName("template_header")
public class TemplateHeader {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "header_name")
    private String headerName;

    @TableField(value = "header_value")
    private String headerValue;

    @TableField(value = "description")
    private String description;

    @TableField(value = "is_enabled")
    private Integer isEnabled;

    @TableField(value = "is_required")
    private Integer isRequired;

    @TableField(value = "is_variable")
    private Integer isVariable;

    @TableField(value = "variable_name")
    private String variableName;

    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
