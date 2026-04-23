package com.example.tooltestingdemo.dto.report;

import lombok.Data;

/**
 * 自动生成报告请求参数
 */
@Data
public class AutoGenerateReportRequest {
    
    /** 报告类型 */
    private String reportType;
    
    /** 数据源ID列表 */
    private String dataSourceIds;
    
    /** 报告名称 */
    private String reportName;
    
    /** 报告描述 */
    private String reportDescription;
    
    /** 时间范围 */
    private String timeRange;
    
    /** 模板ID */
    private Long templateId;
    
    /** 样式配置 */
    private String styleConfig;
}