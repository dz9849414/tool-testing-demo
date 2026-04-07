package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板响应验证规则实体类
 */
@Data
@TableName("template_assertion")
public class TemplateAssertion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "assert_name")
    private String assertName;

    @TableField(value = "assert_type")
    private String assertType;

    @TableField(value = "extract_path")
    private String extractPath;

    @TableField(value = "expected_value")
    private String expectedValue;

    @TableField(value = "operator")
    private String operator;

    @TableField(value = "data_type")
    private String dataType;

    @TableField(value = "error_message")
    private String errorMessage;

    @TableField(value = "is_enabled")
    private Integer isEnabled;

    @TableField(value = "assert_group")
    private String assertGroup;

    @TableField(value = "logic_type")
    private String logicType;

    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
