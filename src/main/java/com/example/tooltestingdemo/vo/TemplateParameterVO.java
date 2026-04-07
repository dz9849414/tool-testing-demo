package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板请求参数 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateParameterVO.java
 */
@Data
public class TemplateParameterVO {

    private Long id;

    private Long templateId;

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
     * 数据类型
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
     * 验证规则
     */
    private String validationRules;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
