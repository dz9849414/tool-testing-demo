package com.example.tooltestingdemo.dto.report;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 报告预览DTO
 */
@Data
public class ReportPreviewDTO {
    
    /** 报告ID */
    private Long reportId;
    
    /** 报告名称 */
    private String reportName;
    
    /** 预览内容（HTML格式） */
    private String previewContent;
    
    /** PDF预览URL */
    private String pdfPreviewUrl;
    
    /** 总页数 */
    private Integer totalPages;
    
    /** 当前页码 */
    private Integer currentPage;
    
    /** 缩放比例 */
    private Double scale;
    
    /** 标注信息 */
    private List<AnnotationDTO> annotations;
    
    /** 预览配置 */
    private Map<String, Object> previewConfig;
    
    /**
     * 标注信息DTO
     */
    @Data
    public static class AnnotationDTO {
        
        /** 标注ID */
        private String id;
        
        /** 标注类型：TEXT/ARROW/RECTANGLE */
        private String type;
        
        /** 标注内容 */
        private String content;
        
        /** 标注位置 */
        private Map<String, Object> position;
        
        /** 标注颜色 */
        private String color;
        
        /** 创建人 */
        private String creator;
        
        /** 创建时间 */
        private String createTime;
    }
}