package com.example.tooltestingdemo.entity.template;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板文件附件实体类
 *
 * 存储模板关联的文件（请求文件、响应文件、附件等）
 */
@Data
@TableName("pdm_tool_template_file")
public class TemplateFile {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板ID
     */
    @TableField(value = "template_id")
    private Long templateId;

    /**
     * 文件名（存储后的文件名）
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 原始文件名
     */
    @TableField(value = "file_original_name")
    private String fileOriginalName;

    /**
     * 文件存储路径
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 文件访问URL
     */
    @TableField(value = "file_url")
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * 文件扩展名
     */
    @TableField(value = "file_extension")
    private String fileExtension;

    /**
     * 文件类别：ATTACHMENT-附件/REQUEST-请求文件/RESPONSE-响应文件
     */
    @TableField(value = "file_category")
    private String fileCategory;

    /**
     * 文件描述
     */
    @TableField(value = "file_description")
    private String fileDescription;

    /**
     * 排序号
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 是否删除
     */
    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    /**
     * 创建人ID
     */
    @TableField(value = "create_id")
    private Long createId;

    /**
     * 创建人姓名
     */
    @TableField(value = "create_name")
    private String createName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改人ID
     */
    @TableField(value = "update_id")
    private Long updateId;

    /**
     * 修改人姓名
     */
    @TableField(value = "update_name")
    private String updateName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 删除人ID
     */
    @TableField(value = "deleted_by")
    private Long deletedBy;

    /**
     * 删除时间（软删除）
     */
    @TableField(value = "deleted_time")
    private LocalDateTime deletedTime;

    // ==================== 常量定义 ====================

    /**
     * 文件类别：附件
     */
    public static final String CATEGORY_ATTACHMENT = "ATTACHMENT";

    /**
     * 文件类别：请求文件
     */
    public static final String CATEGORY_REQUEST = "REQUEST";

    /**
     * 文件类别：响应文件
     */
    public static final String CATEGORY_RESPONSE = "RESPONSE";
}