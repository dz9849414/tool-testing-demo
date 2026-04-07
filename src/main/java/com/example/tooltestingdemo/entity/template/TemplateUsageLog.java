package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板使用记录实体类
 */
@Data
@TableName("template_usage_log")
public class TemplateUsageLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "usage_type")
    private String usageType;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "user_name")
    private String userName;

    @TableField(value = "task_id")
    private Long taskId;

    @TableField(value = "execution_result")
    private Integer executionResult;

    @TableField(value = "execution_duration")
    private Integer executionDuration;

    @TableField(value = "request_summary")
    private String requestSummary;

    @TableField(value = "response_summary")
    private String responseSummary;

    @TableField(value = "error_message")
    private String errorMessage;

    @TableField(value = "client_ip")
    private String clientIp;

    @TableField(value = "user_agent")
    private String userAgent;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
