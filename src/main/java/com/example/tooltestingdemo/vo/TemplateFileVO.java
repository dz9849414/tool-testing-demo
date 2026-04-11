package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板文件附件 VO
 */
@Data
public class TemplateFileVO {

    private Long id;

    private Long templateId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String fileOriginalName;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小（格式化显示，如：1.5MB）
     */
    private String fileSizeDisplay;

    /**
     * 文件MIME类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件类别：ATTACHMENT-附件/REQUEST-请求文件/RESPONSE-响应文件
     */
    private String fileCategory;

    /**
     * 文件描述
     */
    private String fileDescription;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建人姓名
     */
    private String createName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
