package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板FormData参数 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateFormDataVO.java
 */
@Data
public class TemplateFormDataVO {

    private Long id;

    private Long templateId;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段类型：TEXT/FILE
     */
    private String fieldType;

    /**
     * 字段值
     */
    private String fieldValue;

    /**
     * 文件路径
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
     * 是否必填
     */
    private Integer isRequired;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 是否为变量
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
