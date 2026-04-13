package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 模板FormData参数 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplateFormDataDTO.java
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateFormDataDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型：TEXT/FILE
     */
    private String fieldType;

    /**
     * 字段值（TEXT类型）
     */
    private String fieldValue;

    /**
     * 文件路径（FILE类型）
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件Content-Type
     */
    private String contentType;

    /**
     * 描述
     */
    private String description;

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
     * 排序序号
     */
    private Integer sortOrder;
}
