package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.report.StatisticsReportDTO;
import com.example.tooltestingdemo.service.report.ITemplateStatisticsService;
import com.example.tooltestingdemo.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

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
            @RequestParam(required = false) String templateType,
            @RequestParam(defaultValue = "JOB_LOG") String dataSource) {
        try {
            // 参数校验
            if (dataSource != null && !dataSource.trim().isEmpty()) {
                String validDataSource = dataSource.toUpperCase();
                if (!validDataSource.equals("JOB_LOG") && !validDataSource.equals("UNIFIED") && !validDataSource.equals("BATCH")) {
                    return Result.error("数据源参数不正确，请使用JOB_LOG/UNIFIED/BATCH");
                }
            }
            
            com.example.tooltestingdemo.dto.report.ReportDTO report = templateStatisticsService.getTemplateUsageReport(
                timeRange, startDate, endDate, templateType, dataSource);
            return Result.success("模板使用频率报告获取成功", report);
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
            @RequestParam(required = false) String templateId,
            @RequestParam(defaultValue = "JOB_LOG") String dataSource) {
        try {
            // 参数校验
            if (startDate == null || startDate.trim().isEmpty()) {
                return Result.error("开始日期不能为空");
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                return Result.error("结束日期不能为空");
            }
            
            // 验证日期格式
            try {
                java.time.LocalDate.parse(startDate);
                java.time.LocalDate.parse(endDate);
            } catch (Exception e) {
                return Result.error("日期格式不正确，请使用yyyy-MM-dd格式");
            }
            
            // 验证数据源参数
            if (dataSource != null && !dataSource.trim().isEmpty()) {
                String validDataSource = dataSource.toUpperCase();
                if (!validDataSource.equals("JOB_LOG") && !validDataSource.equals("UNIFIED") && !validDataSource.equals("BATCH")) {
                    return Result.error("数据源参数不正确，请使用JOB_LOG/UNIFIED/BATCH");
                }
            }
            
            com.example.tooltestingdemo.dto.report.ReportDTO report = templateStatisticsService.getTemplateEfficiencyReport(
                startDate, endDate, templateId, dataSource);
            return Result.success("模板执行效率报告获取成功", report);
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

    @GetMapping("/response-time/hourly")
    @Operation(summary = "获取每2小时平均响应时间报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<com.example.tooltestingdemo.dto.report.StatisticsReportDTO> getHourlyResponseTimeReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "JOB_LOG") String dataSource) {
        try {
            // 参数校验
            if (dataSource != null && !dataSource.trim().isEmpty()) {
                String validDataSource = dataSource.toUpperCase();
                if (!validDataSource.equals("JOB_LOG") && !validDataSource.equals("UNIFIED") && !validDataSource.equals("BATCH")) {
                    return Result.error("数据源参数不正确，请使用JOB_LOG/UNIFIED/BATCH");
                }
            }

            StatisticsReportDTO report = templateStatisticsService.getHourlyResponseTimeReport(
                startDate, endDate, dataSource);
            return Result.success("每2小时平均响应时间报告获取成功", report);
        } catch (Exception e) {
            return Result.error("获取每2小时平均响应时间报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/weekly-execution")
    @Operation(summary = "获取周一到周日执行量统计报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<StatisticsReportDTO> getWeeklyExecutionReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "JOB_LOG") String dataSource) {
        try {
            // 参数校验
            if (dataSource != null && !dataSource.trim().isEmpty()) {
                String validDataSource = dataSource.toUpperCase();
                if (!validDataSource.equals("JOB_LOG") && !validDataSource.equals("UNIFIED")) {
                    return Result.error("数据源参数不正确，请使用JOB_LOG/UNIFIED");
                }
            }

            StatisticsReportDTO report = templateStatisticsService.getWeeklyExecutionReport(
                startDate, endDate, dataSource);
            return Result.success("周一到周日执行量统计报告获取成功", report);
        } catch (Exception e) {
            return Result.error("获取周一到周日执行量统计报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/success-rate")
    @Operation(summary = "获取成功率分析报告（成功失败占比）")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<com.example.tooltestingdemo.dto.report.StatisticsReportDTO> getSuccessRateReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "JOB_LOG") String dataSource) {
        try {
            // 参数校验
            if (dataSource != null && !dataSource.trim().isEmpty()) {
                String validDataSource = dataSource.toUpperCase();
                if (!validDataSource.equals("JOB_LOG") && !validDataSource.equals("UNIFIED") && !validDataSource.equals("BATCH")) {
                    return Result.error("数据源参数不正确，请使用JOB_LOG/UNIFIED/BATCH");
                }
            }
            
            com.example.tooltestingdemo.dto.report.StatisticsReportDTO report = templateStatisticsService.getSuccessRateReport(
                startDate, endDate, dataSource);
            return Result.success("成功率分析报告获取成功", report);
        } catch (Exception e) {
            return Result.error("获取成功率分析报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/protocol-distribution")
    @Operation(summary = "获取协议类型分布统计报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<com.example.tooltestingdemo.dto.report.StatisticsReportDTO> getProtocolDistributionReport(
            @RequestParam(defaultValue = "CATEGORY") String reportType) {
        try {
            // 参数校验
            if (reportType != null && !reportType.trim().isEmpty()) {
                String validReportType = reportType.toUpperCase();
                if (!validReportType.equals("CATEGORY")) {
                    return Result.error("报告类型参数不正确，请使用CATEGORY");
                }
            }

            StatisticsReportDTO report = templateStatisticsService.getProtocolDistributionReport(
                null, null, reportType);
            return Result.success("协议类型分布统计报告获取成功", report);
        } catch (Exception e) {
            return Result.error("获取协议类型分布统计报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/failure-reasons")
    @Operation(summary = "获取前5的失败原因统计报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:statistics:view')")
    public Result<com.example.tooltestingdemo.dto.report.StatisticsReportDTO> getTopFailureReasonsReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate,
            @RequestParam(required = false, defaultValue = "JOB_LOG") String dataSource) {
        try {
            StatisticsReportDTO report = templateStatisticsService.getTopFailureReasonsReport(
                startDate, endDate, dataSource);
            return Result.success("前5失败原因分析报告获取成功", report);
        } catch (Exception e) {
            return Result.error("获取前5失败原因分析报告失败：" + e.getMessage());
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
                timeRange, null, null, null, "JOB_LOG");
            
            // 获取效率报告（最近7天）
            String endDate = java.time.LocalDate.now().toString();
            String startDate = java.time.LocalDate.now().minusDays(7).toString();
            com.example.tooltestingdemo.dto.report.ReportDTO efficiencyReport = templateStatisticsService.getTemplateEfficiencyReport(
                startDate, endDate, null, "JOB_LOG");
            
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