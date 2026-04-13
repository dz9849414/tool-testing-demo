package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogService operationLogService;

    /**
     * 获取指定用户的操作日志列表
     * 功能描述：系统集中展示指定用户的操作日志，包括登录、配置修改、任务执行等行为
     * 输入：用户ID、时间范围（可选）
     * 输出：结构化操作日志列表（含时间、操作类型、详情）
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("@securityService.hasPermission('system:log:api') or @securityService.isCurrentUser(#userId)")
    public Result<List<SysOperationLog>> getUserOperationLogs(
            @PathVariable String userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        LocalDateTime start = null;
        LocalDateTime end = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析时间范围参数
        if (startTime != null) {
            try {
                start = LocalDateTime.parse(startTime, formatter);
            } catch (Exception e) {
                return Result.error(ErrorStatus.BAD_REQUEST, "开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }

        if (endTime != null) {
            try {
                end = LocalDateTime.parse(endTime, formatter);
            } catch (Exception e) {
                return Result.error(ErrorStatus.BAD_REQUEST, "结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }

        List<SysOperationLog> logs = operationLogService.getOperationLogsByUserIdAndTimeRange(userId, start, end);
        return Result.success("获取用户操作日志成功", logs);
    }

    /**
     * 分页获取操作日志列表
     */
    @GetMapping("/page")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<Page<SysOperationLog>> getOperationLogsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String module) {

        LocalDateTime start = null;
        LocalDateTime end = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 解析时间范围参数
        if (startTime != null) {
            try {
                start = LocalDateTime.parse(startTime, formatter);
            } catch (Exception e) {
                return Result.error(ErrorStatus.BAD_REQUEST, "开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }

        if (endTime != null) {
            try {
                end = LocalDateTime.parse(endTime, formatter);
            } catch (Exception e) {
                return Result.error(ErrorStatus.BAD_REQUEST, "结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }

        Page<SysOperationLog> pageParam = new Page<>(page, size);
        Page<SysOperationLog> logs = operationLogService.getOperationLogsByPage(pageParam, userId, start, end, module);
        return Result.success("获取操作日志列表成功", logs);
    }

    /**
     * 获取最近的操作日志
     */
    @GetMapping("/recent")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<List<SysOperationLog>> getRecentOperationLogs(
            @RequestParam(defaultValue = "10") int limit) {

        List<SysOperationLog> logs = operationLogService.getRecentOperationLogs(limit);
        return Result.success("获取最近操作日志成功", logs);
    }

    /**
     * 根据模块获取操作日志
     */
    @GetMapping("/module")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<List<SysOperationLog>> getOperationLogsByModule(
            @RequestParam String module) {
        List<SysOperationLog> logs = operationLogService.getOperationLogsByModule(module);
        return Result.success("获取模块操作日志成功", logs);
    }
    
    /**
     * 根据角色ID获取操作日志（支持分页和搜索）
     * 功能描述：系统集中展示指定角色的操作日志，包括登录、配置修改、任务执行等行为
     * 输入：角色ID、时间范围（可选）、模块（可选）、页码、每页大小
     * 输出：分页的结构化操作日志列表（含时间、操作类型、详情）
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<Page<SysOperationLog>> getRoleOperationLogs(
            @PathVariable String roleId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 解析时间范围参数
        if (startTime != null) {
            try {
                start = LocalDateTime.parse(startTime, formatter);
            } catch (Exception e) {
                return Result.error(ErrorStatus.BAD_REQUEST, "开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        
        if (endTime != null) {
            try {
                end = LocalDateTime.parse(endTime, formatter);
            } catch (Exception e) {
                return Result.error(ErrorStatus.BAD_REQUEST, "结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
        }
        
        Page<SysOperationLog> pageParam = new Page<>(page, size);
        Page<SysOperationLog> logs = operationLogService.getOperationLogsByRoleIdAndPage(pageParam, roleId, start, end, module);
        return Result.success("获取角色操作日志成功", logs);
    }
}