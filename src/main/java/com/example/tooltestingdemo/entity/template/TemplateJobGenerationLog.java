package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板任务批量生成记录
 */
@Data
@TableName("pdm_tool_template_job_generation_log")
public class TemplateJobGenerationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "generate_count")
    private Integer generateCount;

    @TableField(value = "job_name_prefix")
    private String jobNamePrefix;

    @TableField(value = "job_ids")
    private String jobIds;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "message")
    private String message;

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
}
