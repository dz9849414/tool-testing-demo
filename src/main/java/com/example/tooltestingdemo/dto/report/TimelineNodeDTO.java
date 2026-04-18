package com.example.tooltestingdemo.dto.report;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 时间线节点DTO
 */
@Data
public class TimelineNodeDTO {
    
    /** 节点ID */
    private String id;
    
    /** 节点时间 */
    private LocalDateTime time;
    
    /** 节点标题 */
    private String title;
    
    /** 节点内容 */
    private String content;
    
    /** 操作人 */
    private String operator;
    
    /** 节点类型：TASK_EXECUTE/TEMPLATE_UPDATE/PROTOCOL_TEST */
    private String nodeType;
    
    /** 节点状态：SUCCESS/FAILED/INFO */
    private String status;
    
    /** 关联的业务对象ID */
    private String relatedObjectId;
    
    /** 关联的业务对象名称 */
    private String relatedObjectName;
    
    /** 详细信息 */
    private Map<String, Object> details;
    
    /** 是否可以展开详情 */
    private Boolean expandable;
}