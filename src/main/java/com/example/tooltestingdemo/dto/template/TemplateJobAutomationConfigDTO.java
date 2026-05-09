package com.example.tooltestingdemo.dto.template;

import lombok.Data;

/**
 * 任务自动化配置
 */
@Data
public class TemplateJobAutomationConfigDTO {

    private ConcurrentConfig concurrent = new ConcurrentConfig();

    private ScriptConfig script = new ScriptConfig();

    private LogConfig log = new LogConfig();

    private ReportConfig report = new ReportConfig();

    @Data
    public static class ConcurrentConfig {
        private Boolean enabled = false;
        private Integer maxConcurrency = 5;
        private Long timeoutMs = 120000L;
        private Integer retryCount = 1;
        private Boolean continueOnError = true;
    }

    @Data
    public static class ScriptConfig {
        private Boolean enabled = false;
        private String language = "javascript";
        private String beforeScript;
        private String afterScript;
        private Long timeoutMs = 30000L;
        private Boolean failOnError = true;
    }

    @Data
    public static class LogConfig {
        private Boolean enabled = true;
        private String level = "INFO";
        private Boolean recordDetail = true;
        private Boolean recordRequest = true;
        private Boolean recordResponse = true;
        private Integer retentionDays = 30;
    }

    @Data
    public static class ReportConfig {
        private String format = "csv";
        private Integer exportLimit = 100;
        private Boolean includeRequest = true;
        private Boolean includeResponse = true;
    }
}
