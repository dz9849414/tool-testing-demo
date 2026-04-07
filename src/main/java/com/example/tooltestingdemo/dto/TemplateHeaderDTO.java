package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 模板请求头 DTO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/TemplateHeaderDTO.java
 */
@Data
public class TemplateHeaderDTO {

    /**
     * ID（更新时必填）
     */
    private Long id;

    /**
     * Header名称
     */
    private String headerName;

    /**
     * Header值
     */
    private String headerValue;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 是否启用：0-否 1-是
     */
    private Integer isEnabled;

    /**
     * 是否必填：0-否 1-是
     */
    private Integer isRequired;

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
