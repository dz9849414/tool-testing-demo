package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板定时任务配置
 */
@Data
@TableName("pdm_tool_template_job")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateJob {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @TableField(value = "job_name")
    private String jobName;

    /**
     * Cron表达式
     */
    @TableField(value = "cron_expression")
    private String cronExpression;

    /**
     * 状态：0-停用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 任务描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * XXL-JOB任务ID
     */
    @TableField(value = "xxl_job_id")
    private Integer xxlJobId;

    /**
     * 上次执行时间
     */
    @TableField(value = "last_execute_time")
    private LocalDateTime lastExecuteTime;

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

    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    /**
     * 任务子项列表（非数据库字段）
     */
    @TableField(exist = false)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<TemplateJobItem> items;

    /**
     * 是否是状态修改
     */
    @TableField(exist = false)
    private boolean updateStatus;
}
