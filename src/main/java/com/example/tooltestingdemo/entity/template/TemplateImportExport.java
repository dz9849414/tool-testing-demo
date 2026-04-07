package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板导入导出记录实体类
 */
@Data
@TableName("template_import_export")
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

    @TableField(value = "operator_id")
    private Long operatorId;

    @TableField(value = "operator_name")
    private String operatorName;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
