package com.example.tooltestingdemo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板定时任务日志 VO（结构化展示）
 */
@Data
public class TemplateJobLogVO {

    private Long id;

    private Long jobId;

    /**
     * 任务名称（关联查询填充）
     */
    private String jobName;

    /**
     * 首个模板ID
     */
    private Long templateId;

    /**
     * 是否成功：0-否 1-是
     */
    private Integer success;

    /**
     * 执行耗时（ms）
     */
    private Long durationMs;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行结果摘要（如：2个成功，1个失败）
     */
    private String resultSummary;

    /**
     * 每个模板的详细执行结果
     */
    private List<TemplateJobLogItemVO> results;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
