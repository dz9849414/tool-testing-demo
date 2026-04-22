package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板导入导出记录实体类
 */
@Data
@TableName("pdm_tool_template_import_export")
public class TemplateImportExport {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "operation_type")
    private String operationType;

    @TableField(value = "template_ids")
    private String templateIds;

    @TableField(value = "folder_id")
    private Long folderId;

    @TableField(value = "file_name")
    private String fileName;

    @TableField(value = "file_path")
    private String filePath;

    @TableField(value = "file_size")
    private Long fileSize;

    @TableField(value = "file_format")
    private String fileFormat;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "success_count")
    private Integer successCount;

    @TableField(value = "fail_count")
    private Integer failCount;

    @TableField(value = "error_message")
    private String errorMessage;

    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_id")
    private Long updateId;

    @TableField(value = "update_name")
    private String updateName;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(value = "deleted_by")
    private Long deletedBy;

    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;
}