package com.example.tooltestingdemo.controller.report;

import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.dto.report.CustomChartConfigDTO;
import com.example.tooltestingdemo.dto.report.ReportChartDTO;
import com.example.tooltestingdemo.entity.report.ReportChart;
import com.example.tooltestingdemo.service.report.IReportChartService;
import com.example.tooltestingdemo.util.ChartImageExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 报告图表控制器
 */
@RestController
@RequestMapping("/api/report/charts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "报告图表管理")
public class ReportChartController {

    private final IReportChartService reportChartService;
    private final ChartImageExporter chartImageExporter;

    @PostMapping
    @Operation(summary = "创建图表")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('report:chart:create')")
    public Result<Long> createChart(@RequestBody ReportChartDTO chartDTO) {
        try {
            // 参数校验
            if (chartDTO.getName() == null || chartDTO.getName().trim().isEmpty()) {
                return Result.error("图表名称不能为空");
            }
            if (chartDTO.getChartType() == null || chartDTO.getChartType().trim().isEmpty()) {
                return Result.error("图表类型不能为空");
            }
            if (chartDTO.getDataSourceType() == null || chartDTO.getDataSourceType().trim().isEmpty()) {
                return Result.error("数据源类型不能为空");
            }
            
            // 处理chartConfig，确保是有效的JSON格式
            if (chartDTO.getChartConfig() != null && !chartDTO.getChartConfig().trim().isEmpty()) {
                String chartConfig = chartDTO.getChartConfig().trim();
                if (!chartConfig.startsWith("[") && !chartConfig.startsWith("{")) {
                    return Result.error("图表配置必须是有效的JSON格式");
                }
                
                // 验证JSON格式
                try {
                    com.alibaba.fastjson2.JSON.parse(chartConfig);
                } catch (Exception e) {
                    return Result.error("图表配置JSON格式不正确");
                }
            } else {
                // 如果没有提供chartConfig，设置为空对象
                chartDTO.setChartConfig("{}");
            }
            
            // 处理styleConfig，确保是有效的JSON格式
            if (chartDTO.getStyleConfig() != null && !chartDTO.getStyleConfig().trim().isEmpty()) {
                String styleConfig = chartDTO.getStyleConfig().trim();
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
                chartDTO.setStyleConfig("{}");
            }
            
            Long chartId = reportChartService.createChart(chartDTO);
            return Result.success("图表创建成功", chartId);
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
            @RequestParam(defaultValue = "{}") String chartConfig,
            @RequestParam(defaultValue = "{}") String styleConfig) {
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
    public void exportChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "png") String format,
            @RequestParam(defaultValue = "high") String resolution,
            HttpServletResponse response) {
        try {
            // 获取图表数据
            ReportChart chart = reportChartService.getChartById(id);
            if (chart == null || chart.getIsDeleted() == 1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("图表不存在");
                return;
            }
            
            // 生成图片文件
                String exportPath = chartImageExporter.exportChartToImage(chart, format, resolution);
            
            // 设置响应头
            response.setContentType(getContentType(format));
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"chart_" + id + "." + format.toLowerCase() + "\"");
            
            // 写入图片数据到响应流
            byte[] imageData = chartImageExporter.getImageFileContent(exportPath);
            if (imageData != null) {
                response.getOutputStream().write(imageData);
                response.getOutputStream().flush();
                
                // 清理临时文件
                chartImageExporter.deleteImageFile(exportPath);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("生成图片失败");
            }
            
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("导出图表失败：" + e.getMessage());
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
    
    /**
     * 根据格式获取Content-Type
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            default:
                return "image/png";
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
    public Result<Object> analyzeChartData(
            @PathVariable Long id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String timeRange) {
        try {
            Object analysisResult = reportChartService.analyzeChartData(id, startDate, endDate, timeRange);
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