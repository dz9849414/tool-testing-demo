package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 模板响应验证规则 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplateAssertionDTO.java
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateAssertionDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * 断言名称
     */
    private String assertName;

    /**
     * 断言类型：STATUS_CODE/STATUS_MESSAGE/RESPONSE_HEADER/RESPONSE_BODY/JSON_PATH/XML_PATH/REGEX/CONTAINS/EQUALS/NOT_EQUALS
     */
    private String assertType;

    /**
     * 提取路径（JSONPath/XPath等）
     */
    private String extractPath;

    /**
     * 期望值
     */
    private String expectedValue;

    /**
     * 比较运算符：EQUALS/NOT_EQUALS/CONTAINS/STARTS_WITH/ENDS_WITH/MATCHES/GREATER_THAN/LESS_THAN
     */
    private String operator;

    /**
     * 数据类型：STRING/INTEGER/FLOAT/BOOLEAN/DATETIME
     */
    private String dataType;

    /**
     * 断言失败时的自定义错误信息
     */
    private String errorMessage;

    /**
     * 是否启用：0-否 1-是
     */
    private Integer isEnabled;

    /**
     * 断言分组
     */
    private String assertGroup;

    /**
     * 逻辑关系：AND/OR
     */
    private String logicType;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
