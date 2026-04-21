package com.example.tooltestingdemo.vo;

import com.example.tooltestingdemo.entity.template.TemplateExecuteLog;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TraceId 链路详情。
 */
@Data
public class TraceChainDetailVO {

    private String traceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long totalDurationMs;

    private Boolean success;

    private Integer jobLogCount;

    private Integer executeLogCount;

    private Integer successCount;

    private Integer failCount;

    private List<TemplateJobLog> jobLogs;

    private List<TemplateExecuteLog> executeLogs;
}
