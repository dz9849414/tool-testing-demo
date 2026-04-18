package com.example.tooltestingdemo.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告图表DTO
 */
@Data
public class ReportChartDTO {
    
    private Long id;
    
    /** 图表名称 */
    private String name;
    
    /** 图表描述 */
    private String description;
    
    /** 图表类型：BAR/LINE/PIE/SCATTER/RADAR */
    private String chartType;
    
    /** 数据源类型：TEMPLATE/TASK/PROTOCOL */
    private String dataSourceType;
    
    /** 数据源ID */
    private String dataSourceIds;
    
    /** 图表配置（JSON格式存储） */
    private String chartConfig;
    
    /** 样式配置 */
    private String styleConfig;
    
    /** 图表数据（JSON格式存储） */
    private String chartData;
    
    /** 是否为自定义图表 */
    private Boolean isCustom;
    
    /** 是否公开 */
    private Boolean isPublic;
    
    /** 图表分组 */
    private String chartGroup;
    
    /** 使用次数 */
    private Integer usageCount;
    
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    
    /** 创建人姓名 */
    private String createName;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 修改时间 */
    private LocalDateTime updateTime;
}