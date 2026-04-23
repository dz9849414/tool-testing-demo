package com.example.tooltestingdemo.dto.report;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 失败时间线DTO
 */
@Data
public class FailureTimelineDTO {
    
    /** 时间点 */
    private LocalDateTime timestamp;
    
    /** 失败次数 */
    private Integer failureCount;
    
    /** 失败原因 */
    private String failureReason;
    
    /** 错误代码 */
    private String errorCode;
    
    /** 模板名称 */
    private String templateName;
    
    /** 执行时长（毫秒） */
    private Long durationMs;
    
    /** 请求URL */
    private String requestUrl;
    
    /** 响应状态码 */
    private Integer responseStatusCode;
    
    /** 响应消息 */
    private String responseMessage;
}