package com.example.tooltestingdemo.dto.report;

import lombok.Data;

/**
 * 统计对比请求DTO
 */
@Data
public class CompareRequestDTO {
    
    /**
     * 报告类型（与metricType一致）
     * WEEKLY_EXECUTION - 日执行量统计
     * SUCCESS_RATE - 成功率分析
     * RESPONSE_TIME - 平均响应时间
     * PROTOCOL_DISTRIBUTION - 协议类型分布
     * FAILURE_REASONS - 失败原因TOP5
     */
    private String reportType;
    
    /**
     * 指标类型
     * WEEKLY_EXECUTION - 日执行量统计
     * SUCCESS_RATE - 成功率分析
     * RESPONSE_TIME - 平均响应时间
     * PROTOCOL_DISTRIBUTION - 协议类型分布
     * FAILURE_REASONS - 失败原因TOP5
     */
    private String metricType;
    
    /**
     * 对比组1开始日期 (yyyy-MM-dd)
     */
    private String group1StartDate;
    
    /**
     * 对比组1结束日期 (yyyy-MM-dd)
     */
    private String group1EndDate;
    
    /**
     * 对比组2开始日期 (yyyy-MM-dd)
     */
    private String group2StartDate;
    
    /**
     * 对比组2结束日期 (yyyy-MM-dd)
     */
    private String group2EndDate;
    
    /**
     * 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
     */
    private String dataSource;
}