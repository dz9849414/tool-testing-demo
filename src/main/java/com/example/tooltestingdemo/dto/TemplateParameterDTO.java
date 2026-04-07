package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 模板请求参数 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplateParameterDTO.java
 */
@Data
public class TemplateParameterDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * 参数类型：QUERY/PATH
     */
    private String paramType;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 数据类型：STRING/INTEGER/LONG/FLOAT/DOUBLE/BOOLEAN/DATE/DATETIME/ARRAY/OBJECT/FILE
     */
    private String dataType;

    /**
     * 参数描述
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
     * 是否启用：0-否 1-是
     */
    private Integer isEnabled;

    /**
     * 是否为变量：0-否 1-是
     */
    private Integer isVariable;

    /**
     * 变量名
     */
    private String variableName;

    /**
     * 验证规则（JSON格式）
     */
    private String validationRules;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
