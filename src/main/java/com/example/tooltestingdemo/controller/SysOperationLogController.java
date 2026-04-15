package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.example.tooltestingdemo.vo.SysOperationLogVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    public Result<Page<SysOperationLogVO>> getUserOperationLogs(
            @PathVariable String userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

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
        Page<SysOperationLog> logs = operationLogService.getOperationLogsByUserIdAndTimeRange(pageParam, userId, start, end);
        
        // 转换为VO
        Page<SysOperationLogVO> voPage = new Page<>(logs.getCurrent(), logs.getSize(), logs.getTotal());
        voPage.setRecords(logs.getRecords().stream().map(log -> {
            SysOperationLogVO logVO = new SysOperationLogVO();
            try {
                BeanUtils.copyProperties(logVO, log);
            } catch (Exception e) {
                throw new RuntimeException("操作日志数据转换失败");
            }
            return logVO;
        }).collect(Collectors.toList()));
        
        return Result.success("获取用户操作日志成功", voPage);
    }

    /**
     * 分页获取操作日志列表
     */
    @GetMapping("/page")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<Page<SysOperationLogVO>> getOperationLogsByPage(
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
        
        // 转换为VO
        Page<SysOperationLogVO> voPage = new Page<>(logs.getCurrent(), logs.getSize(), logs.getTotal());
        voPage.setRecords(logs.getRecords().stream().map(log -> {
            SysOperationLogVO logVO = new SysOperationLogVO();
            try {
                BeanUtils.copyProperties(logVO, log);
            } catch (Exception e) {
                throw new RuntimeException("操作日志数据转换失败");
            }
            return logVO;
        }).collect(Collectors.toList()));
        
        return Result.success("获取操作日志列表成功", voPage);
    }

    /**
     * 获取最近的操作日志
     */
    @GetMapping("/recent")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<List<SysOperationLogVO>> getRecentOperationLogs(
            @RequestParam(defaultValue = "10") int limit) {
        List<SysOperationLog> logs = operationLogService.getRecentOperationLogs(limit);
        
        // 转换为VO
        List<SysOperationLogVO> logVOs = logs.stream().map(log -> {
            SysOperationLogVO logVO = new SysOperationLogVO();
            try {
                BeanUtils.copyProperties(logVO, log);
            } catch (Exception e) {
                throw new RuntimeException("操作日志数据转换失败");
            }
            return logVO;
        }).collect(Collectors.toList());
        
        return Result.success("获取最近操作日志成功", logVOs);
    }

    /**
     * 根据模块获取操作日志
     */
    @GetMapping("/module")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<List<SysOperationLogVO>> getOperationLogsByModule(
            @RequestParam String module) {
        List<SysOperationLog> logs = operationLogService.getOperationLogsByModule(module);
        
        // 转换为VO
        List<SysOperationLogVO> logVOs = logs.stream().map(log -> {
            SysOperationLogVO logVO = new SysOperationLogVO();
            try {
                BeanUtils.copyProperties(logVO, log);
            } catch (Exception e) {
                throw new RuntimeException("操作日志数据转换失败");
            }
            return logVO;
        }).collect(Collectors.toList());
        
        return Result.success("获取模块操作日志成功", logVOs);
    }
    
    /**
     * 根据角色ID获取操作日志（支持分页和搜索）
     * 功能描述：系统集中展示指定角色的操作日志，包括登录、配置修改、任务执行等行为
     * 输入：角色ID、时间范围（可选）、模块（可选）、页码、每页大小
     * 输出：分页的结构化操作日志列表（含时间、操作类型、详情）
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<Page<SysOperationLogVO>> getRoleOperationLogs(
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
        
        // 转换为VO
        Page<SysOperationLogVO> voPage = new Page<>(logs.getCurrent(), logs.getSize(), logs.getTotal());
        voPage.setRecords(logs.getRecords().stream().map(log -> {
            SysOperationLogVO logVO = new SysOperationLogVO();
            try {
                BeanUtils.copyProperties(logVO, log);
            } catch (Exception e) {
                throw new RuntimeException("操作日志数据转换失败");
            }
            return logVO;
        }).collect(Collectors.toList()));
        
        return Result.success("获取角色操作日志成功", voPage);
    }
    
    /**
     * 导出角色操作日志
     * 功能描述：导出指定角色的操作日志为Excel文件
     * 输入：角色ID、时间范围（可选）、模块（可选）
     * 输出：Excel文件下载
     */
    @GetMapping("/role/{roleId}/export")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public void exportRoleOperationLogs(
            @PathVariable String roleId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String module,
            HttpServletResponse response) throws IOException {
        // 解析时间范围参数
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        
        if (start == null && startTime != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("开始时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            return;
        }
        
        if (end == null && endTime != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("结束时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            return;
        }
        
        // 获取角色操作日志
        Page<SysOperationLog> pageParam = new Page<>(1, 10000); // 设置一个足够大的分页大小，获取所有符合条件的日志
        Page<SysOperationLog> pageResult = operationLogService.getOperationLogsByRoleIdAndPage(pageParam, roleId, start, end, module);
        List<SysOperationLog> logs = pageResult.getRecords();
        
        // 导出为Excel文件
        exportOperationLogs(logs, response, "角色操作日志");
    }
    
    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 导出操作日志为Excel文件
     */
    private void exportOperationLogs(List<SysOperationLog> logs, HttpServletResponse response, String fileName) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".xlsx", "UTF-8"));
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建Excel工作簿和工作表
            XSSFSheet sheet = workbook.createSheet("操作日志");
            
            // 创建表头样式
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            // 设置浅绿色背景
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 创建数据样式
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // 定义表头列
            String[] headers = {
                "日志ID", "用户ID", "用户名", "角色ID", "操作模块", 
                "操作类型", "方法名", "请求URL", "请求参数", "IP地址", 
                "用户代理", "状态", "错误信息", "执行时间(ms)", "创建时间"
            };
            
            // 定义列宽（单位：字符）
            int[] columnWidths = {
                36, 20, 20, 20, 15, 
                15, 15, 30, 50, 15, 
                50, 10, 50, 15, 25
            };
            
            // 创建表头
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, columnWidths[i] * 256); // 设置列宽
            }
            
            // 填充数据
            int rowIndex = 1;
            for (SysOperationLog log : logs) {
                XSSFRow row = sheet.createRow(rowIndex++);
                
                // 填充数据并应用样式
                setCellValue(row, 0, log.getId(), dataStyle);
                setCellValue(row, 1, log.getUserId(), dataStyle);
                setCellValue(row, 2, log.getUsername(), dataStyle);
                setCellValue(row, 3, log.getRoleId(), dataStyle);
                setCellValue(row, 4, log.getModule(), dataStyle);
                setCellValue(row, 5, log.getOperation(), dataStyle);
                setCellValue(row, 6, log.getMethod(), dataStyle);
                setCellValue(row, 7, log.getRequestUrl(), dataStyle);
                setCellValue(row, 8, log.getRequestParams(), dataStyle);
                setCellValue(row, 9, log.getIpAddress(), dataStyle);
                setCellValue(row, 10, log.getUserAgent(), dataStyle);
                setCellValue(row, 11, log.getStatus() == 1 ? "成功" : "失败", dataStyle);
                setCellValue(row, 12, log.getErrorMessage(), dataStyle);
                setCellValue(row, 13, log.getExecuteTime(), dataStyle);
                setCellValue(row, 14, log.getCreateTime().toString(), dataStyle);
            }
            
            // 写入响应流
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        }
    }
    
    /**
     * 设置单元格值并应用样式
     */
    private void setCellValue(XSSFRow row, int columnIndex, Object value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(columnIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }
}