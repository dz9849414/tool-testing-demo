package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 模板定时任务子项（一个任务可配置多个模板）
 */
@Data
@TableName("pdm_tool_template_job_item")
public class TemplateJobItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "job_id")
    private Long jobId;

    /**
     * 关联模板ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 关联环境ID
     */
    @TableField(value = "environment_id")
    private Long environmentId;

    /**
     * 执行变量（JSON格式）
     */
    @TableField(value = "variables")
    private String variables;

    /**
     * 执行顺序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-停用 1-启用
     */
    @TableField(value = "status")
    private Integer status;
}
