package com.example.tooltestingdemo.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 模板定时任务日志子项 VO（单个模板执行结果）
 */
@Data
public class TemplateJobLogItemVO {

    private Long templateId;
    private String templateName;
    private Boolean success;
    private Integer statusCode;
    private Long durationMs;
    private String message;

    /**
     * 请求信息
     */
    private Map<String, Object> request;

    /**
     * 响应信息
     */
    private Map<String, Object> response;

    /**
     * 断言结果
     */
    private List<Map<String, Object>> assertions;

    /**
     * 最终变量
     */
    private Map<String, Object> variables;
}
