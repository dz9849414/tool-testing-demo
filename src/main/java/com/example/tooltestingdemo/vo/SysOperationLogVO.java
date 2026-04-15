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
    private String operation;
    private String module;
    private String description;
    private String ipAddress;
    private LocalDateTime createTime;
}