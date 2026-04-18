package com.example.tooltestingdemo.service.report;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.tooltestingdemo.dto.report.CustomChartConfigDTO;
import com.example.tooltestingdemo.dto.report.ReportChartDTO;
import com.example.tooltestingdemo.entity.report.ReportChart;

import java.util.List;

/**
 * 报告图表服务接口
 */
public interface IReportChartService extends IService<ReportChart> {
    
    /**
     * 创建图表
     */
    Long createChart(ReportChartDTO chartDTO);
    
    /**
     * 更新图表
     */
    Boolean updateChart(Long id, ReportChartDTO chartDTO);
    
    /**
     * 删除图表
     */
    Boolean deleteChart(Long id);
    
    /**
     * 获取图表列表
     */
    List<ReportChartDTO> getChartList(String chartType, String dataSourceType);
    
    /**
     * 获取图表详情
     */
    ReportChartDTO getChartDetail(Long id);
    
    /**
     * 生成预设图表
     */
    Long generatePresetChart(String chartType, String dataSourceIds);
    
    /**
     * 自定义图表配置
     */
    Boolean customizeChart(Long id, String chartConfig, String styleConfig);
    
    /**
     * 导出图表
     */
    String exportChart(Long id, String format, String resolution);
    
    /**
     * 批量导出图表
     */
    String batchExportCharts(List<Long> chartIds);
    
    /**
     * 图表对比
     */
    Object compareCharts(List<Long> chartIds, String compareType);
    
    /**
     * 图表数据分析
     */
    Object analyzeChartData(Long id);
    
    /**
     * 获取图表分组
     */
    List<String> getChartGroups();

    // ====================== 自定义图表相关方法 ======================

    /**
     * 创建自定义图表
     */
    Long createCustomChart(CustomChartConfigDTO config);

    /**
     * 更新自定义图表配置
     */
    Boolean updateCustomChart(Long id, CustomChartConfigDTO config);

    /**
     * 获取我的图表模板
     */
    List<CustomChartConfigDTO> getMyChartTemplates();

    /**
     * 保存图表为模板
     */
    Long saveChartAsTemplate(Long chartId, String templateName, String description);

    /**
     * 删除图表模板
     */
    Boolean deleteChartTemplate(Long templateId);

    /**
     * 预览图表效果
     */
    Object previewChart(CustomChartConfigDTO config);

    /**
     * 获取可视化配置面板
     */
    Object getVisualConfigPanel(String chartType);
}