package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 模板后置处理器 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplatePostProcessorDTO.java
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplatePostProcessorDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * 处理器名称
     */
    private String processorName;

    /**
     * 处理器类型：JSON_EXTRACT/XML_EXTRACT/REGEX_EXTRACT/HEADER_EXTRACT/COOKIE_EXTRACT/JS_SCRIPT
     */
    private String processorType;

    /**
     * 提取方式：JSON_PATH/XPATH/REGEX/HEADER/COOKIE
     */
    private String extractType;

    /**
     * 提取表达式
     */
    private String extractExpression;

    /**
     * 匹配序号，0表示所有匹配
     */
    private Integer extractMatchNo;

    /**
     * 目标变量名
     */
    private String targetVariable;

    /**
     * 变量作用域：TEMPLATE/STEP/GLOBAL
     */
    private String variableScope;

    /**
     * 默认值（提取失败时使用）
     */
    private String defaultValue;

    /**
     * 处理器配置参数（JSON格式）
     */
    private String config;

    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用：0-否 1-是
     */
    private Integer isEnabled;

    /**
     * 执行顺序
     */
    private Integer sortOrder;
}
