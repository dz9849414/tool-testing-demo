package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板任务自动化配置
 */
@Data
@TableName("pdm_tool_template_job_automation_config")
public class TemplateJobAutomationConfig {

    @TableId(value = "job_id")
    private Long jobId;

    @TableField(value = "concurrent_config")
    private String concurrentConfig;

    @TableField(value = "script_config")
    private String scriptConfig;

    @TableField(value = "log_config")
    private String logConfig;

    @TableField(value = "report_config")
    private String reportConfig;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}
