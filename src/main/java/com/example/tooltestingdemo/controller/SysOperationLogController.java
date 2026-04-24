package com.example.tooltestingdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tooltestingdemo.common.ErrorStatus;
import com.example.tooltestingdemo.common.Result;
import com.example.tooltestingdemo.entity.SysOperationLog;
import com.example.tooltestingdemo.service.SysOperationLogService;
import com.example.tooltestingdemo.util.OperationLogNameUtils;
import com.example.tooltestingdemo.vo.SysOperationLogVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class SysOperationLogController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysOperationLogService operationLogService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:log:api') or @securityService.isCurrentUser(#userId)")
    public Result<Page<SysOperationLogVO>> getUserOperationLogs(
            @PathVariable String userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!isValidDateTime(startTime)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "startTime format must be yyyy-MM-dd HH:mm:ss");
        }
        if (!isValidDateTime(endTime)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "endTime format must be yyyy-MM-dd HH:mm:ss");
        }
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        Page<SysOperationLog> logs = operationLogService.getOperationLogsByUserIdAndTimeRange(new Page<>(page, size), userId, start, end);
        return Result.success("Query user operation logs success", toVOPage(logs));
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:log:api')")
    public Result<Page<SysOperationLogVO>> getOperationLogsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String module) {

        if (!isValidDateTime(startTime)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "startTime format must be yyyy-MM-dd HH:mm:ss");
        }
        if (!isValidDateTime(endTime)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "endTime format must be yyyy-MM-dd HH:mm:ss");
        }
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        Page<SysOperationLog> logs = operationLogService.getOperationLogsByPage(new Page<>(page, size), userId, username, operation, status, start, end, module);
        return Result.success("Query operation logs success", toVOPage(logs));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:log:api')")
    public Result<List<SysOperationLogVO>> getRecentOperationLogs(@RequestParam(defaultValue = "10") int limit) {
        List<SysOperationLogVO> logVOs = operationLogService.getRecentOperationLogs(limit)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success("Query recent operation logs success", logVOs);
    }

    @GetMapping("/module")
    @PreAuthorize("hasRole('ADMIN') or @securityService.hasPermission('system:log:api')")
    public Result<List<SysOperationLogVO>> getOperationLogsByModule(@RequestParam String module) {
        List<SysOperationLogVO> logVOs = operationLogService.getOperationLogsByModule(module)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success("Query module operation logs success", logVOs);
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public Result<Page<SysOperationLogVO>> getRoleOperationLogs(
            @PathVariable String roleId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (!isValidDateTime(startTime)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "startTime format must be yyyy-MM-dd HH:mm:ss");
        }
        if (!isValidDateTime(endTime)) {
            return Result.error(ErrorStatus.BAD_REQUEST, "endTime format must be yyyy-MM-dd HH:mm:ss");
        }
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        Page<SysOperationLog> logs = operationLogService.getOperationLogsByRoleIdAndPage(new Page<>(page, size), roleId, username, operation, status, start, end, module);
        return Result.success("Query role operation logs success", toVOPage(logs));
    }

    @GetMapping("/role/{roleId}/export")
    @PreAuthorize("@securityService.hasPermission('system:log:api')")
    public void exportRoleOperationLogs(
            @PathVariable String roleId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String module,
            HttpServletResponse response) throws IOException {

        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);
        if (start == null && startTime != null && !startTime.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("startTime format must be yyyy-MM-dd HH:mm:ss");
            return;
        }
        if (end == null && endTime != null && !endTime.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("endTime format must be yyyy-MM-dd HH:mm:ss");
            return;
        }

        Page<SysOperationLog> pageResult = operationLogService.getOperationLogsByRoleIdAndPage(new Page<>(1, 10000), roleId, username, operation, status, start, end, module);
        exportOperationLogs(pageResult.getRecords(), response, "role_operation_logs");
    }

    private Page<SysOperationLogVO> toVOPage(Page<SysOperationLog> logs) {
        Page<SysOperationLogVO> voPage = new Page<>(logs.getCurrent(), logs.getSize(), logs.getTotal());
        voPage.setRecords(logs.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    private SysOperationLogVO toVO(SysOperationLog log) {
        SysOperationLogVO logVO = new SysOperationLogVO();
        try {
            BeanUtils.copyProperties(logVO, log);
        } catch (Exception e) {
            throw new RuntimeException("Convert operation log to VO failed", e);
        }
        logVO.setModuleDisplayName(OperationLogNameUtils.getModuleDisplayName(log.getModule()));
        return logVO;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidDateTime(String dateTimeStr) {
        return dateTimeStr == null || dateTimeStr.isEmpty() || parseDateTime(dateTimeStr) != null;
    }

    private void exportOperationLogs(List<SysOperationLog> logs, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".xlsx", "UTF-8"));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("operation_logs");
            XSSFCellStyle headerStyle = buildHeaderStyle(workbook);
            XSSFCellStyle dataStyle = buildDataStyle(workbook);
            String[] headers = {
                    "Log ID", "User ID", "Username", "Role ID", "Module",
                    "Operation", "Method", "Request URL", "Request Params", "IP Address",
                    "User Agent", "Status", "Error Message", "Execute Time(ms)", "Create Time"
            };
            int[] columnWidths = {36, 20, 20, 20, 24, 20, 20, 36, 50, 18, 36, 12, 36, 18, 24};

            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, columnWidths[i] * 256);
            }

            int rowIndex = 1;
            for (SysOperationLog log : logs) {
                XSSFRow row = sheet.createRow(rowIndex++);
                setCellValue(row, 0, log.getId(), dataStyle);
                setCellValue(row, 1, log.getUserId(), dataStyle);
                setCellValue(row, 2, log.getUsername(), dataStyle);
                setCellValue(row, 3, log.getRoleId(), dataStyle);
                setCellValue(row, 4, OperationLogNameUtils.getModuleDisplayName(log.getModule()), dataStyle);
                setCellValue(row, 5, log.getOperation(), dataStyle);
                setCellValue(row, 6, log.getMethod(), dataStyle);
                setCellValue(row, 7, log.getRequestUrl(), dataStyle);
                setCellValue(row, 8, log.getRequestParams(), dataStyle);
                setCellValue(row, 9, log.getIpAddress(), dataStyle);
                setCellValue(row, 10, log.getUserAgent(), dataStyle);
                setCellValue(row, 11, log.getStatus() == 1 ? "SUCCESS" : "FAILED", dataStyle);
                setCellValue(row, 12, log.getErrorMessage(), dataStyle);
                setCellValue(row, 13, log.getExecuteTime(), dataStyle);
                setCellValue(row, 14, log.getCreateTime(), dataStyle);
            }

            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        }
    }

    private XSSFCellStyle buildHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return headerStyle;
    }

    private XSSFCellStyle buildDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return dataStyle;
    }

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
