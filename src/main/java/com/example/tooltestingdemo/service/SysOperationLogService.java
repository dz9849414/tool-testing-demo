package com.example.tooltestingdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.entity.SysOperationLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务接口
 */
public interface SysOperationLogService {

    /**
     * 记录操作日志
     */
    void recordOperationLog(SysOperationLog operationLog);

    /**
     * 根据用户ID查询操作日志
     */
    List<SysOperationLog> getOperationLogsByUserId(String userId);

    /**
     * 根据用户ID和时间范围查询操作日志
     */
    List<SysOperationLog> getOperationLogsByUserIdAndTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 分页查询操作日志
     */
    Page<SysOperationLog> getOperationLogsByPage(Page<SysOperationLog> page, String userId, LocalDateTime startTime, LocalDateTime endTime, String module);

    /**
     * 根据模块查询操作日志
     */
    List<SysOperationLog> getOperationLogsByModule(String module);

    /**
     * 查询最近的操作日志
     */
    List<SysOperationLog> getRecentOperationLogs(Integer limit);
}