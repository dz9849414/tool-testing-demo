package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板定时任务执行日志
 */
@Data
@TableName("template_job_log")
public class TemplateJobLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "job_id")
    private Long jobId;

    /**
     * 模板ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * XXL-JOB日志ID
     */
    @TableField(value = "xxl_job_log_id")
    private Long xxlJobLogId;

    /**
     * 执行结果（JSON）
     */
    @TableField(value = "execute_result")
    private String executeResult;

    /**
     * 是否成功：0-否 1-是
     */
    @TableField(value = "success")
    private Integer success;

    /**
     * 执行耗时（ms）
     */
    @TableField(value = "duration_ms")
    private Long durationMs;

    /**
     * 错误信息
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 链路追踪ID
     */
    @TableField(value = "trace_id")
    private String traceId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
