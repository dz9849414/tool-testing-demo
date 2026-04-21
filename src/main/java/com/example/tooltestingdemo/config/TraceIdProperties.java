package com.example.tooltestingdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.trace-id")
public class TraceIdProperties {

    /**
     * 是否启用 TraceId 处理
     */
    private boolean enabled = true;

    /**
     * 请求头中读取 traceId 的 header 名称
     */
    private String requestHeader = "X-Trace-Id";

    /**
     * 响应头中回传 traceId 的 header 名称
     */
    private String responseHeader = "X-Trace-Id";

    /**
     * 是否允许使用请求头中的 traceId
     */
    private boolean allowRequestOverride = true;

    /**
     * 是否在响应头中返回 traceId
     */
    private boolean includeInResponseHeader = true;
}
