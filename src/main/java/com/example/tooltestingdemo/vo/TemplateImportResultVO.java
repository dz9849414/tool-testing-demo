package com.example.tooltestingdemo.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板导入结果VO
 */
@Data
@Builder
public class TemplateImportResultVO {

    /**
     * 导入是否成功
     */
    private Boolean success;

    /**
     * 导入消息
     */
    private String message;

    /**
     * 导入总数
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 跳过的数量（重复）
     */
    private Integer skipCount;

    /**
     * 导入的模板列表
     */
    private List<ImportedTemplateVO> importedTemplates;

    /**
     * 错误详情
     */
    private List<ImportErrorVO> errors;

    /**
     * 导入时间
     */
    private LocalDateTime importTime;

    /**
     * 导入的模板信息
     */
    @Data
    @Builder
    public static class ImportedTemplateVO {
        private Long templateId;
        private String templateName;
        private String originalName;
        private String status; // CREATED/UPDATED/SKIPPED
    }

    /**
     * 导入错误信息
     */
    @Data
    @Builder
    public static class ImportErrorVO {
        private String templateName;
        private String errorMessage;
        private Integer rowNumber;
    }
}
