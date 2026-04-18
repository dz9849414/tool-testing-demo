package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.service.report.ITemplateStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 模板统计控制器
 */
@RestController
@RequestMapping("/api/report/template-statistics")
@RequiredArgsConstructor
@Tag(name = "模板统计报告")
public class TemplateStatisticsController {

    private final ITemplateStatisticsService templateStatisticsService;

    @GetMapping("/usage")
    @Operation(summary = "获取模板使用频率报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<com.example.tooltestingdemo.dto.report.ReportDTO> getTemplateUsageReport(
            @RequestParam(defaultValue = "7DAYS") String timeRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String templateType) {
        try {
            com.example.tooltestingdemo.dto.report.ReportDTO report = templateStatisticsService.getTemplateUsageReport(
                timeRange, startDate, endDate, templateType);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取模板使用频率报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/efficiency")
    @Operation(summary = "获取模板执行效率报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<com.example.tooltestingdemo.dto.report.ReportDTO> getTemplateEfficiencyReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String templateId) {
        try {
            com.example.tooltestingdemo.dto.report.ReportDTO report = templateStatisticsService.getTemplateEfficiencyReport(
                startDate, endDate, templateId);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取模板执行效率报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/usage/chart")
    @Operation(summary = "生成模板使用统计图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<Object> generateTemplateUsageChart(
            @RequestParam(defaultValue = "7DAYS") String timeRange,
            @RequestParam(defaultValue = "BAR") String chartType) {
        try {
            Object chartData = templateStatisticsService.generateTemplateUsageChart(timeRange, chartType);
            return Result.success(chartData);
        } catch (Exception e) {
            return Result.error("生成模板使用统计图表失败：" + e.getMessage());
        }
    }

    @GetMapping("/efficiency/chart")
    @Operation(summary = "生成模板效率统计图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<Object> generateTemplateEfficiencyChart(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "LINE") String chartType) {
        try {
            Object chartData = templateStatisticsService.generateTemplateEfficiencyChart(startDate, endDate, chartType);
            return Result.success(chartData);
        } catch (Exception e) {
            return Result.error("生成模板效率统计图表失败：" + e.getMessage());
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "获取模板统计摘要")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<Object> getTemplateStatisticsSummary(
            @RequestParam(defaultValue = "30DAYS") String timeRange) {
        try {
            // 获取使用频率报告
            com.example.tooltestingdemo.dto.report.ReportDTO usageReport = templateStatisticsService.getTemplateUsageReport(
                timeRange, null, null, null);
            
            // 获取效率报告（最近7天）
            String endDate = java.time.LocalDate.now().toString();
            String startDate = java.time.LocalDate.now().minusDays(7).toString();
            com.example.tooltestingdemo.dto.report.ReportDTO efficiencyReport = templateStatisticsService.getTemplateEfficiencyReport(
                startDate, endDate, null);
            
            // 构建摘要数据
            java.util.Map<String, Object> summary = new java.util.HashMap<>();
            summary.put("reportName", "模板统计摘要");
            summary.put("timeRange", timeRange);
            summary.put("usageReportId", usageReport.getId());
            summary.put("efficiencyReportId", efficiencyReport.getId());
            summary.put("reportTime", new java.util.Date());
            
            return Result.success(summary);
        } catch (Exception e) {
            return Result.error("获取模板统计摘要失败：" + e.getMessage());
        }
    }
}