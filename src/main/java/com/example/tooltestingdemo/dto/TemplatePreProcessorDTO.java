package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 模板前置处理器 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplatePreProcessorDTO.java
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplatePreProcessorDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * 处理器名称
     */
    private String processorName;

    /**
     * 处理器类型：SET_VARIABLE/TIMESTAMP/RANDOM_STRING/RANDOM_NUMBER/RANDOM_UUID/BASE64_ENCODE/MD5/SHA1/JS_SCRIPT
     */
    private String processorType;

    /**
     * 处理器配置参数（JSON格式）
     */
    private String config;

    /**
     * 脚本内容（JS/GROOVY等）
     */
    private String scriptContent;

    /**
     * 目标变量名
     */
    private String targetVariable;

    /**
     * 变量作用域：TEMPLATE/STEP/GLOBAL
     */
    private String variableScope;

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
