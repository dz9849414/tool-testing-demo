package com.example.tooltestingdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * TraceId 日志查询配置。
 */
@Data
@ConfigurationProperties(prefix = "app.trace-log-query")
public class TraceLogQueryProperties {

    private boolean enabled = true;

    private int maxEntriesPerTrace = 500;

    private int maxTraceCount = 200;

    /**
     * 允许采集的日志器前缀，只保留业务侧日志。
     */
    private List<String> includeLoggerPrefixes = new ArrayList<>(List.of("com.example.tooltestingdemo"));
}
