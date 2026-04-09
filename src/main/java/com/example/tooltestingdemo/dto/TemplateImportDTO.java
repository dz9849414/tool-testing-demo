package com.example.tooltestingdemo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 模板导入请求DTO
 */
@Data
public class TemplateImportDTO {

    /**
     * 导入文件
     */
    private MultipartFile file;

    /**
     * 导入格式：JSON/YAML/POSTMAN/OPENAPI
     * 如果不指定，则根据文件扩展名自动识别
     */
    private String format;

    /**
     * 目标文件夹ID
     */
    private Long folderId;

    /**
     * 导入策略：
     * - SKIP: 跳过重复（默认）
     * - OVERWRITE: 覆盖已有
     * - RENAME: 重命名导入
     */
    private String strategy;

    /**
     * 是否同时导入环境配置
     */
    private Boolean importEnvironments;

    /**
     * 是否同时导入变量
     */
    private Boolean importVariables;
}
