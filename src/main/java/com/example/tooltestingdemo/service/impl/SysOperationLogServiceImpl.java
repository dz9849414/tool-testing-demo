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
    public Page<SysOperationLog> getOperationLogsByUserIdAndTimeRange(Page<SysOperationLog> page, String userId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOperationLog::getUserId, userId);

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
    
    @Override
    public List<SysOperationLog> getOperationLogsByRoleId(String roleId) {
        LambdaQueryWrapper<SysOperationLog> queryWrapper = new LambdaQueryWrapper<>();
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
        if (roleId != null) {
            queryWrapper.eq(SysOperationLog::getRoleId, roleId);
        } else {
            queryWrapper.isNull(SysOperationLog::getRoleId);
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
        
        if (roleId != null) {
            queryWrapper.eq(SysOperationLog::getRoleId, roleId);
        } else {
            queryWrapper.isNull(SysOperationLog::getRoleId);
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
                logDTO.setId(getCellStringValue(row.getCell(0)));
                logDTO.setTraceId(getCellStringValue(row.getCell(1)));
                logDTO.setUserId(getCellStringValue(row.getCell(2)));
                logDTO.setUsername(getCellStringValue(row.getCell(3)));
                logDTO.setRoleId(getCellStringValue(row.getCell(4)));
                logDTO.setModule(getCellStringValue(row.getCell(5)));
                logDTO.setOperation(getCellStringValue(row.getCell(6)));
                logDTO.setMethod(getCellStringValue(row.getCell(7)));
                logDTO.setRequestUrl(getCellStringValue(row.getCell(8)));
                logDTO.setRequestParams(getCellStringValue(row.getCell(9)));
                logDTO.setIpAddress(getCellStringValue(row.getCell(10)));
                logDTO.setUserAgent(getCellStringValue(row.getCell(11)));
                logDTO.setMethodJson(getCellStringValue(row.getCell(16)));
                logDTO.setCreateTime(parseDateTime(getCellStringValue(row.getCell(15))));
                
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
                boolean logExists = false;
                if (logDTO.getId() != null && !logDTO.getId().isEmpty()) {
                    logExists = getById(logDTO.getId()) != null;
                }
                
                if (logExists) {
                    log.info("[{}] 日志已存在，跳过导入: id={}, operation={}", itemTraceId, logDTO.getId(), logDTO.getOperation());
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }
                
                if (executeRollback) {
                    try {
                        if (useMethodJson && logDTO.getMethodJson() != null && !logDTO.getMethodJson().isEmpty()) {
                            methodJsonReplayer.replayByMethodJson(logDTO);
                            log.info("[{}] 基于method_json还原成功", itemTraceId);
                        } else {
                            executeBusinessOperation(logDTO);
                            log.info("[{}] 业务操作执行成功: module={}, method={}", itemTraceId, logDTO.getModule(), logDTO.getMethod());
                        }
                        result.setBusinessExecuteCount(result.getBusinessExecuteCount() + 1);
                        
                        if (!logExists) {
                            log.info("[{}] 还原操作完成: operation={}, module={}", itemTraceId, logDTO.getOperation(), logDTO.getModule());
                        }
                    } catch (Exception e) {
                        result.setFailureCount(result.getFailureCount() + 1);
                        OperationLogImportResultDTO.FailureDetail failure = new OperationLogImportResultDTO.FailureDetail();
                        failure.setRowIndex(i + 1);
                        failure.setOperation(logDTO.getOperation());
                        failure.setModule(logDTO.getModule());
                        failure.setErrorMessage(e.getMessage());
                        result.getFailures().add(failure);
                        
                        log.error("[{}] 还原操作失败: row={}, operation={}, module={}, error={}", 
                                itemTraceId, i + 1, logDTO.getOperation(), logDTO.getModule(), e.getMessage());
                    }
                } else {
                    log.info("[{}] 跳过还原操作: executeRollback=false", itemTraceId);
                    if (!logExists) {
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    }
                }
                
            } catch (Exception e) {
                result.setFailureCount(result.getFailureCount() + 1);
                OperationLogImportResultDTO.FailureDetail failure = new OperationLogImportResultDTO.FailureDetail();
                failure.setRowIndex(i + 1);
                failure.setOperation(logDTO.getOperation());
                failure.setModule(logDTO.getModule());
                failure.setErrorMessage(e.getMessage());
                result.getFailures().add(failure);
                
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
        entity.setMethodJson(dto.getMethodJson());
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