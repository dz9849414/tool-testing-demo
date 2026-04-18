package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.report.CustomChartConfigDTO;
import com.example.tooltestingdemo.dto.report.ReportChartDTO;
import com.example.tooltestingdemo.service.report.IReportChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告图表控制器
 */
@RestController
@RequestMapping("/api/report/charts")
@RequiredArgsConstructor
@Tag(name = "报告图表管理")
public class ReportChartController {

    private final IReportChartService reportChartService;

    @PostMapping
    @Operation(summary = "创建图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:create')")
    public Result<Long> createChart(@RequestBody ReportChartDTO chartDTO) {
        try {
            Long chartId = reportChartService.createChart(chartDTO);
            return Result.success(chartId);
        } catch (Exception e) {
            return Result.error("创建图表失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:update')")
    public Result<Boolean> updateChart(@PathVariable Long id, @RequestBody ReportChartDTO chartDTO) {
        try {
            Boolean result = reportChartService.updateChart(id, chartDTO);
            return result ? Result.success(true) : Result.error("图表不存在");
        } catch (Exception e) {
            return Result.error("更新图表失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:delete')")
    public Result<Boolean> deleteChart(@PathVariable Long id) {
        try {
            Boolean result = reportChartService.deleteChart(id);
            return result ? Result.success(true) : Result.error("图表不存在");
        } catch (Exception e) {
            return Result.error("删除图表失败：" + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取图表列表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<List<ReportChartDTO>> getChartList(
            @RequestParam(required = false) String chartType,
            @RequestParam(required = false) String dataSourceType) {
        try {
            List<ReportChartDTO> charts = reportChartService.getChartList(chartType, dataSourceType);
            return Result.success(charts);
        } catch (Exception e) {
            return Result.error("获取图表列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取图表详情")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<ReportChartDTO> getChartDetail(@PathVariable Long id) {
        try {
            ReportChartDTO chart = reportChartService.getChartDetail(id);
            return chart != null ? Result.success(chart) : Result.error("图表不存在");
        } catch (Exception e) {
            return Result.error("获取图表详情失败：" + e.getMessage());
        }
    }

    @PostMapping("/preset")
    @Operation(summary = "生成预设图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:preset')")
    public Result<Long> generatePresetChart(
            @RequestParam String chartType,
            @RequestParam String dataSourceIds) {
        try {
            Long chartId = reportChartService.generatePresetChart(chartType, dataSourceIds);
            return Result.success(chartId);
        } catch (Exception e) {
            return Result.error("生成预设图表失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/customize")
    @Operation(summary = "自定义图表配置")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:customize')")
    public Result<Boolean> customizeChart(
            @PathVariable Long id,
            @RequestParam String chartConfig,
            @RequestParam String styleConfig) {
        try {
            Boolean result = reportChartService.customizeChart(id, chartConfig, styleConfig);
            return result ? Result.success(true) : Result.error("图表不存在");
        } catch (Exception e) {
            return Result.error("自定义图表配置失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/export")
    @Operation(summary = "导出图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:export')")
    public Result<String> exportChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "png") String format,
            @RequestParam(defaultValue = "high") String resolution) {
        try {
            String exportPath = reportChartService.exportChart(id, format, resolution);
            return exportPath != null ? Result.success(exportPath) : Result.error("图表不存在");
        } catch (Exception e) {
            return Result.error("导出图表失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-export")
    @Operation(summary = "批量导出图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:batch-export')")
    public Result<String> batchExportCharts(@RequestBody List<Long> chartIds) {
        try {
            String exportResult = reportChartService.batchExportCharts(chartIds);
            return Result.success(exportResult);
        } catch (Exception e) {
            return Result.error("批量导出图表失败：" + e.getMessage());
        }
    }

    @PostMapping("/compare")
    @Operation(summary = "图表对比")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:compare')")
    public Result<Object> compareCharts(
            @RequestBody List<Long> chartIds,
            @RequestParam(defaultValue = "merge") String compareType) {
        try {
            Object comparisonResult = reportChartService.compareCharts(chartIds, compareType);
            return Result.success(comparisonResult);
        } catch (Exception e) {
            return Result.error("图表对比失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/analyze")
    @Operation(summary = "图表数据分析")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:analyze')")
    public Result<Object> analyzeChartData(@PathVariable Long id) {
        try {
            Object analysisResult = reportChartService.analyzeChartData(id);
            return Result.success(analysisResult);
        } catch (Exception e) {
            return Result.error("图表数据分析失败：" + e.getMessage());
        }
    }

    @GetMapping("/groups")
    @Operation(summary = "获取图表分组")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<List<String>> getChartGroups() {
        try {
            List<String> groups = reportChartService.getChartGroups();
            return Result.success(groups);
        } catch (Exception e) {
            return Result.error("获取图表分组失败：" + e.getMessage());
        }
    }

    // ====================== 自定义图表相关接口 ======================

    @PostMapping("/custom")
    @Operation(summary = "创建自定义图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:create')")
    public Result<Long> createCustomChart(@RequestBody CustomChartConfigDTO config) {
        try {
            Long chartId = reportChartService.createCustomChart(config);
            return Result.success(chartId);
        } catch (Exception e) {
            return Result.error("创建自定义图表失败：" + e.getMessage());
        }
    }

    @PutMapping("/custom/{id}")
    @Operation(summary = "更新自定义图表配置")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:update')")
    public Result<Boolean> updateCustomChart(
            @PathVariable Long id,
            @RequestBody CustomChartConfigDTO config) {
        try {
            Boolean result = reportChartService.updateCustomChart(id, config);
            return result ? Result.success(true) : Result.error("图表不存在");
        } catch (Exception e) {
            return Result.error("更新自定义图表失败：" + e.getMessage());
        }
    }

    @GetMapping("/my-templates")
    @Operation(summary = "获取我的图表模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<List<CustomChartConfigDTO>> getMyChartTemplates() {
        try {
            List<CustomChartConfigDTO> templates = reportChartService.getMyChartTemplates();
            return Result.success(templates);
        } catch (Exception e) {
            return Result.error("获取图表模板失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/save-as-template")
    @Operation(summary = "保存图表为模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:create')")
    public Result<Long> saveChartAsTemplate(
            @PathVariable Long id,
            @RequestParam String templateName,
            @RequestParam(required = false) String description) {
        try {
            Long templateId = reportChartService.saveChartAsTemplate(id, templateName, description);
            return Result.success(templateId);
        } catch (Exception e) {
            return Result.error("保存图表模板失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/template/{templateId}")
    @Operation(summary = "删除图表模板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:delete')")
    public Result<Boolean> deleteChartTemplate(@PathVariable Long templateId) {
        try {
            Boolean result = reportChartService.deleteChartTemplate(templateId);
            return result ? Result.success(true) : Result.error("模板不存在");
        } catch (Exception e) {
            return Result.error("删除图表模板失败：" + e.getMessage());
        }
    }

    @PostMapping("/preview")
    @Operation(summary = "预览图表效果")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<Object> previewChart(@RequestBody CustomChartConfigDTO config) {
        try {
            Object previewData = reportChartService.previewChart(config);
            return Result.success(previewData);
        } catch (Exception e) {
            return Result.error("预览图表失败：" + e.getMessage());
        }
    }

    @GetMapping("/visual-config")
    @Operation(summary = "获取可视化配置面板")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:view')")
    public Result<Object> getVisualConfigPanel(@RequestParam(required = false) String chartType) {
        try {
            Object configPanel = reportChartService.getVisualConfigPanel(chartType);
            return Result.success(configPanel);
        } catch (Exception e) {
            return Result.error("获取配置面板失败：" + e.getMessage());
        }
    }
}