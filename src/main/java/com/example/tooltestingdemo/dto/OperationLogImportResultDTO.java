package com.example.tooltestingdemo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志导入结果DTO
 */
@Data
public class OperationLogImportResultDTO {
    
    private String batchId;
    
    private int totalCount;
    
    private int successCount;
    
    private int failureCount;
    
    private int businessExecuteCount;
    
    private List<FailureDetail> failures = new ArrayList<>();
    
    @Data
    public static class FailureDetail {
        private int rowIndex;
        private String operation;
        private String module;
        private String errorMessage;
    }
}