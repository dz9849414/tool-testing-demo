package com.example.tooltestingdemo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板任务批量生成记录视图
 */
@Data
public class TemplateJobGenerationLogVO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer generateCount;
    private String jobNamePrefix;
    private List<Long> jobIds;
    private Integer status;
    private String message;
    private String systemModule;
    private String operationType;
    private String operatorName;
    private String department;
    private String requestMethod;
    private String requestUrl;
    private String logSource;
    private Long durationMs;
    private LocalDateTime operationTime;
    private String createName;
    private LocalDateTime createTime;
}
