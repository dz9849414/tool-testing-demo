package com.example.tooltestingdemo.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报告模板实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("report_template")
public class ReportTemplate {
    
    @TableId(value = "id", type = IdType.AUTO)
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
    
    /** 章节结构（JSON格式存储） */
    private String chapterStructure;
    
    /** 模板内容（用于前端展示） */
    private String content;
    
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
    
    /** 创建人ID */
    private Long createId;
    
    /** 创建人姓名 */
    private String createName;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /** 修改人ID */
    private Long updateId;
    
    /** 修改人姓名 */
    private String updateName;
    
    /** 修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /** 是否删除 */
    private Integer isDeleted;
}