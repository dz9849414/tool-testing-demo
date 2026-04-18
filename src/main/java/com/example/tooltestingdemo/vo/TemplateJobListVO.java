package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务列表展示 VO（附带最近一次执行状态）
 */
@Data
public class TemplateJobListVO {

    private Long id;

    private String jobName;

    private String cronExpression;

    private Integer status;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastExecuteTime;

    /**
     * 最近一次执行是否成功：0-否 1-是 null-未执行
     */
    private Integer lastExecuteSuccess;

    /**
     * 最近一次执行耗时（ms）
     */
    private Long lastExecuteDurationMs;

    /**
     * 最近一次执行结果摘要
     */
    private String lastExecuteSummary;

    /**
     * 当前是否正在执行中
     */
    private Boolean executing;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
