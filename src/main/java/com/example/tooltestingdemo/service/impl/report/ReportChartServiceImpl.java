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
import java.util.ArrayList;
import java.util.HashMap;
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
        
        // 验证chartConfig是否为有效JSON，如果不是则使用默认值
        String validChartConfig = validateJsonConfig(chartConfig);
        chart.setChartConfig(validChartConfig);
        
        // 验证styleConfig是否为有效JSON，如果不是则使用默认值
        String validStyleConfig = validateJsonConfig(styleConfig);
        chart.setStyleConfig(validStyleConfig);
        
        chart.setIsCustom(true);
        chart.setUpdateTime(LocalDateTime.now());
        
        return reportChartMapper.updateById(chart) > 0;
    }
    
    /**
     * 验证JSON配置，确保是有效的JSON格式
     */
    private String validateJsonConfig(String config) {
        if (config == null || config.trim().isEmpty()) {
            return "{}";
        }
        
        String trimmedConfig = config.trim();
        
        // 检查是否是有效的JSON对象或数组
        if (trimmedConfig.startsWith("{") && trimmedConfig.endsWith("}")) {
            try {
                JSON.parseObject(trimmedConfig);
                return trimmedConfig;
            } catch (Exception e) {
                log.warn("无效的JSON对象配置: {}, 使用默认值");
                return "{}";
            }
        } else if (trimmedConfig.startsWith("[") && trimmedConfig.endsWith("]")) {
            try {
                JSON.parseArray(trimmedConfig);
                return trimmedConfig;
            } catch (Exception e) {
                log.warn("无效的JSON数组配置: {}, 使用默认值");
                return "{}";
            }
        } else {
            log.warn("配置不是有效的JSON格式: {}, 使用默认值");
            return "{}";
        }
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
    public Object analyzeChartData(Long id, String startTime, String endTime, String timeRange) {
        try {
            // 如果id为0，只根据时间区间查询数据
            if (id == null || id == 0) {
                return analyzeDataByTimeRange(startTime, endTime, timeRange);
            }
            
            // 获取图表信息
            ReportChart chart = reportChartMapper.selectById(id);
            if (chart == null || chart.getIsDeleted() == 1) {
                throw new RuntimeException("图表不存在");
            }
            
            // 根据数据源类型和时间范围分析数据
            String dataSourceType = chart.getDataSourceType();
            String chartData = null;
            
            if ("PROTOCOL".equals(dataSourceType)) {
                // 协议数据源：分析协议测试数据
                chartData = analyzeProtocolTestData(chart, startTime, endTime, timeRange);
            } else if ("TEMPLATE".equals(dataSourceType)) {
                // 模板数据源：分析模板执行数据
                chartData = analyzeTemplateExecuteData(chart, startTime, endTime, timeRange);
            } else if ("REPORT".equals(dataSourceType)) {
                // 报告数据源：分析报告内容数据
                chartData = analyzeReportContentData(chart, startTime, endTime, timeRange);
            } else {
                // 默认数据源：使用模拟数据
                chartData = generateDefaultChartData(startTime, endTime, timeRange);
            }
            
            // 更新图表数据
            chart.setChartData(chartData);
            reportChartMapper.updateById(chart);
            
            // 返回分析结果
            final String finalChartData = chartData;
            final String finalDataSourceType = dataSourceType;
            final String finalTimePeriod = calculateTimePeriod(startTime, endTime, timeRange);
            
            return new Object() {
                public Long chartId = id;
                public String analysisResult = "数据分析完成";
                public String chartData = finalChartData;
                public String message = "数据已成功分析并更新到图表";
                public String timePeriod = finalTimePeriod;
                public String dataSourceType = finalDataSourceType;
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
    private String analyzeProtocolTestData(ReportChart chart, String startTime, String endTime, String timeRange) {
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
    private String analyzeTemplateExecuteData(ReportChart chart, String startTime, String endTime, String timeRange) {
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
    private String analyzeReportContentData(ReportChart chart, String startTime, String endTime, String timeRange) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Report::getIsDeleted, 0);
            
            // 时间范围筛选
            if (startTime != null && endTime != null) {
                queryWrapper.between(Report::getCreateTime, startTime, endTime);
            } else if (timeRange != null) {
                // 根据预定义时间范围筛选
                LocalDateTime[] dateRange = calculateDateRange(timeRange);
                if (dateRange != null) {
                    queryWrapper.between(Report::getCreateTime, dateRange[0], dateRange[1]);
                }
            }
            
            // 获取数据源ID
            String dataSourceIds = chart.getDataSourceIds();
            List<Report> reports = new ArrayList<>();
            
            if (dataSourceIds != null && !dataSourceIds.trim().isEmpty()) {
                // 解析数据源ID（JSON数组格式）
                JSONArray dataSourceArray = JSON.parseArray(dataSourceIds);
                if (!dataSourceArray.isEmpty()) {
                    // 根据数据源ID列表获取报告
                    List<Long> reportIds = dataSourceArray.toJavaList(Long.class);
                    queryWrapper.in(Report::getId, reportIds);
                    reports = reportMapper.selectList(queryWrapper);
                }
            } else {
                // 如果没有指定数据源ID，获取所有符合条件的报告
                reports = reportMapper.selectList(queryWrapper);
            }
            
            if (reports.isEmpty()) {
                return generateDefaultChartData(startTime, endTime, timeRange);
            }
            
            JSONObject chartData = new JSONObject();
            JSONArray dataArray = new JSONArray();
            List<String> xAxisData = new ArrayList<>();
            
            // 添加时间范围信息到图表数据
            String timePeriodDesc = calculateTimePeriod(startTime, endTime, timeRange);
            chartData.put("timePeriod", timePeriodDesc);
            
            // 分析多个报告的数据
            for (Report report : reports) {
                if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    try {
                        JSONObject reportContent = JSON.parseObject(report.getContent());
                        
                        if (reportContent.containsKey("summary")) {
                            JSONObject summary = reportContent.getJSONObject("summary");
                            
                            // 添加X轴数据（报告名称）
                            xAxisData.add(report.getName());
                            
                            // 成功率数据
                            JSONObject successData = new JSONObject();
                            successData.put("name", report.getName());
                            successData.put("value", summary.getDouble("successRate"));
                            successData.put("color", "#52c41a");
                            dataArray.add(successData);
                        }
                    } catch (Exception e) {
                        log.warn("解析报告内容失败: {}");
                    }
                }
            }
            
            // 构建图表数据结构
            chartData.put("xAxis", xAxisData);
            chartData.put("data", dataArray);
            chartData.put("title", chart.getName() + " - " + timePeriodDesc);
            chartData.put("type", chart.getChartType());
            chartData.put("reportCount", reports.size());
            
            return chartData.toJSONString();
            
        } catch (Exception e) {
            log.error("分析报告内容数据失败", e);
            return generateDefaultChartData(startTime, endTime, timeRange);
        }
    }
    
    /**
     * 生成默认图表数据
     */
    private String generateDefaultChartData(String startTime, String endTime, String timeRange) {
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

    /**
     * 计算时间范围描述
     */
    private String calculateTimePeriod(String startTime, String endTime, String timeRange) {
        if (startTime != null && endTime != null) {
            return startTime + " 至 " + endTime;
        } else if (timeRange != null && !timeRange.trim().isEmpty()) {
            switch (timeRange.toLowerCase()) {
                case "today":
                    return "今天";
                case "yesterday":
                    return "昨天";
                case "week":
                    return "本周";
                case "month":
                    return "本月";
                case "quarter":
                    return "本季度";
                case "year":
                    return "本年";
                default:
                    return timeRange;
            }
        } else {
            return "全部时间";
        }
    }
    
    /**
     * 计算日期范围
     */
    private LocalDateTime[] calculateDateRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        LocalDateTime endTime = now;
        
        switch (timeRange.toLowerCase()) {
            case "today":
                startTime = now.toLocalDate().atStartOfDay();
                break;
            case "yesterday":
                startTime = now.toLocalDate().minusDays(1).atStartOfDay();
                endTime = now.toLocalDate().atStartOfDay().minusSeconds(1);
                break;
            case "week":
                startTime = now.toLocalDate().minusDays(6).atStartOfDay();
                break;
            case "month":
                startTime = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
                break;
            case "quarter":
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                int startMonth = (currentQuarter - 1) * 3 + 1;
                startTime = LocalDateTime.of(now.getYear(), startMonth, 1, 0, 0);
                break;
            case "year":
                startTime = LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
                break;
            default:
                return null; // 不支持的时间范围
        }
        
        return new LocalDateTime[]{startTime, endTime};
    }
    
    /**
     * 根据时间范围分析数据（id为0时使用）
     */
    private Object analyzeDataByTimeRange(String startTime, String endTime, String timeRange) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Report::getIsDeleted, 0);
            
            // 时间范围筛选
            if (startTime != null && endTime != null) {
                queryWrapper.between(Report::getCreateTime, startTime, endTime);
            } else if (timeRange != null) {
                // 根据预定义时间范围筛选
                LocalDateTime[] dateRange = calculateDateRange(timeRange);
                if (dateRange != null) {
                    queryWrapper.between(Report::getCreateTime, dateRange[0], dateRange[1]);
                }
            }
            
            // 获取符合条件的报告
            List<Report> reports = reportMapper.selectList(queryWrapper);
            
            if (reports.isEmpty()) {
                JSONObject emptyData = new JSONObject();
                return createTimeRangeResult(startTime, endTime, timeRange, emptyData, "无数据");
            }
            
            // 分析报告数据
            JSONObject chartData = new JSONObject();
            JSONArray dataArray = new JSONArray();
            List<String> xAxisData = new ArrayList<>();
            
            // 统计不同类型的数据
            Map<String, Integer> reportTypeCount = new HashMap<>();
            Map<String, Double> successRateByType = new HashMap<>();
            Map<String, Integer> totalCountByType = new HashMap<>();
            
            for (Report report : reports) {
                String reportType = report.getReportType() != null ? report.getReportType() : "未知类型";
                
                // 统计报告类型数量
                reportTypeCount.put(reportType, reportTypeCount.getOrDefault(reportType, 0) + 1);
                
                // 分析报告内容
                if (report.getContent() != null && !report.getContent().trim().isEmpty()) {
                    try {
                        JSONObject reportContent = JSON.parseObject(report.getContent());
                        if (reportContent.containsKey("summary")) {
                            JSONObject summary = reportContent.getJSONObject("summary");
                            
                            // 累计成功率
                            double successRate = summary.getDouble("successRate");
                            successRateByType.put(reportType, 
                                successRateByType.getOrDefault(reportType, 0.0) + successRate);
                                
                            // 累计总执行次数
                            int totalCount = summary.getInteger("totalCount");
                            totalCountByType.put(reportType, 
                                totalCountByType.getOrDefault(reportType, 0) + totalCount);
                        }
                    } catch (Exception e) {
                        log.warn("解析报告内容失败: {}");
                    }
                }
            }
            
            // 构建图表数据
            for (Map.Entry<String, Integer> entry : reportTypeCount.entrySet()) {
                String reportType = entry.getKey();
                int count = entry.getValue();
                
                JSONObject dataItem = new JSONObject();
                dataItem.put("name", reportType);
                dataItem.put("value", count);
                dataItem.put("color", getColorByIndex(xAxisData.size()));
                
                // 计算平均成功率
                if (successRateByType.containsKey(reportType)) {
                    double avgSuccessRate = successRateByType.get(reportType) / count;
                    dataItem.put("successRate", String.format("%.2f%%", avgSuccessRate));
                }
                
                // 添加总执行次数
                if (totalCountByType.containsKey(reportType)) {
                    dataItem.put("totalCount", totalCountByType.get(reportType));
                }
                
                dataArray.add(dataItem);
                xAxisData.add(reportType);
            }
            
            // 构建图表数据结构
            chartData.put("xAxis", xAxisData);
            chartData.put("data", dataArray);
            chartData.put("title", "时间范围分析 - " + calculateTimePeriod(startTime, endTime, timeRange));
            chartData.put("type", "BAR");
            chartData.put("reportCount", reports.size());
            chartData.put("reportTypes", reportTypeCount.size());
            
            return createTimeRangeResult(startTime, endTime, timeRange, chartData, "时间范围数据分析完成");
            
        } catch (Exception e) {
            log.error("时间范围数据分析失败", e);
            JSONObject errorData = new JSONObject();
            return createTimeRangeResult(startTime, endTime, timeRange, errorData, "数据分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建时间范围分析结果
     */
    private Object createTimeRangeResult(String startTime, String endTime, String timeRange, Object chartData, String message) {
        final Object finalChartData = chartData;
        final String finalTimePeriod = calculateTimePeriod(startTime, endTime, timeRange);
        final String finalMessage = message;
        
        return new Object() {
            public Long chartId = 0L;
            public String analysisResult = "时间范围数据分析完成";
            public Object chartData = finalChartData;
            public String message = finalMessage;
            public String timePeriod = finalTimePeriod;
            public String dataSourceType = "TIME_RANGE";
            public String mode = "time_range_only";
        };
    }
    
    /**
     * 根据索引获取颜色
     */
    private String getColorByIndex(int index) {
        String[] colors = {"#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de", "#3ba272", "#fc8452", "#9a60b4"};
        return colors[index % colors.length];
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