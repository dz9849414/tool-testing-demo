package com.example.tooltestingdemo.dto.template;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量生成模板任务请求
 */
@Data
public class TemplateJobGenerateRequest {

    /**
     * 随机创建时间开始
     */
    private LocalDateTime startTime;

    /**
     * 随机创建时间结束
     */
    private LocalDateTime endTime;

    /**
     * 生成条数
     */
    private Integer count;

    /**
     * 指定生成的系统模块，可为空
     */
    private List<String> systemModules;

    /**
     * 指定生成的操作类型，可为空
     */
    private List<String> operationTypes;
}
