package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 模板变量定义 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplateVariableDTO.java
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateVariableDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * 变量名
     */
    private String variableName;

    /**
     * 变量类型：STRING/INTEGER/DOUBLE/BOOLEAN/JSON/ARRAY/FILE/DATE/DATETIME
     */
    private String variableType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 当前值
     */
    private String currentValue;

    /**
     * 变量描述
     */
    private String description;

    /**
     * 示例值
     */
    private String exampleValue;

    /**
     * 是否必填：0-否 1-是
     */
    private Integer isRequired;

    /**
     * 是否可编辑：0-否 1-是
     */
    private Integer isEditable;

    /**
     * 是否持久化（跨请求保持）：0-否 1-是
     */
    private Integer isPersistent;

    /**
     * 来源类型：MANUAL/EXTRACT/SCRIPT/ENVIRONMENT/DATABASE
     */
    private String sourceType;

    /**
     * 来源配置（JSON格式）
     */
    private String sourceConfig;

    /**
     * 验证规则（JSON格式）
     */
    private String validationRules;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
