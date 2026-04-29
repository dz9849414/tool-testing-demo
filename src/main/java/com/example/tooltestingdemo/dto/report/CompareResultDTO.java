package com.example.tooltestingdemo.dto.report;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计对比结果DTO
 */
@Data
public class CompareResultDTO {
    
    /**
     * 报告类型
     */
    private String reportType;
    
    /**
     * 指标类型
     */
    private String metricType;
    
    /**
     * 指标类型名称
     */
    private String metricTypeName;
    
    /**
     * 对比组1数据 [{value: xxx, name: xxx}, ...]
     */
    private List<Map<String, Object>> data1;
    
    /**
     * 对比组2数据 [{value: xxx, name: xxx}, ...]
     */
    private List<Map<String, Object>> data2;
    
    /**
     * 对比组1摘要
     */
    private Map<String, Object> summary1;
    
    /**
     * 对比组2摘要
     */
    private Map<String, Object> summary2;
    
    /**
     * 对比摘要（可选）
     */
    private Map<String, Object> summary;
    
    /**
     * 生成时间
     */
    private LocalDateTime generateTime;
}