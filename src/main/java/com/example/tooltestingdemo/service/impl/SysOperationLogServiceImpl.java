package com.example.tooltestingdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.mapper.SysOperationLogMapper;
import com.example.tooltestingdemo.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 操作日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    private final SysOperationLogMapper operationLogMapper;

    @Override
    public void recordOperationLog(SysOperationLog operationLog) {
        // 设置日志ID
        if (operationLog.getId() == null) {
            operationLog.setId(UUID.randomUUID().toString().replace("-", "_"));
        }

        // 保存操作日志
        save(operationLog);
    }

    @Override
    public List<SysOperationLog> getOperationLogsByUserId(String userId) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getUserId, userId)
                .orderByDesc(SysOperationLog::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<SysOperationLog> getOperationLogsByUserIdAndTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getUserId, userId);

        if (startTime != null) {
            queryWrapper.ge(SysOperationLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(SysOperationLog::getCreateTime, endTime);
        }

        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public Page<SysOperationLog> getOperationLogsByPage(Page<SysOperationLog> page, String userId, LocalDateTime startTime, LocalDateTime endTime, String module) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            queryWrapper.eq(SysOperationLog::getUserId, userId);
        }

        if (startTime != null) {
            queryWrapper.ge(SysOperationLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(SysOperationLog::getCreateTime, endTime);
        }

        if (module != null) {
            queryWrapper.eq(SysOperationLog::getModule, module);
        }

        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public List<SysOperationLog> getOperationLogsByModule(String module) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getModule, module)
                .orderByDesc(SysOperationLog::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<SysOperationLog> getRecentOperationLogs(Integer limit) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SysOperationLog::getCreateTime)
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }
}