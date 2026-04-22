package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板定时任务执行日志
 */
@Data
@TableName("pdm_tool_template_job_log")
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

    @TableField(value = "execute_at", fill = FieldFill.INSERT)
    private LocalDateTime executeAt;

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_id", fill = FieldFill.UPDATE)
    private Long updateId;

    @TableField(value = "update_name", fill = FieldFill.UPDATE)
    private String updateName;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    @TableLogic
    private Integer isDeleted;

    @TableField(value = "deleted_by")
    private Long deletedBy;

    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
}
