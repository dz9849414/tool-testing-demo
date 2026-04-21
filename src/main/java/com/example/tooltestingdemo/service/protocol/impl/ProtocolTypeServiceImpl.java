package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolTypeCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeModifyDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolTypeStatusUpdateDTO;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.enums.ProtocolTypeImportStrategy;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.service.protocol.support.ProtocolTypeImportFailureReportStore;
import com.example.tooltestingdemo.util.LocalDateUtil;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeExportVO;
import com.example.tooltestingdemo.vo.ProtocolTypeImportResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeStatusChangeVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 协议类型主表 服务实现类
 * </p>
 *
 * @author wanggang
 * @since 2026-04-13
 */
@Slf4j
@Service
public class ProtocolTypeServiceImpl extends ServiceImpl<ProtocolTypeMapper, ProtocolType> implements IProtocolTypeService {

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String EXPORT_FILE_NAME = "协议类型导出";
    private static final String IMPORT_TEMPLATE_FILE_NAME = "协议类型导入模板.xlsx";
    private static final String IMPORT_TEMPLATE_PATH = "templates/协议类型导入模板.xlsx";
    private static final String FAILURE_REPORT_FILE_PREFIX = "协议类型导入失败原因_";
    private final ProtocolTypeMapper protocolTypeMapper;
    private final SysUserMapper sysUserMapper;
    private final ProtocolTypeImportFailureReportStore failureReportStore;
    private final SecurityService securityService;

    public ProtocolTypeServiceImpl(ProtocolTypeMapper protocolTypeMapper,
                                   SysUserMapper sysUserMapper,
                                   ProtocolTypeImportFailureReportStore failureReportStore,
                                   SecurityService securityService) {
        this.protocolTypeMapper = protocolTypeMapper;
        this.sysUserMapper = sysUserMapper;
        this.failureReportStore = failureReportStore;
        this.securityService = securityService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolType createProtocolType(ProtocolTypeCreateDTO dto) {
        String protocolCode = normalizeText(dto.getProtocolCode());
        ensureProtocolCodeUnique(protocolCode, null);

        ProtocolType entity = new ProtocolType();
        entity.setProtocolCode(protocolCode);
        entity.setProtocolName(normalizeText(dto.getProtocolName()));
        entity.setProtocolCategory(normalizeText(dto.getProtocolCategory()));
        entity.setSystemType(normalizeText(dto.getSystemType()));
        entity.setDescription(normalizeText(dto.getDescription()));
        entity.setStatus(resolveStatus(dto.getStatus(), ProtocolType.Status.PENDING.name()));
        entity.setCreateId(getCurrentOperatorId());
        entity.setUpdateId(getCurrentOperatorId());

        save(entity);
        log.info("协议类型创建成功: id={}, code={}, name={}", entity.getId(), entity.getProtocolCode(), entity.getProtocolName());
        return entity;
    }

    @Override
    public IPage<ProtocolType> getProtocolTypeList(ProtocolTypeQueryDTO dto) {
        ProtocolTypeQueryDTO query = dto == null ? new ProtocolTypeQueryDTO() : dto;
        return protocolTypeMapper.selectPage(query.toPage(), buildQueryWrapper(query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolTypeStatusChangeVO updateProtocolTypeStatus(ProtocolTypeStatusUpdateDTO dto) {
        Long id = dto.getId();
        ProtocolType existing = getExistingProtocolType(id);
        String targetStatus = resolveStatus(dto.getStatus(), null);
        RelationStats relationStats = getRelationStats(id);
        boolean requireConfirm = ProtocolType.Status.DISABLED.name().equals(targetStatus)
                && relationStats.hasRelatedData()
                && !Boolean.TRUE.equals(dto.getConfirm());
        if (requireConfirm) {
            String message = buildDisableConfirmMessage(relationStats);
            log.warn("协议类型禁用需要确认: id={}, relatedProjectCount={}, relatedTemplateCount={}",
                    id, relationStats.relatedProjectCount(), relationStats.relatedTemplateCount());
            return buildStatusChangeVO(existing, existing.getStatus(), targetStatus, false, true, message, relationStats);
        }
        if (Objects.equals(existing.getStatus(), targetStatus)) {
            return buildStatusChangeVO(existing, existing.getStatus(), targetStatus, false, false, "状态未发生变化", relationStats);
        }

        ProtocolType updateEntity = new ProtocolType();
        updateEntity.setId(id);
        updateEntity.setStatus(targetStatus);
        updateEntity.setUpdateId(getCurrentOperatorId());
        updateEntity.setUpdateTime(LocalDateTime.now());
        if (protocolTypeMapper.updateById(updateEntity) <= 0) {
            throw new RuntimeException("状态更新失败");
        }

        ProtocolType updated = getExistingProtocolType(id);
        log.info("协议类型状态更新成功: id={}, fromStatus={}, toStatus={}, operatorId={}",
                id, existing.getStatus(), targetStatus, getCurrentOperatorId());
        return buildStatusChangeVO(updated, existing.getStatus(), targetStatus, true, false, "状态更新成功", relationStats);
    }

    @Override
    public ProtocolTypeImportResultVO importProtocolTypes(MultipartFile file, String strategy) throws IOException {
        validateImportFile(file);
        ProtocolTypeImportStrategy importStrategy = ProtocolTypeImportStrategy.fromCode(strategy);
        List<RowImportData> importRows = parseImportRows(file);
        if (importRows.isEmpty()) {
            throw new RuntimeException("导入文件中没有可处理的数据");
        }

        Map<String, Integer> rowNumberByCode = new java.util.HashMap<>();
        List<RowImportData> readyRows = new ArrayList<>();
        List<RowFailure> failures = new ArrayList<>();

        for (RowImportData row : importRows) {
            List<String> rowErrors = validateImportRow(row, rowNumberByCode);
            if (!rowErrors.isEmpty()) {
                failures.add(new RowFailure(row, String.join("；", rowErrors)));
                continue;
            }
            rowNumberByCode.put(normalizeKey(row.getProtocolCode()), row.getRowNumber());
            readyRows.add(row);
        }

        Map<String, ProtocolType> existingProtocolMap = loadExistingProtocolMap(readyRows);
        int successCount = 0;
        int skipCount = 0;
        Long operatorId = getCurrentOperatorId();

        for (RowImportData row : readyRows) {
            ProtocolType existing = existingProtocolMap.get(normalizeKey(row.getProtocolCode()));
            try {
                if (existing == null) {
                    insertProtocolType(row, operatorId);
                    successCount++;
                    continue;
                }

                if (ProtocolTypeImportStrategy.INCREMENTAL == importStrategy) {
                    skipCount++;
                    continue;
                }

                overwriteProtocolType(existing, row, operatorId);
                successCount++;
            } catch (Exception ex) {
                log.warn("导入协议类型失败: row={}, protocolCode={}, reason={}", row.getRowNumber(), row.getProtocolCode(), ex.getMessage());
                failures.add(new RowFailure(row, defaultString(ex.getMessage())));
            }
        }

        String failureReportId = null;
        if (!failures.isEmpty()) {
            failureReportId = failureReportStore.save(buildFailureReportFileName(), buildFailureReportContent(failures));
        }

        String message = String.format("导入完成：成功 %d 条，失败 %d 条，跳过 %d 条", successCount, failures.size(), skipCount);
        return ProtocolTypeImportResultVO.builder()
                .success(failures.isEmpty())
                .message(message)
                .totalCount(importRows.size())
                .successCount(successCount)
                .failCount(failures.size())
                .skipCount(skipCount)
                .strategy(importStrategy.getCode())
                .failureReportId(failureReportId)
                .failureReportDownloadUrl(failureReportId == null ? null : "/api/protocol/protocolType/import/failures/" + failureReportId)
                .importTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource resource = new ClassPathResource(IMPORT_TEMPLATE_PATH);
        if (!resource.exists()) {
            throw new RuntimeException("协议类型导入模板不存在");
        }
        byte[] fileBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        writeExcelResponse(response, IMPORT_TEMPLATE_FILE_NAME, fileBytes);
    }

    @Override
    public void downloadImportFailureReport(String reportId, HttpServletResponse response) throws IOException {
        ProtocolTypeImportFailureReportStore.FailureReportResource reportResource = failureReportStore.get(reportId);
        if (reportResource == null) {
            throw new RuntimeException("失败原因文件不存在或已过期");
        }
        writeExcelResponse(response, reportResource.getFileName(), reportResource.getContent());
    }

    @Override
    public void exportProtocolTypes(ProtocolTypeQueryDTO dto, HttpServletResponse response) throws IOException {
        List<ProtocolType> protocolTypes = listProtocolTypes(dto);
        List<ProtocolTypeExportVO> exportRows = buildExportRows(protocolTypes);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("协议类型");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"协议编码", "协议名称", "协议分类", "系统类型", "状态", "创建人", "创建时间", "描述"};
            int[] columnWidths = {20, 24, 18, 18, 12, 18, 24, 40};
            writeHeaderRow(sheet, headerStyle, headers, columnWidths);
            writeDataRows(sheet, dataStyle, exportRows);

            workbook.write(outputStream);
            writeExcelResponse(response, EXPORT_FILE_NAME + ".xlsx", outputStream.toByteArray());
        }

        log.info("导出协议类型成功: total={}, filterName={}, filterSystem={}, filterStatus={}",
                exportRows.size(),
                dto != null ? dto.getProtocolName() : null,
                dto != null ? dto.getSystemType() : null,
                dto != null ? dto.getStatus() : null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolType modifyProtocolType(ProtocolTypeModifyDTO dto) {
        ProtocolType existing = protocolTypeMapper.selectById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("协议类型不存在");
        }

        ProtocolType updateEntity = new ProtocolType();
        updateEntity.setId(existing.getId());
        String targetCode = StringUtils.isBlank(dto.getProtocolCode()) ? existing.getProtocolCode() : normalizeText(dto.getProtocolCode());
        ensureProtocolCodeUnique(targetCode, existing.getId());
        updateEntity.setProtocolCode(targetCode);
        updateEntity.setProtocolName(resolveNullableUpdateValue(dto.getProtocolName(), existing.getProtocolName()));
        updateEntity.setProtocolCategory(resolveNullableUpdateValue(dto.getProtocolCategory(), existing.getProtocolCategory()));
        updateEntity.setSystemType(resolveNullableUpdateValue(dto.getSystemType(), existing.getSystemType()));
        updateEntity.setStatus(resolveStatus(dto.getStatus(), existing.getStatus()));
        updateEntity.setDescription(resolveNullableUpdateValue(dto.getDescription(), existing.getDescription()));
        updateEntity.setVersion(dto.getVersion() == null ? existing.getVersion() : dto.getVersion());
        updateEntity.setUpdateId(getCurrentOperatorId());
        updateEntity.setUpdateTime(LocalDateTime.now());

        if (protocolTypeMapper.updateById(updateEntity) <= 0) {
            throw new RuntimeException("协议类型编辑失败");
        }

        ProtocolType updated = protocolTypeMapper.selectById(dto.getId());
        log.info("协议类型编辑成功: id={}, protocolCode={}, protocolName={}, operatorId={}",
                updated.getId(), updated.getProtocolCode(), updated.getProtocolName(), getCurrentOperatorId());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProtocolType(Long id) {
        ProtocolType existing = getExistingProtocolType(id);
        RelationStats relationStats = getRelationStats(id);
        if (relationStats.hasRelatedData()) {
            String message = buildDeleteBlockedMessage(relationStats.relatedProjectCount(), relationStats.relatedTemplateCount());
            log.warn("删除协议类型失败，存在关联数据: id={}, name={}, relatedProjects={}, relatedTemplates={}",
                    existing.getId(), existing.getProtocolName(), relationStats.relatedProjectCount(), relationStats.relatedTemplateCount());
            throw new RuntimeException(message);
        }

        doLogicalDelete(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolTypeDeleteResultVO batchDeleteProtocolTypes(Long[] ids) {
        LinkedHashSet<Long> uniqueIds = Arrays.stream(ids)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        List<Long> deletedIds = new ArrayList<>();
        List<ProtocolTypeDeleteResultVO.UndeletableItem> undeletableItems = new ArrayList<>();

        for (Long id : uniqueIds) {
            ProtocolType existing = protocolTypeMapper.selectById(id);
            if (existing == null) {
                undeletableItems.add(buildUndeletableItem(id, null, 0L, 0L, "协议类型不存在"));
                continue;
            }

            RelationStats relationStats = getRelationStats(id);
            if (relationStats.hasRelatedData()) {
                undeletableItems.add(buildUndeletableItem(
                        existing.getId(),
                        existing.getProtocolName(),
                        relationStats.relatedProjectCount(),
                        relationStats.relatedTemplateCount(),
                        buildDeleteBlockedMessage(relationStats.relatedProjectCount(), relationStats.relatedTemplateCount())
                ));
                continue;
            }

            doLogicalDelete(existing);
            deletedIds.add(existing.getId());
        }

        ProtocolTypeDeleteResultVO result = new ProtocolTypeDeleteResultVO();
        result.setDeletedIds(deletedIds);
        result.setUndeletableItems(undeletableItems);
        result.setDeletedCount(deletedIds.size());
        result.setUndeletableCount(undeletableItems.size());
        result.setSummaryMessage(buildBatchDeleteSummaryMessage(deletedIds.size(), undeletableItems));

        log.info("批量删除协议类型完成: deletedCount={}, undeletableCount={}, deletedIds={}, undeletableIds={}",
                result.getDeletedCount(),
                result.getUndeletableCount(),
                result.getDeletedIds(),
                undeletableItems.stream().map(ProtocolTypeDeleteResultVO.UndeletableItem::getId).toList());
        return result;
    }

    private long getRelatedProjectCount(Long protocolId) {
        Long count = protocolTypeMapper.countRelatedProjects(protocolId);
        return count == null ? 0L : count;
    }

    /**
     * 校验导入文件，仅允许 xls/xlsx。
     */
    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("导入文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName) ||
                !(StringUtils.endsWithIgnoreCase(fileName, ".xlsx") || StringUtils.endsWithIgnoreCase(fileName, ".xls"))) {
            throw new RuntimeException("仅支持导入 xls 或 xlsx 文件");
        }
    }

    private List<RowImportData> parseImportRows(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return Collections.emptyList();
            }

            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> headerIndexMap = buildHeaderIndexMap(headerRow, formatter);
            List<RowImportData> rows = new ArrayList<>();

            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmptyRow(row, headerIndexMap, formatter)) {
                    continue;
                }

                RowImportData rowData = new RowImportData();
                rowData.setRowNumber(rowIndex + 1);
                rowData.setProtocolCode(readCellValue(row, headerIndexMap, formatter, 0,
                        "协议编码", "类型编码", "编码", "protocolCode"));
                rowData.setProtocolName(readCellValue(row, headerIndexMap, formatter, 1,
                        "名称", "协议名称", "协议类型名称", "protocolName"));
                rowData.setProtocolCategory(readCellValue(row, headerIndexMap, formatter, 2,
                        "协议分类", "分类", "protocolCategory"));
                rowData.setSystemType(readCellValue(row, headerIndexMap, formatter, 3,
                        "系统类型", "适用系统", "systemType"));
                rowData.setStatusText(readCellValue(row, headerIndexMap, formatter, 3,
                        "状态", "status"));
                rowData.setDescription(readCellValue(row, headerIndexMap, formatter, 4,
                        "描述", "备注", "description"));
                rows.add(rowData);
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("解析导入文件失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Integer> buildHeaderIndexMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        if (headerRow == null) {
            return headerIndexMap;
        }
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            String headerText = normalizeKey(formatter.formatCellValue(cell));
            if (StringUtils.isNotBlank(headerText)) {
                headerIndexMap.put(headerText, cellIndex);
            }
        }
        return headerIndexMap;
    }

    private boolean isEmptyRow(Row row, Map<String, Integer> headerIndexMap, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int cellIndex = 0; cellIndex <= 5; cellIndex++) {
            String value = formatter.formatCellValue(row.getCell(cellIndex));
            if (StringUtils.isNotBlank(value)) {
                return false;
            }
        }
        for (Integer cellIndex : headerIndexMap.values()) {
            if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(cellIndex)))) {
                return false;
            }
        }
        return true;
    }

    private String readCellValue(Row row,
                                 Map<String, Integer> headerIndexMap,
                                 DataFormatter formatter,
                                 int fallbackIndex,
                                 String... aliases) {
        Integer cellIndex = findHeaderIndex(headerIndexMap, aliases);
        Cell cell = row.getCell(cellIndex == null ? fallbackIndex : cellIndex);
        return StringUtils.trimToEmpty(formatter.formatCellValue(cell));
    }

    private Integer findHeaderIndex(Map<String, Integer> headerIndexMap, String... aliases) {
        for (String alias : aliases) {
            Integer index = headerIndexMap.get(normalizeKey(alias));
            if (index != null) {
                return index;
            }
        }
        return null;
    }

    private List<String> validateImportRow(RowImportData row,
                                           Map<String, Integer> rowNumberByCode) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isBlank(row.getProtocolCode())) {
            errors.add("协议编码不能为空");
        } else if (row.getProtocolCode().length() > 50) {
            errors.add("协议编码长度不能超过50");
        }

        if (StringUtils.isBlank(row.getProtocolName())) {
            errors.add("名称不能为空");
        } else if (row.getProtocolName().length() > 100) {
            errors.add("名称长度不能超过100");
        }

        if (StringUtils.isBlank(row.getProtocolCategory())) {
            errors.add("协议分类不能为空");
        }

        if (StringUtils.isBlank(row.getSystemType())) {
            errors.add("系统类型不能为空");
        }

        if (StringUtils.isNotBlank(row.getDescription()) && row.getDescription().length() > 500) {
            errors.add("描述长度不能超过500");
        }

        if (StringUtils.isNotBlank(row.getProtocolCode())) {
            String normalizedIdentifier = normalizeKey(row.getProtocolCode());
            Integer previousRowNumber = rowNumberByCode.get(normalizedIdentifier);
            if (previousRowNumber != null) {
                errors.add("协议编码与第 " + previousRowNumber + " 行重复");
            }
        }

        try {
            row.setStatus(parseStatus(row.getStatusText()));
        } catch (RuntimeException ex) {
            errors.add(ex.getMessage());
        }

        return errors;
    }

    private Map<String, ProtocolType> loadExistingProtocolMap(List<RowImportData> readyRows) {
        if (readyRows.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> identifiers = readyRows.stream()
                .map(RowImportData::getProtocolCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (identifiers.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<ProtocolType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProtocolType::getProtocolCode, identifiers);
        return protocolTypeMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(item -> normalizeKey(item.getProtocolCode()), item -> item, (left, right) -> left));
    }

    private void insertProtocolType(RowImportData row, Long operatorId) {
        ProtocolType entity = new ProtocolType();
        entity.setProtocolCode(row.getProtocolCode());
        entity.setProtocolName(row.getProtocolName());
        entity.setProtocolCategory(row.getProtocolCategory());
        entity.setSystemType(row.getSystemType());
        entity.setStatus(row.getStatus());
        entity.setDescription(row.getDescription());
        entity.setCreateId(operatorId);
        entity.setUpdateId(operatorId);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        protocolTypeMapper.insert(entity);
    }

    private void overwriteProtocolType(ProtocolType existing, RowImportData row, Long operatorId) {
        ProtocolType updateEntity = new ProtocolType();
        updateEntity.setId(existing.getId());
        updateEntity.setProtocolCode(existing.getProtocolCode());
        updateEntity.setProtocolName(row.getProtocolName());
        updateEntity.setProtocolCategory(row.getProtocolCategory());
        updateEntity.setSystemType(row.getSystemType());
        updateEntity.setStatus(row.getStatus());
        updateEntity.setDescription(row.getDescription());
        updateEntity.setUpdateId(operatorId);
        updateEntity.setUpdateTime(LocalDateTime.now());
        protocolTypeMapper.updateById(updateEntity);
    }

    private byte[] buildFailureReportContent(List<RowFailure> failures) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("导入失败原因");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"行号", "协议编码", "名称", "协议分类", "系统类型", "状态", "描述", "失败原因"};
            int[] widths = {10, 20, 24, 18, 18, 12, 36, 48};
            writeHeaderRow(sheet, headerStyle, headers, widths);

            int rowIndex = 1;
            for (RowFailure failure : failures) {
                XSSFRow row = sheet.createRow(rowIndex++);
                setCellValue(row, 0, String.valueOf(failure.row.getRowNumber()), dataStyle);
                setCellValue(row, 1, failure.row.getProtocolCode(), dataStyle);
                setCellValue(row, 2, failure.row.getProtocolName(), dataStyle);
                setCellValue(row, 3, failure.row.getProtocolCategory(), dataStyle);
                setCellValue(row, 4, failure.row.getSystemType(), dataStyle);
                setCellValue(row, 5, defaultString(failure.row.getStatusText()), dataStyle);
                setCellValue(row, 6, failure.row.getDescription(), dataStyle);
                setCellValue(row, 7, failure.reason, dataStyle);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String buildFailureReportFileName() {
        return FAILURE_REPORT_FILE_PREFIX + LocalDateUtil.formatDateTime(LocalDateTime.now(), FILE_TIME_FORMATTER) + ".xlsx";
    }

    private void writeExcelResponse(HttpServletResponse response, String fileName, byte[] content) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename="
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(content);
            outputStream.flush();
        }
    }

    private List<ProtocolType> listProtocolTypes(ProtocolTypeQueryDTO queryDTO) {
        return protocolTypeMapper.selectList(buildQueryWrapper(queryDTO));
    }

    private String parseStatus(String statusText) {
        if (StringUtils.isBlank(statusText)) {
            return ProtocolType.Status.PENDING.name();
        }
        return resolveStatus(statusText, null);
    }

    private String buildDisableConfirmMessage(RelationStats relationStats) {
        String relationImpactScope = buildRelationImpactScope(relationStats.relatedProjectCount(), relationStats.relatedTemplateCount());
        if (StringUtils.isNotBlank(relationImpactScope)) {
            return String.format("禁用后将影响关联数据。%s，是否继续？", relationImpactScope);
        }
        return "禁用后将影响关联数据，是否继续？";
    }

    private ProtocolTypeStatusChangeVO buildStatusChangeVO(ProtocolType protocolType,
                                                           String currentStatus,
                                                           String targetStatus,
                                                           boolean statusChanged,
                                                           boolean requiresConfirm,
                                                           String message,
                                                           RelationStats relationStats) {
        ProtocolTypeStatusChangeVO result = new ProtocolTypeStatusChangeVO();
        result.setId(protocolType.getId());
        result.setProtocolName(protocolType.getProtocolName());
        result.setCurrentStatus(currentStatus);
        result.setTargetStatus(targetStatus);
        result.setStatusChanged(statusChanged);
        result.setRequiresConfirm(requiresConfirm);
        result.setMessage(message);
        result.setRelatedProjectCount(relationStats.relatedProjectCount());
        result.setRelatedTemplateCount(relationStats.relatedTemplateCount());
        result.setRelationImpactScope(buildRelationImpactScope(relationStats.relatedProjectCount(), relationStats.relatedTemplateCount()));
        return result;
    }

    private LambdaQueryWrapper<ProtocolType> buildQueryWrapper(ProtocolTypeQueryDTO protocolType) {
        LambdaQueryWrapper<ProtocolType> lambdaQuery = new LambdaQueryWrapper<>();
        if (protocolType != null) {
            if (StringUtils.isNotBlank(protocolType.getProtocolCode())) {
                lambdaQuery.like(ProtocolType::getProtocolCode, protocolType.getProtocolCode());
            }
            if (StringUtils.isNotBlank(protocolType.getProtocolName())) {
                lambdaQuery.like(ProtocolType::getProtocolName, protocolType.getProtocolName());
            }
            if (StringUtils.isNotBlank(protocolType.getProtocolCategory())) {
                lambdaQuery.eq(ProtocolType::getProtocolCategory, protocolType.getProtocolCategory());
            }
            if (StringUtils.isNotBlank(protocolType.getSystemType())) {
                lambdaQuery.eq(ProtocolType::getSystemType, protocolType.getSystemType());
            }
            if (StringUtils.isNotBlank(protocolType.getStatus())) {
                lambdaQuery.eq(ProtocolType::getStatus, resolveStatus(protocolType.getStatus(), null));
            }
        }

        lambdaQuery.orderByDesc(ProtocolType::getCreateTime).orderByDesc(ProtocolType::getId);
        return lambdaQuery;
    }

    private List<ProtocolTypeExportVO> buildExportRows(List<ProtocolType> protocolTypes) {
        if (protocolTypes == null || protocolTypes.isEmpty()) {
            return Collections.emptyList();
        }

        return protocolTypes.stream()
                .map(this::buildExportRow)
                .collect(Collectors.toList());
    }

    private ProtocolTypeExportVO buildExportRow(ProtocolType protocolType) {
        ProtocolTypeExportVO exportVO = new ProtocolTypeExportVO();
        exportVO.setProtocolCode(protocolType.getProtocolCode());
        exportVO.setProtocolName(protocolType.getProtocolName());
        exportVO.setProtocolCategory(defaultString(protocolType.getProtocolCategory()));
        exportVO.setSystemType(defaultString(protocolType.getSystemType()));
        exportVO.setStatus(defaultString(protocolType.getStatus()));
        exportVO.setCreateUserName(resolveCreateUserName(protocolType.getCreateId()));
        exportVO.setCreateTime(LocalDateUtil.formatDateTime(protocolType.getCreateTime()));
        exportVO.setDescription(defaultString(protocolType.getDescription()));
        return exportVO;
    }

    private String resolveCreateUserName(Long createId) {
        if (createId == null) {
            return "";
        }

        SysUser sysUser = sysUserMapper.selectById(String.valueOf(createId));
        if (sysUser == null) {
            return String.valueOf(createId);
        }
        if (StringUtils.isNotBlank(sysUser.getRealName())) {
            return sysUser.getRealName();
        }
        if (StringUtils.isNotBlank(sysUser.getUsername())) {
            return sysUser.getUsername();
        }
        return String.valueOf(createId);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String normalizeKey(String value) {
        return StringUtils.deleteWhitespace(StringUtils.trimToEmpty(value)).toUpperCase(Locale.ROOT);
    }

    private void writeHeaderRow(XSSFSheet sheet, XSSFCellStyle headerStyle, String[] headers, int[] columnWidths) {
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, columnWidths[i] * 256);
        }
    }

    private void writeDataRows(XSSFSheet sheet, XSSFCellStyle dataStyle, List<ProtocolTypeExportVO> exportRows) {
        int rowIndex = 1;
        for (ProtocolTypeExportVO exportRow : exportRows) {
            XSSFRow row = sheet.createRow(rowIndex++);
            setCellValue(row, 0, exportRow.getProtocolCode(), dataStyle);
            setCellValue(row, 1, exportRow.getProtocolName(), dataStyle);
            setCellValue(row, 2, exportRow.getProtocolCategory(), dataStyle);
            setCellValue(row, 3, exportRow.getSystemType(), dataStyle);
            setCellValue(row, 4, exportRow.getStatus(), dataStyle);
            setCellValue(row, 5, exportRow.getCreateUserName(), dataStyle);
            setCellValue(row, 6, exportRow.getCreateTime(), dataStyle);
            setCellValue(row, 7, exportRow.getDescription(), dataStyle);
        }
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        return headerStyle;
    }

    private XSSFCellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return dataStyle;
    }

    private void setCellValue(XSSFRow row, int columnIndex, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(columnIndex);
        cell.setCellValue(defaultString(value));
        cell.setCellStyle(style);
    }

    private long getRelatedTemplateCount(Long protocolId) {
        Long count = protocolTypeMapper.countRelatedTemplates(protocolId);
        return count == null ? 0L : count;
    }

    private String buildRelationImpactScope(long relatedProjectCount, long relatedTemplateCount) {
        if (relatedProjectCount <= 0 && relatedTemplateCount <= 0) {
            return null;
        }
        return String.format("关联影响范围：%d 个项目、%d 个模板", relatedProjectCount, relatedTemplateCount);
    }

    private ProtocolType getExistingProtocolType(Long id) {
        ProtocolType existing = protocolTypeMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("协议类型不存在！");
        }
        return existing;
    }

    private RelationStats getRelationStats(Long id) {
        return new RelationStats(getRelatedProjectCount(id), getRelatedTemplateCount(id));
    }

    private String buildDeleteBlockedMessage(long relatedProjectCount, long relatedTemplateCount) {
        return String.format("请先解除关联 %d 个项目 / %d 个模板", relatedProjectCount, relatedTemplateCount);
    }

    private void doLogicalDelete(ProtocolType existing) {
        Long operatorId = getCurrentOperatorId();
        ProtocolType deleteEntity = new ProtocolType();
        deleteEntity.setId(existing.getId());
        deleteEntity.setDeletedBy(operatorId);
        deleteEntity.setDeletedTime(LocalDateTime.now());
        deleteEntity.setUpdateId(operatorId);

        if (protocolTypeMapper.updateById(deleteEntity) <= 0) {
            throw new RuntimeException("协议类型删除失败！");
        }
        protocolTypeMapper.deleteById(deleteEntity.getId());
        log.info("删除协议类型成功: id={}, name={}, operatorId={}", existing.getId(), existing.getProtocolName(), operatorId);
    }

    private Long getCurrentOperatorId() {
        String currentUserId = securityService.getCurrentUserId();
        if (StringUtils.isNumeric(currentUserId)) {
            return Long.valueOf(currentUserId);
        }
        return 1L;
    }

    private void ensureProtocolCodeUnique(String protocolCode, Long excludeId) {
        LambdaQueryWrapper<ProtocolType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProtocolType::getProtocolCode, protocolCode);
        if (excludeId != null) {
            wrapper.ne(ProtocolType::getId, excludeId);
        }
        Long count = protocolTypeMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new RuntimeException("协议编码已存在");
        }
    }

    private String resolveNullableUpdateValue(String incomingValue, String oldValue) {
        return StringUtils.isBlank(incomingValue) ? oldValue : normalizeText(incomingValue);
    }

    private String normalizeText(String value) {
        return StringUtils.trimToNull(value);
    }

    private String resolveStatus(String status, String defaultValue) {
        if (StringUtils.isBlank(status)) {
            if (defaultValue == null) {
                throw new RuntimeException("状态不能为空");
            }
            return defaultValue;
        }
        String normalized = normalizeKey(status);
        if (Arrays.asList("PENDING", "待启用").contains(normalized)) {
            return ProtocolType.Status.PENDING.name();
        }
        if (Arrays.asList("ENABLED", "1", "启用", "TRUE").contains(normalized)) {
            return ProtocolType.Status.ENABLED.name();
        }
        if (Arrays.asList("DISABLED", "0", "禁用", "FALSE").contains(normalized)) {
            return ProtocolType.Status.DISABLED.name();
        }
        throw new RuntimeException("状态不合法，仅支持 PENDING/ENABLED/DISABLED");
    }

    private ProtocolTypeDeleteResultVO.UndeletableItem buildUndeletableItem(Long id, String protocolName,
                                                                            long relatedProjectCount,
                                                                            long relatedTemplateCount,
                                                                            String reason) {
        ProtocolTypeDeleteResultVO.UndeletableItem item = new ProtocolTypeDeleteResultVO.UndeletableItem();
        item.setId(id);
        item.setProtocolName(protocolName);
        item.setRelatedProjectCount(relatedProjectCount);
        item.setRelatedTemplateCount(relatedTemplateCount);
        item.setReason(reason);
        return item;
    }

    private String buildBatchDeleteSummaryMessage(int deletedCount, List<ProtocolTypeDeleteResultVO.UndeletableItem> undeletableItems) {
        if (undeletableItems.isEmpty()) {
            return String.format("可删除 %d 个，不可删除 0 个", deletedCount);
        }

        boolean allBlockedByRelation = undeletableItems.stream()
                .allMatch(item -> item.getReason() != null && item.getReason().startsWith("请先解除关联"));
        if (allBlockedByRelation) {
            return String.format("可删除 %d 个，不可删除 %d 个（原因：已关联数据）", deletedCount, undeletableItems.size());
        }
        return String.format("可删除 %d 个，不可删除 %d 个（原因：已关联数据或数据不存在）", deletedCount, undeletableItems.size());
    }

    private record RelationStats(long relatedProjectCount, long relatedTemplateCount) {
        private boolean hasRelatedData() {
            return relatedProjectCount > 0 || relatedTemplateCount > 0;
        }
    }

    @Setter
    @Getter
    private static class RowImportData {
        private Integer rowNumber;
        private String protocolCode;
        private String protocolName;
        private String protocolCategory;
        private String systemType;
        private String statusText;
        private String status;
        private String description;

    }

    private record RowFailure(RowImportData row, String reason) {
    }
}
