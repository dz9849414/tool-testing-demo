package com.example.tooltestingdemo.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告DTO
 */
@Data
public class ReportDTO {
    
    private Long id;
    
    /** 报告名称 */
    private String name;
    
    /** 报告描述 */
    private String description;
    
    /** 报告类型 */
    private String reportType;
    
    /** 关联的模板ID */
    private Long templateId;
    
    /** 模板名称 */
    private String templateName;
    
    /** 报告内容（JSON格式存储） */
    private String content;
    
    /** 报告样式配置 */
    private String styleConfig;
    
    /** 生成方式：AUTO/MANUAL */
    private String generateType;
    
    /** 生成频率：ONCE/DAILY/WEEKLY/MONTHLY */
    private String generateFrequency;
    
    /** 下次生成时间 */
    private LocalDateTime nextGenerateTime;
    
    /** 关联的数据源ID */
    private String dataSourceIds;
    
    /** 关联的图表ID */
    private String chartIds;
    
    /** 报告状态：DRAFT/PUBLISHED/ARCHIVED */
    private String status;
    
    /** 是否定时生成 */
    private Boolean isScheduled;
    
    /** 是否已推送通知 */
    private Boolean isNotified;
    
    /** 导出次数 */
    private Integer exportCount;
    
    /** 最后导出时间 */
    private LocalDateTime lastExportTime;
    
    /** 创建人姓名 */
    private String createName;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 修改时间 */
    private LocalDateTime updateTime;
}