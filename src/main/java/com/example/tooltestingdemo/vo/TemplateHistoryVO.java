package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板历史版本 VO
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/vo/TemplateHistoryVO.java
 */
@Data
public class TemplateHistoryVO {

    private Long id;

    private Long templateId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本类型
     */
    private String versionType;

    /**
     * 变更摘要
     */
    private String changeSummary;

    /**
     * 变更详情
     */
    private String changeDetails;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 是否可回滚
     */
    private Integer canRollback;

    /**
     * 回滚时间点
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rollbackToTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
