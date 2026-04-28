package com.example.tooltestingdemo.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量移除操作结果
 */
@Data
@Schema(description = "批量移除操作结果")
public class BatchRemoveResult {
    
    @Schema(description = "操作是否成功")
    private Boolean success;
    
    @Schema(description = "成功移除的数量")
    private Integer removedCount;
    
    @Schema(description = "总共处理的数量")
    private Integer processedCount;
    
    @Schema(description = "失败原因列表")
    private List<String> failureReasons;
    
    @Schema(description = "操作结果消息")
    private String message;
    
    /**
     * 获取失败数量
     */
    public Integer getFailureCount() {
        return failureReasons != null ? failureReasons.size() : 0;
    }
    
    /**
     * 获取成功率
     */
    public Double getSuccessRate() {
        if (processedCount == null || processedCount == 0) {
            return 0.0;
        }
        return (double) removedCount / processedCount * 100;
    }
}