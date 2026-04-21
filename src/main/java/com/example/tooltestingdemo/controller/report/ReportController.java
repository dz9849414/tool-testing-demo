package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.common.PageResult;
import com.example.tooltestingdemo.dto.report.AutoReportConfigDTO;
import com.example.tooltestingdemo.dto.report.ReportDTO;
import com.example.tooltestingdemo.dto.report.ReportPreviewDTO;
import com.example.tooltestingdemo.dto.report.TestResultTableDTO;
import com.example.tooltestingdemo.dto.report.TimelineNodeDTO;
import com.example.tooltestingdemo.service.report.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

/**
 * 报告控制器
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "报告管理")
public class ReportController {

    private final IReportService reportService;

    @PostMapping
    @Operation(summary = "创建报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:create')")
    public Result<Long> createReport(@RequestBody ReportDTO reportDTO) {
        try {
            // 参数校验
            if (reportDTO.getName() == null || reportDTO.getName().trim().isEmpty()) {
                return Result.error("报告名称不能为空");
            }
            if (reportDTO.getReportType() == null || reportDTO.getReportType().trim().isEmpty()) {
                return Result.error("报告类型不能为空");
            }
            
            // 处理content，确保是有效的JSON格式
            if (reportDTO.getContent() != null && !reportDTO.getContent().trim().isEmpty()) {
                String content = reportDTO.getContent().trim();
                if (!content.startsWith("[") && !content.startsWith("{")) {
                    // 如果是普通文本，转换为JSON对象格式
                    reportDTO.setContent("{\"text\": \"" + content.replace("\"", "\\\"") + "\"}");
                }
            } else {
                // 如果没有提供content，设置为空对象
                reportDTO.setContent("{}");
            }
            
            // 处理styleConfig，确保是有效的JSON格式
            if (reportDTO.getStyleConfig() != null && !reportDTO.getStyleConfig().trim().isEmpty()) {
                String styleConfig = reportDTO.getStyleConfig().trim();
                if (!styleConfig.startsWith("[") && !styleConfig.startsWith("{")) {
                    return Result.error("样式配置必须是有效的JSON格式");
                }
                
                // 验证JSON格式
                try {
                    com.alibaba.fastjson2.JSON.parse(styleConfig);
                } catch (Exception e) {
                    return Result.error("样式配置JSON格式不正确");
                }
            } else {
                // 如果没有提供styleConfig，设置为空对象
                reportDTO.setStyleConfig("{}");
            }
            
            // 处理dataSourceIds，确保是有效的JSON格式
            if (reportDTO.getDataSourceIds() != null && !reportDTO.getDataSourceIds().trim().isEmpty()) {
                String dataSourceIds = reportDTO.getDataSourceIds().trim();
                if (!dataSourceIds.startsWith("[") && !dataSourceIds.startsWith("{")) {
                    // 如果是逗号分隔的ID列表，转换为JSON数组格式
                    reportDTO.setDataSourceIds("[" + dataSourceIds + "]");
                }
            } else {
                // 如果没有提供dataSourceIds，设置为空数组
                reportDTO.setDataSourceIds("[]");
            }
            
            // 处理chartIds，确保是有效的JSON格式
            if (reportDTO.getChartIds() != null && !reportDTO.getChartIds().trim().isEmpty()) {
                String chartIds = reportDTO.getChartIds().trim();
                if (!chartIds.startsWith("[") && !chartIds.startsWith("{")) {
                    // 如果是逗号分隔的ID列表，转换为JSON数组格式
                    reportDTO.setChartIds("[" + chartIds + "]");
                }
            } else {
                // 如果没有提供chartIds，设置为空数组
                reportDTO.setChartIds("[]");
            }
            
            Long reportId = reportService.createReport(reportDTO);
            return Result.success("报告创建成功", reportId);
        } catch (Exception e) {
            return Result.error("创建报告失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:update')")
    public Result<Boolean> updateReport(@PathVariable Long id, @RequestBody ReportDTO reportDTO) {
        try {
            Boolean result = reportService.updateReport(id, reportDTO);
            return result ? Result.success(true) : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("更新报告失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:delete')")
    public Result<Boolean> deleteReport(@PathVariable Long id) {
        try {
            Boolean result = reportService.deleteReport(id);
            return result ? Result.success(true) : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("删除报告失败：" + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取报告列表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<List<ReportDTO>> getReportList(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String status) {
        try {
            List<ReportDTO> reports = reportService.getReportList(reportType, status);
            return Result.success(reports);
        } catch (Exception e) {
            return Result.error("获取报告列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报告详情")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<ReportDTO> getReportDetail(@PathVariable Long id) {
        try {
            ReportDTO report = reportService.getReportDetail(id);
            return report != null ? Result.success(report) : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("获取报告详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/auto-generate")
    @Operation(summary = "自动生成报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:auto-generate')")
    public Result<Long> autoGenerateReport(
            @RequestParam String reportType,
            @RequestParam String dataSourceIds) {
        try {
            // 参数校验
            if (reportType == null || reportType.trim().isEmpty()) {
                return Result.error("报告类型不能为空");
            }
            if (dataSourceIds == null || dataSourceIds.trim().isEmpty()) {
                return Result.error("数据源ID不能为空");
            }
            
            // 处理dataSourceIds，确保是有效的JSON格式
            String processedDataSourceIds = dataSourceIds.trim();
            if (!processedDataSourceIds.startsWith("[") && !processedDataSourceIds.startsWith("{")) {
                // 如果是逗号分隔的ID列表，转换为JSON数组格式
                processedDataSourceIds = "[" + processedDataSourceIds + "]";
            }
            
            // 验证JSON格式
            try {
                com.alibaba.fastjson2.JSON.parse(processedDataSourceIds);
            } catch (Exception e) {
                return Result.error("数据源ID格式不正确，请使用逗号分隔的ID列表或JSON数组格式");
            }
            
            Long reportId = reportService.autoGenerateReport(reportType, processedDataSourceIds);
            return Result.success("报告自动生成成功", reportId);
        } catch (Exception e) {
            return Result.error("自动生成报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "预览报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:preview')")
    public Result<String> previewReport(@PathVariable Long id) {
        try {
            String previewContent = reportService.previewReport(id);
            return previewContent != null ? Result.success(previewContent) : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("预览报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:export')")
    public ResponseEntity<Resource> exportReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(defaultValue = "all") String pageRange) {
        try {
            // 获取报告文件
            File exportFile = reportService.exportReportFile(id, format, pageRange);
            if (exportFile == null || !exportFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 构建文件资源
            Resource resource = new FileSystemResource(exportFile);
            
            // 设置响应头
            String filename = "report_" + id + "_" + System.currentTimeMillis() + "." + format;
            String contentType = getContentType(format);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .contentLength(exportFile.length())
                    .body(resource);
                    
        } catch (Exception e) {
            throw new RuntimeException("导出报告失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 根据文件格式获取Content-Type
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "html":
                return "text/html";
            case "json":
                return "application/json";
            default:
                return "application/octet-stream";
        }
    }

    @PostMapping("/batch-export")
    @Operation(summary = "批量导出报告")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:batch-export')")
    public Result<String> batchExportReports(@RequestBody List<Long> reportIds) {
        try {
            String exportResult = reportService.batchExportReports(reportIds);
            return Result.success(exportResult);
        } catch (Exception e) {
            return Result.error("批量导出报告失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/schedule")
    @Operation(summary = "设置定时生成")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:schedule')")
    public Result<Boolean> setSchedule(
            @PathVariable Long id,
            @RequestParam String frequency,
            @RequestParam String nextTime) {
        try {
            Boolean result = reportService.setSchedule(id, frequency, nextTime);
            return result ? Result.success(true) : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("设置定时生成失败：" + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取报告统计")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:statistics')")
    public Result<Object> getReportStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String reportType) {
        try {
            Object statistics = reportService.getReportStatistics(startTime, endTime, reportType);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取报告统计失败：" + e.getMessage());
        }
    }

    // ====================== 测试结果展示相关接口 ======================

    @GetMapping("/test-results/table")
    @Operation(summary = "获取测试结果表格数据")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<PageResult<TestResultTableDTO>> getTestResultsTable(
            @RequestParam String testType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String timeRange) {
        try {
            PageResult<TestResultTableDTO> result = reportService.getTestResultsTable(
                testType, page, size, status, timeRange);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取测试结果表格失败：" + e.getMessage());
        }
    }

    @PutMapping("/test-results/{id}/field")
    @Operation(summary = "行内编辑测试结果字段")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:update')")
    public Result<Boolean> updateTestResultField(
            @PathVariable String id,
            @RequestParam String field,
            @RequestParam String value) {
        try {
            Boolean result = reportService.updateTestResultField(id, field, value);
            return result ? Result.success(true) : Result.error("测试结果不存在");
        } catch (Exception e) {
            return Result.error("更新测试结果字段失败：" + e.getMessage());
        }
    }

    @GetMapping("/test-results/timeline")
    @Operation(summary = "获取测试结果时间线")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:view')")
    public Result<List<TimelineNodeDTO>> getTestResultsTimeline(
            @RequestParam String timeRange,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String nodeType) {
        try {
            List<TimelineNodeDTO> timeline = reportService.getTestResultsTimeline(
                timeRange, keyword, nodeType);
            return Result.success(timeline);
        } catch (Exception e) {
            return Result.error("获取测试结果时间线失败：" + e.getMessage());
        }
    }

    // ====================== 自动报告生成相关接口 ======================

    @PostMapping("/auto-generate/config")
    @Operation(summary = "设置自动生成报告配置")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:auto-generate')")
    public Result<Long> setupAutoReport(@RequestBody AutoReportConfigDTO config) {
        try {
            Long configId = reportService.setupAutoReport(config);
            return Result.success(configId);
        } catch (Exception e) {
            return Result.error("设置自动报告配置失败：" + e.getMessage());
        }
    }

    @GetMapping("/preview/{reportId}/pdf")
    @Operation(summary = "预览报告PDF")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:preview')")
    public Result<ReportPreviewDTO> previewReportPdf(@PathVariable Long reportId) {
        try {
            ReportPreviewDTO preview = reportService.previewReportPdf(reportId);
            return preview != null ? Result.success(preview) : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("预览报告失败：" + e.getMessage());
        }
    }
}