package com.example.tooltestingdemo.service.impl.report;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.dto.report.CompareResultDTO;
import com.example.tooltestingdemo.dto.report.FailureTimelineDTO;
import com.example.tooltestingdemo.dto.report.ReportDTO;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
import com.example.tooltestingdemo.dto.report.StatisticsReportDTO;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.mapper.template.TemplateStatisticsMapper;
import com.example.tooltestingdemo.service.report.ITemplateStatisticsService;
import com.example.tooltestingdemo.service.report.IReportTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 模板统计服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateStatisticsServiceImpl implements ITemplateStatisticsService {

    private final TemplateStatisticsMapper templateStatisticsMapper;
    private final IReportTemplateService reportTemplateService;

    @Override
    public ReportDTO getTemplateUsageReport(String timeRange, String startDate, String endDate, String templateType, String dataSource) {
        log.info("获取模板使用频率报告，时间范围：{}，模板类型：{}，数据源：{}", timeRange, templateType, dataSource);
        
        // 计算时间范围
        LocalDate[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        
        // 根据数据源选择不同的统计方法
        List<Map<String, Object>> usageStats;
        switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
            case "BATCH":
                usageStats = templateStatisticsMapper.getBatchJobStats(
                    dateRange[0].atStartOfDay(), dateRange[1].atTime(23, 59, 59));
                break;
            case "UNIFIED":
                usageStats = templateStatisticsMapper.getUnifiedExecutionStats(
                    dateRange[0].atStartOfDay(), dateRange[1].atTime(23, 59, 59));
                break;
            case "JOB_LOG":
            default:
                usageStats = templateStatisticsMapper.getTemplateUsageStats(
                    dateRange[0].atStartOfDay(), dateRange[1].atTime(23, 59, 59));
                break;
        }
        
        ReportDTO report = new ReportDTO();
        report.setId(System.currentTimeMillis());
        
        // 根据数据源设置不同的报告名称和描述
        String dataSourceDisplay = getDataSourceDisplayName(dataSource);
        report.setName("模板使用频率报告 - " + dataSourceDisplay + " - " + getTimeRangeDisplay(timeRange));
        report.setDescription("基于" + dataSourceDisplay + "生成的模板使用统计报告");
        report.setReportType("TEMPLATE_USAGE_STATISTICS");
        
        // 根据数据源选择不同的数据处理方法
        Map<String, Object> reportData;
        switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
            case "BATCH":
                reportData = buildBatchUsageReportData(usageStats, dateRange);
                break;
            case "UNIFIED":
                reportData = buildUnifiedUsageReportData(usageStats, dateRange);
                break;
            case "JOB_LOG":
            default:
                reportData = buildJobLogUsageReportData(usageStats, dateRange);
                break;
        }
        
        report.setContent(convertToJson(reportData));
        report.setGenerateType("MANUAL");
        report.setStatus("PUBLISHED");
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        
        return report;
    }

    @Override
    public ReportDTO getTemplateEfficiencyReport(String startDate, String endDate, String templateId, String dataSource) {
        log.info("获取模板执行效率报告，时间范围：{} 至 {}，模板ID：{}，数据源：{}", startDate, endDate, templateId, dataSource);
        
        // 处理时间参数为空的情况
        LocalDateTime startTime;
        LocalDateTime endTime;
        
        if (startDate == null) {
            // 如果不传开始时间，默认使用30天前
            startTime = LocalDateTime.now().minusDays(30);
        } else {
            startTime = LocalDate.parse(startDate).atStartOfDay();
        }
        
        if (endDate == null) {
            // 如果不传结束时间，默认使用当前时间
            endTime = LocalDateTime.now();
        } else {
            endTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        }
        
        // 根据数据源选择不同的统计方法
        List<Map<String, Object>> efficiencyStats;
        switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
            case "BATCH":
                efficiencyStats = templateStatisticsMapper.getBatchJobStats(startTime, endTime);
                break;
            case "UNIFIED":
                if (templateId != null && !templateId.trim().isEmpty()) {
                    efficiencyStats = templateStatisticsMapper.getUnifiedExecutionStatsByTemplateId(
                        startTime, endTime, Long.valueOf(templateId));
                } else {
                    efficiencyStats = templateStatisticsMapper.getUnifiedExecutionStats(startTime, endTime);
                }
                break;
            case "JOB_LOG":
            default:
                if (templateId != null && !templateId.trim().isEmpty()) {
                    efficiencyStats = templateStatisticsMapper.getTemplateEfficiencyStatsByTemplateId(
                        startTime, endTime, Long.valueOf(templateId));
                } else {
                    efficiencyStats = templateStatisticsMapper.getTemplateEfficiencyStats(startTime, endTime);
                }
                break;
        }
        
        // 构建报告数据
        Map<String, Object> reportData = buildEfficiencyReportData(efficiencyStats, startTime.toLocalDate().toString(), endTime.toLocalDate().toString());
        
        // 如果指定了模板ID，使用模板生成报告内容和格式
        ReportDTO report = new ReportDTO();
        report.setId(System.currentTimeMillis());
        
        if (templateId != null && !templateId.trim().isEmpty()) {
            // 使用模板生成报告
            ReportTemplateDTO template = reportTemplateService.getTemplateDetail(Long.valueOf(templateId));
            if (template != null) {
                // 使用模板的名称、描述和格式
                report.setName(template.getName());
                report.setDescription(template.getDescription());
                report.setReportType(template.getTemplateType());
                
                // 使用模板结构格式化报告数据
                String formattedContent = applyTemplateFormatToString(template.getTemplateStructure(), reportData);
                report.setContent(formattedContent);
            } else {
                // 模板不存在，使用默认格式
                report.setName("模板执行效率报告 - 模板" + templateId + " - " + startTime.toLocalDate() + " 至 " + endTime.toLocalDate());
                report.setDescription("基于模板" + templateId + "执行日志生成的效率分析报告");
                report.setReportType("TEMPLATE_EFFICIENCY_STATISTICS");
                report.setContent(convertToJson(reportData));
            }
        } else {
            // 没有指定模板ID，使用默认格式
            report.setName("模板执行效率报告 - " + startTime.toLocalDate() + " 至 " + endTime.toLocalDate());
            report.setDescription("基于所有模板执行日志生成的效率分析报告");
            report.setReportType("TEMPLATE_EFFICIENCY_STATISTICS");
            report.setContent(convertToJson(reportData));
        }
        
        report.setGenerateType("MANUAL");
        report.setStatus("PUBLISHED");
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        
        return report;
    }

    @Override
    public Object generateTemplateUsageChart(String timeRange, String chartType) {
        log.info("生成模板使用统计图表，时间范围：{}，图表类型：{}", timeRange, chartType);
        
        LocalDate[] dateRange = calculateDateRange(timeRange, null, null);
        List<Map<String, Object>> usageStats = templateStatisticsMapper.getTemplateUsageStats(
            dateRange[0].atStartOfDay(), dateRange[1].atTime(23, 59, 59));
        
        return buildUsageChartData(usageStats, chartType, timeRange);
    }

    @Override
    public Object generateTemplateEfficiencyChart(String startDate, String endDate, String chartType) {
        log.info("生成模板效率统计图表，时间范围：{} 至 {}，图表类型：{}", startDate, endDate, chartType);
        
        // 处理时间参数为空的情况
        LocalDateTime startTime;
        LocalDateTime endTime;
        
        if (startDate == null ) {
            // 如果不传开始时间，默认使用30天前
            startTime = LocalDateTime.now().minusDays(30);
        } else {
            startTime = LocalDate.parse(startDate).atStartOfDay();
        }
        
        if (endDate == null ) {
            // 如果不传结束时间，默认使用当前时间
            endTime = LocalDateTime.now();
        } else {
            endTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        }
        
        List<Map<String, Object>> efficiencyStats = templateStatisticsMapper.getTemplateEfficiencyStats(startTime, endTime);
        
        return buildEfficiencyChartData(efficiencyStats, chartType, startTime.toLocalDate().toString(), endTime.toLocalDate().toString());
    }

    // ====================== 基于实际数据的辅助方法 ======================

    private LocalDate[] calculateDateRange(String timeRange, String startDate, String endDate) {
        LocalDate endDateObj = LocalDate.now();
        LocalDate startDateObj;
        
        switch (timeRange) {
            case "TODAY":
                startDateObj = LocalDate.now();
                break;
            case "7DAYS":
                startDateObj = LocalDate.now().minusDays(7);
                break;
            case "30DAYS":
                startDateObj = LocalDate.now().minusDays(30);
                break;
            case "CUSTOM":
                startDateObj = LocalDate.parse(startDate);
                endDateObj = LocalDate.parse(endDate);
                break;
            default:
                startDateObj = LocalDate.now().minusDays(7);
        }
        
        return new LocalDate[]{startDateObj, endDateObj};
    }

    private Map<String, Object> buildUsageReportData(List<Map<String, Object>> usageStats, LocalDate[] dateRange) {
        Map<String, Object> reportData = new HashMap<>();
        
        int totalUsageCount = 0;
        int totalSuccessCount = 0;
        long totalDuration = 0;
        
        List<Map<String, Object>> templateStats = new ArrayList<>();
        
        for (Map<String, Object> stat : usageStats) {
            // 直接使用BigDecimal进行计算，避免精度损失
            BigDecimal templateId = (BigDecimal) stat.get("template_id");
            BigDecimal usageCount = BigDecimal.valueOf((Long)stat.get("usage_count"));
            BigDecimal successCount = (BigDecimal) stat.get("success_count");
            BigDecimal avgDuration = (BigDecimal) stat.get("avg_duration");
            
            Map<String, Object> templateStat = new HashMap<>();
            templateStat.put("templateId", templateId != null ? templateId.longValue() : null);
            templateStat.put("templateName", "模板" + (templateId != null ? templateId.longValue() : "未知")); // 实际项目中需要关联模板表获取名称
            templateStat.put("usageCount", usageCount != null ? usageCount.longValue() : 0);
            templateStat.put("successCount", successCount != null ? successCount.longValue() : 0);
            templateStat.put("failureCount", usageCount != null && successCount != null ? 
                usageCount.subtract(successCount).longValue() : 0);
            templateStat.put("successRate", usageCount != null && successCount != null && usageCount.compareTo(BigDecimal.ZERO) > 0 ? 
                successCount.divide(usageCount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue() : 0);
            templateStat.put("avgExecuteTime", avgDuration != null ? avgDuration.longValue() : 0);
            
            templateStats.add(templateStat);
            totalUsageCount += usageCount != null ? usageCount.longValue() : 0;
            totalSuccessCount += successCount != null ? successCount.longValue() : 0;
            totalDuration += avgDuration != null ? avgDuration.doubleValue() * (usageCount != null ? usageCount.longValue() : 0) : 0;
        }
        
        reportData.put("usageStats", templateStats);
        reportData.put("totalUsageCount", totalUsageCount);
        reportData.put("totalSuccessCount", totalSuccessCount);
        reportData.put("avgSuccessRate", totalUsageCount > 0 ? (double) totalSuccessCount / totalUsageCount * 100 : 0);
        reportData.put("avgExecuteTime", totalUsageCount > 0 ? totalDuration / totalUsageCount : 0);
        reportData.put("startTime", dateRange[0].format(DateTimeFormatter.ISO_DATE));
        reportData.put("endTime", dateRange[1].format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    private Map<String, Object> buildEfficiencyReportData(List<Map<String, Object>> efficiencyStats, String startDate, String endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        int totalJobs = 0;
        int totalSuccess = 0;
        long totalDuration = 0;
        
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        
        for (Map<String, Object> stat : efficiencyStats) {
            String date = String.valueOf( stat.get("date"));
            Long jobCount = (Long)stat.get("job_count");
            BigDecimal successCount = (BigDecimal)stat.get("success_count");
            BigDecimal avgDuration = (BigDecimal)stat.get("avg_duration");
            Long maxDuration = (Long)stat.get("max_duration");
            Long minDuration = (Long)stat.get("min_duration");
            
            Map<String, Object> dailyStat = new HashMap<>();
            dailyStat.put("date", date);
            dailyStat.put("jobCount", jobCount != null ? jobCount.longValue() : 0);
            dailyStat.put("successCount", successCount != null ? successCount.longValue() : 0);
            dailyStat.put("failureCount", jobCount != null && successCount != null ? 
                BigDecimal.valueOf(jobCount).subtract(successCount).longValue() : 0);
            dailyStat.put("successRate", jobCount != null && successCount != null && jobCount.compareTo(0l) > 0 ?
                successCount.divide(BigDecimal.valueOf(jobCount), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue() : 0);
            dailyStat.put("avgDuration", avgDuration != null ? avgDuration.longValue() : 0);
            dailyStat.put("maxDuration", maxDuration != null ? maxDuration.longValue() : 0);
            dailyStat.put("minDuration", minDuration != null ? minDuration.longValue() : 0);
            
            dailyStats.add(dailyStat);
            totalJobs += jobCount != null ? jobCount.longValue() : 0;
            totalSuccess += successCount != null ? successCount.longValue() : 0;
            totalDuration += avgDuration != null ? avgDuration.doubleValue() * (jobCount != null ? jobCount.longValue() : 0) : 0;
        }
        
        reportData.put("efficiencyStats", dailyStats);
        reportData.put("totalJobs", totalJobs);
        reportData.put("totalSuccess", totalSuccess);
        reportData.put("overallSuccessRate", totalJobs > 0 ? (double) totalSuccess / totalJobs * 100 : 0);
        reportData.put("avgDuration", totalJobs > 0 ? totalDuration / totalJobs : 0);
        reportData.put("reportPeriod", dailyStats.size() + " 天");
        reportData.put("startDate", startDate);
        reportData.put("endDate", endDate);
        reportData.put("generateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return reportData;
    }

    private String convertToJson(Object data) {
        // 简化版的JSON转换，使用Gson或其他JSON库
        try {
            // 如果项目中有JSON库，可以使用相应的转换方法
            // 这里使用简单的toString()作为临时解决方案
            return data.toString();
        } catch (Exception e) {
            // 如果转换失败，返回空字符串
            return "{}";
        }
    }

    private int getBaseUsageCount(String timeRange) {
        switch (timeRange) {
            case "TODAY": return 5;
            case "7DAYS": return 20;
            case "30DAYS": return 50;
            case "CUSTOM": return 30;
            default: return 10;
        }
    }

    private String getTimeRangeDisplay(String timeRange) {
        switch (timeRange) {
            case "TODAY": return "今日";
            case "7DAYS": return "近7天";
            case "30DAYS": return "近30天";
            case "CUSTOM": return "自定义";
            default: return "默认";
        }
    }

    private Date parseDate(String dateStr) {
        try {
            return java.sql.Date.valueOf(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }

    private String getDefaultStartDate(String timeRange) {
        LocalDate today = LocalDate.now();
        switch (timeRange) {
            case "TODAY": return today.toString();
            case "7DAYS": return today.minusDays(7).toString();
            case "30DAYS": return today.minusDays(30).toString();
            default: return today.minusDays(7).toString();
        }
    }

    private Map<String, Object> generateBarChartData(String timeRange) {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("HTTP接口", "数据库", "ERP", "文件处理", "数据转换"));
        data.put("values", Arrays.asList(45, 32, 28, 15, 20));
        data.put("title", "模板使用频率统计");
        return data;
    }

    private Map<String, Object> generateLineChartData(String timeRange) {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("Day1", "Day2", "Day3", "Day4", "Day5", "Day6", "Day7"));
        data.put("values", Arrays.asList(12, 19, 15, 22, 18, 25, 20));
        data.put("title", "模板使用趋势");
        return data;
    }

    private Map<String, Object> generatePieChartData(String timeRange) {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("HTTP接口", "数据库", "ERP", "文件处理", "数据转换"));
        data.put("values", Arrays.asList(35, 25, 20, 10, 10));
        data.put("title", "模板类型分布");
        return data;
    }

    private Map<String, Object> buildBatchUsageReportData(List<Map<String, Object>> usageStats, LocalDate[] dateRange) {
        // 构建批处理作业的统计报告数据
        Map<String, Object> reportData = new HashMap<>();
        
        int totalBatchJobs = 0;
        int totalBatchSuccess = 0;
        
        List<Map<String, Object>> batchStats = new ArrayList<>();
        
        for (Map<String, Object> stat : usageStats) {
            String date = safeGetString(stat, "date");
            BigDecimal batchCount = safeGetBigDecimal(stat, "batch_count");
            BigDecimal successCount = safeGetBigDecimal(stat, "success_count");
            BigDecimal successRate = safeGetBigDecimal(stat, "success_rate");
            
            Map<String, Object> batchStat = new HashMap<>();
            batchStat.put("date", date);
            batchStat.put("batchCount", batchCount != null ? batchCount.longValue() : 0);
            batchStat.put("successCount", successCount != null ? successCount.longValue() : 0);
            batchStat.put("failureCount", batchCount != null && successCount != null ? 
                batchCount.subtract(successCount).longValue() : 0);
            batchStat.put("successRate", successRate != null ? successRate.doubleValue() : 0);
            
            batchStats.add(batchStat);
            totalBatchJobs += batchCount != null ? batchCount.longValue() : 0;
            totalBatchSuccess += successCount != null ? successCount.longValue() : 0;
        }
        
        reportData.put("batchStats", batchStats);
        reportData.put("totalBatchJobs", totalBatchJobs);
        reportData.put("totalBatchSuccess", totalBatchSuccess);
        reportData.put("avgBatchSuccessRate", totalBatchJobs > 0 ? (double) totalBatchSuccess / totalBatchJobs * 100 : 0);
        reportData.put("startTime", dateRange[0].format(DateTimeFormatter.ISO_DATE));
        reportData.put("endTime", dateRange[1].format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    private Map<String, Object> buildUnifiedUsageReportData(List<Map<String, Object>> usageStats, LocalDate[] dateRange) {
        // 构建统一执行平台的统计报告数据
        Map<String, Object> reportData = new HashMap<>();
        
        int totalExecutions = 0;
        int totalExecutionSuccess = 0;
        long totalExecutionDuration = 0;
        
        List<Map<String, Object>> executionStats = new ArrayList<>();
        
        for (Map<String, Object> stat : usageStats) {
            String executeType = safeGetString(stat, "execute_type");
            BigDecimal totalCount = safeGetBigDecimal(stat, "total_count");
            BigDecimal successCount = safeGetBigDecimal(stat, "success_count");
            BigDecimal avgDuration = safeGetBigDecimal(stat, "avg_duration");
            
            Map<String, Object> executionStat = new HashMap<>();
            executionStat.put("executeType", executeType);
            executionStat.put("executeTypeName", "MANUAL".equals(executeType) ? "手动执行" : "定时任务");
            executionStat.put("totalCount", totalCount != null ? totalCount.longValue() : 0);
            executionStat.put("successCount", successCount != null ? successCount.longValue() : 0);
            executionStat.put("failureCount", totalCount != null && successCount != null ? 
                totalCount.subtract(successCount).longValue() : 0);
            executionStat.put("successRate", calculateSuccessRate(totalCount, successCount));
            executionStat.put("avgExecuteTime", avgDuration != null ? avgDuration.longValue() : 0);
            
            executionStats.add(executionStat);
            totalExecutions += totalCount != null ? totalCount.longValue() : 0;
            totalExecutionSuccess += successCount != null ? successCount.longValue() : 0;
            totalExecutionDuration += avgDuration != null ? avgDuration.doubleValue() * (totalCount != null ? totalCount.longValue() : 0) : 0;
        }
        
        reportData.put("executionStats", executionStats);
        reportData.put("totalExecutions", totalExecutions);
        reportData.put("totalExecutionSuccess", totalExecutionSuccess);
        reportData.put("avgExecutionSuccessRate", totalExecutions > 0 ? (double) totalExecutionSuccess / totalExecutions * 100 : 0);
        reportData.put("avgExecutionTime", totalExecutions > 0 ? totalExecutionDuration / totalExecutions : 0);
        reportData.put("startTime", dateRange[0].format(DateTimeFormatter.ISO_DATE));
        reportData.put("endTime", dateRange[1].format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    private Map<String, Object> buildJobLogUsageReportData(List<Map<String, Object>> usageStats, LocalDate[] dateRange) {
        // 构建作业日志的统计报告数据
        return buildUsageReportData(usageStats, dateRange);
    }

    private String getDataSourceDisplayName(String dataSource) {
        if (dataSource == null) {
            return "作业日志";
        }
        
        switch (dataSource.toUpperCase()) {
            case "BATCH":
                return "批处理作业";
            case "UNIFIED":
                return "统一执行平台";
            case "JOB_LOG":
            default:
                return "作业日志";
        }
    }

    private Map<String, Object> generateEfficiencyBarChart(String startDate, String endDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("平均执行时间", "最大执行时间", "最小执行时间"));
        data.put("values", Arrays.asList(1200, 2500, 300));
        data.put("title", "模板执行时间统计");
        return data;
    }

    private Map<String, Object> generateEfficiencyLineChart(String startDate, String endDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("Day1", "Day2", "Day3", "Day4", "Day5"));
        data.put("values", Arrays.asList(1100, 1250, 980, 1350, 1200));
        data.put("title", "模板执行时间趋势");
        return data;
    }

    private Map<String, Object> buildUsageChartData(List<Map<String, Object>> usageStats, String chartType, String timeRange) {
        Map<String, Object> chartData = new HashMap<>();
        
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        
        for (Map<String, Object> stat : usageStats) {
            Long templateId = (Long) stat.get("template_id");
            Long usageCount = (Long) stat.get("usage_count");
            
            labels.add("模板" + templateId);
            values.add(usageCount);
        }
        
        chartData.put("chartType", chartType);
        chartData.put("labels", labels);
        chartData.put("values", values);
        chartData.put("title", "模板使用频率统计 - " + getTimeRangeDisplay(timeRange));
        
        return chartData;
    }

    private Map<String, Object> buildEfficiencyChartData(List<Map<String, Object>> efficiencyStats, String chartType, String startDate, String endDate) {
        Map<String, Object> chartData = new HashMap<>();
        
        List<String> labels = new ArrayList<>();
        List<Long> durations = new ArrayList<>();
        List<Double> successRates = new ArrayList<>();
        
        for (Map<String, Object> stat : efficiencyStats) {
            String date =  ((java.sql.Date) stat.get("date")).toString();
            BigDecimal avgDuration = (BigDecimal)stat.get("avg_duration") ;
            Long jobCount = (Long) stat.get("job_count");
            BigDecimal successCount = (BigDecimal)stat.get("success_count");
            
            labels.add(date.substring(5)); // 显示月-日格式
            durations.add(avgDuration != null ? avgDuration.longValue() : 0);
            successRates.add(jobCount > 0 ? successCount.divide(BigDecimal.valueOf(jobCount), 2).multiply(BigDecimal.valueOf(100)).doubleValue() : 0);
        }
        
        chartData.put("chartType", chartType);
        chartData.put("labels", labels);
        chartData.put("durations", durations);
        chartData.put("successRates", successRates);
        chartData.put("title", "模板执行效率趋势 - " + startDate + " 至 " + endDate);
        
        return chartData;
    }

    // ====================== 模板格式化方法 ======================

    /**
     * 应用模板格式，将报告数据填充到模板内容中
     */
    private String applyTemplateFormatToString(String templateContent, Map<String, Object> reportData) {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            return convertToJson(reportData);
        }

        try {
            // 简单的模板变量替换，支持 ${variableName} 格式
            String formattedContent = templateContent;

            // 替换基本变量
            formattedContent = replaceTemplateVariables(formattedContent, reportData);

            // 替换日期变量
            formattedContent = replaceDateVariables(formattedContent);

            // 替换统计变量
            formattedContent = replaceStatisticsVariables(formattedContent, reportData);

            return formattedContent;
        } catch (Exception e) {
            log.error("模板格式化失败，使用默认格式", e);
            return convertToJson(reportData);
        }
    }

    /**
     * 应用模板格式，将报告数据填充到模板内容中
     */
    private JSONArray applyTemplateFormat(String templateContent, Map<String, Object> reportData) {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            return contentArray;
        }
        
        try {
            // 简单的模板变量替换，支持 ${variableName} 格式
            String formattedContent = templateContent;
            
            // 替换基本变量
            formattedContent = replaceTemplateVariables(formattedContent, reportData);
            
            // 替换日期变量
            formattedContent = replaceDateVariables(formattedContent);
            
            // 替换统计变量
            formattedContent = replaceStatisticsVariables(formattedContent, reportData);
            
            // 将格式化后的字符串转换为JSONArray
            JSONArray contentArray = new JSONArray();
            Map<String, Object> formattedData = new HashMap<>();
            formattedData.put("formattedContent", formattedContent);
            formattedData.put("originalData", reportData);
            contentArray.add(formattedData);
            
            return contentArray;
        } catch (Exception e) {
            log.error("模板格式化失败，使用默认格式", e);
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            return contentArray;
        }
    }

    /**
     * 替换模板中的变量 ${variableName}
     */
    private String replaceTemplateVariables(String content, Map<String, Object> data) {
        String result = content;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String variable = "\\${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(variable, value);
        }
        
        return result;
    }

    /**
     * 替换模板中的日期变量
     */
    private String replaceDateVariables(String content) {
        String result = content;
        
        // 当前日期
        LocalDate now = LocalDate.now();
        result = result.replace("${currentDate}", now.toString());
        result = result.replace("${currentYear}", String.valueOf(now.getYear()));
        result = result.replace("${currentMonth}", String.valueOf(now.getMonthValue()));
        result = result.replace("${currentDay}", String.valueOf(now.getDayOfMonth()));
        
        return result;
    }

    /**
     * 替换模板中的统计变量
     */
    private String replaceStatisticsVariables(String content, Map<String, Object> data) {
        String result = content;
        
        // 总任务数
        if (data.containsKey("totalJobs")) {
            result = result.replace("${totalJobs}", data.get("totalJobs").toString());
        }
        
        // 成功任务数
        if (data.containsKey("totalSuccess")) {
            result = result.replace("${totalSuccess}", data.get("totalSuccess").toString());
        }
        
        // 平均成功率
        if (data.containsKey("avgSuccessRate")) {
            double successRate = (double) data.get("avgSuccessRate");
            result = result.replace("${avgSuccessRate}", String.format("%.2f%%", successRate));
        }
        
        // 平均执行时间
        if (data.containsKey("avgDuration")) {
            double avgDuration = (double) data.get("avgDuration");
            result = result.replace("${avgDuration}", String.format("%.2fms", avgDuration));
        }
        
        return result;
    }

    // ====================== 安全数据获取工具方法 ======================

    /**
     * 安全获取字符串值
     */
    private String safeGetString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全获取BigDecimal值
     */
    private BigDecimal safeGetBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算成功率
     */
    private double calculateSuccessRate(BigDecimal totalCount, BigDecimal successCount) {
        if (totalCount == null || successCount == null || totalCount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return successCount.divide(totalCount, 4, BigDecimal.ROUND_HALF_UP)
                          .multiply(new BigDecimal("100"))
                          .doubleValue();
    }

    // ====================== 每2小时响应时间统计方法 ======================

    @Override
    public List<Map<String, Object>> getHourlyResponseTimeReportSimple(String startDate, String endDate, String dataSource) {
        try {
            // 参数验证
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            // 根据数据源获取统计信息
            List<Map<String, Object>> hourlyStats;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    hourlyStats = templateStatisticsMapper.getBatchHourlyResponseTimeStats(startTime, endTime);
                    break;
                case "UNIFIED":
                    hourlyStats = templateStatisticsMapper.getUnifiedHourlyResponseTimeStats(startTime, endTime);
                    break;
                case "JOB_LOG":
                default:
                    hourlyStats = templateStatisticsMapper.getHourlyResponseTimeStats(startTime, endTime);
                    break;
            }
            
            // 构建简化格式的数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> stat : hourlyStats) {
                String timeSlot = safeGetString(stat, "time_slot");
                BigDecimal avgDuration = safeGetBigDecimal(stat, "avg_duration");
                
                Map<String, Object> item = new HashMap<>();
                item.put("name", timeSlot);
                item.put("value", avgDuration != null ? avgDuration.doubleValue() : 0);
                
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取每2小时平均响应时间报告（简化格式）失败", e);
            throw new RuntimeException("获取每2小时平均响应时间报告（简化格式）失败：" + e.getMessage(), e);
        }
    }

    @Override
    public StatisticsReportDTO getHourlyResponseTimeReport(String startDate, String endDate, String dataSource) {
        try {
            // 参数验证
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            // 根据数据源获取统计信息
            List<Map<String, Object>> hourlyStats;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    hourlyStats = templateStatisticsMapper.getBatchHourlyResponseTimeStats(startTime, endTime);
                    break;
                case "UNIFIED":
                    hourlyStats = templateStatisticsMapper.getUnifiedHourlyResponseTimeStats(startTime, endTime);
                    break;
                case "JOB_LOG":
                default:
                    hourlyStats = templateStatisticsMapper.getHourlyResponseTimeStats(startTime, endTime);
                    break;
            }
            
            // 构建报告数据
            Map<String, Object> reportData = buildHourlyResponseTimeData(hourlyStats, dataSource, start, end);
            
            // 构建统计报告DTO
            StatisticsReportDTO report = new StatisticsReportDTO();
            report.setId(System.currentTimeMillis());
            report.setName("每2小时平均响应时间报告 - " + getDataSourceDisplayName(dataSource) + " - " + startDate + " 至 " + endDate);
            report.setDescription("基于" + getDataSourceDisplayName(dataSource) + "生成的每2小时平均响应时间统计报告");
            
            // 将content改为JSON数组格式
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            report.setContent(contentArray);
            
            report.setCreateTime(LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("获取每2小时平均响应时间报告失败", e);
            throw new RuntimeException("获取每2小时平均响应时间报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建每2小时响应时间数据
     */
    private Map<String, Object> buildHourlyResponseTimeData(List<Map<String, Object>> hourlyStats, String dataSource, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        List<Map<String, Object>> timeSlots = new ArrayList<>();
        double totalAvgDuration = 0;
        double maxDuration = 0;
        double minDuration = Double.MAX_VALUE;
        int totalExecutions = 0;
        
        for (Map<String, Object> stat : hourlyStats) {
            String timeSlot = safeGetString(stat, "time_slot");
            BigDecimal executionCount = safeGetBigDecimal(stat, "execution_count");
            BigDecimal avgDuration = safeGetBigDecimal(stat, "avg_duration");
            BigDecimal maxDurationStat = safeGetBigDecimal(stat, "max_duration");
            BigDecimal minDurationStat = safeGetBigDecimal(stat, "min_duration");
            
            Map<String, Object> timeSlotData = new HashMap<>();
            timeSlotData.put("timeSlot", timeSlot);
            timeSlotData.put("hourGroup", safeGetString(stat, "hour_group"));
            timeSlotData.put("executionCount", executionCount != null ? executionCount.longValue() : 0);
            timeSlotData.put("avgDuration", avgDuration != null ? avgDuration.doubleValue() : 0);
            timeSlotData.put("maxDuration", maxDurationStat != null ? maxDurationStat.doubleValue() : 0);
            timeSlotData.put("minDuration", minDurationStat != null ? minDurationStat.doubleValue() : 0);
            
            // 对于UNIFIED数据源，添加执行类型信息
            if ("UNIFIED".equalsIgnoreCase(dataSource)) {
                String executeType = safeGetString(stat, "execute_type");
                timeSlotData.put("executeType", executeType);
                timeSlotData.put("executeTypeName", "MANUAL".equals(executeType) ? "手动执行" : "定时任务");
            }
            
            timeSlots.add(timeSlotData);
            
            // 统计总体信息
            if (avgDuration != null) {
                totalAvgDuration += avgDuration.doubleValue() * (executionCount != null ? executionCount.longValue() : 0);
                maxDuration = Math.max(maxDuration, maxDurationStat != null ? maxDurationStat.doubleValue() : 0);
                minDuration = Math.min(minDuration, minDurationStat != null ? minDurationStat.doubleValue() : Double.MAX_VALUE);
                totalExecutions += executionCount != null ? executionCount.longValue() : 0;
            }
        }
        
        reportData.put("timeSlots", timeSlots);
        reportData.put("totalExecutions", totalExecutions);
        reportData.put("overallAvgDuration", totalExecutions > 0 ? totalAvgDuration / totalExecutions : 0);
        reportData.put("maxDuration", maxDuration);
        reportData.put("minDuration", minDuration == Double.MAX_VALUE ? 0 : minDuration);
        reportData.put("dataSource", dataSource);
        reportData.put("dataSourceName", getDataSourceDisplayName(dataSource));
        reportData.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    // ====================== 周一到周日执行量统计方法 ======================

    @Override
    public StatisticsReportDTO getWeeklyExecutionReport(String startDate, String endDate, String dataSource) {
        try {
            // 参数验证
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            // 根据数据源获取统计信息
            List<Map<String, Object>> weeklyStats;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "UNIFIED":
                    weeklyStats = templateStatisticsMapper.getUnifiedWeeklyExecutionStats(startTime, endTime);
                    break;
                case "JOB_LOG":
                default:
                    weeklyStats = templateStatisticsMapper.getWeeklyExecutionStats(startTime, endTime);
                    break;
            }
            
            // 构建报告数据
            Map<String, Object> reportData = buildWeeklyExecutionData(weeklyStats, dataSource, start, end);
            
            // 构建报告DTO
            StatisticsReportDTO report = new StatisticsReportDTO();
            report.setId(System.currentTimeMillis());
            report.setName("周一到周日执行量统计报告 - " + getDataSourceDisplayName(dataSource) + " - " + startDate + " 至 " + endDate);
            report.setDescription("基于" + getDataSourceDisplayName(dataSource) + "生成的周一到周日执行量统计报告");
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            report.setContent(contentArray);
            report.setCreateTime(LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("获取周一到周日执行量统计报告失败", e);
            throw new RuntimeException("获取周一到周日执行量统计报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建周一到周日执行量统计数据
     */
    private Map<String, Object> buildWeeklyExecutionData(List<Map<String, Object>> weeklyStats, String dataSource, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        // 初始化周一到周日的数据结构
        Map<String, Object> weekDays = new LinkedHashMap<>();
        weekDays.put("Monday", createDayData("Monday", "星期一", 2));
        weekDays.put("Tuesday", createDayData("Tuesday", "星期二", 3));
        weekDays.put("Wednesday", createDayData("Wednesday", "星期三", 4));
        weekDays.put("Thursday", createDayData("Thursday", "星期四", 5));
        weekDays.put("Friday", createDayData("Friday", "星期五", 6));
        weekDays.put("Saturday", createDayData("Saturday", "星期六", 7));
        weekDays.put("Sunday", createDayData("Sunday", "星期日", 1));
        
        int totalExecutions = 0;
        
        // 填充实际统计数据
        for (Map<String, Object> stat : weeklyStats) {
            String dayName = safeGetString(stat, "day_name");
            BigDecimal executionCount = safeGetBigDecimal(stat, "execution_count");
            
            if (dayName != null && weekDays.containsKey(dayName)) {
                Map<String, Object> dayData = (Map<String, Object>) weekDays.get(dayName);
                dayData.put("executionCount", executionCount != null ? executionCount.longValue() : 0);
                
                // 对于UNIFIED数据源，添加执行类型信息
                if ("UNIFIED".equalsIgnoreCase(dataSource)) {
                    String executeType = safeGetString(stat, "execute_type");
                    dayData.put("executeType", executeType);
                    dayData.put("executeTypeName", "MANUAL".equals(executeType) ? "手动执行" : "定时任务");
                }
                
                totalExecutions += executionCount != null ? executionCount.longValue() : 0;
            }
        }
        
        // 转换为按星期顺序排列的列表
        List<Map<String, Object>> weekData = new ArrayList<>();
        weekData.add((Map<String, Object>) weekDays.get("Monday"));
        weekData.add((Map<String, Object>) weekDays.get("Tuesday"));
        weekData.add((Map<String, Object>) weekDays.get("Wednesday"));
        weekData.add((Map<String, Object>) weekDays.get("Thursday"));
        weekData.add((Map<String, Object>) weekDays.get("Friday"));
        weekData.add((Map<String, Object>) weekDays.get("Saturday"));
        weekData.add((Map<String, Object>) weekDays.get("Sunday"));
        
        reportData.put("weekData", weekData);
        reportData.put("totalExecutions", totalExecutions);
        // 计算实际天数的平均值，而不是固定除以7
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        reportData.put("avgDailyExecutions", daysBetween > 0 ? totalExecutions / (double) daysBetween : 0);
        reportData.put("dataSource", dataSource);
        reportData.put("dataSourceName", getDataSourceDisplayName(dataSource));
        reportData.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    /**
     * 创建每日数据模板
     */
    private Map<String, Object> createDayData(String dayName, String dayNameCn, int dayOfWeek) {
        Map<String, Object> dayData = new HashMap<>();
        dayData.put("dayName", dayName);
        dayData.put("dayNameCn", dayNameCn);
        dayData.put("dayOfWeek", dayOfWeek);
        dayData.put("executionCount", 0);
        return dayData;
    }

    // ====================== 成功率分析方法 ======================

    @Override
    public StatisticsReportDTO getSuccessRateReport(String startDate, String endDate, String dataSource) {
        try {
            // 参数验证
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            // 根据数据源获取统计信息
            Map<String, Object> successRateStats;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    successRateStats = templateStatisticsMapper.getBatchSuccessRateStats(startTime, endTime);
                    break;
                case "UNIFIED":
                    successRateStats = templateStatisticsMapper.getUnifiedSuccessRateStats(startTime, endTime);
                    break;
                case "JOB_LOG":
                default:
                    successRateStats = templateStatisticsMapper.getSuccessRateStats(startTime, endTime);
                    break;
            }
            
            // 构建报告数据
            Map<String, Object> reportData = buildSuccessRateData(successRateStats, dataSource, start, end);
            
            // 构建统计报告DTO
            StatisticsReportDTO report = new StatisticsReportDTO();
            report.setId(System.currentTimeMillis());
            report.setName("成功率分析报告 - " + getDataSourceDisplayName(dataSource) + " - " + startDate + " 至 " + endDate);
            report.setDescription("基于" + getDataSourceDisplayName(dataSource) + "生成的成功失败占比分析报告");
            
            // 将content改为JSON数组格式
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            report.setContent(contentArray);
            
            report.setCreateTime(LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("获取成功率分析报告失败", e);
            throw new RuntimeException("获取成功率分析报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建成功率分析数据
     */
    private Map<String, Object> buildSuccessRateData(Map<String, Object> successRateStats, String dataSource, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        // 获取统计数据
        BigDecimal totalCount = safeGetBigDecimal(successRateStats, "total_count");
        BigDecimal successCount = safeGetBigDecimal(successRateStats, "success_count");
        BigDecimal failureCount = safeGetBigDecimal(successRateStats, "failure_count");
        BigDecimal successRate = safeGetBigDecimal(successRateStats, "success_rate");
        
        long total = totalCount != null ? totalCount.longValue() : 0;
        long success = successCount != null ? successCount.longValue() : 0;
        long failure = failureCount != null ? failureCount.longValue() : 0;
        double rate = successRate != null ? successRate.doubleValue() : 0;
        
        // 构建成功率占比数据（用于饼图）
        List<Map<String, Object>> rateData = new ArrayList<>();
        
        Map<String, Object> successData = new HashMap<>();
        successData.put("name", "成功");
        successData.put("value", success);
        successData.put("percentage", total > 0 ? (double) success / total * 100 : 0);
        successData.put("color", "#52c41a"); // 绿色
        rateData.add(successData);
        
        Map<String, Object> failureData = new HashMap<>();
        failureData.put("name", "失败");
        failureData.put("value", failure);
        failureData.put("percentage", total > 0 ? (double) failure / total * 100 : 0);
        failureData.put("color", "#ff4d4f"); // 红色
        rateData.add(failureData);
        
        // 构建统计摘要
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", total);
        summary.put("successCount", success);
        summary.put("failureCount", failure);
        summary.put("successRate", rate);
        summary.put("failureRate", total > 0 ? 100 - rate : 0);
        
        reportData.put("rateData", rateData);
        reportData.put("summary", summary);
        reportData.put("dataSource", dataSource);
        reportData.put("dataSourceName", getDataSourceDisplayName(dataSource));
        reportData.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    // ====================== 协议类型分布统计方法 ======================

    @Override
    public StatisticsReportDTO getProtocolDistributionReport(String startDate, String endDate, String reportType) {
        try {
            // 解析日期参数
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (startDate != null && endDate != null) {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                startTime = start.atStartOfDay();
                endTime = end.atTime(23, 59, 59);
            }
            
            // 根据报告类型获取统计信息
            Map<String, Object> reportData;
            switch (reportType != null ? reportType.toUpperCase() : "CATEGORY") {
                case "CATEGORY":
                default:
                    // 查询指定日期范围内的协议类型分布统计
                    List<Map<String, Object>> categoryStats = templateStatisticsMapper.getProtocolCategoryStats(startTime, endTime);
                    reportData = buildProtocolCategoryData(categoryStats);
                    break;
            }
            
            // 构建统计报告DTO
            StatisticsReportDTO report = new StatisticsReportDTO();
            report.setId(System.currentTimeMillis());
            report.setName("协议类型分布统计报告 - " + getReportTypeDisplayName(reportType) + " - " + startDate + " 至 " + endDate);
            report.setDescription("基于协议测试记录生成的" + getReportTypeDisplayName(reportType) + "分布统计报告");
            
            // 将content改为JSON数组格式
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            report.setContent(contentArray);
            
            report.setCreateTime(LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("获取协议类型分布统计报告失败", e);
            throw new RuntimeException("获取协议类型分布统计报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建协议分类分布数据
     */
    private Map<String, Object> buildProtocolCategoryData(List<Map<String, Object>> categoryStats) {
        Map<String, Object> reportData = new HashMap<>();
        
        List<Map<String, Object>> categoryData = new ArrayList<>();
        int totalProtocolCount = 0;
        
        for (Map<String, Object> stat : categoryStats) {
            String category = safeGetString(stat, "category");
            BigDecimal protocolCount = safeGetBigDecimal(stat, "protocol_count");
            
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("category", category);
            categoryInfo.put("categoryName", category); // 直接使用protocol_name作为显示名称
            categoryInfo.put("protocolCount", protocolCount != null ? protocolCount.longValue() : 0);
            
            categoryData.add(categoryInfo);
            totalProtocolCount += protocolCount != null ? protocolCount.longValue() : 0;
        }
        
        reportData.put("categoryData", categoryData);
        reportData.put("totalProtocolCount", totalProtocolCount);
        reportData.put("reportType", "CATEGORY");
        reportData.put("reportTypeName", "按协议分类");
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    /**
     * 构建协议详情分布数据
     */
    private Map<String, Object> buildProtocolDetailData(List<Map<String, Object>> detailStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        List<Map<String, Object>> protocolData = new ArrayList<>();
        int totalUsageCount = 0;
        int totalSuccessCount = 0;
        
        for (Map<String, Object> stat : detailStats) {
            String protocolCode = safeGetString(stat, "protocol_code");
            String protocolName = safeGetString(stat, "protocol_name");
            String category = safeGetString(stat, "category");
            BigDecimal usageCount = safeGetBigDecimal(stat, "usage_count");
            BigDecimal successCount = safeGetBigDecimal(stat, "success_count");
            
            Map<String, Object> protocolInfo = new HashMap<>();
            protocolInfo.put("protocolCode", protocolCode);
            protocolInfo.put("protocolName", protocolName);
            protocolInfo.put("category", category);
            protocolInfo.put("categoryName", getProtocolCategoryDisplayName(category));
            protocolInfo.put("usageCount", usageCount != null ? usageCount.longValue() : 0);
            protocolInfo.put("successCount", successCount != null ? successCount.longValue() : 0);
            protocolInfo.put("successRate", usageCount != null && successCount != null && usageCount.compareTo(BigDecimal.ZERO) > 0 ? 
                successCount.divide(usageCount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue() : 0);
            protocolInfo.put("failureCount", usageCount != null && successCount != null ? 
                usageCount.subtract(successCount).longValue() : 0);
            
            protocolData.add(protocolInfo);
            totalUsageCount += usageCount != null ? usageCount.longValue() : 0;
            totalSuccessCount += successCount != null ? successCount.longValue() : 0;
        }
        
        reportData.put("protocolData", protocolData);
        reportData.put("totalUsageCount", totalUsageCount);
        reportData.put("totalSuccessCount", totalSuccessCount);
        reportData.put("overallSuccessRate", totalUsageCount > 0 ? (double) totalSuccessCount / totalUsageCount * 100 : 0);
        reportData.put("reportType", "DETAIL");
        reportData.put("reportTypeName", "按具体协议");
        reportData.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    /**
     * 构建协议测试类型分布数据
     */
    private Map<String, Object> buildProtocolTestTypeData(List<Map<String, Object>> testTypeStats, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        List<Map<String, Object>> testTypeData = new ArrayList<>();
        int totalTestCount = 0;
        int totalSuccessCount = 0;
        
        for (Map<String, Object> stat : testTypeStats) {
            String testType = safeGetString(stat, "test_type");
            BigDecimal testCount = safeGetBigDecimal(stat, "test_count");
            BigDecimal successCount = safeGetBigDecimal(stat, "success_count");
            
            Map<String, Object> testTypeInfo = new HashMap<>();
            testTypeInfo.put("testType", testType);
            testTypeInfo.put("testTypeName", getTestTypeDisplayName(testType));
            testTypeInfo.put("testCount", testCount != null ? testCount.longValue() : 0);
            testTypeInfo.put("successCount", successCount != null ? successCount.longValue() : 0);
            testTypeInfo.put("successRate", testCount != null && testCount.compareTo(BigDecimal.ZERO) > 0 ? 
                successCount.divide(testCount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")).doubleValue() : 0);
            
            testTypeData.add(testTypeInfo);
            totalTestCount += testCount != null ? testCount.longValue() : 0;
            totalSuccessCount += successCount != null ? successCount.longValue() : 0;
        }
        
        reportData.put("testTypeData", testTypeData);
        reportData.put("totalTestCount", totalTestCount);
        reportData.put("totalSuccessCount", totalSuccessCount);
        reportData.put("overallSuccessRate", totalTestCount > 0 ? (double) totalSuccessCount / totalTestCount * 100 : 0);
        reportData.put("reportType", "TEST_TYPE");
        reportData.put("reportTypeName", "按测试类型");
        reportData.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE));
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    /**
     * 获取报告类型显示名称
     */
    private String getReportTypeDisplayName(String reportType) {
        if (reportType == null) return "按协议分类";
        switch (reportType.toUpperCase()) {
            case "DETAIL": return "按具体协议";
            case "TEST_TYPE": return "按测试类型";
            case "CATEGORY":
            default: return "按协议分类";
        }
    }

    /**
     * 获取协议名称显示名称（直接返回协议名称）
     */
    private String getProtocolCategoryDisplayName(String category) {
        if (category == null) return "未知协议";
        return category;
    }

    /**
     * 获取测试类型显示名称
     */
    private String getTestTypeDisplayName(String testType) {
        if (testType == null) return "未知测试";
        switch (testType.toUpperCase()) {
            case "CONNECT": return "连接测试";
            case "TRANSFER": return "数据传输";
            case "COMPREHENSIVE": return "综合测试";
            default: return testType;
        }
    }

    /**
     * 将失败记录转换为FailureTimelineDTO
     */
    private FailureTimelineDTO convertToFailureTimelineDTO(Map<String, Object> record) {
        if (record == null || record.isEmpty()) {
            return null;
        }
        
        FailureTimelineDTO dto = new FailureTimelineDTO();
        
        // 设置基本字段
        if (record.get("create_time") != null) {
            dto.setTimestamp((LocalDateTime) record.get("create_time"));
        }
        if (record.get("template_name") != null) {
            dto.setTemplateName((String) record.get("template_name"));
        }
        if (record.get("duration_ms") != null) {
            dto.setDurationMs(((Number) record.get("duration_ms")).longValue());
        }
        
        // 解析execute_result JSON字段
        if (record.get("execute_result") != null) {
            try {
                String jsonStr = (String) record.get("execute_result");
                if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                    JSONObject jsonResult = JSONObject.parseObject(jsonStr);
                    
                    // 提取失败原因和错误信息
                    if (jsonResult.containsKey("message")) {
                        dto.setFailureReason(jsonResult.getString("message"));
                    }
                    if (jsonResult.containsKey("statusCode")) {
                        dto.setErrorCode(jsonResult.getString("statusCode"));
                    }
                    
                    // 提取响应信息
                    if (jsonResult.containsKey("response")) {
                        JSONObject response = jsonResult.getJSONObject("response");
                        if (response != null) {
                            if (response.containsKey("statusCode")) {
                                dto.setResponseStatusCode(response.getInteger("statusCode"));
                            }
                            if (response.containsKey("body")) {
                                dto.setResponseMessage(response.getString("body"));
                            }
                        }
                    }
                    
                    // 提取请求信息
                    if (jsonResult.containsKey("request")) {
                        JSONObject request = jsonResult.getJSONObject("request");
                        if (request != null && request.containsKey("url")) {
                            dto.setRequestUrl(request.getString("url"));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析execute_result JSON失败: {}", record.get("execute_result"), e);
            }
        }
        
        return dto;
    }

    @Override
    public java.util.List<FailureTimelineDTO> getFailureTimelineData(Long templateId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // 查询失败记录
            List<Map<String, Object>> failureRecords = templateStatisticsMapper.getFailureTimelineData(
                templateId, startTime, endTime);
            
            // 转换为FailureTimelineDTO列表
            List<FailureTimelineDTO> timeline = new java.util.ArrayList<>();
            if (failureRecords != null) {
                for (Map<String, Object> record : failureRecords) {
                    FailureTimelineDTO dto = convertToFailureTimelineDTO(record);
                    if (dto != null) {
                        timeline.add(dto);
                    }
                }
            }
            
            return timeline;
        } catch (Exception e) {
            log.error("获取失败时间线数据失败 - templateId: {}, startTime: {}, endTime: {}", 
                templateId, startTime, endTime, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTopFailureReasonsReportSimple(String startDate, String endDate, String dataSource) {
        try {
            // 参数验证
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            // 根据数据源获取前5的失败原因统计
            List<Map<String, Object>> failureReasons;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    failureReasons = templateStatisticsMapper.getBatchTopFailureReasons(startTime, endTime);
                    break;
                case "UNIFIED":
                case "JOB_LOG":
                default:
                    failureReasons = templateStatisticsMapper.getTopFailureReasons(startTime, endTime);
                    break;
            }
            
            // 构建简化格式的数据
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> reason : failureReasons) {
                String failureReason = safeGetString(reason, "failure_reason");
                BigDecimal failureCount = safeGetBigDecimal(reason, "failure_count");
                
                Map<String, Object> item = new HashMap<>();
                item.put("name", failureReason);
                item.put("value", failureCount != null ? failureCount.longValue() : 0);
                
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取前5失败原因分析报告（简化格式）失败", e);
            throw new RuntimeException("获取前5失败原因分析报告（简化格式）失败：" + e.getMessage(), e);
        }
    }

    @Override
    public StatisticsReportDTO getTopFailureReasonsReport(String startDate, String endDate, String dataSource) {
        try {
            // 参数验证
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("开始日期和结束日期不能为空");
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("开始日期不能晚于结束日期");
            }
            
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            // 根据数据源获取前5的失败原因统计
            List<Map<String, Object>> failureReasons;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    failureReasons = templateStatisticsMapper.getBatchTopFailureReasons(startTime, endTime);
                    break;
                case "UNIFIED":
                case "JOB_LOG":
                default:
                    failureReasons = templateStatisticsMapper.getTopFailureReasons(startTime, endTime);
                    break;
            }
            
            // 构建报告数据
            Map<String, Object> reportData = buildFailureReasonsData(failureReasons, start, end);
            
            // 构建统计报告DTO
            StatisticsReportDTO report = new StatisticsReportDTO();
            report.setId(System.currentTimeMillis());
            report.setName("前5失败原因分析报告 - " + getDataSourceDisplayName(dataSource) + " - " + startDate + " 至 " + endDate);
            report.setDescription("基于" + getDataSourceDisplayName(dataSource) + "生成的前5失败原因统计分析报告");
            
            // 将content改为JSON数组格式
            JSONArray contentArray = new JSONArray();
            contentArray.add(reportData);
            report.setContent(contentArray);
            
            report.setCreateTime(LocalDateTime.now());
            
            return report;
        } catch (Exception e) {
            log.error("获取前5失败原因分析报告失败", e);
            throw new RuntimeException("获取前5失败原因分析报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构建失败原因分析数据
     */
    private Map<String, Object> buildFailureReasonsData(List<Map<String, Object>> failureReasons, 
                                                        LocalDate startDate, LocalDate endDate) {
        Map<String, Object> reportData = new HashMap<>();
        
        List<Map<String, Object>> failureData = new ArrayList<>();
        int totalFailureCount = 0;
        
        // 计算总失败次数
        for (Map<String, Object> reason : failureReasons) {
            BigDecimal failureCount = safeGetBigDecimal(reason, "failure_count");
            totalFailureCount += failureCount != null ? failureCount.longValue() : 0;
        }
        
        for (Map<String, Object> reason : failureReasons) {
            String failureReason = safeGetString(reason, "failure_reason");
            BigDecimal failureCount = safeGetBigDecimal(reason, "failure_count");
            String errorCode = safeGetString(reason, "error_code");
            
            Map<String, Object> failureInfo = new HashMap<>();
            failureInfo.put("failureReason", failureReason);
            failureInfo.put("failureCount", failureCount != null ? failureCount.longValue() : 0);
            failureInfo.put("errorCode", errorCode);
            failureInfo.put("percentage", totalFailureCount > 0 ? 
                (double) (failureCount != null ? failureCount.longValue() : 0) / totalFailureCount * 100 : 0);
            
            failureData.add(failureInfo);
            totalFailureCount += failureCount != null ? failureCount.longValue() : 0;
        }
        
        // 计算每个失败原因的占比
        for (Map<String, Object> failureInfo : failureData) {
            long count = (Long) failureInfo.get("failureCount");
            if (totalFailureCount > 0) {
                double percentage = (double) count / totalFailureCount * 100;
                failureInfo.put("percentage", Math.round(percentage * 100.0) / 100.0);
            }
        }
        
        // 构建统计摘要
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalFailureCount", totalFailureCount);
        summary.put("topFailureCount", failureReasons.size());
        summary.put("startDate", startDate.format(DateTimeFormatter.ISO_DATE));
        summary.put("endDate", endDate.format(DateTimeFormatter.ISO_DATE));
        
        reportData.put("failureData", failureData);
        reportData.put("summary", summary);
        reportData.put("generateTime", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return reportData;
    }

    // ====================== 统计对比报告方法 ======================

    @Override
    public CompareResultDTO getCompareReport(String reportType,
                                              String group1StartDate, 
                                              String group1EndDate, 
                                              String group2StartDate, 
                                              String group2EndDate,
                                              String dataSource) {
        log.info("获取统计对比报告，报告类型：{}，对比组1：{}~{}，对比组2：{}~{}，数据源：{}", 
                reportType, group1StartDate, group1EndDate, group2StartDate, group2EndDate, dataSource);
        
        CompareResultDTO result = new CompareResultDTO();
        result.setReportType(reportType);
        result.setMetricTypeName(getMetricTypeName(reportType));
        result.setGenerateTime(LocalDateTime.now());
        
        // 根据reportType获取实际统计数据（直接调用现有的查询方法）
        switch (reportType != null ? reportType.toUpperCase() : "WEEKLY_EXECUTION") {
            case "SUCCESS_RATE":
                // 调用现有的成功率分析接口
                StatisticsReportDTO successRateReport1 = getSuccessRateReport(group1StartDate, group1EndDate, dataSource);
                StatisticsReportDTO successRateReport2 = getSuccessRateReport(group2StartDate, group2EndDate, dataSource);
                result.setData1(extractSuccessRateCompareData(successRateReport1));
                result.setData2(extractSuccessRateCompareData(successRateReport2));
                result.setSummary1(extractSuccessRateSummary(successRateReport1));
                result.setSummary2(extractSuccessRateSummary(successRateReport2));
                break;
            case "RESPONSE_TIME":
                // 调用现有的响应时间接口
                List<Map<String, Object>> responseTimeData1 = getHourlyResponseTimeReportSimple(group1StartDate, group1EndDate, dataSource);
                List<Map<String, Object>> responseTimeData2 = getHourlyResponseTimeReportSimple(group2StartDate, group2EndDate, dataSource);
                result.setData1(responseTimeData1);
                result.setData2(responseTimeData2);
                result.setSummary1(calculateResponseTimeSummary(responseTimeData1));
                result.setSummary2(calculateResponseTimeSummary(responseTimeData2));
                break;
            case "PROTOCOL_DISTRIBUTION":
                // 调用现有的协议类型分布接口
                StatisticsReportDTO protocolReport1 = getProtocolDistributionReport(group1StartDate, group1EndDate, "CATEGORY");
                StatisticsReportDTO protocolReport2 = getProtocolDistributionReport(group2StartDate, group2EndDate, "CATEGORY");
                result.setData1(extractProtocolDistributionCompareData(protocolReport1));
                result.setData2(extractProtocolDistributionCompareData(protocolReport2));
                result.setSummary1(extractProtocolDistributionSummary(protocolReport1));
                result.setSummary2(extractProtocolDistributionSummary(protocolReport2));
                break;
            case "FAILURE_REASONS":
                // 调用现有的失败原因TOP5接口
                List<Map<String, Object>> failureReasonsData1 = getTopFailureReasonsReportSimple(group1StartDate, group1EndDate, dataSource);
                List<Map<String, Object>> failureReasonsData2 = getTopFailureReasonsReportSimple(group2StartDate, group2EndDate, dataSource);
                result.setData1(failureReasonsData1);
                result.setData2(failureReasonsData2);
                result.setSummary1(calculateFailureReasonsSummary(failureReasonsData1));
                result.setSummary2(calculateFailureReasonsSummary(failureReasonsData2));
                break;
            case "WEEKLY_EXECUTION":
            default:
                // 调用现有的日执行量统计接口
                StatisticsReportDTO weeklyReport1 = getWeeklyExecutionReport(group1StartDate, group1EndDate, dataSource);
                StatisticsReportDTO weeklyReport2 = getWeeklyExecutionReport(group2StartDate, group2EndDate, dataSource);
                result.setData1(extractWeeklyExecutionCompareData(weeklyReport1));
                result.setData2(extractWeeklyExecutionCompareData(weeklyReport2));
                result.setSummary1(extractWeeklyExecutionSummary(weeklyReport1));
                result.setSummary2(extractWeeklyExecutionSummary(weeklyReport2));
                break;
        }
        
        // 计算对比摘要（使用reportType）
        result.setSummary(calculateCompareSummary(result, reportType));
        
        return result;
    }

    /**
     * 从周执行量报告中提取对比数据
     */
    private List<Map<String, Object>> extractWeeklyExecutionCompareData(StatisticsReportDTO report) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (report != null && report.getContent() != null && !report.getContent().isEmpty()) {
            Map<String, Object> reportData = report.getContent().getJSONObject(0);
            if (reportData != null && reportData.containsKey("weekData")) {
                List<Map<String, Object>> weekData = (List<Map<String, Object>>) reportData.get("weekData");
                for (Map<String, Object> day : weekData) {
                    Map<String, Object> item = new HashMap<>();
                    String dayName = safeGetString(day, "dayName");
                    // 将英文星期名称转换为中文
                    item.put("name", translateDayNameToChinese(dayName));
                    item.put("value", day.getOrDefault("executionCount", 0));
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * 将英文星期名称转换为中文
     */
    private String translateDayNameToChinese(String dayName) {
        if (dayName == null) {
            return "";
        }
        switch (dayName) {
            case "Monday":
                return "星期一";
            case "Tuesday":
                return "星期二";
            case "Wednesday":
                return "星期三";
            case "Thursday":
                return "星期四";
            case "Friday":
                return "星期五";
            case "Saturday":
                return "星期六";
            case "Sunday":
                return "星期日";
            default:
                return dayName;
        }
    }

    /**
     * 从周执行量报告中提取摘要
     */
    private Map<String, Object> extractWeeklyExecutionSummary(StatisticsReportDTO report) {
        Map<String, Object> summary = new HashMap<>();
        if (report != null && report.getContent() != null && !report.getContent().isEmpty()) {
            Map<String, Object> reportData = report.getContent().getJSONObject(0);
            if (reportData != null && reportData.containsKey("summary")) {
                summary = (Map<String, Object>) reportData.get("summary");
            }
        }
        return summary;
    }

    /**
     * 从成功率报告中提取对比数据
     */
    private List<Map<String, Object>> extractSuccessRateCompareData(StatisticsReportDTO report) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (report != null && report.getContent() != null && !report.getContent().isEmpty()) {
            Map<String, Object> reportData = report.getContent().getJSONObject(0);
            if (reportData != null && reportData.containsKey("rateData")) {
                List<Map<String, Object>> rateData = (List<Map<String, Object>>) reportData.get("rateData");
                for (Map<String, Object> item : rateData) {
                    Map<String, Object> dataItem = new HashMap<>();
                    dataItem.put("name", item.get("name"));
                    dataItem.put("value", item.get("value"));
                    dataItem.put("percentage", item.get("percentage"));
                    result.add(dataItem);
                }
            }
        }
        return result;
    }

    /**
     * 从成功率报告中提取摘要
     */
    private Map<String, Object> extractSuccessRateSummary(StatisticsReportDTO report) {
        Map<String, Object> summary = new HashMap<>();
        if (report != null && report.getContent() != null && !report.getContent().isEmpty()) {
            Map<String, Object> reportData = report.getContent().getJSONObject(0);
            if (reportData != null && reportData.containsKey("summary")) {
                summary = (Map<String, Object>) reportData.get("summary");
            }
        }
        return summary;
    }

    /**
     * 从协议类型分布报告中提取对比数据
     */
    private List<Map<String, Object>> extractProtocolDistributionCompareData(StatisticsReportDTO report) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (report != null && report.getContent() != null && !report.getContent().isEmpty()) {
            Map<String, Object> reportData = report.getContent().getJSONObject(0);
            if (reportData != null && reportData.containsKey("categoryData")) {
                List<Map<String, Object>> categoryData = (List<Map<String, Object>>) reportData.get("categoryData");
                for (Map<String, Object> item : categoryData) {
                    Map<String, Object> dataItem = new HashMap<>();
                    dataItem.put("name", item.get("categoryName"));
                    dataItem.put("value", item.get("protocolCount"));
                    result.add(dataItem);
                }
            }
        }
        return result;
    }

    /**
     * 从协议类型分布报告中提取摘要
     */
    private Map<String, Object> extractProtocolDistributionSummary(StatisticsReportDTO report) {
        Map<String, Object> summary = new HashMap<>();
        if (report != null && report.getContent() != null && !report.getContent().isEmpty()) {
            Map<String, Object> reportData = report.getContent().getJSONObject(0);
            if (reportData != null) {
                summary.put("total", reportData.get("totalProtocolCount"));
                summary.put("categoryCount", reportData.containsKey("categoryData") ? 
                    ((List<?>) reportData.get("categoryData")).size() : 0);
            }
        }
        return summary;
    }

    /**
     * 获取指标类型显示名称
     */
    private String getMetricTypeName(String metricType) {
        if (metricType == null) return "日执行量统计";
        switch (metricType.toUpperCase()) {
            case "SUCCESS_RATE": return "成功率分析";
            case "RESPONSE_TIME": return "平均响应时间";
            case "PROTOCOL_DISTRIBUTION": return "协议类型分布";
            case "FAILURE_REASONS": return "失败原因TOP5";
            case "WEEKLY_EXECUTION":
            default: return "日执行量统计";
        }
    }

    /**
     * 获取日执行量对比数据
     */
    private List<Map<String, Object>> getWeeklyExecutionCompareData(String startDate, String endDate, String dataSource) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            List<Map<String, Object>> weeklyStats;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "UNIFIED":
                    weeklyStats = templateStatisticsMapper.getUnifiedWeeklyExecutionStats(startTime, endTime);
                    break;
                case "JOB_LOG":
                default:
                    weeklyStats = templateStatisticsMapper.getWeeklyExecutionStats(startTime, endTime);
                    break;
            }
            
            // 初始化周一到周日的数据
            Map<String, Object> weekDays = new LinkedHashMap<>();
            weekDays.put("Monday", createDayData("Monday", "星期一", 2));
            weekDays.put("Tuesday", createDayData("Tuesday", "星期二", 3));
            weekDays.put("Wednesday", createDayData("Wednesday", "星期三", 4));
            weekDays.put("Thursday", createDayData("Thursday", "星期四", 5));
            weekDays.put("Friday", createDayData("Friday", "星期五", 6));
            weekDays.put("Saturday", createDayData("Saturday", "星期六", 7));
            weekDays.put("Sunday", createDayData("Sunday", "星期日", 1));
            
            // 填充实际数据
            for (Map<String, Object> stat : weeklyStats) {
                String dayName = safeGetString(stat, "day_name");
                BigDecimal executionCount = safeGetBigDecimal(stat, "execution_count");
                if (dayName != null && weekDays.containsKey(dayName)) {
                    Map<String, Object> dayData = (Map<String, Object>) weekDays.get(dayName);
                    dayData.put("value", executionCount != null ? executionCount.longValue() : 0);
                }
            }
            
            // 转换为列表
            List<Map<String, Object>> result = new ArrayList<>();
            result.add((Map<String, Object>) weekDays.get("Monday"));
            result.add((Map<String, Object>) weekDays.get("Tuesday"));
            result.add((Map<String, Object>) weekDays.get("Wednesday"));
            result.add((Map<String, Object>) weekDays.get("Thursday"));
            result.add((Map<String, Object>) weekDays.get("Friday"));
            result.add((Map<String, Object>) weekDays.get("Saturday"));
            result.add((Map<String, Object>) weekDays.get("Sunday"));
            
            return result;
        } catch (Exception e) {
            log.error("获取日执行量对比数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取成功率对比数据
     */
    private List<Map<String, Object>> getSuccessRateCompareData(String startDate, String endDate, String dataSource) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            Map<String, Object> stats;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    stats = templateStatisticsMapper.getBatchSuccessRateStats(startTime, endTime);
                    break;
                case "UNIFIED":
                    stats = templateStatisticsMapper.getUnifiedSuccessRateStats(startTime, endTime);
                    break;
                case "JOB_LOG":
                default:
                    stats = templateStatisticsMapper.getSuccessRateStats(startTime, endTime);
                    break;
            }
            
            BigDecimal totalCount = safeGetBigDecimal(stats, "total_count");
            BigDecimal successCount = safeGetBigDecimal(stats, "success_count");
            BigDecimal failureCount = safeGetBigDecimal(stats, "failure_count");
            
            long total = totalCount != null ? totalCount.longValue() : 0;
            long success = successCount != null ? successCount.longValue() : 0;
            long failure = failureCount != null ? failureCount.longValue() : 0;
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            Map<String, Object> successData = new HashMap<>();
            successData.put("name", "成功");
            successData.put("value", success);
            successData.put("percentage", total > 0 ? (double) success / total * 100 : 0);
            result.add(successData);
            
            Map<String, Object> failureData = new HashMap<>();
            failureData.put("name", "失败");
            failureData.put("value", failure);
            failureData.put("percentage", total > 0 ? (double) failure / total * 100 : 0);
            result.add(failureData);
            
            return result;
        } catch (Exception e) {
            log.error("获取成功率对比数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取响应时间对比数据（按日期聚合）
     */
    private List<Map<String, Object>> getResponseTimeCompareData(String startDate, String endDate, String dataSource) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 按日期遍历，获取每天的平均响应时间
            LocalDate currentDate = start;
            while (!currentDate.isAfter(end)) {
                LocalDateTime startTime = currentDate.atStartOfDay();
                LocalDateTime endTime = currentDate.atTime(23, 59, 59);
                
                List<Map<String, Object>> hourlyStats;
                switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                    case "BATCH":
                        hourlyStats = templateStatisticsMapper.getBatchHourlyResponseTimeStats(startTime, endTime);
                        break;
                    case "UNIFIED":
                        hourlyStats = templateStatisticsMapper.getUnifiedHourlyResponseTimeStats(startTime, endTime);
                        break;
                    case "JOB_LOG":
                    default:
                        hourlyStats = templateStatisticsMapper.getHourlyResponseTimeStats(startTime, endTime);
                        break;
                }
                
                // 计算当日平均响应时间
                double totalDuration = 0;
                int totalExecutions = 0;
                for (Map<String, Object> stat : hourlyStats) {
                    BigDecimal executionCount = safeGetBigDecimal(stat, "execution_count");
                    BigDecimal avgDuration = safeGetBigDecimal(stat, "avg_duration");
                    if (executionCount != null && avgDuration != null) {
                        totalDuration += avgDuration.doubleValue() * executionCount.longValue();
                        totalExecutions += executionCount.longValue();
                    }
                }
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("name", currentDate.format(DateTimeFormatter.ISO_DATE));
                dayData.put("value", totalExecutions > 0 ? totalDuration / totalExecutions : 0);
                dayData.put("executionCount", totalExecutions);
                
                result.add(dayData);
                
                currentDate = currentDate.plusDays(1);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取响应时间对比数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取协议类型分布对比数据
     */
    private List<Map<String, Object>> getProtocolDistributionCompareData(String startDate, String endDate, String dataSource) {
        try {
            // 协议类型分布不依赖时间范围，查询协议类型表
            List<Map<String, Object>> categoryStats = templateStatisticsMapper.getProtocolCategoryStats(null, null);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> stat : categoryStats) {
                String category = safeGetString(stat, "category");
                BigDecimal protocolCount = safeGetBigDecimal(stat, "protocol_count");
                
                Map<String, Object> item = new HashMap<>();
                item.put("name", category);
                item.put("value", protocolCount != null ? protocolCount.longValue() : 0);
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取协议类型分布对比数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取失败原因对比数据
     */
    private List<Map<String, Object>> getFailureReasonsCompareData(String startDate, String endDate, String dataSource) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            LocalDateTime startTime = start.atStartOfDay();
            LocalDateTime endTime = end.atTime(23, 59, 59);
            
            List<Map<String, Object>> failureReasons;
            switch (dataSource != null ? dataSource.toUpperCase() : "JOB_LOG") {
                case "BATCH":
                    failureReasons = templateStatisticsMapper.getBatchTopFailureReasons(startTime, endTime);
                    break;
                case "UNIFIED":
                case "JOB_LOG":
                default:
                    failureReasons = templateStatisticsMapper.getTopFailureReasons(startTime, endTime);
                    break;
            }
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> reason : failureReasons) {
                String failureReason = safeGetString(reason, "failure_reason");
                BigDecimal failureCount = safeGetBigDecimal(reason, "failure_count");
                
                Map<String, Object> item = new HashMap<>();
                item.put("name", failureReason);
                item.put("value", failureCount != null ? failureCount.longValue() : 0);
                result.add(item);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取失败原因对比数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算日执行量摘要
     */
    private Map<String, Object> calculateWeeklyExecutionSummary(List<Map<String, Object>> data) {
        Map<String, Object> summary = new HashMap<>();
        long total = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        
        for (Map<String, Object> day : data) {
            long value = (Long) day.getOrDefault("value", 0L);
            total += value;
            max = Math.max(max, value);
            min = Math.min(min, value);
        }
        
        summary.put("total", total);
        summary.put("avg", data.size() > 0 ? total / data.size() : 0);
        summary.put("max", max);
        summary.put("min", min == Long.MAX_VALUE ? 0 : min);
        
        return summary;
    }

    /**
     * 计算成功率摘要
     */
    private Map<String, Object> calculateSuccessRateSummary(List<Map<String, Object>> data) {
        Map<String, Object> summary = new HashMap<>();
        long success = 0;
        long failure = 0;
        
        for (Map<String, Object> item : data) {
            String name = (String) item.get("name");
            long value = (Long) item.getOrDefault("value", 0L);
            if ("成功".equals(name)) {
                success = value;
            } else if ("失败".equals(name)) {
                failure = value;
            }
        }
        
        long total = success + failure;
        summary.put("total", total);
        summary.put("success", success);
        summary.put("failure", failure);
        summary.put("successRate", total > 0 ? (double) success / total * 100 : 0);
        
        return summary;
    }

    /**
     * 计算响应时间摘要
     */
    private Map<String, Object> calculateResponseTimeSummary(List<Map<String, Object>> data) {
        Map<String, Object> summary = new HashMap<>();
        double totalDuration = 0;
        int totalExecutions = 0;
        double max = 0;
        double min = Double.MAX_VALUE;
        
        for (Map<String, Object> day : data) {
            double value = (Double) day.getOrDefault("value", 0.0);
            int executions = (Integer) day.getOrDefault("executionCount", 0);
            totalDuration += value * executions;
            totalExecutions += executions;
            max = Math.max(max, value);
            min = Math.min(min, value);
        }
        
        summary.put("avgDuration", totalExecutions > 0 ? totalDuration / totalExecutions : 0);
        summary.put("maxDuration", max);
        summary.put("minDuration", min == Double.MAX_VALUE ? 0 : min);
        summary.put("totalExecutions", totalExecutions);
        
        return summary;
    }

    /**
     * 计算协议分布摘要
     */
    private Map<String, Object> calculateProtocolDistributionSummary(List<Map<String, Object>> data) {
        Map<String, Object> summary = new HashMap<>();
        long total = 0;
        
        for (Map<String, Object> item : data) {
            long value = (Long) item.getOrDefault("value", 0L);
            total += value;
        }
        
        summary.put("total", total);
        summary.put("categoryCount", data.size());
        
        return summary;
    }

    /**
     * 计算失败原因摘要
     */
    private Map<String, Object> calculateFailureReasonsSummary(List<Map<String, Object>> data) {
        Map<String, Object> summary = new HashMap<>();
        long total = 0;
        
        for (Map<String, Object> item : data) {
            long value = (Long) item.getOrDefault("value", 0L);
            total += value;
        }
        
        summary.put("total", total);
        summary.put("topCount", data.size());
        
        return summary;
    }

    /**
     * 计算对比摘要
     */
    private Map<String, Object> calculateCompareSummary(CompareResultDTO result, String reportType) {
        Map<String, Object> summary = new HashMap<>();
        
        Map<String, Object> group1Summary = result.getSummary1();
        Map<String, Object> group2Summary = result.getSummary2();
        
        if (group1Summary == null) group1Summary = new HashMap<>();
        if (group2Summary == null) group2Summary = new HashMap<>();
        
        switch (reportType != null ? reportType.toUpperCase() : "WEEKLY_EXECUTION") {
            case "SUCCESS_RATE":
                double rate1 = (Double) group1Summary.getOrDefault("successRate", 0.0);
                double rate2 = (Double) group2Summary.getOrDefault("successRate", 0.0);
                summary.put("change", rate2 - rate1);
                summary.put("changePercentage", rate1 > 0 ? (rate2 - rate1) / rate1 * 100 : 0);
                break;
            case "RESPONSE_TIME":
                double time1 = (Double) group1Summary.getOrDefault("avgDuration", 0.0);
                double time2 = (Double) group2Summary.getOrDefault("avgDuration", 0.0);
                summary.put("change", time2 - time1);
                summary.put("changePercentage", time1 > 0 ? (time2 - time1) / time1 * 100 : 0);
                break;
            case "PROTOCOL_DISTRIBUTION":
                long protoTotal1 = Long.valueOf(String.valueOf(group1Summary.getOrDefault("total", 0)));
                long protoTotal2 = Long.valueOf(String.valueOf(group2Summary.getOrDefault("total", 0)));
                summary.put("change", protoTotal2 - protoTotal1);
                summary.put("changePercentage", protoTotal1 > 0 ? (double) (protoTotal2 - protoTotal1) / protoTotal1 * 100 : 0);
                break;
            case "FAILURE_REASONS":
                long failTotal1 = (Long) group1Summary.getOrDefault("total", 0L);
                long failTotal2 = (Long) group2Summary.getOrDefault("total", 0L);
                summary.put("change", failTotal2 - failTotal1);
                summary.put("changePercentage", failTotal1 > 0 ? (double) (failTotal2 - failTotal1) / failTotal1 * 100 : 0);
                break;
            case "WEEKLY_EXECUTION":
            default:
                long total1 = (Long) group1Summary.getOrDefault("total", 0L);
                long total2 = (Long) group2Summary.getOrDefault("total", 0L);
                summary.put("change", total2 - total1);
                summary.put("changePercentage", total1 > 0 ? (double) (total2 - total1) / total1 * 100 : 0);
                break;
        }
        
        return summary;
    }
}