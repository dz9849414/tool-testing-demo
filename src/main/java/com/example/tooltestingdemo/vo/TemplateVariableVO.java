package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板变量定义 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateVariableVO.java
 */
@Data
public class TemplateVariableVO {

    private Long id;

    private Long templateId;

    /**
     * 变量名
     */
    private String variableName;

    /**
     * 变量类型
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
     * 是否必填
     */
    private Integer isRequired;

    /**
     * 是否可编辑
     */
    private Integer isEditable;

    /**
     * 是否持久化
     */
    private Integer isPersistent;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源配置
     */
    private String sourceConfig;

    /**
     * 验证规则
     */
    private String validationRules;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
