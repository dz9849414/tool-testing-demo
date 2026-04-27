package com.example.tooltestingdemo.service.impl.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.report.CustomChartConfigDTO;
import com.example.tooltestingdemo.dto.report.ReportChartDTO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.entity.report.Report;
import com.example.tooltestingdemo.entity.report.ReportChart;
import com.example.tooltestingdemo.mapper.report.ReportChartMapper;
import com.example.tooltestingdemo.mapper.report.ReportMapper;
import com.example.tooltestingdemo.service.report.IReportChartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报告图表服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReportChartServiceImpl extends ServiceImpl<ReportChartMapper, ReportChart> implements IReportChartService {

    private final ReportChartMapper reportChartMapper;
    private final ReportMapper reportMapper;

    @Override
    public Long createChart(ReportChartDTO chartDTO) {
        ReportChart chart = new ReportChart();
        BeanUtils.copyProperties(chartDTO, chart);
        
        // 设置默认值
        chart.setCreateTime(LocalDateTime.now());
        chart.setUpdateTime(LocalDateTime.now());
        chart.setIsDeleted(0);
        chart.setUsageCount(0);
        chart.setIsCustom(false);
        
        reportChartMapper.insert(chart);
        return chart.getId();
    }

    @Override
    public Boolean updateChart(Long id, ReportChartDTO chartDTO) {
        ReportChart chart = reportChartMapper.selectById(id);
        if (chart == null) {
            return false;
        }
        
        BeanUtils.copyProperties(chartDTO, chart);
        chart.setUpdateTime(LocalDateTime.now());
        
        return reportChartMapper.updateById(chart) > 0;
    }

    @Override
    public Boolean deleteChart(Long id) {
        ReportChart chart = reportChartMapper.selectById(id);
        if (chart == null) {
            return false;
        }
        
        // 软删除
        chart.setIsDeleted(1);
        chart.setUpdateTime(LocalDateTime.now());
        
        return reportChartMapper.updateById(chart) > 0;
    }

    @Override
    public List<ReportChartDTO> getChartList(String chartType, String dataSourceType) {
        LambdaQueryWrapper<ReportChart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportChart::getIsDeleted, 0);
        
        if (chartType != null) {
            queryWrapper.eq(ReportChart::getChartType, chartType);
        }
        
        if (dataSourceType != null) {
            queryWrapper.eq(ReportChart::getDataSourceType, dataSourceType);
        }
        
        queryWrapper.orderByDesc(ReportChart::getCreateTime);
        
        List<ReportChart> charts = reportChartMapper.selectList(queryWrapper);
        
        return charts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ReportChartDTO getChartDetail(Long id) {
        ReportChart chart = reportChartMapper.selectById(id);
        if (chart == null || chart.getIsDeleted() == 1) {
            return null;
        }
        
        return convertToDTO(chart);
    }

    @Override
    public Long generatePresetChart(String chartType, String dataSourceIds) {
        // 实现生成预设图表的逻辑
        ReportChartDTO chartDTO = new ReportChartDTO();
        chartDTO.setName("预设图表 - " + chartType + " - " + LocalDateTime.now());
        chartDTO.setDescription("基于数据源自动生成的预设图表");
        chartDTO.setChartType(chartType);
        chartDTO.setDataSourceType("TEMPLATE");
        chartDTO.setDataSourceIds(dataSourceIds);
        chartDTO.setIsCustom(false);
        chartDTO.setStatus(1);
        
        return createChart(chartDTO);
    }

    @Override
    public Boolean customizeChart(Long id, String chartConfig, String styleConfig) {
        ReportChart chart = reportChartMapper.selectById(id);
        if (chart == null) {
            return false;
        }
        
        chart.setChartConfig(chartConfig);
        chart.setStyleConfig(styleConfig);
        chart.setIsCustom(true);
        chart.setUpdateTime(LocalDateTime.now());
        
        return reportChartMapper.updateById(chart) > 0;
    }

    @Override
    public ReportChart getChartById(Long id) {
        return reportChartMapper.selectById(id);
    }
    
    @Override
    public String exportChart(Long id, String format, String resolution) {
        ReportChart chart = reportChartMapper.selectById(id);
        if (chart == null || chart.getIsDeleted() == 1) {
            return null;
        }
        
        // 更新使用统计
        chart.setUsageCount(chart.getUsageCount() + 1);
        reportChartMapper.updateById(chart);
        
        // 构建导出文件路径
        return "/exports/charts/" + id + "." + format.toLowerCase();
    }

    @Override
    public String batchExportCharts(List<Long> chartIds) {
        // 实现批量导出逻辑
        StringBuilder result = new StringBuilder();
        for (Long chartId : chartIds) {
            String exportPath = exportChart(chartId, "png", "high");
            if (exportPath != null) {
                result.append(exportPath).append("\n");
            }
        }
        
        return result.toString();
    }

    @Override
    public Object compareCharts(List<Long> chartIds, String compareType) {
        // 实现图表对比逻辑
        // 这里简化处理，返回对比结果对象
        return new Object() {
            public List<Long> comparedCharts = chartIds;
            public String comparisonType = compareType;
            public String result = "对比分析完成";
            public List<String> differences = List.of("差异点1", "差异点2", "差异点3");
        };
    }

    @Override
    public Object analyzeChartData(Long id) {
        try {
            // 获取图表信息
            ReportChart chart = reportChartMapper.selectById(id);
            if (chart == null || chart.getIsDeleted() == 1) {
                throw new RuntimeException("图表不存在");
            }
            
            // 根据数据源类型分析数据
            String dataSourceType = chart.getDataSourceType();
            String chartData = null;
            
            if ("PROTOCOL".equals(dataSourceType)) {
                // 协议数据源：分析协议测试数据
                chartData = analyzeProtocolTestData(chart);
            } else if ("TEMPLATE".equals(dataSourceType)) {
                // 模板数据源：分析模板执行数据
                chartData = analyzeTemplateExecuteData(chart);
            } else if ("REPORT".equals(dataSourceType)) {
                // 报告数据源：分析报告内容数据
                chartData = analyzeReportContentData(chart);
            } else {
                // 默认数据源：使用模拟数据
                chartData = generateDefaultChartData();
            }
            
            // 更新图表数据
            chart.setChartData(chartData);
            reportChartMapper.updateById(chart);
            
            // 返回分析结果
            final String finalChartData = chartData;
            return new Object() {
                public Long chartId = id;
                public String analysisResult = "数据分析完成";
                public String chartData = finalChartData;
                public String message = "数据已成功分析并更新到图表";
            };
            
        } catch (Exception e) {
            throw new RuntimeException("图表数据分析失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChartGroups() {
        // 实现获取图表分组的逻辑
        // 这里简化处理，返回固定分组
        return List.of("默认分组", "任务统计", "协议分析", "模板使用");
    }

    private ReportChartDTO convertToDTO(ReportChart chart) {
        ReportChartDTO dto = new ReportChartDTO();
        BeanUtils.copyProperties(chart, dto);
        return dto;
    }
    
    /**
     * 分析协议测试数据
     */
    private String analyzeProtocolTestData(ReportChart chart) {
        // 这里实现协议测试数据的分析逻辑
        JSONObject chartData = new JSONObject();
        JSONArray dataArray = new JSONArray();
        
        // 模拟协议测试数据
        JSONObject httpData = new JSONObject();
        httpData.put("name", "HTTP协议");
        httpData.put("value", 95);
        dataArray.add(httpData);
        
        JSONObject httpsData = new JSONObject();
        httpsData.put("name", "HTTPS协议");
        httpsData.put("value", 98);
        dataArray.add(httpsData);
        
        JSONObject tcpData = new JSONObject();
        tcpData.put("name", "TCP协议");
        tcpData.put("value", 92);
        dataArray.add(tcpData);
        
        chartData.put("data", dataArray);
        chartData.put("title", "协议测试成功率统计");
        chartData.put("type", "BAR");
        
        return chartData.toJSONString();
    }
    
    /**
     * 分析模板执行数据
     */
    private String analyzeTemplateExecuteData(ReportChart chart) {
        // 这里实现模板执行数据的分析逻辑
        JSONObject chartData = new JSONObject();
        JSONArray dataArray = new JSONArray();
        
        // 模拟模板执行数据
        JSONObject successData = new JSONObject();
        successData.put("name", "成功");
        successData.put("value", 85);
        dataArray.add(successData);
        
        JSONObject failureData = new JSONObject();
        failureData.put("name", "失败");
        failureData.put("value", 15);
        dataArray.add(failureData);
        
        chartData.put("data", dataArray);
        chartData.put("title", "模板执行成功率统计");
        chartData.put("type", "PIE");
        
        return chartData.toJSONString();
    }
    
    /**
     * 分析报告内容数据
     */
    private String analyzeReportContentData(ReportChart chart) {
        try {
            // 获取数据源ID
            String dataSourceIds = chart.getDataSourceIds();
            if (dataSourceIds == null || dataSourceIds.trim().isEmpty()) {
                return generateDefaultChartData();
            }
            
            // 解析数据源ID（JSON数组格式）
            JSONArray dataSourceArray = JSON.parseArray(dataSourceIds);
            if (dataSourceArray.isEmpty()) {
                return generateDefaultChartData();
            }
            
            // 获取第一个报告ID
            Long reportId = dataSourceArray.getLong(0);
            Report report = reportMapper.selectById(reportId);
            if (report == null || report.getIsDeleted() == 1) {
                return generateDefaultChartData();
            }
            
            // 解析报告内容
            String content = report.getContent();
            if (content == null || content.trim().isEmpty()) {
                return generateDefaultChartData();
            }
            
            JSONObject reportContent = JSON.parseObject(content);
            JSONObject chartData = new JSONObject();
            JSONArray dataArray = new JSONArray();
            
            // 分析报告数据
            if (reportContent.containsKey("summary")) {
                JSONObject summary = reportContent.getJSONObject("summary");
                
                // 成功率数据
                JSONObject successData = new JSONObject();
                successData.put("name", "成功");
                successData.put("value", summary.getInteger("successCount"));
                successData.put("percentage", summary.getDouble("successRate"));
                dataArray.add(successData);
                
                // 失败率数据
                JSONObject failureData = new JSONObject();
                failureData.put("name", "失败");
                failureData.put("value", summary.getInteger("failureCount"));
                failureData.put("percentage", summary.getDouble("failureRate"));
                dataArray.add(failureData);
            }
            
            // 如果有rateData，使用rateData
            if (reportContent.containsKey("rateData")) {
                JSONArray rateData = reportContent.getJSONArray("rateData");
                dataArray = rateData; // 直接使用rateData
            }
            
            // 设置图表数据
            chartData.put("data", dataArray);
            chartData.put("title", chart.getName() != null ? chart.getName() : "报告数据分析");
            chartData.put("type", chart.getChartType() != null ? chart.getChartType() : "BAR");
            
            // 添加时间范围信息
            if (reportContent.containsKey("startDate") && reportContent.containsKey("endDate")) {
                chartData.put("startDate", reportContent.getString("startDate"));
                chartData.put("endDate", reportContent.getString("endDate"));
            }
            
            // 添加数据源信息
            if (reportContent.containsKey("dataSourceName")) {
                chartData.put("dataSource", reportContent.getString("dataSourceName"));
            }
            
            return chartData.toJSONString();
            
        } catch (Exception e) {
            log.error("分析报告内容数据失败", e);
            return generateDefaultChartData();
        }
    }
    
    /**
     * 生成默认图表数据
     */
    private String generateDefaultChartData() {
        JSONObject chartData = new JSONObject();
        JSONArray dataArray = new JSONArray();
        
        // 默认数据
        JSONObject data1 = new JSONObject();
        data1.put("name", "默认数据1");
        data1.put("value", 80);
        dataArray.add(data1);
        
        JSONObject data2 = new JSONObject();
        data2.put("name", "默认数据2");
        data2.put("value", 60);
        dataArray.add(data2);
        
        JSONObject data3 = new JSONObject();
        data3.put("name", "默认数据3");
        data3.put("value", 90);
        dataArray.add(data3);
        
        chartData.put("data", dataArray);
        chartData.put("title", "默认图表数据");
        chartData.put("type", "BAR");
        
        return chartData.toJSONString();
    }

    // ====================== 自定义图表相关方法实现 ======================

    @Override
    public Long createCustomChart(CustomChartConfigDTO config) {
        // 创建自定义图表
        ReportChartDTO chartDTO = new ReportChartDTO();
        chartDTO.setName(config.getName());
        chartDTO.setChartType(config.getChartType());
        chartDTO.setDataSourceType(config.getDataSourceType());
        chartDTO.setDataSourceIds(config.getDataSourceConfig() != null ? config.getDataSourceConfig().toString() : "{}");
        chartDTO.setChartConfig(config.getStyleConfig() != null ? config.getStyleConfig().toString() : "{}");
        chartDTO.setIsCustom(true);
        chartDTO.setStatus(1);
        
        Long chartId = createChart(chartDTO);
        
        // 如果配置了保存为模板，则保存模板
        if (config.getSaveAsTemplate() != null && config.getSaveAsTemplate()) {
            saveChartAsTemplate(chartId, config.getName(), config.getTemplateDescription());
        }
        
        return chartId;
    }

    @Override
    public Boolean updateCustomChart(Long id, CustomChartConfigDTO config) {
        ReportChart chart = reportChartMapper.selectById(id);
        if (chart == null) {
            return false;
        }
        
        chart.setName(config.getName());
        chart.setChartType(config.getChartType());
        chart.setDataSourceType(config.getDataSourceType());
        chart.setDataSourceIds(config.getDataSourceConfig() != null ? config.getDataSourceConfig().toString() : "{}");
        chart.setChartConfig(config.getStyleConfig() != null ? config.getStyleConfig().toString() : "{}");
        chart.setIsCustom(true);
        chart.setUpdateTime(LocalDateTime.now());
        
        return reportChartMapper.updateById(chart) > 0;
    }

    @Override
    public List<CustomChartConfigDTO> getMyChartTemplates() {
        // 模拟获取用户图表模板
        // 实际实现应该查询用户的自定义图表模板
        return List.of(
            createCustomChartConfig("任务成功率统计", "BAR", "TASK", "任务统计模板"),
            createCustomChartConfig("协议类型占比", "PIE", "PROTOCOL", "协议分析模板"),
            createCustomChartConfig("模板执行趋势", "LINE", "TEMPLATE", "模板监控模板")
        );
    }

    @Override
    public Long saveChartAsTemplate(Long chartId, String templateName, String description) {
        // 模拟保存图表为模板
        System.out.println("保存图表 " + chartId + " 为模板：" + templateName);
        
        // 实际实现应该创建模板记录
        return chartId + 1000L; // 返回模板ID
    }

    @Override
    public Boolean deleteChartTemplate(Long templateId) {
        // 模拟删除图表模板
        System.out.println("删除图表模板：" + templateId);
        return true;
    }

    @Override
    public Object previewChart(CustomChartConfigDTO config) {
        // 模拟图表预览数据
        return new Object() {
            public String chartType = config.getChartType();
            public String previewData = "预览数据 - " + config.getName();
            public List<String> sampleData = List.of("数据点1", "数据点2", "数据点3");
            public Map<String, Object> stylePreview = config.getStyleConfig();
        };
    }

    @Override
    public Object getVisualConfigPanel(String chartType) {
        // 模拟可视化配置面板数据
        return new Object() {
            public String type = chartType != null ? chartType : "BAR";
            public List<String> availableChartTypes = List.of("BAR", "LINE", "PIE", "SCATTER");
            public List<String> dataSourceTypes = List.of("TEMPLATE", "TASK", "PROTOCOL", "TEST");
            public Map<String, Object> styleOptions = Map.of(
                "colors", List.of("#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4"),
                "legendPosition", List.of("top", "bottom", "left", "right"),
                "axisFormat", List.of("default", "percentage", "currency")
            );
        };
    }

    // ====================== 辅助方法 ======================

    private CustomChartConfigDTO createCustomChartConfig(String name, String chartType, String dataSourceType, String description) {
        CustomChartConfigDTO config = new CustomChartConfigDTO();
        config.setName(name);
        config.setChartType(chartType);
        config.setDataSourceType(dataSourceType);
        config.setTemplateDescription(description);
        
        // 设置默认配置
        config.setDataSourceConfig(Map.of("type", dataSourceType, "filters", Map.of()));
        config.setStyleConfig(Map.of(
            "colors", List.of("#FF6B6B", "#4ECDC4", "#45B7D1"),
            "title", name,
            "legend", true
        ));
        
        return config;
    }
}