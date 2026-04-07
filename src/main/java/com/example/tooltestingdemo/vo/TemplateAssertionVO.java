package com.example.tooltestingdemo.vo;

import lombok.Data;

/**
 * 模板响应验证规则 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateAssertionVO.java
 */
@Data
public class TemplateAssertionVO {

    private Long id;

    private Long templateId;

    /**
     * 断言名称
     */
    private String assertName;

    /**
     * 断言类型
     */
    private String assertType;

    /**
     * 提取路径
     */
    private String extractPath;

    /**
     * 期望值
     */
    private String expectedValue;

    /**
     * 比较运算符
     */
    private String operator;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 断言失败时的自定义错误信息
     */
    private String errorMessage;

    /**
     * 是否启用
     */
    private Integer isEnabled;

    /**
     * 断言分组
     */
    private String assertGroup;

    /**
     * 逻辑关系
     */
    private String logicType;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
