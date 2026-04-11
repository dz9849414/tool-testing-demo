package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;

/**
 * 模板执行器接口
 * 
 * 策略模式核心接口，不同类型的模板（HTTP、SQL、WebSocket等）实现此接口
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
public interface TemplateExecutor {

    /**
     * 获取执行器类型
     * 
     * @return 执行器类型标识，如 "HTTP", "HTTPS", "SQL", "WEBSOCKET"
     */
    String getType();

    /**
     * 执行模板
     * 
     * <p>执行流程：</p>
     * <ol>
     *   <li>从 context 获取模板配置</li>
     *   <li>构建请求</li>
     *   <li>发送请求</li>
     *   <li>解析响应</li>
     *   <li>封装结果</li>
     * </ol>
     *
     * @param context 执行上下文，包含模板、变量、环境等信息
     * @return 执行结果
     * @throws RuntimeException 执行失败时抛出
     */
    ExecutionResult execute(TemplateContext context);

    /**
     * 验证模板配置是否正确
     * 
     * <p>在执行前调用，检查模板配置是否合法</p>
     *
     * @param context 执行上下文
     * @return 验证结果
     */
    ValidationResult validate(TemplateContext context);

    /**
     * 预览请求（生成最终请求内容，但不发送）
     * 
     * <p>用于调试，查看变量替换后的实际请求内容</p>
     *
     * @param context 执行上下文
     * @return 预览结果
     */
    PreviewResult preview(TemplateContext context);

    /**
     * 是否支持该协议类型
     *
     * @param protocolType 协议类型
     * @return 是否支持
     */
    default boolean supports(String protocolType) {
        return getType().equalsIgnoreCase(protocolType);
    }

    /**
     * 验证结果内部类
     */
    class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 预览结果内部类
     */
    class PreviewResult {
        private final String url;
        private final String method;
        private final Object headers;
        private final String body;
        private final Object parameters;

        public PreviewResult(String url, String method, Object headers, String body, Object parameters) {
            this.url = url;
            this.method = method;
            this.headers = headers;
            this.body = body;
            this.parameters = parameters;
        }

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public Object getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }

        public Object getParameters() {
            return parameters;
        }
    }
}
