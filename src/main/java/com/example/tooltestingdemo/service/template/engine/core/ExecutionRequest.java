package com.example.tooltestingdemo.service.template.engine.core;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 模板执行请求
 * 
 * 封装执行所需的参数
 */
@Data
@Builder
public class ExecutionRequest {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 环境ID（可选）
     */
    private Long environmentId;

    /**
     * 执行变量（会覆盖环境变量）
     */
    private Map<String, Object> variables;

    /**
     * 是否执行断言
     */
    @Builder.Default
    private boolean executeAssertions = true;

    /**
     * 是否执行前置处理器
     */
    @Builder.Default
    private boolean executePreProcessors = true;

    /**
     * 是否执行后置处理器
     */
    @Builder.Default
    private boolean executePostProcessors = true;

    /**
     * 是否记录执行日志
     */
    @Builder.Default
    private boolean enableLogging = true;

    /**
     * 超时时间（毫秒）- 覆盖模板配置
     */
    private Integer timeout;

    /**
     * 重试次数 - 覆盖模板配置
     */
    private Integer retryCount;

    /**
     * 扩展配置
     */
    private Map<String, Object> config;

    /**
     * 创建简单执行请求
     */
    public static ExecutionRequest of(Long templateId) {
        return ExecutionRequest.builder()
                .templateId(templateId)
                .build();
    }

    /**
     * 创建带环境的执行请求
     */
    public static ExecutionRequest of(Long templateId, Long environmentId) {
        return ExecutionRequest.builder()
                .templateId(templateId)
                .environmentId(environmentId)
                .build();
    }

    /**
     * 创建带变量的执行请求
     */
    public static ExecutionRequest of(Long templateId, Long environmentId, Map<String, Object> variables) {
        return ExecutionRequest.builder()
                .templateId(templateId)
                .environmentId(environmentId)
                .variables(variables)
                .build();
    }
}
