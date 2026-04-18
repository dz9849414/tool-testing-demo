package com.example.tooltestingdemo.dto.report;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 测试结果表格数据DTO
 */
@Data
public class TestResultTableDTO {
    
    /** 测试记录ID */
    private String id;
    
    /** 测试类型：PROTOCOL_TEST/TEMPLATE_EXECUTE */
    private String testType;
    
    /** 测试名称 */
    private String name;
    
    /** 测试状态：SUCCESS/FAILED/RUNNING */
    private String status;
    
    /** 执行时间 */
    private LocalDateTime executeTime;
    
    /** 执行人 */
    private String executor;
    
    /** 执行耗时（毫秒） */
    private Long duration;
    
    /** 成功率 */
    private Double successRate;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 关联的业务对象ID */
    private String relatedObjectId;
    
    /** 关联的业务对象名称 */
    private String relatedObjectName;
    
    /** 扩展字段 */
    private Map<String, Object> extraFields;
}