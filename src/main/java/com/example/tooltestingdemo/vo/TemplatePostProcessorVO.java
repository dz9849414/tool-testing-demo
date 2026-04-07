package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板后置处理器 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplatePostProcessorVO.java
 */
@Data
public class TemplatePostProcessorVO {

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
     * 提取方式
     */
    private String extractType;

    /**
     * 提取表达式
     */
    private String extractExpression;

    /**
     * 匹配序号
     */
    private Integer extractMatchNo;

    /**
     * 目标变量名
     */
    private String targetVariable;

    /**
     * 变量作用域
     */
    private String variableScope;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 处理器配置参数
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
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 执行顺序
     */
    private Integer sortOrder;
}
