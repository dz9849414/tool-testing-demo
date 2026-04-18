package com.example.tooltestingdemo.dto.report;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 自动生成报告配置DTO
 */
@Data
public class AutoReportConfigDTO {
    
    /** 报告名称 */
    private String reportName;
    
    /** 报告类型 */
    private String reportType;
    
    /** 模板ID */
    private Long templateId;
    
    /** 数据源配置 */
    private Map<String, Object> dataSourceConfig;
    
    /** 生成频率：ONCE/DAILY/WEEKLY/MONTHLY */
    private String frequency;
    
    /** 生成时间（针对定时生成） */
    private String scheduleTime;
    
    /** 开始日期 */
    private LocalDateTime startDate;
    
    /** 结束日期 */
    private LocalDateTime endDate;
    
    /** 是否启用 */
    private Boolean enabled;
    
    /** 通知方式：SYSTEM/EMAIL/BOTH */
    private String notifyType;
    
    /** 接收人列表 */
    private String recipients;
    
    /** 筛选条件 */
    private Map<String, Object> filters;
}