package com.example.tooltestingdemo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志查询响应VO
 */
@Data
public class SysOperationLogVO {
    private String id;
    private String userId;
    private String username;
    private String roleId;
    private String operation;
    private String module;
    private String moduleDisplayName;
    private String method;
    private String requestUrl;
    private String requestParams;
    private String description;
    private String ipAddress;
    private String userAgent;
    private Integer status;
    private String errorMessage;
    private Long executeTime;
    private LocalDateTime createTime;
}
