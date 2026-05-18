package com.example.tooltestingdemo.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.OperationLogImportDTO;
import com.example.tooltestingdemo.dto.OperationLogImportResultDTO;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.mapper.SysOperationLogMapper;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.example.tooltestingdemo.service.impl.MethodJsonReplayer;
import com.example.tooltestingdemo.util.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final MethodJsonReplayer methodJsonReplayer;

    @Override
    public void recordOperationLog(SysOperationLog operationLog) {
        if (operationLog.getId() == null) {
            operationLog.setId(UUID.randomUUID().toString().replace("-", "_"));
        }
        save(operationLog);
    }

    @Override
    public List<SysOperationLog> getOperationLogsByUserId(String userId) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getUserId, userId)
                .and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true))
                .orderByDesc(SysOperationLog::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<SysOperationLog> getOperationLogsByUserIdAndTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getUserId, userId);
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));

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
    public Page<SysOperationLog> getOperationLogsByUserIdAndTimeRange(Page<SysOperationLog> page, String userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getUserId, userId);
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));

        if (startTime != null) {
            queryWrapper.ge(SysOperationLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(SysOperationLog::getCreateTime, endTime);
        }

        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);
        return page(page, queryWrapper);
    }
    
    @Override
    public Page<SysOperationLog> getOperationLogsByPage(Page<SysOperationLog> page, String userId, String username, String operation, Integer status, LocalDateTime startTime, LocalDateTime endTime, String module) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));

        if (userId != null && !userId.isEmpty()) {
            queryWrapper.eq(SysOperationLog::getUserId, userId);
        }

        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysOperationLog::getUsername, username);
        }

        if (operation != null && !operation.isEmpty()) {
            queryWrapper.like(SysOperationLog::getOperation, operation);
        }

        if (status != null) {
            queryWrapper.eq(SysOperationLog::getStatus, status);
        }

        if (startTime != null) {
            queryWrapper.ge(SysOperationLog::getCreateTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(SysOperationLog::getCreateTime, endTime);
        }

        if (module != null && !module.isEmpty()) {
            queryWrapper.eq(SysOperationLog::getModule, module);
        }

        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public List<SysOperationLog> getOperationLogsByModule(String module) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getModule, module)
                .and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true))
                .orderByDesc(SysOperationLog::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<SysOperationLog> getRecentOperationLogs(Integer limit) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));
        queryWrapper.orderByDesc(SysOperationLog::getCreateTime)
                .last("LIMIT " + limit);
        return list(queryWrapper);
    }
    
    @Override
    public List<SysOperationLog> getOperationLogsByRoleId(String roleId) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));
        if (roleId != null) {
            queryWrapper.eq(SysOperationLog::getRoleId, roleId);
        } else {
            queryWrapper.isNull(SysOperationLog::getRoleId);
        }
        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);
        return list(queryWrapper);
    }
    
    @Override
    public List<SysOperationLog> getOperationLogsByRoleIdAndTimeRange(String roleId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));
        // 如果roleId为0或null，不添加roleId查询条件（查询所有角色）
        if (roleId != null && !roleId.isEmpty() && !"0".equals(roleId)) {
            queryWrapper.eq(SysOperationLog::getRoleId, roleId);
        }
        
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
    public Page<SysOperationLog> getOperationLogsByRoleIdAndPage(Page<SysOperationLog> page, String roleId, String username, String operation, Integer status, LocalDateTime startTime, LocalDateTime endTime, String module) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.isNull(SysOperationLog::getShowInSystemLog).or().eq(SysOperationLog::getShowInSystemLog, true));
        
        // 如果roleId为0或null，不添加roleId查询条件（查询所有角色）
        if (roleId != null && !roleId.isEmpty() && !"0".equals(roleId)) {
            queryWrapper.eq(SysOperationLog::getRoleId, roleId);
        }
        
        if (username != null && !username.isEmpty()) {
            queryWrapper.like(SysOperationLog::getUsername, username);
        }

        if (operation != null && !operation.isEmpty()) {
            queryWrapper.like(SysOperationLog::getOperation, operation);
        }

        if (status != null) {
            queryWrapper.eq(SysOperationLog::getStatus, status);
        }

        if (startTime != null) {
            queryWrapper.ge(SysOperationLog::getCreateTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(SysOperationLog::getCreateTime, endTime);
        }
        
        if (module != null && !module.isEmpty()) {
            queryWrapper.eq(SysOperationLog::getModule, module);
        }
        
        queryWrapper.orderByDesc(SysOperationLog::getCreateTime);
        return page(page, queryWrapper);
    }

    @Override
    public OperationLogImportResultDTO importOperationLogsFromExcel(InputStream inputStream, boolean executeRollback, boolean useMethodJson) throws IOException {
        List<OperationLogImportDTO> logs = new ArrayList<>();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                
                OperationLogImportDTO logDTO = new OperationLogImportDTO();
                logDTO.setId(getCellStringValue(row.getCell(0)));              // 列0: 日志ID
                logDTO.setTraceId(getCellStringValue(row.getCell(1)));        // 列1: 追踪ID
                logDTO.setUserId(getCellStringValue(row.getCell(2)));         // 列2: 用户ID
                logDTO.setUsername(getCellStringValue(row.getCell(3)));       // 列3: 用户名
                logDTO.setRoleId(getCellStringValue(row.getCell(4)));         // 列4: 角色ID
                logDTO.setModule(getCellStringValue(row.getCell(5)));         // 列5: 模块
                logDTO.setOperation(getCellStringValue(row.getCell(6)));      // 列6: 操作
                logDTO.setMethod(getCellStringValue(row.getCell(7)));         // 列7: 方法
                logDTO.setRequestUrl(getCellStringValue(row.getCell(8)));     // 列8: 请求URL
                logDTO.setRequestParams(getCellStringValue(row.getCell(9)));   // 列9: 请求参数
                logDTO.setIpAddress(getCellStringValue(row.getCell(10)));     // 列10: IP地址
                logDTO.setUserAgent(getCellStringValue(row.getCell(11)));     // 列11: 用户代理
                // 列12: 状态, 列13: 错误信息, 列14: 执行时间(ms) 暂时不需要导入
                logDTO.setCreateTime(parseDateTime(getCellStringValue(row.getCell(15))));  // 列15: 创建时间
                logDTO.setMethodJson(getCellStringValue(row.getCell(16)));     // 列16: 方法调用链
                
                if (isValidLog(logDTO)) {
                    logs.add(logDTO);
                }
            }
        }
        
        return importOperationLogs(logs, executeRollback, useMethodJson);
    }

    public OperationLogImportResultDTO importOperationLogs(List<OperationLogImportDTO> logs, boolean executeRollback, boolean useMethodJson) {
        String restoreBatchId = "RESTORE_" + TraceIdContext.generate();
        log.info("开始还原操作日志, batchId={}, count={}, useMethodJson={}", restoreBatchId, logs.size(), useMethodJson);
        
        OperationLogImportResultDTO result = new OperationLogImportResultDTO();
        result.setTotalCount(logs.size());
        result.setBatchId(restoreBatchId);
        
        for (int i = 0; i < logs.size(); i++) {
            OperationLogImportDTO logDTO = logs.get(i);
            String itemTraceId = restoreBatchId + "_" + i;
            TraceIdContext.set(itemTraceId);
            
            try {
                // 1. 处理日志记录的插入/更新
                processLogRecord(logDTO, itemTraceId);
                
                // 2. 处理业务还原（即使失败也继续执行后续记录）
                if (executeRollback) {
                    handleBusinessRollback(result, logDTO, itemTraceId, i, useMethodJson);
                } else {
                    // 不执行业务还原，日志记录成功即算成功
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }
                
            } catch (Exception e) {
                // 日志记录处理失败
                result.setFailureCount(result.getFailureCount() + 1);
                addFailureDetail(result, i, logDTO, "日志处理失败: " + e.getMessage());
                log.error("[{}] 处理日志失败: row={}, operation={}, module={}, error={}", 
                        itemTraceId, i + 1, logDTO.getOperation(), logDTO.getModule(), e.getMessage());
            } finally {
                TraceIdContext.clear();
            }
        }
        
        log.info("还原操作日志完成, batchId={}, success={}, failure={}, businessExecute={}", 
                restoreBatchId, result.getSuccessCount(), result.getFailureCount(), result.getBusinessExecuteCount());
        return result;
    }
    
    /**
     * 处理业务还原逻辑
     */
    private void handleBusinessRollback(OperationLogImportResultDTO result, OperationLogImportDTO logDTO, 
                                        String itemTraceId, int index, boolean useMethodJson) {
        try {
            if (useMethodJson && hasMethodJson(logDTO)) {
                methodJsonReplayer.replayByMethodJson(logDTO);
                log.info("[{}] 基于method_json还原成功", itemTraceId);
            } else {
                executeBusinessOperation(logDTO);
                log.info("[{}] 业务操作执行成功: module={}, method={}", itemTraceId, logDTO.getModule(), logDTO.getMethod());
            }
            // 日志记录成功 + 业务还原成功 = 成功
            result.setSuccessCount(result.getSuccessCount() + 1);
            result.setBusinessExecuteCount(result.getBusinessExecuteCount() + 1);
        } catch (Exception e) {
            // 日志记录成功但业务还原失败 = 失败
            result.setFailureCount(result.getFailureCount() + 1);
            addFailureDetail(result, index, logDTO, "业务还原失败: " + e.getMessage());
            
            log.error("[{}] 业务还原失败: row={}, operation={}, module={}, error={}", 
                    itemTraceId, index + 1, logDTO.getOperation(), logDTO.getModule(), e.getMessage());
        }
    }
    
    /**
     * 判断是否有method_json
     */
    private boolean hasMethodJson(OperationLogImportDTO logDTO) {
        return logDTO.getMethodJson() != null && !logDTO.getMethodJson().isEmpty();
    }
    
    /**
     * 添加失败记录（带前缀和异常）
     */
    private void addFailure(OperationLogImportResultDTO result, int index, OperationLogImportDTO logDTO, 
                           String prefix, Exception e) {
        addFailureDetail(result, index, logDTO, prefix + ": " + e.getMessage());
    }
    
    /**
     * 添加失败详情
     */
    private void addFailureDetail(OperationLogImportResultDTO result, int index, OperationLogImportDTO logDTO, 
                                  String errorMessage) {
        OperationLogImportResultDTO.FailureDetail failure = new OperationLogImportResultDTO.FailureDetail();
        failure.setRowIndex(index + 1);
        failure.setOperation(logDTO.getOperation());
        failure.setModule(logDTO.getModule());
        failure.setErrorMessage(errorMessage);
        result.getFailures().add(failure);
    }

    /**
     * 处理日志记录的插入/更新
     */
    private void processLogRecord(OperationLogImportDTO logDTO, String itemTraceId) {
        boolean logExists = hasLogRecord(logDTO.getId());
        
        if (logExists) {
            log.info("[{}] 日志已存在，跳过插入: id={}, operation={}", itemTraceId, logDTO.getId(), logDTO.getOperation());
        } else {
            log.info("[{}] 日志不存在，执行插入: id={}, operation={}", itemTraceId, logDTO.getId(), logDTO.getOperation());
            operationLogMapper.insert(convertToEntity(logDTO));
        }
    }
    
    /**
     * 检查日志记录是否存在
     */
    private boolean hasLogRecord(String logId) {
        return logId != null && !logId.isEmpty() && getById(logId) != null;
    }

    private void executeBusinessOperation(OperationLogImportDTO logDTO) {
        log.info("执行业务操作: module={}, method={}", logDTO.getModule(), logDTO.getMethod());
    }

    private SysOperationLog convertToEntity(OperationLogImportDTO dto) {
        SysOperationLog entity = new SysOperationLog();
        entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID().toString().replace("-", "_"));
        entity.setTraceId(dto.getTraceId());
        entity.setUserId(dto.getUserId());
        entity.setUsername(dto.getUsername());
        entity.setRoleId(dto.getRoleId());
        entity.setOperation(dto.getOperation());
        entity.setModule(dto.getModule());
        entity.setMethod(dto.getMethod());
        //entity.setMethodJson(dto.getMethodJson());
        entity.setRequestUrl(dto.getRequestUrl());
        entity.setRequestParams(dto.getRequestParams());
        entity.setIpAddress(dto.getIpAddress());
        entity.setUserAgent(dto.getUserAgent());
        entity.setStatus(1);
        entity.setCreateTime(dto.getCreateTime() != null ? dto.getCreateTime() : LocalDateTime.now());
        return entity;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            log.warn("解析日期时间失败: {}, 尝试其他格式", dateTimeStr);
            try {
                DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                return LocalDateTime.parse(dateTimeStr, isoFormatter);
            } catch (Exception ex) {
                log.error("解析日期时间失败: {}", dateTimeStr);
                return null;
            }
        }
    }

    private boolean isValidLog(OperationLogImportDTO logDTO) {
        return logDTO.getModule() != null && !logDTO.getModule().isEmpty() &&
               logDTO.getOperation() != null && !logDTO.getOperation().isEmpty();
    }
}
