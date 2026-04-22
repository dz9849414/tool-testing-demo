package com.example.tooltestingdemo.dto.report;

import com.alibaba.fastjson2.JSONArray;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计报告DTO - 专门用于统计接口，content字段为JSONArray格式
 * 适用接口：
 * - 每2小时统计
 * - 执行量统计（按日期）
 * - 成功率分析
 * - 协议类型分布
 * - 获取前5的失败原因统计报告
 */
@Data
public class StatisticsReportDTO {
    
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
    
    /** 报告内容（JSONArray格式存储） */
    private JSONArray content;
    
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
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}