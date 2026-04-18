package com.example.tooltestingdemo.service.report;

import com.example.tooltestingdemo.dto.report.ReportDTO;

/**
 * 模板统计服务接口
 */
public interface ITemplateStatisticsService {
    
    /**
     * 获取模板使用频率报告
     * 
     * @param timeRange 时间范围：TODAY/7DAYS/30DAYS/CUSTOM
     * @param startDate 开始日期（当timeRange为CUSTOM时使用）
     * @param endDate 结束日期（当timeRange为CUSTOM时使用）
     * @param templateType 模板类型筛选
     * @return 模板使用频率报告
     */
    ReportDTO getTemplateUsageReport(String timeRange, String startDate, String endDate, String templateType);
    
    /**
     * 获取模板执行效率报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param templateId 模板ID筛选（可选）
     * @return 模板执行效率报告
     */
    ReportDTO getTemplateEfficiencyReport(String startDate, String endDate, String templateId);
    
    /**
     * 生成模板使用统计图表数据
     * 
     * @param timeRange 时间范围
     * @param chartType 图表类型：BAR/LINE/PIE
     * @return 图表数据
     */
    Object generateTemplateUsageChart(String timeRange, String chartType);
    
    /**
     * 生成模板效率统计图表数据
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param chartType 图表类型：BAR/LINE
     * @return 图表数据
     */
    Object generateTemplateEfficiencyChart(String startDate, String endDate, String chartType);
}