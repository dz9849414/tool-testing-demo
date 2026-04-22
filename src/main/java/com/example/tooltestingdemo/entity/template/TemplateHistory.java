package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板历史版本实体类
 */
@Data
@TableName("pdm_tool_template_history")
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

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "operation_type")
    private String operationType;

    @TableField(value = "can_rollback")
    private Integer canRollback;

    @TableField(value = "rollback_to_time")
    private LocalDateTime rollbackToTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_id")
    private Long updateId;

    @TableField(value = "update_name")
    private String updateName;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    @TableField(value = "deleted_by")
    private Long deletedBy;

    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
}