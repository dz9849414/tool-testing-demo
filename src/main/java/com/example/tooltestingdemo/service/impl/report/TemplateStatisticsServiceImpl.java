package com.example.tooltestingdemo.service.impl.report;

import com.example.tooltestingdemo.dto.report.ReportDTO;
import com.example.tooltestingdemo.dto.report.ReportTemplateDTO;
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
    public ReportDTO getTemplateUsageReport(String timeRange, String startDate, String endDate, String templateType) {
        log.info("获取模板使用频率报告，时间范围：{}，模板类型：{}", timeRange, templateType);
        
        // 计算时间范围
        LocalDate[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        
        // 从数据库获取实际统计数据
        List<Map<String, Object>> usageStats = templateStatisticsMapper.getTemplateUsageStats(
            dateRange[0].atStartOfDay(), dateRange[1].atTime(23, 59, 59));
        
        ReportDTO report = new ReportDTO();
        report.setId(System.currentTimeMillis());
        report.setName("模板使用频率报告 - " + getTimeRangeDisplay(timeRange));
        report.setDescription("基于模板任务执行日志生成的统计报告");
        report.setReportType("TEMPLATE_USAGE_STATISTICS");
        report.setContent(convertToJson(buildUsageReportData(usageStats, dateRange)));
        report.setGenerateType("MANUAL");
        report.setStatus("PUBLISHED");
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        
        return report;
    }

    @Override
    public ReportDTO getTemplateEfficiencyReport(String startDate, String endDate, String templateId) {
        log.info("获取模板执行效率报告，时间范围：{} 至 {}，模板ID：{}", startDate, endDate, templateId);
        
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
        
        // 从数据库获取实际效率统计数据
        List<Map<String, Object>> efficiencyStats;
        
        if (templateId != null && !templateId.trim().isEmpty()) {
            // 如果指定了模板ID，只统计该模板的效率数据
            efficiencyStats = templateStatisticsMapper.getTemplateEfficiencyStatsByTemplateId(
                startTime, endTime, Long.valueOf(templateId));
        } else {
            // 如果没有指定模板ID，统计所有模板的效率数据
            efficiencyStats = templateStatisticsMapper.getTemplateEfficiencyStats(startTime, endTime);
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
                String formattedContent = applyTemplateFormat(template.getTemplateStructure(), reportData);
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
    private String applyTemplateFormat(String templateContent, Map<String, Object> reportData) {
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
}