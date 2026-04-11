package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 模板文件附件 DTO
 */
@Data
public class TemplateFileDTO {

    private Long id;

    /**
     * 模板ID
     */
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
     * 文件大小（字节）
     */
    private Long fileSize;

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
}
