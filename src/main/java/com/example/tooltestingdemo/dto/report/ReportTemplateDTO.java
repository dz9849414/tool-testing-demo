package com.example.tooltestingdemo.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报告模板DTO
 */
@Data
public class ReportTemplateDTO {
    
    private Long id;
    
    /** 模板名称 */
    private String name;
    
    /** 模板描述 */
    private String description;
    
    /** 模板类型：STATISTICAL/ANALYTICAL/ARCHIVAL */
    private String templateType;
    
    /** 适用场景 */
    private String applicableScene;
    
    /** 模板结构（JSON格式存储） */
    private String templateStructure;
    
    /** 模板样式配置 */
    private String styleConfig;
    
    /** 是否系统预设模板 */
    private Boolean isSystemTemplate;
    
    /** 是否公开模板 */
    private Boolean isPublic;
    
    /** 关联的业务对象类型 */
    private String relatedBusinessType;
    
    /** 使用次数 */
    private Integer usageCount;
    
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    
    /** 排序序号 */
    private Integer sortOrder;
    
    /** 预览图路径 */
    private String previewImage;
    
    /** 创建人姓名 */
    private String createName;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 修改时间 */
    private LocalDateTime updateTime;
}