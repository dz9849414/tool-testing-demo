package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板请求头 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateHeaderVO.java
 */
@Data
public class TemplateHeaderVO {

    private Long id;

    private Long templateId;

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
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 是否必填
     */
    private Integer isRequired;

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
