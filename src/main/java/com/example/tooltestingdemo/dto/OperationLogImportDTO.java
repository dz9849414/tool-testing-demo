package com.example.tooltestingdemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志导入DTO
 */
@Data
public class OperationLogImportDTO {
    
    private String id;
    
    private String traceId;
    
    private String userId;
    
    private String username;
    
    private String roleId;
    
    private String module;
    
    private String operation;
    
    private String method;
    
    private String methodJson;
    
    private String requestUrl;
    
    private String requestParams;
    
    private String ipAddress;
    
    private String userAgent;
    
    private LocalDateTime createTime;
}