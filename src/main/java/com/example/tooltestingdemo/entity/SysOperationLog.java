package com.example.tooltestingdemo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@TableName("pdm_tool_sys_operation_log")
@Data
public class SysOperationLog {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    @TableField(value = "trace_id")
    private String traceId;
    
    @TableField(value = "user_id")
    private String userId;
    
    @TableField(value = "username")
    private String username;
    
    @TableField(value = "role_id")
    private String roleId;
    
    @TableField(value = "operation")
    private String operation;
    
    @TableField(value = "module")
    private String module;
    
    @TableField(value = "method")
    private String method;
    
    @TableField(value = "method_json")
    private String methodJson;
    
    @TableField(value = "request_url")
    private String requestUrl;
    
    @TableField(value = "request_params")
    private String requestParams;
    
    @TableField(value = "ip_address")
    private String ipAddress;
    
    @TableField(value = "user_agent")
    private String userAgent;
    
    @TableField(value = "status")
    private Integer status = 1;
    
    @TableField(value = "error_message")
    private String errorMessage;
    
    @TableField(value = "execute_time")
    private Long executeTime;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}