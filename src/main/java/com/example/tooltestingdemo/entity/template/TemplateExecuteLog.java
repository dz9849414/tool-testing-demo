package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板执行统一日志表（手动执行 + 定时任务执行）
 */
@Data
@TableName("template_execute_log")
public class TemplateExecuteLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 模板名称
     */
    @TableField(value = "template_name")
    private String templateName;

    /**
     * 关联任务ID（定时任务时填充）
     */
    @TableField(value = "job_id")
    private Long jobId;

    /**
     * 任务名称（定时任务时填充）
     */
    @TableField(value = "job_name")
    private String jobName;

    /**
     * 执行方式：MANUAL-手动执行 JOB-定时任务
     */
    @TableField(value = "execute_type")
    private String executeType;

    /**
     * 环境ID
     */
    @TableField(value = "environment_id")
    private Long environmentId;

    /**
     * 是否成功：0-否 1-是
     */
    @TableField(value = "success")
    private Integer success;

    /**
     * HTTP 状态码
     */
    @TableField(value = "status_code")
    private Integer statusCode;

    /**
     * 执行耗时（ms）
     */
    @TableField(value = "duration_ms")
    private Long durationMs;

    /**
     * 执行结果（JSON）
     */
    @TableField(value = "execute_result")
    private String executeResult;

    /**
     * 错误信息
     */
    @TableField(value = "error_msg")
    private String errorMsg;

    /**
     * 执行人ID（手动执行时填充）
     */
    @TableField(value = "create_id")
    private Long createId;

    /**
     * 执行人姓名
     */
    @TableField(value = "create_name")
    private String createName;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
