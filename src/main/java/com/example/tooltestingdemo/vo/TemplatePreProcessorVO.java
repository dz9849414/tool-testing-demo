package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板前置处理器 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplatePreProcessorVO.java
 */
@Data
public class TemplatePreProcessorVO {

    private Long id;

    private Long templateId;

    /**
     * 处理器名称
     */
    private String processorName;

    /**
     * 处理器类型
     */
    private String processorType;

    /**
     * 处理器配置参数
     */
    private String config;

    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 目标变量名
     */
    private String targetVariable;

    /**
     * 变量作用域
     */
    private String variableScope;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 执行顺序
     */
    private Integer sortOrder;
}
