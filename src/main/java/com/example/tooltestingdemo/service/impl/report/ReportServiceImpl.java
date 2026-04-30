package com.example.tooltestingdemo.service.impl.report;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.common.PageResult;
import com.example.tooltestingdemo.dto.report.*;
import com.example.tooltestingdemo.entity.report.Report;
import com.example.tooltestingdemo.entity.template.TemplateJobLog;
import com.example.tooltestingdemo.mapper.report.ReportMapper;
import com.example.tooltestingdemo.mapper.template.TemplateJobLogMapper;
import com.example.tooltestingdemo.service.report.IReportService;
import com.example.tooltestingdemo.service.report.IReportTemplateService;
import com.example.tooltestingdemo.service.report.ITemplateStatisticsService;
import com.example.tooltestingdemo.entity.report.ReportTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
// Standard14Fonts is not public, use PDType1Font directly

/**
 * 报告服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private final ReportMapper reportMapper;
    private final TemplateJobLogMapper templateJobLogMapper;
    private final ITemplateStatisticsService templateStatisticsService;
    private final IReportTemplateService reportTemplateService;

    @Override
    public Long createReport(ReportDTO reportDTO) {
        Report report = new Report();
        BeanUtils.copyProperties(reportDTO, report);
        
        // 设置默认值
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setIsDeleted(0);
        report.setExportCount(0);
        report.setIsNotified(false);
        
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public Boolean updateReport(Long id, ReportDTO reportDTO) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return false;
        }
        
        BeanUtils.copyProperties(reportDTO, report);
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.updateById(report) > 0;
    }

    @Override
    public Boolean deleteReport(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return false;
        }
        
        // 软删除
        report.setIsDeleted(1);
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.deleteById(report) > 0;
    }

    @Override
    public List<ReportDTO> getReportList(String reportType, String status) {
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Report::getIsDeleted, 0);
        
        if (reportType != null) {
            queryWrapper.eq(Report::getReportType, reportType);
        }
        
        if (status != null) {
            queryWrapper.eq(Report::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Report::getCreateTime);
        
        List<Report> reports = reportMapper.selectList(queryWrapper);
        
        return reports.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ReportDTO getReportDetail(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        return convertToDTO(report);
    }

    @Override
    public java.util.List<FailureTimelineDTO> getFailureTimeline(Long templateId, String timeRange) {
        try {
            // 根据时间范围计算开始时间
            LocalDateTime startTime = calculateStartTime(timeRange);
            LocalDateTime endTime = LocalDateTime.now();
            
            // 查询失败记录
            List<FailureTimelineDTO> timeline = templateStatisticsService.getFailureTimelineData(templateId, startTime, endTime);
            
            return timeline != null ? timeline : new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("获取失败时间线失败 - templateId: {}, timeRange: {}", templateId, timeRange, e);
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public Long autoGenerateReport(String reportType, String dataSourceIds) {
        try {
            // 根据reportType获取对应的模板
            ReportTemplate template = getTemplateByReportType(reportType);
            
            // 构建报告DTO
            ReportDTO reportDTO = new ReportDTO();
            reportDTO.setName("自动生成报告 - " + reportType + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            reportDTO.setDescription("基于最近测试数据自动生成的" + reportType + "报告");
            reportDTO.setReportType(reportType);
            reportDTO.setGenerateType("AUTO");
            reportDTO.setDataSourceIds(dataSourceIds);
            reportDTO.setStatus("DRAFT");
            
            // 获取模板结构和章节结构
            String chapterStructure = template != null ? template.getChapterStructure() : null;
            String templateStructure = template != null ? template.getTemplateStructure() : null;
            
            // 优先级：chapter_structure > template_structure > 旧逻辑
            if (chapterStructure != null && !chapterStructure.trim().isEmpty() && 
                (chapterStructure.startsWith("[") || chapterStructure.startsWith("{"))) {
                // 优先级1：根据章节结构组织报告内容
                log.info("使用章节结构(chapter_structure)生成报告内容");
                reportDTO.setContent(buildContentByChapterStructure(chapterStructure, reportType, dataSourceIds));
            } else if (templateStructure != null && !templateStructure.trim().isEmpty() && 
                       templateStructure.startsWith("{")) {
                // 优先级2：根据模板结构(template_structure)生成报告内容
                log.info("使用模板结构(template_structure)生成报告内容");
                reportDTO.setContent(buildContentByTemplateStructure(templateStructure, reportType, dataSourceIds));
            } else {
                // 优先级3：旧逻辑：直接获取统计数据
                log.info("使用默认逻辑生成报告内容");
                Object statisticsData = getRecentTestDataFromExecuteLog(reportType, dataSourceIds);
                
                // 将统计数据转换为JSON字符串
                if (statisticsData != null) {
                    if (statisticsData instanceof StatisticsReportDTO) {
                        StatisticsReportDTO statsReport = (StatisticsReportDTO) statisticsData;
                        reportDTO.setContent(statsReport.getContent() != null ? 
                            statsReport.getContent().toString() : "{}");
                    } else if (statisticsData instanceof List) {
                        // 简化格式的数据
                        reportDTO.setContent(new JSONArray((List) statisticsData).toString());
                    } else {
                        reportDTO.setContent("{}");
                    }
                } else {
                    reportDTO.setContent("{}");
                }
            }
            
            return createReport(reportDTO);
            
        } catch (Exception e) {
            log.error("自动生成报告失败", e);
            throw new RuntimeException("自动生成报告失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据报告类型获取模板
     */
    private ReportTemplate getTemplateByReportType(String reportType) {
        try {
            LambdaQueryWrapper<ReportTemplate> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ReportTemplate::getTemplateType, reportType)
                       .eq(ReportTemplate::getStatus, 1)
                       .orderByDesc(ReportTemplate::getSortOrder)
                       .last("LIMIT 1");
            return reportTemplateService.getOne(queryWrapper);
        } catch (Exception e) {
            log.warn("未找到报告类型 {} 对应的模板", reportType);
            return null;
        }
    }

    /**
     * 根据模板结构(template_structure)构建报告内容
     * template_structure格式：{"sections": ["任务概览", "执行统计", "成功率分析", "性能趋势", "问题总结"]}
     */
    private String buildContentByTemplateStructure(String templateStructure, String reportType, String dataSourceIds) {
        try {
            JSONObject templateJson = JSON.parseObject(templateStructure);
            JSONArray sections = templateJson.getJSONArray("sections");
            
            if (sections == null || sections.isEmpty()) {
                log.warn("模板结构中sections为空");
                return "{}";
            }
            
            JSONArray contentArray = new JSONArray();
            
            // 获取最近7天的数据时间范围
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);
            String startDate = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDate = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String normalizedDataSource = dataSourceIds != null && !dataSourceIds.isEmpty() ? dataSourceIds : "UNIFIED";
            
            for (int i = 0; i < sections.size(); i++) {
                String sectionName = sections.getString(i);
                JSONObject sectionContent = new JSONObject();
                sectionContent.put("title", sectionName);
                
                // 根据section名称确定数据来源和类型
                SectionMapping mapping = getSectionMapping(sectionName);
                sectionContent.put("type", mapping.getType());
                sectionContent.put("dataSource", mapping.getDataSource());
                
                // 获取对应的数据
                Object data = getDataByDataSource(mapping.getDataSource(), startDate, endDate, normalizedDataSource);
                sectionContent.put("data", data);
                
                contentArray.add(sectionContent);
            }
            
            return contentArray.toJSONString();
        } catch (Exception e) {
            log.error("根据模板结构构建报告内容失败", e);
            return "{}";
        }
    }

    /**
     * 获取章节名称到数据来源的映射
     */
    private SectionMapping getSectionMapping(String sectionName) {
        SectionMapping mapping = new SectionMapping();
        
        switch (sectionName) {
            case "任务概览":
            case "概述":
            case "测试概览":
            case "执行概览":
                mapping.setType("text");
                mapping.setDataSource("OVERVIEW");
                break;
            case "执行统计":
            case "日执行量统计":
            case "效率趋势":
            case "使用趋势":
            case "任务统计":
            case "周执行统计":
                mapping.setType("chart");
                mapping.setDataSource("WEEKLY_EXECUTION");
                break;
            case "成功率分析":
            case "成功率趋势":
                mapping.setType("chart");
                mapping.setDataSource("SUCCESS_RATE");
                break;
            case "性能趋势":
            case "响应时间":
            case "性能对比":
            case "响应时间分析":
            case "响应时间趋势":
            case "响应时间分布":
                mapping.setType("chart");
                mapping.setDataSource("RESPONSE_TIME");
                break;
            case "协议分布":
            case "协议类型分布":
                mapping.setType("chart");
                mapping.setDataSource("PROTOCOL_DISTRIBUTION");
                break;
            case "问题总结":
            case "失败原因":
            case "失败原因TOP5":
            case "错误统计":
            case "故障原因分析":
                mapping.setType("chart");
                mapping.setDataSource("FAILURE_REASONS");
                break;
            case "优化建议":
            case "测试结论":
            case "改进建议":
                mapping.setType("text");
                mapping.setDataSource("OPTIMIZATION_SUGGESTIONS");
                break;
            case "接口详情":
                mapping.setType("text");
                mapping.setDataSource("INTERFACE_DETAILS");
                break;
            case "异常检测":
                mapping.setType("chart");
                mapping.setDataSource("ANOMALY_DETECTION");
                break;
            case "影响因素":
                mapping.setType("text");
                mapping.setDataSource("INFLUENCING_FACTORS");
                break;
            default:
                mapping.setType("text");
                mapping.setDataSource("UNKNOWN");
                break;
        }
        
        return mapping;
    }

    /**
     * 章节映射内部类
     */
    private static class SectionMapping {
        private String type;
        private String dataSource;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getDataSource() {
            return dataSource;
        }
        
        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }
    }

    /**
     * 根据章节结构构建报告内容
     */
    private String buildContentByChapterStructure(String chapterStructure, String reportType, String dataSourceIds) {
        try {
            JSONArray chapters = JSON.parseArray(chapterStructure);
            JSONArray contentArray = new JSONArray();
            
            // 获取最近7天的数据时间范围
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);
            String startDate = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDate = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            for (int i = 0; i < chapters.size(); i++) {
                JSONObject chapter = chapters.getJSONObject(i);
                String title = chapter.getString("title");
                String type = chapter.getString("type");
                String dataSource = chapter.getString("dataSource");
                String chartType = chapter.getString("chartType");
                
                JSONObject chapterContent = new JSONObject();
                chapterContent.put("title", title);
                chapterContent.put("type", type);
                chapterContent.put("dataSource", dataSource);
                
                if (chartType != null) {
                    chapterContent.put("chartType", chartType);
                }
                
                // 根据dataSource获取对应的数据
                Object data = getDataByDataSource(dataSource, startDate, endDate, dataSourceIds);
                chapterContent.put("data", data);
                
                contentArray.add(chapterContent);
            }
            
            return contentArray.toJSONString();
        } catch (Exception e) {
            log.error("根据章节结构构建报告内容失败", e);
            return "{}";
        }
    }

    /**
     * 根据dataSource获取对应的数据
     */
    private Object getDataByDataSource(String dataSource, String startDate, String endDate, String dataSourceIds) {
        String normalizedDataSource = dataSourceIds != null && !dataSourceIds.isEmpty() ? dataSourceIds : "UNIFIED";
        
        switch (dataSource != null ? dataSource.toUpperCase() : "UNKNOWN") {
            case "OVERVIEW":
                return buildOverviewData(startDate, endDate, normalizedDataSource);
            case "EFFICIENCY_TREND":
            case "WEEKLY_EXECUTION":
                StatisticsReportDTO weeklyReport = templateStatisticsService.getWeeklyExecutionReport(startDate, endDate, normalizedDataSource);
                return weeklyReport != null && weeklyReport.getContent() != null && !weeklyReport.getContent().isEmpty() ? 
                    weeklyReport.getContent().get(0) : buildEmptyChartData(startDate, endDate, "WEEKLY_EXECUTION");
            case "SUCCESS_RATE":
                StatisticsReportDTO successRateReport = templateStatisticsService.getSuccessRateReport(startDate, endDate, normalizedDataSource);
                return successRateReport != null && successRateReport.getContent() != null && !successRateReport.getContent().isEmpty() ? 
                    successRateReport.getContent().get(0) : buildEmptyChartData(startDate, endDate, "SUCCESS_RATE");
            case "RESPONSE_TIME":
                Object responseData = templateStatisticsService.getHourlyResponseTimeReportSimple(startDate, endDate, normalizedDataSource);
                if (responseData != null && responseData instanceof List && !((List<?>) responseData).isEmpty()) {
                    return responseData;
                }
                // 业务查询没有数据就返回空列表，不生成模拟数据
                return new JSONArray();
            case "PROTOCOL_DISTRIBUTION":
                StatisticsReportDTO protocolReport = templateStatisticsService.getProtocolDistributionReport(startDate, endDate, "CATEGORY");
                return protocolReport != null && protocolReport.getContent() != null && !protocolReport.getContent().isEmpty() ? 
                    protocolReport.getContent().get(0) : buildEmptyChartData(startDate, endDate, "PROTOCOL_DISTRIBUTION");
            case "FAILURE_REASONS":
                Object failureData = templateStatisticsService.getTopFailureReasonsReportSimple(startDate, endDate, normalizedDataSource);
                if (failureData != null && failureData instanceof List && !((List<?>) failureData).isEmpty()) {
                    return failureData;
                }
                return buildEmptyChartData(startDate, endDate, "FAILURE_REASONS");
            case "OPTIMIZATION_SUGGESTIONS":
                return buildOptimizationSuggestions(startDate, endDate, normalizedDataSource);
            case "INTERFACE_DETAILS":
                return buildInterfaceDetails(startDate, endDate, normalizedDataSource);
            case "ANOMALY_DETECTION":
                return buildEmptyChartData(startDate, endDate, "ANOMALY_DETECTION");
            case "INFLUENCING_FACTORS":
                return buildInfluencingFactors(startDate, endDate, normalizedDataSource);
            default:
                // 默认返回空数据
                return new JSONObject();
        }
    }

    /**
     * 构建空的图表数据结构
     */
    private JSONObject buildEmptyChartData(String startDate, String endDate, String dataSource) {
        JSONObject emptyData = new JSONObject();
        emptyData.put("startDate", startDate);
        emptyData.put("endDate", endDate);
        emptyData.put("dataSource", dataSource);
        emptyData.put("summary", new JSONObject());
        emptyData.put("details", new JSONArray());
        return emptyData;
    }

    /**
     * 生成模拟的响应时间数据（用于响应时间趋势、响应时间分布、性能对比）
     */
    private JSONArray generateMockResponseTimeData(String startDate, String endDate) {
        JSONArray result = new JSONArray();
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // 计算天数
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            int totalDays = (int) Math.min(days, 7); // 最多生成7天数据
            
            Random random = new Random();
            
            for (int i = 0; i < totalDays; i++) {
                LocalDate currentDate = start.plusDays(i);
                String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 模拟不同时间段的响应时间数据
                for (int hour = 0; hour < 24; hour += 2) { // 每2小时一个数据点
                    JSONObject dataPoint = new JSONObject();
                    dataPoint.put("name", dateStr + " " + String.format("%02d:00", hour));
                    dataPoint.put("date", dateStr);
                    dataPoint.put("hour", hour);
                    
                    // 模拟响应时间（毫秒）- 工作时间较高，夜间较低
                    double baseValue = (hour >= 9 && hour <= 18) ? 150 : 80;
                    double value = baseValue + random.nextDouble() * 100 - 30; // 添加随机波动
                    dataPoint.put("value", Math.round(value * 100) / 100.0);
                    
                    // 添加其他统计字段
                    dataPoint.put("min", Math.round((value - 20) * 100) / 100.0);
                    dataPoint.put("max", Math.round((value + 30) * 100) / 100.0);
                    dataPoint.put("avg", Math.round(value * 100) / 100.0);
                    dataPoint.put("count", random.nextInt(50) + 10);
                    
                    result.add(dataPoint);
                }
            }
        } catch (Exception e) {
            log.warn("生成模拟响应时间数据失败", e);
            // 返回默认的空数据
            for (int i = 0; i < 14; i++) {
                JSONObject dataPoint = new JSONObject();
                dataPoint.put("name", "时间点" + (i + 1));
                dataPoint.put("value", 100 + i * 10);
                dataPoint.put("min", 80 + i * 8);
                dataPoint.put("max", 120 + i * 12);
                dataPoint.put("avg", 100 + i * 10);
                dataPoint.put("count", 20 + i * 3);
                result.add(dataPoint);
            }
        }
        
        return result;
    }

    /**
     * 生成模拟的性能对比数据
     */
    private JSONObject generateMockPerformanceComparisonData(String startDate, String endDate) {
        JSONObject result = new JSONObject();
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // 计算天数
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            int totalDays = (int) Math.min(days, 7);
            
            Random random = new Random();
            
            // 生成对比数据 - 模拟两组数据进行比较
            JSONArray data1 = new JSONArray(); // 当前周期数据
            JSONArray data2 = new JSONArray(); // 对比周期数据（模拟上一周）
            
            for (int i = 0; i < totalDays; i++) {
                LocalDate currentDate = start.plusDays(i);
                String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 当前周期数据
                JSONObject point1 = new JSONObject();
                point1.put("name", dateStr);
                double value1 = 120 + random.nextDouble() * 60;
                point1.put("value", Math.round(value1 * 100) / 100.0);
                point1.put("count", random.nextInt(100) + 50);
                data1.add(point1);
                
                // 对比周期数据（模拟比当前略低或略高）
                JSONObject point2 = new JSONObject();
                point2.put("name", dateStr);
                double value2 = value1 * (0.9 + random.nextDouble() * 0.2); // 90%-110% 的波动
                point2.put("value", Math.round(value2 * 100) / 100.0);
                point2.put("count", random.nextInt(100) + 50);
                data2.add(point2);
            }
            
            result.put("data1", data1);
            result.put("data2", data2);
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            
            // 添加汇总统计
            JSONObject summary = new JSONObject();
            double avg1 = data1.stream().mapToDouble(obj -> ((JSONObject) obj).getDoubleValue("value")).average().orElse(0);
            double avg2 = data2.stream().mapToDouble(obj -> ((JSONObject) obj).getDoubleValue("value")).average().orElse(0);
            summary.put("currentAvg", Math.round(avg1 * 100) / 100.0);
            summary.put("compareAvg", Math.round(avg2 * 100) / 100.0);
            summary.put("changeRate", Math.round((avg1 - avg2) / avg2 * 10000) / 100.0);
            result.put("summary", summary);
            
        } catch (Exception e) {
            log.warn("生成模拟性能对比数据失败", e);
            // 返回默认数据
            JSONArray data1 = new JSONArray();
            JSONArray data2 = new JSONArray();
            for (int i = 0; i < 7; i++) {
                JSONObject point1 = new JSONObject();
                point1.put("name", "第" + (i + 1) + "天");
                point1.put("value", 120 + i * 5);
                data1.add(point1);
                
                JSONObject point2 = new JSONObject();
                point2.put("name", "第" + (i + 1) + "天");
                point2.put("value", 115 + i * 5);
                data2.add(point2);
            }
            result.put("data1", data1);
            result.put("data2", data2);
            
            JSONObject summary = new JSONObject();
            summary.put("currentAvg", 140);
            summary.put("compareAvg", 135);
            summary.put("changeRate", 3.7);
            result.put("summary", summary);
        }
        
        return result;
    }

    /**
     * 构建接口详情数据
     */
    private JSONObject buildInterfaceDetails(String startDate, String endDate, String dataSource) {
        JSONObject details = new JSONObject();
        details.put("startDate", startDate);
        details.put("endDate", endDate);
        details.put("totalInterfaces", 0);
        details.put("successCount", 0);
        details.put("failureCount", 0);
        details.put("interfaces", new JSONArray());
        return details;
    }

    /**
     * 构建影响因素数据
     */
    private JSONObject buildInfluencingFactors(String startDate, String endDate, String dataSource) {
        JSONObject factors = new JSONObject();
        factors.put("startDate", startDate);
        factors.put("endDate", endDate);
        factors.put("factors", new JSONArray());
        return factors;
    }

    /**
     * 构建下周计划数据（优化建议）
     */
    private JSONObject buildOptimizationSuggestions(String startDate, String endDate, String dataSource) {
        JSONObject suggestions = new JSONObject();
        suggestions.put("startDate", startDate);
        suggestions.put("endDate", endDate);
        
        List<String> suggestionList = new ArrayList<>();
        
        try {
            // 获取统计数据
            StatisticsReportDTO successRateReport = templateStatisticsService.getSuccessRateReport(startDate, endDate, dataSource);
            
            // 一、本周总结
            suggestionList.add("【本周总结】");
            if (successRateReport != null && successRateReport.getContent() != null && !successRateReport.getContent().isEmpty()) {
                JSONObject successData = successRateReport.getContent().getJSONObject(0);
                if (successData.containsKey("summary")) {
                    JSONObject summary = successData.getJSONObject("summary");
                    double successRate = summary.getDoubleValue("successRate");
                    long totalCount = summary.getLongValue("totalCount");
                    long failureCount = summary.getLongValue("failureCount");
                    
                    suggestionList.add("  执行任务：" + totalCount + " 次");
                    suggestionList.add("  成功率：" + String.format("%.1f", successRate) + "%");
                    suggestionList.add("  失败次数：" + failureCount + " 次");
                    
                    if (successRate >= 90) {
                        suggestionList.add("  [OK] 本周执行情况良好，成功率达标");
                    } else {
                        suggestionList.add("  [WARN] 本周成功率未达标，需重点关注");
                    }
                }
            } else {
                suggestionList.add("  暂无执行数据");
            }
            
            // 二、下周计划
            suggestionList.add("");
            suggestionList.add("【下周计划】");
            
            // 2.1 问题修复
            suggestionList.add("  -- 问题修复：");
            if (successRateReport != null && successRateReport.getContent() != null && !successRateReport.getContent().isEmpty()) {
                JSONObject successData = successRateReport.getContent().getJSONObject(0);
                if (successData.containsKey("summary")) {
                    JSONObject summary = successData.getJSONObject("summary");
                    long failureCount = summary.getLongValue("failureCount");
                    double successRate = summary.getDoubleValue("successRate");
                    
                    if (failureCount > 0) {
                        suggestionList.add("    * 分析 " + failureCount + " 次失败原因，制定修复方案");
                    }
                    if (successRate < 90) {
                        suggestionList.add("    * 优化测试用例，提升执行成功率");
                    }
                }
            }
            suggestionList.add("    * 跟进历史遗留问题处理进度");
            
            // 2.2 日常维护
            suggestionList.add("  -- 日常维护：");
            suggestionList.add("    * 定期检查模板执行日志");
            suggestionList.add("    * 根据性能趋势调整任务调度时间");
            suggestionList.add("    * 关注协议分布变化，及时更新支持");
            
            // 2.3 优化改进
            suggestionList.add("  -- 优化改进：");
            suggestionList.add("    * 优化执行效率，减少耗时");
            suggestionList.add("    * 完善异常处理机制");
            
            // 三、风险评估
            suggestionList.add("");
            suggestionList.add("【风险评估】");
            suggestionList.add("  * 暂无重大风险");
            suggestionList.add("  * 需关注执行稳定性");
            
            // 四、需要支持
            suggestionList.add("");
            suggestionList.add("【需要支持】");
            suggestionList.add("  * 暂无需要协调的事项");
            
        } catch (Exception e) {
            log.warn("构建下周计划失败", e);
            suggestionList.add("根据报告数据分析进行相应优化");
        }
        
        suggestions.put("suggestions", suggestionList);
        return suggestions;
    }

    /**
     * 构建概述数据
     */
    private JSONObject buildOverviewData(String startDate, String endDate, String dataSource) {
        JSONObject overview = new JSONObject();
        overview.put("startDate", startDate);
        overview.put("endDate", endDate);
        overview.put("generateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        overview.put("dataSource", dataSource);
        
        // 获取关键统计指标
        try {
            StatisticsReportDTO successRateReport = templateStatisticsService.getSuccessRateReport(startDate, endDate, dataSource);
            if (successRateReport != null && successRateReport.getContent() != null && !successRateReport.getContent().isEmpty()) {
                JSONObject successData = successRateReport.getContent().getJSONObject(0);
                if (successData.containsKey("summary")) {
                    JSONObject summary = successData.getJSONObject("summary");
                    overview.put("totalCount", summary.getOrDefault("totalCount", 0));
                    overview.put("successCount", summary.getOrDefault("successCount", 0));
                    overview.put("failureCount", summary.getOrDefault("failureCount", 0));
                    overview.put("successRate", summary.getOrDefault("successRate", 0));
                }
            }
            
            StatisticsReportDTO weeklyReport = templateStatisticsService.getWeeklyExecutionReport(startDate, endDate, dataSource);
            if (weeklyReport != null && weeklyReport.getContent() != null && !weeklyReport.getContent().isEmpty()) {
                JSONObject weeklyData = weeklyReport.getContent().getJSONObject(0);
                if (weeklyData.containsKey("summary")) {
                    JSONObject summary = weeklyData.getJSONObject("summary");
                    overview.put("avgDailyExecution", summary.getOrDefault("avg", 0));
                }
            }
        } catch (Exception e) {
            log.warn("构建概述数据失败", e);
        }
        
        return overview;
    }

    /**
     * 从pdm_tool_template_execute_log表获取最近的测试数据
     * 
     * @param reportType 报告类型（支持数字和字符串）
     * @param dataSourceIds 数据源ID
     * @return 统计数据
     */
    private Object getRecentTestDataFromExecuteLog(String reportType, String dataSourceIds) {
        try {
            // 获取最近7天的数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);
            
            // 根据reportType调用不同的统计方法（支持数字和字符串）
            String normalizedReportType = normalizeReportType(reportType);
            
            switch (normalizedReportType) {
                case "PROTOCOL_DISTRIBUTION":
                case "1":
                    return templateStatisticsService.getProtocolDistributionReport(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "CATEGORY");
                case "RESPONSE_TIME":
                case "2":
                    return templateStatisticsService.getHourlyResponseTimeReportSimple(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                case "FAILURE_REASONS":
                case "3":
                    return templateStatisticsService.getTopFailureReasonsReportSimple(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                case "WEEKLY_EXECUTION":
                case "4":
                    return templateStatisticsService.getWeeklyExecutionReport(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                case "SUCCESS_RATE":
                case "5":
                    return templateStatisticsService.getSuccessRateReport(
                        startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                        "UNIFIED");
                default:
                    // 默认返回空数据
                    return null;
            }
        } catch (Exception e) {
            log.error("获取最近测试数据失败", e);
            return null;
        }
    }
    
    /**
     * 标准化报告类型参数
     * 
     * @param reportType 原始报告类型参数
     * @return 标准化的报告类型
     */
    private String normalizeReportType(String reportType) {
        if (reportType == null) {
            return "UNKNOWN";
        }
        
        // 数字映射
        switch (reportType) {
            case "1": return "PROTOCOL_DISTRIBUTION";
            case "2": return "RESPONSE_TIME";
            case "3": return "FAILURE_REASONS";
            case "4": return "WEEKLY_EXECUTION";
            case "5": return "SUCCESS_RATE";
            default: return reportType.toUpperCase();
        }
    }

    @Override
    public String previewReportHtml(Long id, String pageRange, String dataSource) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        // 构建HTML格式预览内容，与导出接口内容一致
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"zh-CN\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>").append(report.getName()).append(" - 预览</title>");
        html.append("<style>");
        html.append("body { font-family: 'Microsoft YaHei', sans-serif; margin: 20px; background: #f5f5f5; }");
        html.append(".report-container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        html.append(".report-header { border-bottom: 2px solid #1890ff; padding-bottom: 20px; margin-bottom: 30px; }");
        html.append(".report-title { font-size: 24px; color: #1890ff; margin: 0; }");
        html.append(".report-meta { color: #666; font-size: 14px; margin-top: 10px; }");
        html.append(".report-content { line-height: 1.6; }");
        html.append(".section { margin-bottom: 25px; }");
        html.append(".section-title { font-size: 18px; color: #333; border-left: 4px solid #1890ff; padding-left: 10px; margin-bottom: 15px; }");
        html.append(".data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        html.append(".data-table th, .data-table td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }");
        html.append(".data-table th { background: #f0f0f0; font-weight: bold; }");
        html.append(".success { color: #52c41a; }");
        html.append(".failure { color: #ff4d4f; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"report-container\">");
        
        // 报告头部信息
        html.append("<div class=\"report-header\">");
        html.append("<h1 class=\"report-title\">").append(report.getName()).append("</h1>");
        html.append("<div class=\"report-meta\">");
        html.append("<p><strong>报告ID：</strong>").append(report.getId()).append("</p>");
        html.append("<p><strong>描述：</strong>").append(report.getDescription()).append("</p>");
        html.append("<p><strong>类型：</strong>").append(report.getReportType()).append("</p>");
        html.append("<p><strong>状态：</strong>").append(report.getStatus()).append("</p>");
        html.append("<p><strong>数据源：</strong>").append(dataSource).append("</p>");
        html.append("<p><strong>页面范围：</strong>").append(pageRange).append("</p>");
        html.append("</div>");
        html.append("</div>");
        
        // 报告内容（与导出接口内容一致）
        html.append("<div class=\"report-content\">");
        
        // 解析报告内容数据
        if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
            try {
                JSONObject content = JSON.parseObject(report.getContent());
                
                // 添加统计信息
                if (content.containsKey("summary")) {
                    html.append("<div class=\"section\">");
                    html.append("<h2 class=\"section-title\">执行统计</h2>");
                    
                    JSONObject summary = content.getJSONObject("summary");
                    html.append("<table class=\"data-table\">");
                    html.append("<tr><th>指标</th><th>数值</th></tr>");
                    html.append("<tr><td>总执行次数</td><td>").append(summary.getInteger("totalCount")).append("</td></tr>");
                    html.append("<tr><td>成功次数</td><td class=\"success\">").append(summary.getInteger("successCount")).append("</td></tr>");
                    html.append("<tr><td>失败次数</td><td class=\"failure\">").append(summary.getInteger("failureCount")).append("</td></tr>");
                    html.append("<tr><td>成功率</td><td class=\"success\">").append(summary.getDouble("successRate")).append("%</td></tr>");
                    html.append("<tr><td>失败率</td><td class=\"failure\">").append(summary.getDouble("failureRate")).append("%</td></tr>");
                    html.append("</table>");
                    html.append("</div>");
                }
                
                // 添加时间范围信息
                if (content.containsKey("startDate") && content.containsKey("endDate")) {
                    html.append("<div class=\"section\">");
                    html.append("<h2 class=\"section-title\">时间范围</h2>");
                    html.append("<p><strong>开始时间：</strong>").append(content.getString("startDate")).append("</p>");
                    html.append("<p><strong>结束时间：</strong>").append(content.getString("endDate")).append("</p>");
                    html.append("</div>");
                }
                
                // 添加数据源信息
                if (content.containsKey("dataSourceName")) {
                    html.append("<div class=\"section\">");
                    html.append("<h2 class=\"section-title\">数据源</h2>");
                    html.append("<p><strong>数据源名称：</strong>").append(content.getString("dataSourceName")).append("</p>");
                    html.append("</div>");
                }
                
            } catch (Exception e) {
                html.append("<div class=\"section\">");
                html.append("<h2 class=\"section-title\">报告内容</h2>");
                html.append("<p>无法解析报告内容：").append(e.getMessage()).append("</p>");
                html.append("</div>");
            }
        } else {
            html.append("<div class=\"section\">");
            html.append("<h2 class=\"section-title\">报告内容</h2>");
            html.append("<p>暂无报告内容数据</p>");
            html.append("</div>");
        }
        
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    @Override
    public byte[] previewReportImage(Long id, String format, String pageRange, String dataSource) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        try {
            // 创建图片
            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // 设置背景色
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 800, 600);
            
            // 设置抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制报告标题
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 24));
            String title = report.getName();
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (800 - titleWidth) / 2, 50);
            
            // 绘制报告信息
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            g2d.drawString("报告ID: " + report.getId(), 50, 100);
            g2d.drawString("类型: " + report.getReportType(), 50, 130);
            g2d.drawString("数据源: " + dataSource, 50, 160);
            
            // 解析报告内容并绘制图表
            if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
                try {
                    JSONObject content = JSON.parseObject(report.getContent());
                    
                    if (content.containsKey("summary")) {
                        JSONObject summary = content.getJSONObject("summary");
                        
                        // 绘制成功率图表
                        int successRate = summary.getInteger("successRate");
                        int failureRate = summary.getInteger("failureRate");
                        
                        // 绘制饼图
                        int centerX = 400;
                        int centerY = 350;
                        int radius = 100;
                        
                        // 成功部分
                        g2d.setColor(Color.GREEN);
                        g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, (int)(360 * successRate / 100));
                        
                        // 失败部分
                        g2d.setColor(Color.RED);
                        g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int)(360 * successRate / 100), (int)(360 * failureRate / 100));
                        
                        // 绘制图例
                        g2d.setColor(Color.BLACK);
                        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                        g2d.drawString("成功: " + successRate + "%", centerX + radius + 20, centerY - 20);
                        g2d.drawString("失败: " + failureRate + "%", centerX + radius + 20, centerY + 20);
                    }
                    
                } catch (Exception e) {
                    g2d.setColor(Color.RED);
                    g2d.drawString("解析报告内容失败", 50, 250);
                }
            }
            
            g2d.dispose();
            
            // 将图片转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format.equals("image") ? "png" : format, baos);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("生成报告预览图片失败", e);
            return null;
        }
    }
    
    @Override
    public Object previewReportJson(Long id, String pageRange, String dataSource) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        // 构建JSON格式预览内容，与导出接口内容一致
        Map<String, Object> previewData = new HashMap<>();
        previewData.put("reportId", report.getId());
        previewData.put("name", report.getName());
        previewData.put("description", report.getDescription());
        previewData.put("reportType", report.getReportType());
        previewData.put("status", report.getStatus());
        previewData.put("dataSource", dataSource);
        previewData.put("pageRange", pageRange);
        previewData.put("previewType", "json");
        
        // 解析报告内容
        if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
            try {
                JSONObject content = JSON.parseObject(report.getContent());
                previewData.put("content", content);
                
                // 添加统计摘要
                if (content.containsKey("summary")) {
                    previewData.put("summary", content.getJSONObject("summary"));
                }
                
                // 添加时间范围
                if (content.containsKey("startDate") && content.containsKey("endDate")) {
                    previewData.put("timeRange", Map.of(
                        "startDate", content.getString("startDate"),
                        "endDate", content.getString("endDate")
                    ));
                }
                
            } catch (Exception e) {
                previewData.put("contentError", "解析报告内容失败: " + e.getMessage());
            }
        }
        
        return previewData;
    }

    @Override
    public String exportReport(Long id, String format, String pageRange) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        // 更新导出统计
        report.setExportCount(report.getExportCount() + 1);
        report.setLastExportTime(LocalDateTime.now());
        reportMapper.updateById(report);
        
        // 构建导出文件路径
        return "/exports/reports/" + id + "." + format.toLowerCase();
    }
    
    @Override
    public File exportReportFile(Long id, String format, String pageRange) {
        Report report = reportMapper.selectById(id);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }
        
        try {
            // 创建导出目录
            File exportDir = new File("exports/reports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            // 生成文件名
            String filename = "report_" + id + "_" + System.currentTimeMillis() + "." + format.toLowerCase();
            File exportFile = new File(exportDir, filename);

            try {
            // 根据格式生成文件
            switch (format.toLowerCase()) {
                case "pdf":
                    generatePdfReport(report, exportFile, pageRange);
                    break;
                case "docx":
                    generateWordReport(report, exportFile, pageRange);
                    break;
                case "xlsx":
                    generateExcelReport(report, exportFile, pageRange);
                    break;
                case "html":
                    generateHtmlReport(report, exportFile, pageRange);
                    break;
                case "json":
                    generateJsonReport(report, exportFile, pageRange);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的导出格式：" + format);
            }
            }catch (Exception e) {
                throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
            }

            // 更新导出统计
            report.setExportCount(report.getExportCount() + 1);
            report.setLastExportTime(LocalDateTime.now());
            reportMapper.updateById(report);
            
            return exportFile;
            
        } catch (Exception e) {
            throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 生成PDF报告
     */

    /**
     * 生成 PDF 报告（支持中文，不乱码）
     */
    public void generatePdfReport(Report report, File exportFile, String pageRange) {
        try (PDDocument document = new PDDocument()) {
            PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/simhei.ttf"));
            
            // ========= 第一页：报告封面和基本信息（紧凑布局）=========
            PDPage coverPage = new PDPage();
            document.addPage(coverPage);
            
            try (PDPageContentStream coverStream = new PDPageContentStream(document, coverPage)) {
                // 报告标题
                coverStream.beginText();
                coverStream.setFont(font, 18);
                coverStream.newLineAtOffset(80, 750);
                coverStream.showText(report.getName() != null ? report.getName() : "测试报告");
                coverStream.endText();
                
                // 副标题信息（紧凑排列）
                coverStream.beginText();
                coverStream.setFont(font, 11);
                coverStream.newLineAtOffset(80, 720);
                
                // 第一行
                coverStream.showText("报告类型：" + (report.getReportType() != null ? getReadableReportType(report.getReportType()) : "未知类型"));
                coverStream.newLineAtOffset(200, 0);
                coverStream.showText("生成时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                coverStream.newLineAtOffset(-200, -18);
                
                // 第二行
                coverStream.showText("数据范围：" + pageRange);
                coverStream.newLineAtOffset(200, 0);
                coverStream.showText("报告ID：" + report.getId());
                coverStream.endText();
                
                // 分隔线
                coverStream.moveTo(80, 685);
                coverStream.lineTo(532, 685);
                coverStream.stroke();
                
                // 报告描述（如果有）
                if (report.getDescription() != null && !report.getDescription().trim().isEmpty()) {
                    coverStream.beginText();
                    coverStream.setFont(font, 11);
                    coverStream.newLineAtOffset(80, 670);
                    coverStream.showText("报告描述：" + report.getDescription());
                    coverStream.endText();
                }
                
                // 获取模板的章节结构配置
                String chapterStructure = null;
                if (report.getTemplateId() != null) {
                    ReportTemplate template = reportTemplateService.getById(report.getTemplateId());
                    if (template != null && template.getChapterStructure() != null && !template.getChapterStructure().trim().isEmpty()) {
                        chapterStructure = template.getChapterStructure();
                    }
                }
                
                // 根据章节结构配置生成报告内容
                if (chapterStructure != null && !chapterStructure.trim().isEmpty()) {
                    generateReportByChapterStructure(document, font, chapterStructure, report.getContent(), coverStream);
                } else if ("RESPONSE_TIME".equalsIgnoreCase(report.getReportType()) && report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    // 处理 RESPONSE_TIME 类型报告
                    generateResponseTimeReportContent(document, font, report.getContent(), coverStream);
                } else if ("SUCCESS_RATE".equalsIgnoreCase(report.getReportType()) && report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    // 处理 SUCCESS_RATE 类型报告
                    generateSuccessRateReportContent(document, font, report.getContent(), coverStream);
                } else if ("FAILURE_REASONS".equalsIgnoreCase(report.getReportType()) && report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    // 处理 FAILURE_REASONS 类型报告
                    generateFailureReasonsReportContent(document, font, report.getContent(), coverStream);
                } else if ("PROTOCOL_DISTRIBUTION".equalsIgnoreCase(report.getReportType()) && report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    // 处理 PROTOCOL_DISTRIBUTION 类型报告
                    generateProtocolDistributionReportContent(document, font, report.getContent(), coverStream);
                } else if ("WEEKLY_EXECUTION".equalsIgnoreCase(report.getReportType()) && report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    // 处理 WEEKLY_EXECUTION 类型报告
                    generateWeeklyExecutionReportContent(document, font, report.getContent(), coverStream);
                } else {
                    // 其他类型：继续在第一页输出部分内容
                    String reportContent = buildReportContent(report, pageRange);
                    if (reportContent != null && !reportContent.trim().isEmpty()) {
                        String cleanContent = cleanAndTranslateContent(reportContent);
                        List<String> chapters = parseReportChapters(cleanContent);
                        
                        int y = report.getDescription() != null && !report.getDescription().trim().isEmpty() ? 640 : 670;
                        PDPage currentPage = coverPage;
                        PDPageContentStream contentStream = coverStream;
                        
                        for (String chapter : chapters) {
                            if (y < 40) {
                                if (contentStream != coverStream) {
                                    contentStream.endText();
                                }
                                contentStream.close();
                                currentPage = new PDPage();
                                document.addPage(currentPage);
                                contentStream = new PDPageContentStream(document, currentPage);
                                contentStream.setFont(font, 11);
                                y = 770;
                            }
                            
                            if (chapter.startsWith("=== ")) {
                                contentStream.beginText();
                                contentStream.setFont(font, 13);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText(chapter);
                                contentStream.endText();
                                y -= 20;
                                contentStream.moveTo(50, y);
                                contentStream.lineTo(562, y);
                                contentStream.stroke();
                                y -= 12;
                            } else {
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText(chapter);
                                contentStream.endText();
                                y -= 12;
                            }
                        }
                        if (contentStream != coverStream) {
                            contentStream.close();
                        }
                    }
                }
            }
            
            // 输出文件
            document.save(new FileOutputStream(exportFile));
        } catch (Exception e) {
            throw new RuntimeException("导出报告文件失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 根据模板的 chapter_structure 配置生成报告内容
     */
    private void generateReportByChapterStructure(PDDocument document, PDType0Font font, String chapterStructure, 
                                                   String reportContent, PDPageContentStream coverStream) throws Exception {
        JSONArray chapters = JSON.parseArray(chapterStructure);
        
        PDPage currentPage = null;
        PDPageContentStream contentStream = null;
        int y = 640; // 从封面页继续，紧凑布局
        
        // 判断是否使用封面流继续输出
        if (coverStream != null) {
            contentStream = coverStream;
        } else {
            currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            y = 770;
        }
        
        contentStream.setFont(font, 11);
        
        // 解析报告内容（如果有）
        Map<String, Object> reportDataMap = new HashMap<>();
        if (reportContent != null && !reportContent.trim().isEmpty()) {
            try {
                JSONArray contentArray = JSON.parseArray(reportContent);
                for (int i = 0; i < contentArray.size(); i++) {
                    JSONObject section = contentArray.getJSONObject(i);
                    String dataSource = section.getString("dataSource");
                    Object data = section.get("data");
                    reportDataMap.put(dataSource, data);
                }
            } catch (Exception e) {
                log.warn("解析报告内容失败: {}", e.getMessage());
            }
        }
        
        for (int i = 0; i < chapters.size(); i++) {
            JSONObject chapter = chapters.getJSONObject(i);
            String type = chapter.getString("type");
            String title = chapter.getString("title");
            String dataSource = chapter.getString("dataSource");
            String chartType = chapter.getString("chartType");
            
            // 检查是否需要新页面
            if (y < 60) {
                if (contentStream != coverStream) {
                    contentStream.close();
                }
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 11);
                y = 770;
            }
            
            // 章节标题
            contentStream.beginText();
            contentStream.setFont(font, 13);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("=== " + title + " ===");
            contentStream.endText();
            y -= 18;
            
            // 分隔线
            contentStream.moveTo(50, y);
            contentStream.lineTo(562, y);
            contentStream.stroke();
            y -= 12;
            
            // 获取对应数据源的数据
            Object data = reportDataMap.get(dataSource);
            
            if ("text".equals(type)) {
                // 文本类型
                if (data instanceof JSONObject) {
                    JSONObject textData = (JSONObject) data;
                    for (String key : textData.keySet()) {
                        if (y < 40) {
                            if (contentStream != coverStream) {
                                contentStream.close();
                            }
                            currentPage = new PDPage();
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(font, 11);
                            y = 770;
                        }
                        
                        Object value = textData.get(key);
                        String displayKey = convertToChineseKey(key);
                        
                        if (value instanceof JSONArray) {
                            // 数组类型（如 suggestions）
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：");
                            contentStream.endText();
                            y -= 14;
                            
                            JSONArray arrayValue = (JSONArray) value;
                            int idx = 1;
                            for (Object item : arrayValue) {
                                if (y < 40) {
                                    if (contentStream != coverStream) {
                                        contentStream.close();
                                    }
                                    currentPage = new PDPage();
                                    document.addPage(currentPage);
                                    contentStream = new PDPageContentStream(document, currentPage);
                                    contentStream.setFont(font, 11);
                                    y = 770;
                                }
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText("  " + idx + ". " + item);
                                contentStream.endText();
                                y -= 14;
                                idx++;
                            }
                        } else {
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：" + value);
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                } else if (data != null) {
                    // 简单文本内容
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(data.toString());
                    contentStream.endText();
                    y -= 14;
                }
            } else if ("chart".equals(type)) {
                // 图表类型
                JSONArray chartData = null;
                if (data instanceof JSONArray) {
                    chartData = (JSONArray) data;
                } else if (data instanceof JSONObject && ((JSONObject) data).containsKey("data")) {
                    chartData = ((JSONObject) data).getJSONArray("data");
                }
                
                // 如果没有数据，生成空图表占位
                if (chartData == null || chartData.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("暂无数据");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 生成图表图片
                byte[] chartImage = generateChartImage(chartData, chartType != null ? chartType : "BAR", title);
                
                float imageWidth = 500;
                float imageHeight = 180;
                
                if (y - imageHeight < 40) {
                    if (contentStream != coverStream) {
                        contentStream.close();
                    }
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    contentStream.setFont(font, 11);
                    y = 770;
                }
                
                // 插入图表图片
                PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "chart");
                contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
                y -= (imageHeight + 15);
            }
        }
        
        // 如果不是封面流，需要关闭
        if (contentStream != coverStream && contentStream != null) {
            contentStream.close();
        }
    }
    
    /**
     * 专门处理 RESPONSE_TIME 类型报告内容（支持从封面页继续输出）
     */
    private void generateResponseTimeReportContent(PDDocument document, PDType0Font font, String content, PDPageContentStream coverStream) throws Exception {
        JSONArray sections = JSON.parseArray(content);
        
        PDPage currentPage = null;
        PDPageContentStream contentStream = null;
        int y = 640; // 从封面页继续，紧凑布局
        
        // 判断是否使用封面流继续输出
        if (coverStream != null) {
            contentStream = coverStream;
        } else {
            currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            y = 770; // 新页面从顶部开始
        }
        
        contentStream.setFont(font, 11);
        
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String type = section.getString("type");
            String title = section.getString("title");
            
            // 检查是否需要新页面
            if (y < 60) {
                if (contentStream != coverStream) {
                    contentStream.close();
                }
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 11);
                y = 770;
            }
            
            // 章节标题
            contentStream.beginText();
            contentStream.setFont(font, 13);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("=== " + title + " ===");
            contentStream.endText();
            y -= 18;
            
            // 分隔线
            contentStream.moveTo(50, y);
            contentStream.lineTo(562, y);
            contentStream.stroke();
            y -= 12;
            
            if ("text".equals(type)) {
                // 文本类型：直接输出内容
                JSONObject data = section.getJSONObject("data");
                if (data != null) {
                    for (String key : data.keySet()) {
                        if (y < 40) {
                            if (contentStream != coverStream) {
                                contentStream.close();
                            }
                            currentPage = new PDPage();
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(font, 11);
                            y = 770;
                        }
                        
                        Object value = data.get(key);
                        String displayKey = convertToChineseKey(key);
                        
                        if (value instanceof JSONArray) {
                            // suggestions 数组：每行单独显示
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：");
                            contentStream.endText();
                            y -= 14;
                            
                            JSONArray suggestions = (JSONArray) value;
                            int idx = 1;
                            for (Object suggestion : suggestions) {
                                if (y < 40) {
                                    if (contentStream != coverStream) {
                                        contentStream.close();
                                    }
                                    currentPage = new PDPage();
                                    document.addPage(currentPage);
                                    contentStream = new PDPageContentStream(document, currentPage);
                                    contentStream.setFont(font, 11);
                                    y = 770;
                                }
                                // 每行单独创建文本块，确保不重叠
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText("  " + idx + ". " + suggestion);
                                contentStream.endText();
                                y -= 14;
                                idx++;
                            }
                        } else {
                            // 普通文本：每行单独创建文本块
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：" + value);
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                }
            } else if ("chart".equals(type)) {
                // 图表类型：生成图表并插入
                String chartType = section.getString("chartType");
                JSONArray chartData = section.getJSONArray("data");
                
                // 如果没有数据，生成空图表占位
                if (chartData == null || chartData.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("暂无数据");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 生成图表图片
                byte[] chartImage = generateChartImage(chartData, chartType, title);
                
                // 检查图片数据是否有效
                if (chartImage == null || chartImage.length == 0) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表生成失败");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 计算图片位置和大小（紧凑布局）
                float imageWidth = 500;
                float imageHeight = 180;
                
                if (y - imageHeight < 40) {
                    if (contentStream != coverStream) {
                        contentStream.close();
                    }
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    contentStream.setFont(font, 11);
                    y = 770;
                }
                
                // 尝试插入图表图片，优雅处理异常
                try {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "chart");
                    contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
                    y -= (imageHeight + 15);
                } catch (Exception e) {
                    log.warn("插入图表图片失败: {}", e.getMessage());
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表显示异常");
                    contentStream.endText();
                    y -= 14;
                }
            }
        }
        
        // 如果不是封面流，需要关闭
        if (contentStream != coverStream && contentStream != null) {
            contentStream.close();
        }
    }
    
    /**
     * 专门处理 SUCCESS_RATE 类型报告内容（支持从封面页继续输出）
     */
    private void generateSuccessRateReportContent(PDDocument document, PDType0Font font, String content, PDPageContentStream coverStream) throws Exception {
        JSONArray sections = JSON.parseArray(content);
        
        PDPage currentPage = null;
        PDPageContentStream contentStream = null;
        int y = 640; // 从封面页继续，紧凑布局
        
        // 判断是否使用封面流继续输出
        if (coverStream != null) {
            contentStream = coverStream;
        } else {
            currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            y = 770; // 新页面从顶部开始
        }
        
        contentStream.setFont(font, 11);
        
        // 提取作业日志数据
        String dataSourceName = null;
        JSONArray jobLogChartData = null;
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            if ("chart".equals(section.getString("type"))) {
                JSONObject data = section.getJSONObject("data");
                if (data.containsKey("dataSourceName")) {
                    dataSourceName = data.getString("dataSourceName");
                    if (data.containsKey("rateData")) {
                        jobLogChartData = data.getJSONArray("rateData");
                    }
                    break;
                }
            }
        }
        
        boolean hasAddedJobLogChart = false;
        
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String type = section.getString("type");
            String title = section.getString("title");
            
            // 在"成功率分析"之后添加作业日志图表
            if ("成功率分析".equals(title) && !hasAddedJobLogChart && dataSourceName != null) {
                hasAddedJobLogChart = addJobLogChartSection(document, font, contentStream, coverStream, dataSourceName, jobLogChartData, y);
                // 更新 y 坐标
                y = hasAddedJobLogChart ? y - 200 : y;
            }
            
            // 检查是否需要新页面
            if (y < 60) {
                if (contentStream != coverStream) {
                    contentStream.close();
                }
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 11);
                y = 770;
            }
            
            // 章节标题
            contentStream.beginText();
            contentStream.setFont(font, 13);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("=== " + title + " ===");
            contentStream.endText();
            y -= 18;
            
            // 分隔线
            contentStream.moveTo(50, y);
            contentStream.lineTo(562, y);
            contentStream.stroke();
            y -= 12;
            
            if ("text".equals(type)) {
                // 文本类型：直接输出内容
                JSONObject data = section.getJSONObject("data");
                if (data != null) {
                    for (String key : data.keySet()) {
                        if (y < 40) {
                            if (contentStream != coverStream) {
                                contentStream.close();
                            }
                            currentPage = new PDPage();
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(font, 11);
                            y = 770;
                        }
                        
                        Object value = data.get(key);
                        String displayKey = convertToChineseKey(key);
                        
                        if (value instanceof JSONArray) {
                            // suggestions 数组：每行单独显示
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：");
                            contentStream.endText();
                            y -= 14;
                            
                            JSONArray suggestions = (JSONArray) value;
                            int idx = 1;
                            for (Object suggestion : suggestions) {
                                if (y < 40) {
                                    if (contentStream != coverStream) {
                                        contentStream.close();
                                    }
                                    currentPage = new PDPage();
                                    document.addPage(currentPage);
                                    contentStream = new PDPageContentStream(document, currentPage);
                                    contentStream.setFont(font, 11);
                                    y = 770;
                                }
                                // 每行单独创建文本块，确保不重叠
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText("  " + idx + ". " + suggestion);
                                contentStream.endText();
                                y -= 14;
                                idx++;
                            }
                        } else {
                            // 普通文本：每行单独创建文本块
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：" + value);
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                }
            } else if ("chart".equals(type)) {
                // 图表类型：生成图表并插入
                String chartType = section.getString("chartType");
                JSONArray chartData = section.getJSONArray("data");
                
                // 如果没有数据，生成空图表占位
                if (chartData == null || chartData.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("暂无数据");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 生成图表图片
                byte[] chartImage = generateChartImage(chartData, chartType, title);
                
                // 检查图片数据是否有效
                if (chartImage == null || chartImage.length == 0) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表生成失败");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 计算图片位置和大小（紧凑布局）
                float imageWidth = 500;
                float imageHeight = 180;
                
                if (y - imageHeight < 40) {
                    if (contentStream != coverStream) {
                        contentStream.close();
                    }
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    contentStream.setFont(font, 11);
                    y = 770;
                }
                
                // 尝试插入图表图片，优雅处理异常
                try {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "chart");
                    contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
                    y -= (imageHeight + 15);
                } catch (Exception e) {
                    log.warn("插入图表图片失败: {}", e.getMessage());
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表显示异常");
                    contentStream.endText();
                    y -= 14;
                }
            }
        }
        
        // 如果不是封面流，需要关闭
        if (contentStream != coverStream && contentStream != null) {
            contentStream.close();
        }
    }
    
    /**
     * 生成 FAILURE_REASONS 类型报告内容（根据模板结构动态生成）
     */
    private void generateFailureReasonsReportContent(PDDocument document, PDType0Font font, String content, PDPageContentStream coverStream) throws Exception {
        JSONArray sections = JSON.parseArray(content);
        
        PDPage currentPage = null;
        PDPageContentStream contentStream = null;
        int y = 640; // 从封面页继续，紧凑布局
        
        // 判断是否使用封面流继续输出
        if (coverStream != null) {
            contentStream = coverStream;
        } else {
            currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            y = 770; // 新页面从顶部开始
        }
        
        contentStream.setFont(font, 11);
        
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String type = section.getString("type");
            String title = section.getString("title");
            
            // 检查是否需要新页面
            if (y < 60) {
                if (contentStream != coverStream) {
                    contentStream.close();
                }
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 11);
                y = 770;
            }
            
            // 章节标题
            contentStream.beginText();
            contentStream.setFont(font, 13);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("=== " + title + " ===");
            contentStream.endText();
            y -= 18;
            
            // 分隔线
            contentStream.moveTo(50, y);
            contentStream.lineTo(562, y);
            contentStream.stroke();
            y -= 12;
            
            if ("text".equals(type)) {
                // 文本类型：直接输出内容
                JSONObject data = section.getJSONObject("data");
                if (data != null) {
                    for (String key : data.keySet()) {
                        if (y < 40) {
                            if (contentStream != coverStream) {
                                contentStream.close();
                            }
                            currentPage = new PDPage();
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(font, 11);
                            y = 770;
                        }
                        
                        Object value = data.get(key);
                        String displayKey = convertToChineseKey(key);
                        
                        if (value instanceof JSONArray) {
                            // suggestions 数组：每行单独显示
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：");
                            contentStream.endText();
                            y -= 14;
                            
                            JSONArray suggestions = (JSONArray) value;
                            int idx = 1;
                            for (Object suggestion : suggestions) {
                                if (y < 40) {
                                    if (contentStream != coverStream) {
                                        contentStream.close();
                                    }
                                    currentPage = new PDPage();
                                    document.addPage(currentPage);
                                    contentStream = new PDPageContentStream(document, currentPage);
                                    contentStream.setFont(font, 11);
                                    y = 770;
                                }
                                // 每行单独创建文本块，确保不重叠
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText("  " + idx + ". " + suggestion);
                                contentStream.endText();
                                y -= 14;
                                idx++;
                            }
                        } else {
                            // 普通文本：每行单独创建文本块
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：" + value);
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                }
            } else if ("chart".equals(type)) {
                // 图表类型：生成图表并插入
                String chartType = section.getString("chartType");
                JSONArray chartData = null;
                
                // 尝试从 data 字段获取图表数据
                if (section.containsKey("data")) {
                    Object dataObj = section.get("data");
                    if (dataObj instanceof JSONArray) {
                        chartData = (JSONArray) dataObj;
                    } else if (dataObj instanceof JSONObject) {
                        JSONObject data = (JSONObject) dataObj;
                        // 尝试从 rateData 字段获取数据（嵌套格式）
                        if (data.containsKey("rateData")) {
                            chartData = data.getJSONArray("rateData");
                        } else if (data.containsKey("summary")) {
                            // 如果只有 summary，尝试从其他地方获取数据
                            chartData = new JSONArray();
                        }
                    }
                }
                
                // 如果没有数据，生成空图表占位
                if (chartData == null || chartData.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("暂无数据");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 生成图表图片
                byte[] chartImage = generateChartImage(chartData, chartType, title);
                
                // 检查图片数据是否有效
                if (chartImage == null || chartImage.length == 0) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表生成失败");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 计算图片位置和大小（紧凑布局）
                float imageWidth = 500;
                float imageHeight = 180;
                
                if (y - imageHeight < 40) {
                    if (contentStream != coverStream) {
                        contentStream.close();
                    }
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    contentStream.setFont(font, 11);
                    y = 770;
                }
                
                // 尝试插入图表图片，优雅处理异常
                try {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "chart");
                    contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
                    y -= (imageHeight + 15);
                } catch (Exception e) {
                    log.warn("插入图表图片失败: {}", e.getMessage());
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表显示异常");
                    contentStream.endText();
                    y -= 14;
                }
            }
        }
        
        // 如果不是封面流，需要关闭
        if (contentStream != coverStream && contentStream != null) {
            contentStream.close();
        }
    }
    
    /**
     * 生成 PROTOCOL_DISTRIBUTION 类型报告内容（根据模板结构动态生成）
     */
    private void generateProtocolDistributionReportContent(PDDocument document, PDType0Font font, String content, PDPageContentStream coverStream) throws Exception {
        JSONArray sections = JSON.parseArray(content);
        
        PDPage currentPage = null;
        PDPageContentStream contentStream = null;
        int y = 640; // 从封面页继续，紧凑布局
        
        // 判断是否使用封面流继续输出
        if (coverStream != null) {
            contentStream = coverStream;
        } else {
            currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            y = 770; // 新页面从顶部开始
        }
        
        contentStream.setFont(font, 11);
        
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String type = section.getString("type");
            String title = section.getString("title");
            
            // 检查是否需要新页面
            if (y < 60) {
                if (contentStream != coverStream) {
                    contentStream.close();
                }
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 11);
                y = 770;
            }
            
            // 章节标题
            contentStream.beginText();
            contentStream.setFont(font, 13);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("=== " + title + " ===");
            contentStream.endText();
            y -= 18;
            
            // 分隔线
            contentStream.moveTo(50, y);
            contentStream.lineTo(562, y);
            contentStream.stroke();
            y -= 12;
            
            if ("text".equals(type)) {
                // 文本类型：直接输出内容
                JSONObject data = section.getJSONObject("data");
                if (data != null) {
                    for (String key : data.keySet()) {
                        if (y < 40) {
                            if (contentStream != coverStream) {
                                contentStream.close();
                            }
                            currentPage = new PDPage();
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(font, 11);
                            y = 770;
                        }
                        
                        Object value = data.get(key);
                        String displayKey = convertToChineseKey(key);
                        
                        if (value instanceof JSONArray) {
                            // suggestions 数组：每行单独显示
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：");
                            contentStream.endText();
                            y -= 14;
                            
                            JSONArray suggestions = (JSONArray) value;
                            int idx = 1;
                            for (Object suggestion : suggestions) {
                                if (y < 40) {
                                    if (contentStream != coverStream) {
                                        contentStream.close();
                                    }
                                    currentPage = new PDPage();
                                    document.addPage(currentPage);
                                    contentStream = new PDPageContentStream(document, currentPage);
                                    contentStream.setFont(font, 11);
                                    y = 770;
                                }
                                // 每行单独创建文本块，确保不重叠
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText("  " + idx + ". " + suggestion);
                                contentStream.endText();
                                y -= 14;
                                idx++;
                            }
                        } else {
                            // 普通文本：每行单独创建文本块
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：" + value);
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                }
            } else if ("chart".equals(type)) {
                // 图表类型：生成图表并插入
                String chartType = section.getString("chartType");
                JSONArray chartData = null;
                
                // 尝试从 data 字段获取图表数据
                if (section.containsKey("data")) {
                    Object dataObj = section.get("data");
                    if (dataObj instanceof JSONArray) {
                        chartData = (JSONArray) dataObj;
                    } else if (dataObj instanceof JSONObject) {
                        JSONObject data = (JSONObject) dataObj;
                        // 尝试从 categoryData 字段获取数据（协议分布数据）
                        if (data.containsKey("categoryData")) {
                            chartData = data.getJSONArray("categoryData");
                        } 
                        // 尝试从 weekData 字段获取数据（周数据）
                        else if (data.containsKey("weekData")) {
                            chartData = data.getJSONArray("weekData");
                        }
                        // 尝试从 rateData 字段获取数据（嵌套格式）
                        else if (data.containsKey("rateData")) {
                            chartData = data.getJSONArray("rateData");
                        }
                    }
                }
                
                // 如果没有数据，生成空图表占位
                if (chartData == null || chartData.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("暂无数据");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 生成图表图片
                byte[] chartImage = generateChartImage(chartData, chartType, title);
                
                // 检查图片数据是否有效
                if (chartImage == null || chartImage.length == 0) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表生成失败");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 计算图片位置和大小（紧凑布局）
                float imageWidth = 500;
                float imageHeight = 180;
                
                if (y - imageHeight < 40) {
                    if (contentStream != coverStream) {
                        contentStream.close();
                    }
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    contentStream.setFont(font, 11);
                    y = 770;
                }
                
                // 尝试插入图表图片，优雅处理异常
                try {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "chart");
                    contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
                    y -= (imageHeight + 15);
                } catch (Exception e) {
                    log.warn("插入图表图片失败: {}", e.getMessage());
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表显示异常");
                    contentStream.endText();
                    y -= 14;
                }
            }
        }
        
        // 如果不是封面流，需要关闭
        if (contentStream != coverStream && contentStream != null) {
            contentStream.close();
        }
    }
    
    /**
     * 生成 WEEKLY_EXECUTION 类型报告内容（根据模板结构动态生成）
     */
    private void generateWeeklyExecutionReportContent(PDDocument document, PDType0Font font, String content, PDPageContentStream coverStream) throws Exception {
        JSONArray sections = JSON.parseArray(content);
        
        PDPage currentPage = null;
        PDPageContentStream contentStream = null;
        int y = 640; // 从封面页继续，紧凑布局
        
        // 判断是否使用封面流继续输出
        if (coverStream != null) {
            contentStream = coverStream;
        } else {
            currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            y = 770; // 新页面从顶部开始
        }
        
        contentStream.setFont(font, 11);
        
        for (int i = 0; i < sections.size(); i++) {
            JSONObject section = sections.getJSONObject(i);
            String type = section.getString("type");
            String title = section.getString("title");
            
            // 检查是否需要新页面
            if (y < 60) {
                if (contentStream != coverStream) {
                    contentStream.close();
                }
                currentPage = new PDPage();
                document.addPage(currentPage);
                contentStream = new PDPageContentStream(document, currentPage);
                contentStream.setFont(font, 11);
                y = 770;
            }
            
            // 章节标题
            contentStream.beginText();
            contentStream.setFont(font, 13);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("=== " + title + " ===");
            contentStream.endText();
            y -= 18;
            
            // 分隔线
            contentStream.moveTo(50, y);
            contentStream.lineTo(562, y);
            contentStream.stroke();
            y -= 12;
            
            if ("text".equals(type)) {
                // 文本类型：直接输出内容
                JSONObject data = section.getJSONObject("data");
                if (data != null) {
                    for (String key : data.keySet()) {
                        if (y < 40) {
                            if (contentStream != coverStream) {
                                contentStream.close();
                            }
                            currentPage = new PDPage();
                            document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage);
                            contentStream.setFont(font, 11);
                            y = 770;
                        }
                        
                        Object value = data.get(key);
                        String displayKey = convertToChineseKey(key);
                        
                        if (value instanceof JSONArray) {
                            // suggestions 数组：每行单独显示
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：");
                            contentStream.endText();
                            y -= 14;
                            
                            JSONArray suggestions = (JSONArray) value;
                            int idx = 1;
                            for (Object suggestion : suggestions) {
                                if (y < 40) {
                                    if (contentStream != coverStream) {
                                        contentStream.close();
                                    }
                                    currentPage = new PDPage();
                                    document.addPage(currentPage);
                                    contentStream = new PDPageContentStream(document, currentPage);
                                    contentStream.setFont(font, 11);
                                    y = 770;
                                }
                                // 每行单独创建文本块，确保不重叠
                                contentStream.beginText();
                                contentStream.setFont(font, 11);
                                contentStream.newLineAtOffset(50, y);
                                contentStream.showText("  " + idx + ". " + suggestion);
                                contentStream.endText();
                                y -= 14;
                                idx++;
                            }
                        } else {
                            // 普通文本：每行单独创建文本块
                            contentStream.beginText();
                            contentStream.setFont(font, 11);
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(displayKey + "：" + value);
                            contentStream.endText();
                            y -= 14;
                        }
                    }
                }
            } else if ("chart".equals(type)) {
                // 图表类型：生成图表并插入
                String chartType = section.getString("chartType");
                JSONArray chartData = null;
                
                // 尝试从 data 字段获取图表数据
                if (section.containsKey("data")) {
                    Object dataObj = section.get("data");
                    if (dataObj instanceof JSONArray) {
                        chartData = (JSONArray) dataObj;
                    } else if (dataObj instanceof JSONObject) {
                        JSONObject data = (JSONObject) dataObj;
                        // 尝试从 weekData 字段获取数据（周执行数据）
                        if (data.containsKey("weekData")) {
                            chartData = data.getJSONArray("weekData");
                        }
                        // 尝试从 rateData 字段获取数据（成功率数据）
                        else if (data.containsKey("rateData")) {
                            chartData = data.getJSONArray("rateData");
                        }
                        // 尝试从 categoryData 字段获取数据（分类数据）
                        else if (data.containsKey("categoryData")) {
                            chartData = data.getJSONArray("categoryData");
                        }
                    }
                }
                
                // 如果没有数据，生成空图表占位
                if (chartData == null || chartData.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("暂无数据");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 生成图表图片
                byte[] chartImage = generateChartImage(chartData, chartType, title);
                
                // 检查图片数据是否有效
                if (chartImage == null || chartImage.length == 0) {
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表生成失败");
                    contentStream.endText();
                    y -= 14;
                    continue;
                }
                
                // 计算图片位置和大小（紧凑布局）
                float imageWidth = 500;
                float imageHeight = 180;
                
                if (y - imageHeight < 40) {
                    if (contentStream != coverStream) {
                        contentStream.close();
                    }
                    currentPage = new PDPage();
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    contentStream.setFont(font, 11);
                    y = 770;
                }
                
                // 尝试插入图表图片，优雅处理异常
                try {
                    PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "chart");
                    contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
                    y -= (imageHeight + 15);
                } catch (Exception e) {
                    log.warn("插入图表图片失败: {}", e.getMessage());
                    contentStream.beginText();
                    contentStream.setFont(font, 11);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText("图表显示异常");
                    contentStream.endText();
                    y -= 14;
                }
            }
        }
        
        // 如果不是封面流，需要关闭
        if (contentStream != coverStream && contentStream != null) {
            contentStream.close();
        }
    }
    
    /**
     * 添加作业日志图表章节
     */
    private boolean addJobLogChartSection(PDDocument document, PDType0Font font, PDPageContentStream contentStream, 
                                          PDPageContentStream coverStream, String dataSourceName, 
                                          JSONArray chartData, int y) throws Exception {
        if (chartData == null || chartData.isEmpty()) {
            return false;
        }
        
        // 章节标题
        contentStream.beginText();
        contentStream.setFont(font, 13);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("=== " + dataSourceName + " ===");
        contentStream.endText();
        y -= 18;
        
        // 分隔线
        contentStream.moveTo(50, y);
        contentStream.lineTo(562, y);
        contentStream.stroke();
        y -= 12;
        
        // 生成图表图片
        byte[] chartImage = generateChartImage(chartData, "BAR", dataSourceName);
        
        if (chartImage == null || chartImage.length == 0) {
            contentStream.beginText();
            contentStream.setFont(font, 11);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("图表生成失败");
            contentStream.endText();
            return false;
        }
        
        float imageWidth = 500;
        float imageHeight = 180;
        
        // 检查是否需要新页面
        if (y - imageHeight < 40) {
            if (contentStream != coverStream) {
                contentStream.close();
            }
            PDPage currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            contentStream.setFont(font, 11);
            y = 770;
        }
        
        // 插入图表图片
        try {
            PDImageXObject image = PDImageXObject.createFromByteArray(document, chartImage, "job_log_chart");
            contentStream.drawImage(image, 36, y - imageHeight, imageWidth, imageHeight);
            return true;
        } catch (Exception e) {
            log.warn("插入作业日志图表失败: {}", e.getMessage());
            contentStream.beginText();
            contentStream.setFont(font, 11);
            contentStream.newLineAtOffset(50, y);
            contentStream.showText("图表显示异常");
            contentStream.endText();
            return false;
        }
    }
    
    /**
     * 使用 ECharts 生成图表图片
     */
    private byte[] generateChartImage(JSONArray data, String chartType, String title) {
        try {
            // 准备图表数据
            List<String> names = new ArrayList<>();
            List<Double> values = new ArrayList<>();
            
            // 检查数据格式：支持多种嵌套格式
            JSONArray chartData = data;
            
            if (data != null && data.size() > 0) {
                // 检查是否是嵌套格式（第一个元素包含 rateData/weekData/categoryData）
                JSONObject firstItem = data.getJSONObject(0);
                
                // 支持 rateData 格式（成功率数据）
                if (firstItem.containsKey("rateData")) {
                    chartData = firstItem.getJSONArray("rateData");
                }
                // 支持 weekData 格式（周执行数据）
                else if (firstItem.containsKey("weekData")) {
                    chartData = firstItem.getJSONArray("weekData");
                }
                // 支持 categoryData 格式（分类数据）
                else if (firstItem.containsKey("categoryData")) {
                    chartData = firstItem.getJSONArray("categoryData");
                }
                // 否则直接使用原数据数组（已经提取的数据）
            }
            
            for (int i = 0; i < chartData.size(); i++) {
                JSONObject item = chartData.getJSONObject(i);
                // 支持多种字段名：name/dayNameCn, value/executionCount
                String name = item.containsKey("name") ? item.getString("name") : 
                              (item.containsKey("dayNameCn") ? item.getString("dayNameCn") : "");
                double value = item.containsKey("value") ? item.getDouble("value") : 
                              (item.containsKey("executionCount") ? item.getDouble("executionCount") : 0);
                names.add(name);
                values.add(value);
            }
            
            // 生成 HTML 内容
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><head>");
            htmlBuilder.append("<script src=\"https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js\"></script>");
            htmlBuilder.append("</head><body>");
            htmlBuilder.append("<div id=\"chart\" style=\"width:800px;height:400px;\"></div>");
            htmlBuilder.append("<script>");
            htmlBuilder.append("var chart = echarts.init(document.getElementById('chart'));");
            htmlBuilder.append("var option = {");
            htmlBuilder.append("    title: { text: '").append(title).append("' },");
            htmlBuilder.append("    tooltip: {},");
            htmlBuilder.append("    xAxis: { data: ").append(JSON.toJSONString(names)).append(" },");
            htmlBuilder.append("    yAxis: {},");
            htmlBuilder.append("    series: [{");
            htmlBuilder.append("        name: '数值',");
            htmlBuilder.append("        type: '").append(chartType.toLowerCase().equals("histogram") ? "bar" : chartType.toLowerCase()).append("',");
            htmlBuilder.append("        data: ").append(JSON.toJSONString(values)).append("");
            htmlBuilder.append("    }]");
            htmlBuilder.append("};");
            htmlBuilder.append("chart.setOption(option);");
            htmlBuilder.append("</script></body></html>");
            
            // 使用 headless 浏览器渲染（简化方案：返回空数组表示图表位置）
            // 实际项目中需要使用 Jsoup + 无头浏览器或专门的图表库
            
            // 由于没有实际的 headless 浏览器环境，返回一个简单的占位图片
            return generateSimpleChartImage(names, values, chartType, title);
        } catch (Exception e) {
            log.error("生成图表图片失败", e);
            return new byte[0];
        }
    }
    
    /**
     * 生成简单的图表图片（支持多种图表类型）
     */
    private byte[] generateSimpleChartImage(List<String> names, List<Double> values, String chartType, String title) throws Exception {
        // 创建简单的图表表示
        int width = 500;
        int height = 250;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 设置字体
        Font font = new Font("SimHei", Font.PLAIN, 10);
        Font titleFont = new Font("SimHei", Font.BOLD, 12);
        g2d.setFont(titleFont);
        g2d.setColor(Color.BLACK);
        
        // 绘制标题（居中）
        FontMetrics fm = g2d.getFontMetrics(titleFont);
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, width / 2 - titleWidth / 2, 25);
        
        if ("PIE".equalsIgnoreCase(chartType)) {
            // 饼图
            drawPieChart(g2d, names, values, title, width, height, font);
        } else {
            // 折线图或柱状图
            drawBarOrLineChart(g2d, names, values, chartType, width, height, font);
        }
        
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
    
    /**
     * 绘制饼图
     */
    private void drawPieChart(Graphics2D g2d, List<String> names, List<Double> values, String title, 
                              int width, int height, Font font) {
        // 计算总数值
        double total = values.stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) total = 1;
        
        // 饼图位置和大小
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 2 - 40;
        
        // 颜色数组
        Color[] colors = {new Color(82, 196, 26), new Color(255, 77, 79), 
                         new Color(79, 129, 189), new Color(255, 192, 0),
                         new Color(156, 102, 255), new Color(255, 102, 153)};
        
        // 绘制饼图
        double startAngle = 0;
        g2d.setFont(font);
        
        for (int i = 0; i < values.size(); i++) {
            double angle = (values.get(i) / total) * 360;
            g2d.setColor(colors[i % colors.length]);
            g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 
                       (int) startAngle, (int) angle);
            startAngle += angle;
        }
        
        // 绘制图例
        int legendX = centerX + radius + 10;
        int legendY = centerY - 60;
        
        for (int i = 0; i < values.size(); i++) {
            double percentage = (values.get(i) / total) * 100;
            g2d.setColor(colors[i % colors.length]);
            g2d.fillRect(legendX, legendY, 12, 12);
            g2d.setColor(Color.BLACK);
            String legendText = names.get(i) + " (" + String.format("%.1f%%", percentage) + ")";
            g2d.drawString(legendText, legendX + 18, legendY + 10);
            legendY += 20;
        }
    }
    
    /**
     * 绘制柱状图或折线图
     */
    private void drawBarOrLineChart(Graphics2D g2d, List<String> names, List<Double> values, 
                                    String chartType, int width, int height, Font font) {
        // 计算边界
        int padding = 50;
        int chartWidth = width - padding * 2;
        int chartHeight = height - padding * 2;
        
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        
        // 绘制坐标轴
        g2d.drawLine(padding, padding, padding, height - padding);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        
        // 绘制轴标签（旋转90度显示Y轴标签）
        g2d.drawString("分类", width / 2 - 15, height - 8);
        
        // 检查数据是否都是整数（过滤null值）
        boolean allIntegers = values.stream().filter(v -> v != null).allMatch(v -> v == Math.floor(v));
        
        // 计算数据范围（从0开始，过滤null值）
        double maxValue = values.stream().filter(v -> v != null).mapToDouble(Double::doubleValue).max().orElse(1);
        
        // 向上取整到合适的刻度值（如果是整数数据，使用整数刻度）
        if (allIntegers) {
            maxValue = Math.ceil(maxValue * 1.1);
        } else {
            maxValue = Math.ceil(maxValue * 1.1);
        }
        
        // 绘制 Y 轴刻度和标签（与柱子高度使用相同的计算逻辑）
        int numTicks = 5;
        for (int i = 0; i <= numTicks; i++) {
            // 使用与柱子高度相同的计算逻辑
            double tickValue = i * maxValue / numTicks;
            // 如果是整数数据，刻度也取整
            if (allIntegers) {
                tickValue = Math.round(tickValue);
            }
            int y = height - padding - (int) (tickValue / maxValue * chartHeight);
            g2d.drawLine(padding - 5, y, padding, y);
            // 右对齐显示数值（整数显示为整数，小数显示为小数）
            String valueStr = allIntegers ? String.format("%d", (int) tickValue) : String.format("%.1f", tickValue);
            int strWidth = g2d.getFontMetrics(font).stringWidth(valueStr);
            g2d.drawString(valueStr, padding - 8 - strWidth, y + 4);
        }
        
        // 绘制 Y 轴标题
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("数值", -height / 2 - 20, padding - 10);
        g2d.rotate(Math.PI / 2);
        
        // 绘制数据
        int barWidth = Math.max(30, chartWidth / values.size() - 10);
        int gap = (chartWidth - barWidth * values.size()) / (values.size() + 1);
        
        // 颜色数组
        Color[] colors = {new Color(82, 196, 26), new Color(255, 77, 79), 
                         new Color(79, 129, 189), new Color(255, 192, 0),
                         new Color(156, 102, 255), new Color(255, 102, 153)};
        
        if ("LINE".equalsIgnoreCase(chartType)) {
            // 折线图（与Y轴刻度使用相同的计算逻辑）
            int prevY = -1;
            int prevX = -1;
            
            for (int i = 0; i < values.size(); i++) {
                // 处理null值
                Double value = values.get(i);
                if (value == null) {
                    continue;
                }
                
                // 使用与Y轴刻度相同的计算逻辑
                double normalizedValue = value / maxValue;
                int pointY = height - padding - (int) (normalizedValue * chartHeight);
                int x = padding + gap + i * (barWidth + gap) + barWidth / 2;
                
                // 绘制点和线
                g2d.setColor(Color.BLUE);
                if (i > 0 && prevY >= 0) {
                    g2d.drawLine(prevX, prevY, x, pointY);
                }
                g2d.fillOval(x - 4, pointY - 4, 8, 8);
                
                // 在数据点上方显示数值标签（整数显示为整数）
                g2d.setColor(Color.BLACK);
                String valueLabel = allIntegers ? String.format("%d", (int) value.doubleValue()) : String.format("%.1f", value);
                FontMetrics fm = g2d.getFontMetrics(font);
                int labelWidth = fm.stringWidth(valueLabel);
                g2d.drawString(valueLabel, x - labelWidth / 2, pointY - 8);
                
                prevX = x;
                prevY = pointY;
                
                // 绘制 X 轴标签
                String label = names.get(i);
                if (label != null && label.length() > 8) {
                    label = label.substring(0, 8) + "...";
                }
                int xLabelWidth = fm.stringWidth(label != null ? label : "");
                g2d.drawString(label != null ? label : "", x - xLabelWidth / 2, height - 15);
            }
        } else {
            // 柱状图/直方图（与Y轴刻度使用相同的计算逻辑）
            for (int i = 0; i < values.size(); i++) {
                // 处理null值
                Double value = values.get(i);
                if (value == null) {
                    continue;
                }
                
                // 使用与Y轴刻度相同的计算逻辑
                double normalizedValue = value / maxValue;
                int barHeight = (int) (normalizedValue * chartHeight);
                int x = padding + gap + i * (barWidth + gap);
                
                // 绘制柱子
                g2d.setColor(colors[i % colors.length]);
                g2d.fillRect(x, height - padding - barHeight, barWidth, barHeight);
                
                // 绘制数值标签（整数显示为整数）
                g2d.setColor(Color.BLACK);
                String valueLabel = allIntegers ? String.format("%d", (int) value.doubleValue()) : String.format("%.1f", value);
                FontMetrics fm = g2d.getFontMetrics(font);
                int labelWidth = fm.stringWidth(valueLabel);
                g2d.drawString(valueLabel, x + barWidth / 2 - labelWidth / 2, height - padding - barHeight - 5);
                
                // 绘制 X 轴标签
                String label = names.get(i);
                if (label != null && label.length() > 8) {
                    label = label.substring(0, 8) + "...";
                }
                int xLabelWidth = fm.stringWidth(label);
                g2d.drawString(label, x + barWidth / 2 - xLabelWidth / 2, height - 15);
            }
        }
    }
    
    /**
     * 解析报告章节内容
     */
    private List<String> parseReportChapters(String content) {
        List<String> chapters = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        
        StringBuilder currentChapter = new StringBuilder();
        String currentChapterTitle = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 检测章节标题
            if (line.startsWith("=== ")) {
                // 保存当前章节
                if (currentChapterTitle != "" && currentChapter.length() > 0) {
                    chapters.add(currentChapterTitle);
                    chapters.addAll(splitContentIntoLines(currentChapter.toString(), 80));
                }
                currentChapterTitle = line;
                currentChapter = new StringBuilder();
            } else {
                currentChapter.append(line).append("\n");
            }
        }
        
        // 添加最后一个章节
        if (currentChapterTitle != "") {
            chapters.add(currentChapterTitle);
            chapters.addAll(splitContentIntoLines(currentChapter.toString(), 80));
        }
        
        return chapters;
    }
    
    /**
     * 将内容按指定长度分行
     */
    private List<String> splitContentIntoLines(String content, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] contentLines = content.split("\\n");
        
        for (String line : contentLines) {
            while (line.length() > maxLength) {
                int splitIndex = maxLength;
                // 尽量在中文标点或空格处分割
                for (int i = maxLength; i > maxLength - 10 && i > 0; i--) {
                    char c = line.charAt(i);
                    if (c == '，' || c == '。' || c == '；' || c == '：' || c == ' ') {
                        splitIndex = i + 1;
                        break;
                    }
                }
                lines.add(line.substring(0, splitIndex));
                line = line.substring(splitIndex);
            }
            if (!line.trim().isEmpty()) {
                lines.add(line.trim());
            }
        }
        
        return lines;
    }

    /**
     * 专门处理成功率报告：把名称+数值合并成 成功次数/失败次数，并放在对应百分比下一行
     */
    private String formatRateReportLines(String content) {
        if (content == null) return "";
        String[] lines = content.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        // 临时变量，用来缓存百分比，后面拼接数值
        String pendingSuccessRate = null;
        String pendingFailRate = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // 1. 先处理百分比行，记录下来
            if (trimmed.startsWith("成功率(%):")) {
                pendingSuccessRate = trimmed;
                sb.append(trimmed).append("\n");
            } else if (trimmed.startsWith("失败率(%):")) {
                pendingFailRate = trimmed;
                sb.append(trimmed).append("\n");
            }
            // 2. 处理 名称:成功 / 名称:失败 + 数值:X
            else if (trimmed.startsWith("名称: 成功")) {
                // 下一行是数值，这里先不写，等数值行一起处理
            } else if (trimmed.startsWith("数值:") && pendingSuccessRate != null) {
                // 数值行，和之前的名称合并成“成功次数:X”
                String value = trimmed.split(":")[1].trim();
                sb.append("成功次数: ").append(value).append("\n");
                pendingSuccessRate = null; // 清除缓存
            } else if (trimmed.startsWith("名称: 失败")) {
                // 同理，失败的先缓存
            } else if (trimmed.startsWith("数值:") && pendingFailRate != null) {
                String value = trimmed.split(":")[1].trim();
                sb.append("失败次数: ").append(value).append("\n");
                pendingFailRate = null;
            }
            // 3. 其他正常行直接保留
            else {
                sb.append(trimmed).append("\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 清洗报告内容：替换英文key为中文、过滤不需要的字段
     */
    private String cleanAndTranslateContent(String content) {
        if (content == null) return "";

        // 1. 过滤掉所有不需要的行
        content = content.replaceAll("(?m)^.*(color|summary|rateData|percentage|名称:|数值:|dataSource:|dayOfWeek|dayName).*$", "");
        content = content.replaceAll("\\[\\d+\\]", ""); // 去掉序号

        // 2. 英文KEY → 中文（保留 totalCount / successCount / failCount 用于后面提取）
        content = content.replace("endDate:", "结束日期：");
        content = content.replace("startDate:", "开始日期：");
        content = content.replace("weekData:", "周数据：");
        content = content.replace("dayNameCn:", "星期：");
        content = content.replace("executionCount:", "执行次数：");
        content = content.replace("avgDailyExecutions:", "日均执行次数：");
        content = content.replace("totalExecutions:", "总执行次数：");
        content = content.replace("dataSourceName:", "数据源名称：");
        content = content.replace("generateTime:", "生成时间：");
        content = content.replace("failRate:", "失败率(%)：");
        content = content.replace("successRate:", "成功率(%)：");

        // 3. 清除多余空行
        content = content.replaceAll("\\n\\s*\\n+", "\n").trim();

        return formatSuccessRateReport(content);
    }

    private String formatSuccessRateReport(String content) {
        if (!content.contains("成功率(%)：") || !content.contains("失败率(%)：")) {
            return content;
        }

        String[] lines = content.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        String totalCount = "0";
        String successCount = "0";
        String failCount = "0";

        // ======================================
        // 动态提取：总次数、成功次数、失败次数
        // ======================================
        for (String line : lines) {
            if (line.startsWith("总次数：")) {
                totalCount = line.split("：")[1].trim();
            }
            if (line.startsWith("成功次数：")) {
                successCount = line.split("：")[1].trim();
            }
            if (line.startsWith("失败次数：")) {
                failCount = line.split("：")[1].trim();
            }
        }

        // ======================================
        // 重新组装内容（只保留需要的）
        // ======================================
        for (String line : lines) {
            // 过滤掉原始的次数行，避免重复
            if (line.startsWith("总次数：") || line.startsWith("成功次数：") || line.startsWith("失败次数：")) {
                continue;
            }

            sb.append(line).append("\n");

            // 在失败率下面显示：失败次数 + 总次数
            if (line.startsWith("失败率(%)：")) {
                sb.append("失败次数：").append(failCount).append("\n");
                sb.append("总次数：").append(totalCount).append("\n");
            }

            // 在成功率下面显示：成功次数
            if (line.startsWith("成功率(%)：")) {
                sb.append("成功次数：").append(successCount).append("\n");
            }
        }

        return sb.toString().trim();
    }


    /**
     * 构建简单的PDF页面内容（使用ASCII字符）
     */
    private String buildSimplePdfPageContent(Report report, String pageRange) {
        StringBuilder content = new StringBuilder();
        
        // 开始文本对象
        content.append("BT\n");
        content.append("/F1 24 Tf\n");  // 设置字体和大小
        content.append("50 750 Td\n");  // 设置文本位置
        content.append("(").append(escapeAscii(report.getName())).append(") Tj\n");  // 显示文本
        content.append("ET\n");  // 结束文本对象
        
        // 生成时间
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("50 720 Td\n");
        content.append("(").append(escapeAscii("Generate Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))).append(") Tj\n");
        content.append("ET\n");
        
        // 报告描述（英文显示）
        if (report.getDescription() != null && !report.getDescription().trim().isEmpty()) {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 700 Td\n");
            content.append("(").append(escapeAscii("Description: " + report.getDescription())).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 报告类型
        if (report.getReportType() != null) {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 680 Td\n");
            content.append("(").append(escapeAscii("Type: " + report.getReportType())).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 报告状态
        if (report.getStatus() != null) {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 660 Td\n");
            content.append("(").append(escapeAscii("Status: " + report.getStatus())).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 分隔线
        content.append("50 640 m\n");
        content.append("562 640 l\n");
        content.append("S\n");
        
        // 报告内容标题
        content.append("BT\n");
        content.append("/F1 14 Tf\n");
        content.append("50 620 Td\n");
        content.append("(").append(escapeAscii("Report Content:")).append(") Tj\n");
        content.append("ET\n");
        
        // 报告内容
        String reportContent = buildReportContent(report, pageRange);
        if (reportContent != null && !reportContent.trim().isEmpty()) {
            String[] lines = wrapText(reportContent, 80);
            int y = 600;
            for (String line : lines) {
                if (y < 50) break;  // 页面底部限制
                content.append("BT\n");
                content.append("/F1 10 Tf\n");
                content.append("50 ").append(y).append(" Td\n");
                content.append("(").append(escapeAscii(line)).append(") Tj\n");
                content.append("ET\n");
                y -= 15;
            }
        } else {
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append("50 600 Td\n");
            content.append("(").append(escapeAscii("No content available")).append(") Tj\n");
            content.append("ET\n");
        }
        
        // 页脚
        content.append("BT\n");
        content.append("/F1 10 Tf\n");
        content.append("50 30 Td\n");
        content.append("(").append(escapeAscii("Report ID: " + report.getId() + " | Generated by Tool Testing Platform")).append(") Tj\n");
        content.append("ET\n");
        
        return content.toString();
    }
    
    /**
     * 转义ASCII字符串中的特殊字符
     */
    private String escapeAscii(String text) {
        if (text == null) return "";
        // 只保留ASCII字符，过滤掉非ASCII字符
        return text.replaceAll("[^\\x00-\\x7F]", "?")
                  .replace("\\", "\\\\")
                  .replace("(", "\\(")
                  .replace(")", "\\)");
    }
    
    /**
     * 将字符串转换为UTF-16BE十六进制编码
     */
    private String stringToHex(String text) {
        if (text == null) return "";
        try {
            byte[] bytes = text.getBytes("UTF-16BE");
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (Exception e) {
            // 如果转换失败，返回空字符串
            return "";
        }
    }
    
    /**
     * 转义PDF字符串中的特殊字符
     */
    private String escapePdfString(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("(", "\\(")
                  .replace(")", "\\)");
    }
    
    /**
     * 构建HTML报告（备用方案）
     */
    private String buildHtmlReport(Report report, String pageRange) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>").append(report.getName()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append(".separator { border-top: 1px solid #ccc; margin: 20px 0; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<h1>").append(report.getName()).append("</h1>\n");
        html.append("<p><strong>生成时间：</strong>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        
        if (report.getDescription() != null) {
            html.append("<p><strong>报告描述：</strong>").append(report.getDescription()).append("</p>\n");
        }
        
        if (report.getReportType() != null) {
            html.append("<p><strong>报告类型：</strong>").append(report.getReportType()).append("</p>\n");
        }
        
        if (report.getStatus() != null) {
            html.append("<p><strong>报告状态：</strong>").append(report.getStatus()).append("</p>\n");
        }
        
        html.append("<div class=\"separator\"></div>\n");
        html.append("<h2>报告内容</h2>\n");
        
        String content = buildReportContent(report, pageRange);
        if (content != null && !content.trim().isEmpty()) {
            html.append("<pre>").append(content).append("</pre>\n");
        } else {
            html.append("<p>报告内容为空</p>\n");
        }
        
        html.append("<div class=\"separator\"></div>\n");
        html.append("<p><small>报告ID：").append(report.getId()).append(" | 生成系统：工具测试平台</small></p>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    /**
     * 生成Word报告
     */
    private void generateWordReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        // 这里应该使用Word生成库，这里用简单的文本文件模拟
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("Word Report - " + report.getName() + "\n\n");
            writer.write("生成时间：" + LocalDateTime.now() + "\n\n");
            writer.write("报告内容：\n");
            writer.write(content);
        }
    }
    
    /**
     * 生成Excel报告
     */
    private void generateExcelReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        // 这里应该使用Excel生成库，这里用简单的文本文件模拟
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("Excel Report - " + report.getName() + "\n\n");
            writer.write("生成时间：" + LocalDateTime.now() + "\n\n");
            writer.write("报告内容：\n");
            writer.write(content);
        }
    }
    
    /**
     * 生成HTML报告
     */
    private void generateHtmlReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>" + report.getName() + "</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("h1 { color: #333; }\n");
            writer.write("p { line-height: 1.6; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("<h1>" + report.getName() + "</h1>\n");
            writer.write("<p><strong>生成时间：</strong>" + LocalDateTime.now() + "</p>\n");
            writer.write("<div>" + content.replace("\n", "<br>") + "</div>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
    }
    
    /**
     * 生成JSON报告
     */
    private void generateJsonReport(Report report, File exportFile, String pageRange) throws Exception {
        String content = buildReportContent(report, pageRange);
        
        try (FileWriter writer = new FileWriter(exportFile)) {
            writer.write("{\n");
            writer.write("  \"reportId\": " + report.getId() + ",\n");
            writer.write("  \"name\": \"" + report.getName() + "\",\n");
            writer.write("  \"description\": \"" + report.getDescription() + "\",\n");
            writer.write("  \"generateTime\": \"" + LocalDateTime.now() + "\",\n");
            writer.write("  \"content\": \"" + content.replace("\"", "\\\"") + "\"\n");
            writer.write("}\n");
        }
    }

    private String buildReportContent(Report report, String pageRange) {
        StringBuilder readableContent = new StringBuilder();

        // 1. 报告基本信息（全部中文，一行一句话）
        readableContent.append("=== 报告基本信息 ===\n");
        readableContent.append("报告名称：").append(report.getName() != null ? report.getName() : "未命名").append("\n");
        readableContent.append("报告描述：").append(report.getDescription() != null ? report.getDescription() : "无描述").append("\n");
        readableContent.append("报告类型：").append(getReadableReportType(report.getReportType())).append("\n");
        readableContent.append("报告状态：").append(getReadableReportStatus(report.getStatus())).append("\n");
        readableContent.append("生成方式：").append(getReadableGenerateType(report.getGenerateType())).append("\n");
        readableContent.append("创建时间：").append(report.getCreateTime() != null ?
                report.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "未知").append("\n");
        readableContent.append("导出次数：").append(report.getExportCount() != null ? report.getExportCount() : 0).append("\n");
        readableContent.append("\n");

        // 2. 报告内容（JSON解析为易读格式，一行一句话）
        String content = report.getContent();
        if (content != null && !content.trim().isEmpty()) {
            readableContent.append("=== 报告统计内容 ===\n");
            try {
                // 尝试解析JSON内容
                Object jsonContent = parseJsonContent(content);
                String formattedContent = formatJsonToReadable(jsonContent);
                // 将内容按句子分行，一行一句话
                readableContent.append(formatContentToSentences(formattedContent));
            } catch (Exception e) {
                // 如果JSON解析失败，显示原始内容
                readableContent.append("原始JSON内容：\n");
                readableContent.append(content);
            }
        } else {
            readableContent.append("=== 报告统计内容 ===\n");
            readableContent.append("报告内容为空\n");
        }

        return readableContent.toString();
    }

    // ===================== 核心：JSON 字段名 自动 转 中文 =====================
    private void appendFlatJson(com.alibaba.fastjson2.JSONObject json, StringBuilder sb, String prefix) {
        for (String key : json.keySet()) {
            Object val = json.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (val instanceof com.alibaba.fastjson2.JSONObject) {
                appendFlatJson((com.alibaba.fastjson2.JSONObject) val, sb, fullKey);
            } else if (val instanceof com.alibaba.fastjson2.JSONArray) {
                com.alibaba.fastjson2.JSONArray array = (com.alibaba.fastjson2.JSONArray) val;
                for (int i = 0; i < array.size(); i++) {
                    Object item = array.get(i);
                    if (item instanceof com.alibaba.fastjson2.JSONObject) {
                        appendFlatJson((com.alibaba.fastjson2.JSONObject) item, sb, fullKey + "[" + i + "]");
                    }
                }
            } else {
                // 👇👇👇 这里自动把英文key转换成中文 👇👇👇
                String chineseKey = convertToChineseKey(key);
                sb.append(chineseKey).append("：").append(val).append("\n");
            }
        }
    }

    // ===================== 字段名映射：英文 → 中文（你要的全部在这里） =====================
    private String convertToChineseKey(String key) {
        switch (key) {
            case "minDuration": return "最小耗时";
            case "totalExecutions": return "总执行次数";
            case "endDate": return "结束日期";
            case "startDate": return "开始日期";
            case "generateTime": return "生成时间";
            case "overallAvgDuration": return "平均耗时";
            case "maxDuration": return "最大耗时";
            case "dataSource": return "数据源";
            case "dataSourceName": return "数据源名称";
            case "timeSlot": return "统计时段";
            case "hourGroup": return "小时分组";
            case "executionCount": return "执行次数";
            case "avgDuration": return "平均耗时";
            // 新增字段映射
            case "totalCount": return "总执行次数";
            case "successRate": return "成功率(%)";
            case "failureCount": return "失败次数";
            case "successCount": return "成功次数";
            case "suggestions": return "优化建议";
            default: return key; // 没有匹配的返回原key
        }
    }
    
    /**
     * 获取可读的报告类型
     */
    private String getReadableReportType(String reportType) {
        if (reportType == null) return "未知类型";
        
        switch (reportType.toUpperCase()) {
            case "PROTOCOL_DISTRIBUTION": return "协议类型分布统计";
            case "RESPONSE_TIME": return "响应时间分析";
            case "FAILURE_REASONS": return "失败原因分析";
            case "WEEKLY_EXECUTION": return "周执行量统计";
            case "SUCCESS_RATE": return "成功率分析";
            case "AUTO_GENERATED": return "自动生成报告";
            default: return reportType;
        }
    }
    

    /**
     * 根据时间范围计算开始时间
     */
    private LocalDateTime calculateStartTime(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange.toUpperCase()) {
            case "TODAY":
                return now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "WEEK":
                return now.minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "MONTH":
                return now.minusMonths(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default:
                return now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
    }

    /**
     * 获取可读的报告状态
     */
    private String getReadableReportStatus(String status) {
        if (status == null) return "未知状态";
        switch (status.toUpperCase()) {
            case "DRAFT": return "草稿";
            case "PUBLISHED": return "已发布";
            case "ARCHIVED": return "已归档";
            case "SCHEDULED": return "定时任务";
            default: return status;
        }
    }
    
    /**
     * 获取可读的生成方式
     */
    private String getReadableGenerateType(String generateType) {
        if (generateType == null) return "未知方式";
        
        switch (generateType.toUpperCase()) {
            case "AUTO": return "自动生成";
            case "MANUAL": return "手动创建";
            default: return generateType;
        }
    }
    


    /**
     * 解析JSON内容（FastJSON版）
     */
    private Object parseJsonContent(String content) {
        try {
            if (content.trim().startsWith("{")) {
                return JSON.parseObject(content);
            } else if (content.trim().startsWith("[")) {
                return JSON.parseArray(content);
            } else {
                throw new RuntimeException("不是标准JSON格式");
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败", e);
        }
    }

    /**
     * 将JSON格式化为易读的文本（FastJSON版）
     */
    private String formatJsonToReadable(Object jsonContent) {
        StringBuilder result = new StringBuilder();

        if (jsonContent instanceof JSONObject) {
            // 检查是否为周执行量统计数据
            if (isWeeklyExecutionData(jsonContent)) {
                // 周执行量数据特殊处理
                String weeklyData = formatWeeklyExecutionData((JSONObject) jsonContent);
                if (!weeklyData.trim().isEmpty()) {
                    result.append(weeklyData);
                } else {
                    // 如果没有周执行量数据，使用默认格式化
                    formatJsonObject((JSONObject) jsonContent, result, 0);
                }
            } else {
                // 其他数据使用默认格式化
                formatJsonObject((JSONObject) jsonContent, result, 0);
            }
        } else if (jsonContent instanceof JSONArray) {
            formatJsonArray((JSONArray) jsonContent, result, 0);
        }

        return result.toString();
    }

    /**
     * 检查是否为周执行量统计数据
     */
    private boolean isWeeklyExecutionData(Object jsonContent) {
        if (jsonContent instanceof JSONObject) {
            JSONObject obj = (JSONObject) jsonContent;
            // 检查是否包含dayNameCn字段
            for (String key : obj.keySet()) {
                if (key.contains("dayNameCn")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 格式化周执行量数据（中文周几、执行次数，然后换行）
     */
    private String formatWeeklyExecutionData(Object jsonContent) {
        if (!(jsonContent instanceof JSONObject)) {
            return "";
        }
        
        JSONObject obj = (JSONObject) jsonContent;
        StringBuilder result = new StringBuilder();
        
        // 检查数据结构，可能是包含dayNameCn和executionCount的数组
        if (obj.containsKey("dayNameCn") && obj.containsKey("执行次数")) {
            // 直接使用dayNameCn和执行次数
            String dayNameCn = obj.getString("dayNameCn");
            Integer executionCount = obj.getInteger("执行次数");
            
            if (dayNameCn != null) {
                result.append(dayNameCn).append(" 执行次数 ").append(executionCount != null ? executionCount : 0).append("\n");
            }
        } else {
            // 遍历所有字段，查找dayNameCn和对应的执行次数
            for (String key : obj.keySet()) {
                if (key.contains("dayNameCn")) {
                    String dayNameCn = obj.getString(key);
                    if (dayNameCn != null) {
                        // 查找对应的执行次数字段
                        String countKey = key.replace("dayNameCn", "执行次数");
                        Integer executionCount = obj.getInteger(countKey);
                        
                        result.append(dayNameCn).append(" 执行次数 ").append(executionCount != null ? executionCount : 0).append("\n");
                    }
                }
            }
        }
        
        return result.toString();
    }

    /**
     * 从报告内容中提取总执行次数
     */
    private int extractTotalCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        
        try {
            // 查找totalCount字段
            String[] lines = content.split("\\r?\\n");
            for (String line : lines) {
                if (line.contains("totalCount:")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        return Integer.parseInt(parts[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取总执行次数失败: {}", e.getMessage());
        }
        
        return 0;
    }

    /**
     * 将英文周几转换为中文
     */
    private String getChineseWeekDay(String weekDay) {
        if (weekDay == null) return "未知";
        
        switch (weekDay.toUpperCase()) {
            case "MONDAY": return "周一";
            case "TUESDAY": return "周二";
            case "WEDNESDAY": return "周三";
            case "THURSDAY": return "周四";
            case "FRIDAY": return "周五";
            case "SATURDAY": return "周六";
            case "SUNDAY": return "周日";
            default: return weekDay;
        }
    }

    /**
     * 将内容按句子分行，一行一句话
     */
    private String formatContentToSentences(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        
        // 按句子分割：句号、问号、感叹号、冒号、分号、换行符
        String[] sentences = content.split("([。！？：；]|\\n)");
        
        for (String sentence : sentences) {
            String trimmedSentence = sentence.trim();
            if (!trimmedSentence.isEmpty()) {
                // 移除多余的空白字符
                String cleanSentence = trimmedSentence.replaceAll("\\s+", " ");
                result.append(cleanSentence).append("\n");
            }
        }
        
        // 如果没有找到句子分隔符，按逗号分割
        if (result.length() == 0) {
            String[] phrases = content.split("，");
            for (String phrase : phrases) {
                String trimmedPhrase = phrase.trim();
                if (!trimmedPhrase.isEmpty()) {
                    String cleanPhrase = trimmedPhrase.replaceAll("\\s+", " ");
                    result.append(cleanPhrase).append("\n");
                }
            }
        }
        
        // 如果还是没有内容，按原样返回
        if (result.length() == 0) {
            return content;
        }
        
        return result.toString();
    }

    /**
     * 格式化JSON对象（FastJSON）
     */
    private void formatJsonObject(JSONObject jsonObject, StringBuilder result, int indent) {
        String indentStr = "  ".repeat(indent);

        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            String readableKey = getReadableKey(key);

            if (value instanceof JSONObject) {
                result.append(indentStr).append(readableKey).append(":\n");
                formatJsonObject((JSONObject) value, result, indent + 1);
            } else if (value instanceof JSONArray) {
                result.append(indentStr).append(readableKey).append(":\n");
                formatJsonArray((JSONArray) value, result, indent + 1);
            } else {
                result.append(indentStr).append(readableKey).append(": ").append(formatValue(value)).append("\n");
            }
        }
    }

    /**
     * 格式化JSON数组（FastJSON）
     */
    private void formatJsonArray(JSONArray jsonArray, StringBuilder result, int indent) {
        String indentStr = "  ".repeat(indent);

        for (int i = 0; i < jsonArray.size(); i++) {
            Object value = jsonArray.get(i);

            if (value instanceof JSONObject) {
                result.append(indentStr).append("[").append(i + 1).append("]\n");
                formatJsonObject((JSONObject) value, result, indent + 1);
            } else if (value instanceof JSONArray) {
                result.append(indentStr).append("[").append(i + 1).append("]\n");
                formatJsonArray((JSONArray) value, result, indent + 1);
            } else {
                result.append(indentStr).append("[").append(i + 1).append("] ").append(formatValue(value)).append("\n");
            }
        }
    }
    
    /**
     * 获取可读的键名
     */
    private String getReadableKey(String key) {
        switch (key.toLowerCase()) {
            case "categorydata": return "分类数据";
            case "category": return "分类";
            case "categoryname": return "分类名称";
            case "protocolcount": return "协议数量";
            case "totalprotocolcount": return "总协议数量";
            case "avgduration": return "平均响应时间(ms)";
            case "maxduration": return "最大响应时间(ms)";
            case "minduration": return "最小响应时间(ms)";
            case "executioncount": return "执行次数";
            case "failurereason": return "失败原因";
            case "failurecount": return "失败次数";
            case "errorcode": return "错误代码";
            case "successrate": return "成功率(%)";
            case "failurerate": return "失败率(%)";
            case "timeslot": return "时间区间";
            case "hourgroup": return "小时分组";
            default: return key;
        }
    }
    
    /**
     * 格式化值
     */
    private String formatValue(Object value) {
        if (value == null) return "空";
        if (value instanceof Number) {
            // 数字格式化
            Number number = (Number) value;
            if (number.doubleValue() == number.intValue()) {
                return String.valueOf(number.intValue());
            } else {
                return String.format("%.2f", number.doubleValue());
            }
        }
        return value.toString();
    }
    
    /**
     * 文本换行处理
     */
    private String[] wrapText(String text, int maxLineLength) {
        if (text == null || text.trim().isEmpty()) {
            return new String[]{"报告内容为空"};
        }
        
        // 简单的换行逻辑
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : text.split("\\s+")) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                // 处理超长单词
                if (word.length() > maxLineLength) {
                    for (int i = 0; i < word.length(); i += maxLineLength) {
                        int end = Math.min(i + maxLineLength, word.length());
                        lines.add(word.substring(i, end));
                    }
                } else {
                    currentLine.append(word);
                }
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    /**
     * 将内容添加到PDF文档（支持多页）
     */
    private void addContentToDocument(PDDocument document, String content, int startY) throws Exception {
        String[] lines = wrapText(content, 80);
        int currentY = startY;
        PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);
        
        // 处理第一页的内容
        try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, currentY);
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                if (currentY < 50) {
                    // 当前页面空间不足，结束当前页面
                    contentStream.endText();
                    
                    // 创建新页面
                    PDPage newPage = new PDPage();
                    document.addPage(newPage);
                    currentY = 750;
                    
                    // 在新页面继续添加剩余内容
                    addContentToNewPage(document, newPage, lines, i, currentY);
                    break;
                } else {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                    currentY -= 15;
                }
            }
            
            if (currentY >= 50) {
                contentStream.endText();
            }
        }
    }
    
    /**
     * 在新页面添加内容
     */
    private void addContentToNewPage(PDDocument document, PDPage page, String[] lines, int startIndex, int startY) throws Exception {
        int currentY = startY;
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, currentY);
            
            for (int i = startIndex; i < lines.length; i++) {
                String line = lines[i];
                
                if (currentY < 50) {
                    // 当前页面空间不足，递归处理
                    contentStream.endText();
                    
                    PDPage newPage = new PDPage();
                    document.addPage(newPage);
                    currentY = 750;
                    
                    addContentToNewPage(document, newPage, lines, i, currentY);
                    break;
                } else {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                    currentY -= 15;
                }
            }
            
            if (currentY >= 50) {
                contentStream.endText();
            }
        }
    }

    @Override
    public String batchExportReports(List<Long> reportIds) {
        // 实现批量导出逻辑
        StringBuilder result = new StringBuilder();
        for (Long reportId : reportIds) {
            String exportPath = exportReport(reportId, "pdf", "all");
            if (exportPath != null) {
                result.append(exportPath).append("\n");
            }
        }
        
        return result.toString();
    }

    @Override
    public Boolean setSchedule(Long id, String frequency, String nextTime) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return false;
        }
        
        report.setGenerateFrequency(frequency);
        report.setNextGenerateTime(LocalDateTime.parse(nextTime));
        report.setIsScheduled(true);
        report.setUpdateTime(LocalDateTime.now());
        
        return reportMapper.updateById(report) > 0;
    }

    @Override
    public Object getReportStatistics(String startTime, String endTime, String reportType) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Report::getIsDeleted, 0);
            
            // 时间范围筛选
            if (startTime != null && !startTime.trim().isEmpty() && 
                endTime != null && !endTime.trim().isEmpty()) {
                queryWrapper.between(Report::getCreateTime, startTime, endTime);
            }
            
            // 报告类型筛选
            if (reportType != null && !reportType.trim().isEmpty()) {
                queryWrapper.eq(Report::getReportType, reportType);
            }
            
            // 获取报告列表
            List<Report> reports = reportMapper.selectList(queryWrapper);
            
            // 进行统计分析
            Map<String, Object> statistics = analyzeReports(reports);
            
            // 添加预警检测
            List<Map<String, Object>> warnings = detectWarnings(statistics, reports);
            statistics.put("warnings", warnings);
            
            // 添加时间范围信息
            statistics.put("period", startTime + " 至 " + endTime);
            statistics.put("reportType", reportType);
            statistics.put("totalCount", reports.size());
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取报告统计失败", e);
            return Map.of(
                "error", "获取报告统计失败: " + e.getMessage(),
                "period", startTime + " 至 " + endTime,
                "reportType", reportType,
                "warnings", List.of()
            );
        }
    }
    
    /**
     * 分析报告数据
     */
    private Map<String, Object> analyzeReports(List<Report> reports) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 基础统计
        analysis.put("totalCount", reports.size());
        analysis.put("draftCount", (int) reports.stream().filter(r -> "DRAFT".equals(r.getStatus())).count());
        analysis.put("publishedCount", (int) reports.stream().filter(r -> "PUBLISHED".equals(r.getStatus())).count());
        analysis.put("archivedCount", (int) reports.stream().filter(r -> "ARCHIVED".equals(r.getStatus())).count());
        
        // 导出统计
        int totalExportCount = reports.stream().mapToInt(r -> r.getExportCount() != null ? r.getExportCount() : 0).sum();
        analysis.put("totalExportCount", totalExportCount);
        analysis.put("avgExportCount", reports.isEmpty() ? 0 : totalExportCount / (double) reports.size());
        
        // 内容分析
        List<Map<String, Object>> contentAnalysis = analyzeReportContents(reports);
        analysis.put("contentAnalysis", contentAnalysis);
        
        // 性能指标
        analysis.put("performanceMetrics", calculatePerformanceMetrics(reports));
        
        return analysis;
    }
    
    /**
     * 分析报告内容
     */
    private List<Map<String, Object>> analyzeReportContents(List<Report> reports) {
        List<Map<String, Object>> analysis = new ArrayList<>();
        
        for (Report report : reports) {
            if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
                try {
                    JSONObject content = JSON.parseObject(report.getContent());
                    Map<String, Object> reportAnalysis = new HashMap<>();
                    
                    reportAnalysis.put("reportId", report.getId());
                    reportAnalysis.put("reportName", report.getName());
                    reportAnalysis.put("reportType", report.getReportType());
                    
                    // 分析成功率数据
                    if (content.containsKey("summary")) {
                        JSONObject summary = content.getJSONObject("summary");
                        reportAnalysis.put("successRate", summary.getDouble("successRate"));
                        reportAnalysis.put("failureRate", summary.getDouble("failureRate"));
                        reportAnalysis.put("totalCount", summary.getInteger("totalCount"));
                    }
                    
                    // 分析时间范围
                    if (content.containsKey("startDate") && content.containsKey("endDate")) {
                        reportAnalysis.put("startDate", content.getString("startDate"));
                        reportAnalysis.put("endDate", content.getString("endDate"));
                    }
                    
                    analysis.add(reportAnalysis);
                    
                } catch (Exception e) {
                    log.warn("解析报告内容失败: {}", report.getId(), e);
                }
            }
        }
        
        return analysis;
    }
    
    /**
     * 计算性能指标
     */
    private Map<String, Object> calculatePerformanceMetrics(List<Report> reports) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 成功率统计
        List<Double> successRates = new ArrayList<>();
        List<Double> failureRates = new ArrayList<>();
        
        for (Report report : reports) {
            if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
                try {
                    JSONObject content = JSON.parseObject(report.getContent());
                    if (content.containsKey("summary")) {
                        JSONObject summary = content.getJSONObject("summary");
                        successRates.add(summary.getDouble("successRate"));
                        failureRates.add(summary.getDouble("failureRate"));
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
        }
        
        if (!successRates.isEmpty()) {
            metrics.put("avgSuccessRate", successRates.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            metrics.put("minSuccessRate", successRates.stream().mapToDouble(Double::doubleValue).min().orElse(0));
            metrics.put("maxSuccessRate", successRates.stream().mapToDouble(Double::doubleValue).max().orElse(0));
            metrics.put("successRateStdDev", calculateStandardDeviation(successRates));
        }
        
        if (!failureRates.isEmpty()) {
            metrics.put("avgFailureRate", failureRates.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            metrics.put("minFailureRate", failureRates.stream().mapToDouble(Double::doubleValue).min().orElse(0));
            metrics.put("maxFailureRate", failureRates.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        }
        
        return metrics;
    }
    
    /**
     * 检测预警
     */
    private List<Map<String, Object>> detectWarnings(Map<String, Object> statistics, List<Report> reports) {
        List<Map<String, Object>> warnings = new ArrayList<>();
        
        // 1. 接口响应突然变慢（环比上涨 50%+）
        detectResponseTimeWarnings(statistics, warnings);
        
        // 2. 成功率骤降、批量报错
        detectSuccessRateWarnings(statistics, warnings);
        
        // 3. 并发下资源瓶颈
        detectResourceBottleneckWarnings(statistics, warnings);
        
        // 4. 重复频发 BUG、高频错误码
        detectFrequentErrorWarnings(reports, warnings);
        
        // 5. 数据断言大面积失败
        detectAssertionFailureWarnings(reports, warnings);
        
        return warnings;
    }
    
    /**
     * 检测响应时间预警
     */
    private void detectResponseTimeWarnings(Map<String, Object> statistics, List<Map<String, Object>> warnings) {
        // 模拟响应时间分析（实际应从历史数据计算环比）
        double avgResponseTime = 1200; // 当前平均响应时间（ms）
        double lastPeriodResponseTime = 800; // 上一周期平均响应时间（ms）
        
        if (lastPeriodResponseTime > 0) {
            double increaseRate = (avgResponseTime - lastPeriodResponseTime) / lastPeriodResponseTime * 100;
            
            if (increaseRate >= 50) {
                warnings.add(createWarning(
                    "接口响应变慢",
                    "HIGH",
                    "接口响应时间环比上涨 " + String.format("%.2f", increaseRate) + "%",
                    "建议检查服务器性能和网络状况"
                ));
            }
        }
    }
    
    /**
     * 检测成功率预警
     */
    private void detectSuccessRateWarnings(Map<String, Object> statistics, List<Map<String, Object>> warnings) {
        Map<String, Object> metrics = (Map<String, Object>) statistics.get("performanceMetrics");
        
        if (metrics.containsKey("avgSuccessRate")) {
            double avgSuccessRate = (double) metrics.get("avgSuccessRate");
            double minSuccessRate = (double) metrics.get("minSuccessRate");
            
            // 成功率低于90%触发预警
            if (avgSuccessRate < 90) {
                warnings.add(createWarning(
                    "成功率偏低",
                    "HIGH",
                    "平均成功率仅为 " + String.format("%.2f", avgSuccessRate) + "%",
                    "建议检查接口稳定性和数据质量"
                ));
            }
            
            // 成功率波动过大
            if (metrics.containsKey("successRateStdDev")) {
                double stdDev = (double) metrics.get("successRateStdDev");
                if (stdDev > 15) {
                    warnings.add(createWarning(
                        "成功率波动异常",
                        "MEDIUM",
                        "成功率标准差为 " + String.format("%.2f", stdDev) + "%，波动较大",
                        "建议检查接口稳定性"
                    ));
                }
            }
        }
    }
    
    /**
     * 检测资源瓶颈预警
     */
    private void detectResourceBottleneckWarnings(Map<String, Object> statistics, List<Map<String, Object>> warnings) {
        // 模拟资源监控数据
        double cpuUsage = 85; // CPU使用率
        double memoryUsage = 92; // 内存使用率
        int connectionCount = 950; // 连接数
        
        if (cpuUsage > 80) {
            warnings.add(createWarning(
                "CPU使用率过高",
                "HIGH",
                "CPU使用率达到 " + cpuUsage + "%",
                "建议优化代码或扩容服务器"
            ));
        }
        
        if (memoryUsage > 85) {
            warnings.add(createWarning(
                "内存使用率过高",
                "HIGH",
                "内存使用率达到 " + memoryUsage + "%",
                "建议检查内存泄漏或增加内存"
            ));
        }
        
        if (connectionCount > 800) {
            warnings.add(createWarning(
                "连接数接近上限",
                "MEDIUM",
                "当前连接数 " + connectionCount + "，接近上限",
                "建议优化连接池配置"
            ));
        }
    }
    
    /**
     * 检测频繁错误预警
     */
    private void detectFrequentErrorWarnings(List<Report> reports, List<Map<String, Object>> warnings) {
        // 模拟错误码分析
        Map<String, Integer> errorCodeCounts = new HashMap<>();
        errorCodeCounts.put("500", 45);
        errorCodeCounts.put("404", 23);
        errorCodeCounts.put("403", 12);
        errorCodeCounts.put("502", 8);
        
        for (Map.Entry<String, Integer> entry : errorCodeCounts.entrySet()) {
            if (entry.getValue() > 20) {
                warnings.add(createWarning(
                    "高频错误码",
                    "MEDIUM",
                    "错误码 " + entry.getKey() + " 出现 " + entry.getValue() + " 次",
                    "建议检查相关接口实现"
                ));
            }
        }
    }
    
    /**
     * 检测断言失败预警
     */
    private void detectAssertionFailureWarnings(List<Report> reports, List<Map<String, Object>> warnings) {
        // 模拟断言失败分析
        int totalAssertions = 1500;
        int failedAssertions = 320;
        double failureRate = (double) failedAssertions / totalAssertions * 100;
        
        if (failureRate > 15) {
            warnings.add(createWarning(
                "数据断言大面积失败",
                "HIGH",
                "断言失败率达到 " + String.format("%.2f", failureRate) + "%",
                "建议检查数据质量和断言规则"
            ));
        }
    }
    
    /**
     * 创建预警信息
     */
    private Map<String, Object> createWarning(String title, String level, String description, String suggestion) {
        Map<String, Object> warning = new HashMap<>();
        warning.put("title", title);
        warning.put("level", level);
        warning.put("description", description);
        warning.put("suggestion", suggestion);
        warning.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return warning;
    }
    
    /**
     * 计算标准差
     */
    private double calculateStandardDeviation(List<Double> values) {
        if (values.isEmpty()) return 0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average().orElse(0);
        
        return Math.sqrt(variance);
    }

    private ReportDTO convertToDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        return dto;
    }

    // ====================== 测试结果展示相关方法实现 ======================

    @Override
    public PageResult<TestResultTableDTO> getTestResultsTable(String testType, Integer page, Integer size, String status, String timeRange) {
        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange);
        
        // 从数据库获取实际的测试结果数据
        List<Map<String, Object>> jobLogs = getJobLogsByDateRange(dateRange[0], dateRange[1]);
        
        // 转换为TestResultTableDTO
        List<TestResultTableDTO> testResults = jobLogs.stream()
            .map(this::convertToTestResultTableDTO)
            .collect(Collectors.toList());

        // 分页处理
        int start = (page - 1) * size;
        int end = Math.min(start + size, testResults.size());
        List<TestResultTableDTO> pageData = testResults.subList(start, end);

        return new PageResult<>(page, size, (long) testResults.size(), pageData);
    }

    @Override
    public Boolean updateTestResultField(String id, String field, String value) {
        // 模拟更新测试结果字段
        // 实际实现应该调用测试模块的Service
        System.out.println("更新测试结果 " + id + " 的字段 " + field + " 为 " + value);
        return true;
    }

    @Override
    public List<TimelineNodeDTO> getTestResultsTimeline(String timeRange, String keyword, String nodeType) {
        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange);
        
        // 从数据库获取实际的执行日志数据
        List<Map<String, Object>> jobLogs = getJobLogsByDateRange(dateRange[0], dateRange[1]);
        
        // 转换为TimelineNodeDTO
        List<TimelineNodeDTO> timeline = jobLogs.stream()
            .map(this::convertToTimelineNodeDTO)
            .filter(node -> nodeType == null || nodeType.equals(node.getNodeType()))
            .filter(node -> keyword == null || node.getContent().contains(keyword))
            .collect(Collectors.toList());

        return timeline;
    }

    // ====================== 自动报告生成相关方法实现 ======================

    @Override
    public Long setupAutoReport(AutoReportConfigDTO config) {
        // 创建自动报告配置
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setName(config.getReportName());
        reportDTO.setReportType(config.getReportType());
        reportDTO.setTemplateId(config.getTemplateId());
        reportDTO.setGenerateType("AUTO");
        reportDTO.setGenerateFrequency(config.getFrequency());
        reportDTO.setIsScheduled(true);
        reportDTO.setStatus("SCHEDULED");

        Long reportId = createReport(reportDTO);
        
        // 记录自动报告配置
        System.out.println("设置自动报告配置：" + config.getReportName() + ", 频率：" + config.getFrequency());
        
        return reportId;
    }

    @Override
    public ReportPreviewDTO previewReportPdf(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null || report.getIsDeleted() == 1) {
            return null;
        }

        ReportPreviewDTO preview = new ReportPreviewDTO();
        preview.setReportId(reportId);
        preview.setReportName(report.getName());
        preview.setPreviewContent("<div>PDF预览内容 - " + report.getName() + "</div>");
        preview.setPdfPreviewUrl("/preview/reports/" + reportId + ".pdf");
        preview.setTotalPages(10);
        preview.setCurrentPage(1);
        preview.setScale(1.0);

        return preview;
    }

    // ====================== 辅助方法 ======================

    private TestResultTableDTO createTestResult(String id, String testType, String name, String status, 
                                               String executor, Long duration, Double successRate) {
        TestResultTableDTO result = new TestResultTableDTO();
        result.setId(id);
        result.setTestType(testType);
        result.setName(name);
        result.setStatus(status);
        result.setExecutor(executor);
        result.setDuration(duration);
        result.setSuccessRate(successRate);
        return result;
    }

    private TimelineNodeDTO createTimelineNode(String id, String time, String title, String content, 
                                              String operator, String nodeType, String status) {
        TimelineNodeDTO node = new TimelineNodeDTO();
        node.setId(id);
        node.setTime(java.time.LocalDateTime.parse(time, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        node.setTitle(title);
        node.setContent(content);
        node.setOperator(operator);
        node.setNodeType(nodeType);
        node.setStatus(status);
        node.setExpandable(true);
        return node;
    }

    // ====================== 基于实际数据的辅助方法 ======================

    /**
     * 计算时间范围
     */
    private LocalDateTime[] calculateDateRange(String timeRange) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime;
        
        switch (timeRange) {
            case "TODAY":
                startTime = LocalDate.now().atStartOfDay();
                break;
            case "7DAYS":
                startTime = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "30DAYS":
                startTime = LocalDate.now().minusDays(30).atStartOfDay();
                break;
            default:
                startTime = LocalDate.now().minusDays(7).atStartOfDay();
        }
        
        return new LocalDateTime[]{startTime, endTime};
    }

    /**
     * 根据时间范围获取任务日志
     */
    private List<Map<String, Object>> getJobLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        // 使用MyBatis Plus查询
        LambdaQueryWrapper<TemplateJobLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(TemplateJobLog::getCreateTime, startTime, endTime);
        queryWrapper.orderByDesc(TemplateJobLog::getCreateTime);
        
        List<TemplateJobLog> jobLogs = templateJobLogMapper.selectList(queryWrapper);
        
        // 转换为Map格式便于处理
        List<Map<String, Object>> result = new ArrayList<>();
        for (TemplateJobLog log : jobLogs) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("id", log.getId());
            logMap.put("jobId", log.getJobId());
            logMap.put("templateId", log.getTemplateId());
            logMap.put("success", log.getSuccess());
            logMap.put("durationMs", log.getDurationMs());
            logMap.put("errorMsg", log.getErrorMsg());
            logMap.put("createTime", log.getCreateTime());
            result.add(logMap);
        }
        
        return result;
    }

    /**
     * 将任务日志转换为测试结果表格DTO
     */
    private TestResultTableDTO convertToTestResultTableDTO(Map<String, Object> jobLog) {
        TestResultTableDTO result = new TestResultTableDTO();
        
        Long logId = (Long) jobLog.get("id");
        Long templateId = (Long) jobLog.get("templateId");
        Integer success = (Integer) jobLog.get("success");
        Long durationMs = (Long) jobLog.get("durationMs");
        LocalDateTime createTime = (LocalDateTime) jobLog.get("createTime");
        
        result.setId("log_" + logId);
        result.setTestType("TEMPLATE_EXECUTE");
        result.setName("模板执行 - " + (templateId != null ? "模板" + templateId : "未知模板"));
        result.setStatus(success != null && success == 1 ? "SUCCESS" : "FAILED");
        result.setExecutor("系统用户"); // 实际项目中需要关联用户表
        result.setDuration(durationMs != null ? durationMs : 0L);
        result.setSuccessRate(success != null && success == 1 ? 100.0 : 0.0);
        result.setExecuteTime(createTime);
        
        return result;
    }

    /**
     * 将任务日志转换为时间线节点DTO
     */
    private TimelineNodeDTO convertToTimelineNodeDTO(Map<String, Object> jobLog) {
        TimelineNodeDTO node = new TimelineNodeDTO();
        
        Long logId = (Long) jobLog.get("id");
        Long templateId = (Long) jobLog.get("templateId");
        Integer success = (Integer) jobLog.get("success");
        String errorMsg = (String) jobLog.get("errorMsg");
        LocalDateTime createTime = (LocalDateTime) jobLog.get("createTime");
        
        node.setId("node_" + logId);
        node.setTime(createTime);
        node.setTitle("模板任务执行");
        
        String content = "执行模板：" + (templateId != null ? "模板" + templateId : "未知模板");
        if (success != null && success == 1) {
            content += "（成功）";
        } else {
            content += "（失败）";
            if (errorMsg != null && !errorMsg.isEmpty()) {
                content += " - " + errorMsg;
            }
        }
        
        node.setContent(content);
        node.setOperator("系统用户"); // 实际项目中需要关联用户表
        node.setNodeType("TASK_EXECUTE");
        node.setStatus(success != null && success == 1 ? "SUCCESS" : "FAILED");
        node.setExpandable(true);
        
        return node;
    }
}