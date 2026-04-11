package com.example.tooltestingdemo.service.template.engine.core;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模板执行结果
 * 
 * 封装执行后的完整结果，包括请求信息、响应信息、断言结果等
 */
@Data
@Builder
public class ExecutionResult {

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 执行状态码
     */
    private String statusCode;

    /**
     * 状态消息
     */
    private String message;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 请求信息
     */
    private RequestInfo request;

    /**
     * 响应信息
     */
    private ResponseInfo response;

    /**
     * 执行后的变量上下文
     */
    private Map<String, Object> variables;

    /**
     * 断言结果列表
     */
    private List<AssertionResult> assertions;

    /**
     * 扩展属性
     */
    private Map<String, Object> extras;

    /**
     * 请求信息内部类
     */
    @Data
    @Builder
    public static class RequestInfo {
        /**
         * 请求URL（变量已替换）
         */
        private String url;

        /**
         * HTTP方法
         */
        private String method;

        /**
         * 请求头
         */
        private Map<String, String> headers;

        /**
         * 请求体
         */
        private String body;

        /**
         * URL参数
         */
        private Map<String, String> parameters;
    }

    /**
     * 响应信息内部类
     */
    @Data
    @Builder
    public static class ResponseInfo {
        /**
         * HTTP状态码
         */
        private Integer statusCode;

        /**
         * 状态描述
         */
        private String statusText;

        /**
         * 响应头
         */
        private Map<String, String> headers;

        /**
         * 响应体（已解析为对象或原始字符串）
         */
        private Object body;

        /**
         * 响应大小（字节）
         */
        private Long size;

        /**
         * 响应时间（毫秒）
         */
        private Long responseTime;
    }

    /**
     * 断言结果内部类
     */
    @Data
    @Builder
    public static class AssertionResult {
        /**
         * 断言名称
         */
        private String name;

        /**
         * 是否通过
         */
        private boolean passed;

        /**
         * 断言类型
         */
        private String type;

        /**
         * 实际值
         */
        private Object actualValue;

        /**
         * 期望值
         */
        private Object expectedValue;

        /**
         * 错误信息
         */
        private String errorMessage;
    }
}
