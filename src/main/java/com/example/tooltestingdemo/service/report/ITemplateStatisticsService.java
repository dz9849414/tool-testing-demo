package com.example.tooltestingdemo.service.report;

import com.example.tooltestingdemo.dto.report.ReportDTO;
import com.example.tooltestingdemo.dto.report.StatisticsReportDTO;

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
     * @param dataSource 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
     * @return 模板使用频率报告
     */
    ReportDTO getTemplateUsageReport(String timeRange, String startDate, String endDate, String templateType, String dataSource);
    
    /**
     * 获取模板执行效率报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param templateId 模板ID筛选（可选）
     * @param dataSource 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
     * @return 模板执行效率报告
     */
    ReportDTO getTemplateEfficiencyReport(String startDate, String endDate, String templateId, String dataSource);
    
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

    /**
     * 获取每2小时平均响应时间报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dataSource 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
     * @return 每2小时平均响应时间报告
     */
    StatisticsReportDTO getHourlyResponseTimeReport(String startDate, String endDate, String dataSource);

    /**
     * 获取周一到周日执行量统计报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dataSource 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）
     * @return 周一到周日执行量统计报告
     */
    StatisticsReportDTO getWeeklyExecutionReport(String startDate, String endDate, String dataSource);

    /**
     * 获取成功率分析报告（成功失败占比）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dataSource 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
     * @return 成功率分析报告
     */
    StatisticsReportDTO getSuccessRateReport(String startDate, String endDate, String dataSource);

    /**
     * 获取协议类型分布统计报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param reportType 报告类型：CATEGORY（按分类）/DETAIL（按具体协议）/TEST_TYPE（按测试类型）
     * @return 协议类型分布统计报告
     */
    StatisticsReportDTO getProtocolDistributionReport(String startDate, String endDate, String reportType);

    /**
     * 获取前5的失败原因统计报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param dataSource 数据源：JOB_LOG（定时任务）/UNIFIED（手动+定时）/BATCH（批量任务）
     * @return 前5的失败原因统计报告
     */
    StatisticsReportDTO getTopFailureReasonsReport(String startDate, String endDate, String dataSource);
}