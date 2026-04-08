package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板历史版本实体类
 */
@Data
@TableName("template_history")
public class TemplateHistory {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "template_id")
    private Long templateId;

    @TableField(value = "version")
    private String version;

    @TableField(value = "version_type")
    private String versionType;

    @TableField(value = "change_summary")
    private String changeSummary;

    @TableField(value = "change_details")
    private String changeDetails;

    @TableField(value = "template_snapshot")
    private String templateSnapshot;

    @TableField(value = "operator_id", fill = FieldFill.INSERT)
    private Long operatorId;

    @TableField(value = "operator_name", fill = FieldFill.INSERT)
    private String operatorName;

    @TableField(value = "operation_type")
    private String operationType;

    @TableField(value = "can_rollback")
    private Integer canRollback;

    @TableField(value = "rollback_to_time")
    private LocalDateTime rollbackToTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
