package com.example.tooltestingdemo.dto.report;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 自定义图表配置DTO
 */
@Data
public class CustomChartConfigDTO {
    
    /** 图表名称 */
    private String name;
    
    /** 图表类型：BAR/LINE/PIE/SCATTER */
    private String chartType;
    
    /** 数据源类型 */
    private String dataSourceType;
    
    /** 数据源配置 */
    private Map<String, Object> dataSourceConfig;
    
    /** 分组维度 */
    private List<String> groupBy;
    
    /** 指标字段 */
    private List<String> metrics;
    
    /** 筛选条件 */
    private Map<String, Object> filters;
    
    /** 图表样式配置 */
    private Map<String, Object> styleConfig;
    
    /** 是否保存为模板 */
    private Boolean saveAsTemplate;
    
    /** 模板描述 */
    private String templateDescription;
}