package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysDictionary;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.enums.ProtocolTypeImportStrategy;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.SysDictionaryService;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.service.protocol.support.ProtocolTypeImportFailureReportStore;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String EXPORT_FILE_NAME = "协议类型导出";
    private static final String IMPORT_TEMPLATE_FILE_NAME = "协议类型导入模板.xlsx";
    private static final String IMPORT_TEMPLATE_PATH = "templates/协议类型导入模板.xlsx";
    private static final String FAILURE_REPORT_FILE_PREFIX = "协议类型导入失败原因_";
    private static final List<String> SYSTEM_DICT_TYPES = List.of(
            "pdm_system_type",
            "applicable_system",
            "protocol_applicable_system",
            "protocol_type_classification"
    );

    private final ProtocolTypeMapper protocolTypeMapper;
    private final SysUserMapper sysUserMapper;
    private final SysDictionaryService sysDictionaryService;
    private final ProtocolTypeImportFailureReportStore failureReportStore;
    private final SecurityService securityService;

    public ProtocolTypeServiceImpl(ProtocolTypeMapper protocolTypeMapper,
                                   SysUserMapper sysUserMapper,
                                   SysDictionaryService sysDictionaryService,
                                   ProtocolTypeImportFailureReportStore failureReportStore,
                                   SecurityService securityService) {
        this.protocolTypeMapper = protocolTypeMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysDictionaryService = sysDictionaryService;
        this.failureReportStore = failureReportStore;
        this.securityService = securityService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolType createProtocolType(ProtocolType protocolType) {
        // 校验协议标识符（协议编码）唯一性
        LambdaQueryWrapper<ProtocolType> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(ProtocolType::getProtocolIdentifier, protocolType.getProtocolIdentifier());
        if (protocolTypeMapper.selectCount(lambdaQuery) > 0) {
            throw new RuntimeException("协议编码重复，请重新输入！");
        }

        protocolType.setCreateId(getCurrentOperatorId());


        save(protocolType);
        log.info("新增协议类型成功: id={}, name={}", protocolType.getId(), protocolType.getProtocolName());
        return protocolType;
    }

    @Override
    public IPage<ProtocolType> getProtocolTypeList(ProtocolType protocolType) {
        ProtocolType query = protocolType == null ? new ProtocolType() : protocolType;
        return protocolTypeMapper.selectPage(query.toPage(), buildQueryWrapper(query));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolTypeStatusChangeVO updateProtocolTypeStatus(Long id, Integer status, Boolean confirm) {
        if (id == null) {
            throw new RuntimeException("协议类型ID不能为空！");
        }
        validateStatusValue(status);

        ProtocolType existing = getExistingProtocolType(id);
        RelationStats relationStats = getRelationStats(id);
        fillRelationImpactFields(existing, relationStats.relatedProjectCount(), relationStats.relatedTemplateCount());

        if (Objects.equals(existing.getStatus(), status)) {
            return buildStatusChangeVO(
                    existing,
                    existing.getStatus(),
                    status,
                    false,
                    false,
                    Integer.valueOf(1).equals(status) ? "协议类型已启用，无需重复操作" : "协议类型已禁用，无需重复操作"
            );
        }

        if (requiresDisableConfirm(existing.getStatus(), status, confirm)) {
            return buildStatusChangeVO(
                    existing,
                    existing.getStatus(),
                    status,
                    false,
                    true,
                    buildDisableConfirmMessage(existing.getRelationImpactScope())
            );
        }

        ProtocolType updateEntity = new ProtocolType();
        updateEntity.setId(existing.getId());
        updateEntity.setStatus(status);
        updateEntity.setUpdateId(getCurrentOperatorId());
        updateEntity.setUpdateTime(LocalDateTime.now());

        if (protocolTypeMapper.updateById(updateEntity) <= 0) {
            throw new RuntimeException("协议类型状态更新失败！");
        }

        ProtocolType updated = getExistingProtocolType(id);
        fillRelationImpactFields(updated, relationStats.relatedProjectCount(), relationStats.relatedTemplateCount());
        log.info("协议类型状态更新成功: id={}, name={}, fromStatus={}, toStatus={}, operatorId={}",
                updated.getId(), updated.getProtocolName(), existing.getStatus(), status, getCurrentOperatorId());

        return buildStatusChangeVO(
                updated,
                existing.getStatus(),
                status,
                true,
                false,
                buildStatusChangeSuccessMessage(status, updated.getRelationImpactScope())
        );
    }

    @Override
    public ProtocolTypeImportResultVO importProtocolTypes(MultipartFile file, String strategy) throws IOException {
        validateImportFile(file);

        ProtocolTypeImportStrategy importStrategy = ProtocolTypeImportStrategy.fromCode(strategy);
        List<RowImportData> importRows = parseImportRows(file);
        if (importRows.isEmpty()) {
            throw new RuntimeException("导入文件中没有可处理的数据");
        }

        Map<String, String> legalSystemMap = buildLegalSystemMap();
        Map<String, Integer> rowNumberByIdentifier = new HashMap<>();
        List<RowImportData> readyRows = new ArrayList<>();
        List<RowFailure> failures = new ArrayList<>();

        for (RowImportData row : importRows) {
            List<String> rowErrors = validateImportRow(row, legalSystemMap, rowNumberByIdentifier);
            if (!rowErrors.isEmpty()) {
                failures.add(new RowFailure(row, String.join("；", rowErrors)));
                continue;
            }
            row.setApplicableSystem(legalSystemMap.get(normalizeKey(row.getApplicableSystem())));
            rowNumberByIdentifier.put(normalizeKey(row.getProtocolIdentifier()), row.getRowNumber());
            readyRows.add(row);
        }

        Map<String, ProtocolType> existingProtocolMap = loadExistingProtocolMap(readyRows);
        int successCount = 0;
        int skipCount = 0;
        Long operatorId = getCurrentOperatorId();

        for (RowImportData row : readyRows) {
            ProtocolType existing = existingProtocolMap.get(normalizeKey(row.getProtocolIdentifier()));
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
                log.warn("导入协议类型失败: row={}, identifier={}, reason={}", row.getRowNumber(), row.getProtocolIdentifier(), ex.getMessage());
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
    public void exportProtocolTypes(ProtocolType protocolType, HttpServletResponse response) throws IOException {
        List<ProtocolType> protocolTypes = listProtocolTypes(protocolType);
        List<ProtocolTypeExportVO> exportRows = buildExportRows(protocolTypes);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("协议类型");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"类型编码", "名称", "分类", "状态", "创建人", "创建时间", "描述"};
            int[] columnWidths = {20, 24, 18, 12, 18, 24, 40};
            writeHeaderRow(sheet, headerStyle, headers, columnWidths);
            writeDataRows(sheet, dataStyle, exportRows);

            workbook.write(outputStream);
            writeExcelResponse(response, EXPORT_FILE_NAME + ".xlsx", outputStream.toByteArray());
        }

        log.info("导出协议类型成功: total={}, filterName={}, filterSystem={}, filterStatus={}",
                exportRows.size(),
                protocolType != null ? protocolType.getProtocolName() : null,
                protocolType != null ? protocolType.getApplicableSystem() : null,
                protocolType != null ? protocolType.getStatus() : null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolType modifyProtocolType(ProtocolType protocolType) {
        if (protocolType.getId() == null) {
            throw new RuntimeException("协议类型ID不能为空！");
        }

        ProtocolType existing = protocolTypeMapper.selectById(protocolType.getId());
        if (existing == null) {
            throw new RuntimeException("协议类型不存在！");
        }

        if (StringUtils.isBlank(protocolType.getProtocolName())) {
            throw new RuntimeException("协议类型名称不能为空！");
        }

        long relatedProjectCount = getRelatedProjectCount(protocolType.getId());
        long relatedTemplateCount = getRelatedTemplateCount(protocolType.getId());
        String relationImpactScope = buildRelationImpactScope(relatedProjectCount, relatedTemplateCount);

        if (StringUtils.isNotBlank(protocolType.getProtocolIdentifier())
                && !StringUtils.equals(existing.getProtocolIdentifier(), protocolType.getProtocolIdentifier())) {
            String message = StringUtils.isNotBlank(relationImpactScope)
                    ? String.format("协议编码不可修改，避免关联数据混乱。%s。", relationImpactScope)
                    : "协议编码不可修改，避免关联数据混乱。";
            throw new RuntimeException(message);
        }

        ProtocolType updateEntity = new ProtocolType();
        updateEntity.setId(existing.getId());
        updateEntity.setProtocolIdentifier(existing.getProtocolIdentifier());
        updateEntity.setApplicableSystem(existing.getApplicableSystem());
        updateEntity.setStatus(existing.getStatus());
        updateEntity.setProtocolName(protocolType.getProtocolName());
        updateEntity.setDescription(protocolType.getDescription());
        updateEntity.setUpdateId(protocolType.getUpdateId());

        if (protocolTypeMapper.updateById(updateEntity) <= 0) {
            throw new RuntimeException("协议类型修改失败！");
        }

        ProtocolType updated = protocolTypeMapper.selectById(protocolType.getId());
        fillRelationImpactFields(updated, relatedProjectCount, relatedTemplateCount);
        log.info("编辑协议类型成功: id={}, name={}", updated.getId(), updated.getProtocolName());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProtocolType(Long id) {
        if (id == null) {
            throw new RuntimeException("协议类型ID不能为空！");
        }

        ProtocolType existing = getExistingProtocolType(id);
        RelationStats relationStats = getRelationStats(id);
        if (relationStats.hasRelatedData()) {
            String message = buildDeleteBlockedMessage(relationStats.relatedProjectCount, relationStats.relatedTemplateCount);
            log.warn("删除协议类型失败，存在关联数据: id={}, name={}, relatedProjects={}, relatedTemplates={}",
                    existing.getId(), existing.getProtocolName(), relationStats.relatedProjectCount, relationStats.relatedTemplateCount);
            throw new RuntimeException(message);
        }

        doLogicalDelete(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolTypeDeleteResultVO batchDeleteProtocolTypes(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new RuntimeException("协议类型ID列表不能为空！");
        }

        LinkedHashSet<Long> uniqueIds = Arrays.stream(ids)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (uniqueIds.isEmpty()) {
            throw new RuntimeException("协议类型ID列表不能为空！");
        }

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
                        relationStats.relatedProjectCount,
                        relationStats.relatedTemplateCount,
                        buildDeleteBlockedMessage(relationStats.relatedProjectCount, relationStats.relatedTemplateCount)
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
                rowData.setProtocolIdentifier(readCellValue(row, headerIndexMap, formatter, 0,
                        "类型编码", "协议编码", "编码", "protocolIdentifier"));
                rowData.setProtocolName(readCellValue(row, headerIndexMap, formatter, 1,
                        "名称", "协议名称", "协议类型名称", "protocolName"));
                rowData.setApplicableSystem(readCellValue(row, headerIndexMap, formatter, 2,
                        "分类", "适用系统", "applicableSystem"));
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
        for (int cellIndex = 0; cellIndex <= 4; cellIndex++) {
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
                                           Map<String, String> legalSystemMap,
                                           Map<String, Integer> rowNumberByIdentifier) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isBlank(row.getProtocolIdentifier())) {
            errors.add("类型编码不能为空");
        } else if (row.getProtocolIdentifier().length() > 50) {
            errors.add("类型编码长度不能超过50");
        }

        if (StringUtils.isBlank(row.getProtocolName())) {
            errors.add("名称不能为空");
        } else if (row.getProtocolName().length() > 100) {
            errors.add("名称长度不能超过100");
        }

        if (StringUtils.isBlank(row.getApplicableSystem())) {
            errors.add("分类不能为空");
        } else if (!legalSystemMap.containsKey(normalizeKey(row.getApplicableSystem()))) {
            errors.add("分类不合法，可选值：" + String.join("/", listAllowedSystemCodes(legalSystemMap)));
        }

        if (StringUtils.isNotBlank(row.getDescription()) && row.getDescription().length() > 500) {
            errors.add("描述长度不能超过500");
        }

        if (StringUtils.isNotBlank(row.getProtocolIdentifier())) {
            String normalizedIdentifier = normalizeKey(row.getProtocolIdentifier());
            Integer previousRowNumber = rowNumberByIdentifier.get(normalizedIdentifier);
            if (previousRowNumber != null) {
                errors.add("类型编码与第 " + previousRowNumber + " 行重复");
            }
        }

        try {
            row.setStatus(parseStatus(row.getStatusText()));
        } catch (RuntimeException ex) {
            errors.add(ex.getMessage());
        }

        return errors;
    }

    private Map<String, String> buildLegalSystemMap() {
        Map<String, String> legalSystemMap = new LinkedHashMap<>();
        for (TemplateEnums.PdmSystemType systemType : TemplateEnums.PdmSystemType.values()) {
            legalSystemMap.put(normalizeKey(systemType.getCode()), systemType.getCode());
            legalSystemMap.put(normalizeKey(systemType.getDesc()), systemType.getCode());
        }

        for (String dictType : SYSTEM_DICT_TYPES) {
            List<SysDictionary> dictionaries = sysDictionaryService.getDictionariesByType(dictType);
            for (SysDictionary dictionary : dictionaries) {
                if (StringUtils.isNotBlank(dictionary.getCode())) {
                    legalSystemMap.put(normalizeKey(dictionary.getCode()), dictionary.getCode());
                }
                if (StringUtils.isNotBlank(dictionary.getValue())) {
                    legalSystemMap.put(normalizeKey(dictionary.getValue()),
                            StringUtils.defaultIfBlank(dictionary.getCode(), dictionary.getValue()));
                }
            }
        }
        return legalSystemMap;
    }

    private List<String> listAllowedSystemCodes(Map<String, String> legalSystemMap) {
        return legalSystemMap.values().stream().distinct().toList();
    }

    private Map<String, ProtocolType> loadExistingProtocolMap(List<RowImportData> readyRows) {
        if (readyRows.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> identifiers = readyRows.stream()
                .map(RowImportData::getProtocolIdentifier)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        if (identifiers.isEmpty()) {
            return Collections.emptyMap();
        }

        LambdaQueryWrapper<ProtocolType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProtocolType::getProtocolIdentifier, identifiers);
        return protocolTypeMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(item -> normalizeKey(item.getProtocolIdentifier()), item -> item, (left, right) -> left));
    }

    private void insertProtocolType(RowImportData row, Long operatorId) {
        ProtocolType entity = new ProtocolType();
        entity.setProtocolIdentifier(row.getProtocolIdentifier());
        entity.setProtocolName(row.getProtocolName());
        entity.setApplicableSystem(row.getApplicableSystem());
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
        updateEntity.setProtocolIdentifier(existing.getProtocolIdentifier());
        updateEntity.setProtocolName(row.getProtocolName());
        updateEntity.setApplicableSystem(row.getApplicableSystem());
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

            String[] headers = {"行号", "类型编码", "名称", "分类", "状态", "描述", "失败原因"};
            int[] widths = {10, 20, 24, 18, 12, 36, 48};
            writeHeaderRow(sheet, headerStyle, headers, widths);

            int rowIndex = 1;
            for (RowFailure failure : failures) {
                XSSFRow row = sheet.createRow(rowIndex++);
                setCellValue(row, 0, String.valueOf(failure.row.getRowNumber()), dataStyle);
                setCellValue(row, 1, failure.row.getProtocolIdentifier(), dataStyle);
                setCellValue(row, 2, failure.row.getProtocolName(), dataStyle);
                setCellValue(row, 3, failure.row.getApplicableSystem(), dataStyle);
                setCellValue(row, 4, defaultString(failure.row.getStatusText()), dataStyle);
                setCellValue(row, 5, failure.row.getDescription(), dataStyle);
                setCellValue(row, 6, failure.reason, dataStyle);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String buildFailureReportFileName() {
        return FAILURE_REPORT_FILE_PREFIX + FILE_TIME_FORMATTER.format(LocalDateTime.now()) + ".xlsx";
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

    private List<ProtocolType> listProtocolTypes(ProtocolType protocolType) {
        return protocolTypeMapper.selectList(buildQueryWrapper(protocolType));
    }

    private Integer parseStatus(String statusText) {
        if (StringUtils.isBlank(statusText)) {
            return 1;
        }

        String normalizedStatus = normalizeKey(statusText);
        if (Arrays.asList("1", "启用", "enabled", "true").contains(normalizedStatus)) {
            return 1;
        }
        if (Arrays.asList("0", "禁用", "disabled", "false").contains(normalizedStatus)) {
            return 0;
        }
        throw new RuntimeException("状态不合法，仅支持 启用/禁用 或 enabled/disabled 或 true/false 或 1/0");
    }

    private void validateStatusValue(Integer status) {
        if (!Integer.valueOf(0).equals(status) && !Integer.valueOf(1).equals(status)) {
            throw new RuntimeException("状态值必须是0（禁用）或1（启用）");
        }
    }

    private boolean requiresDisableConfirm(Integer currentStatus, Integer targetStatus, Boolean confirm) {
        return Integer.valueOf(0).equals(targetStatus)
                && !Integer.valueOf(0).equals(currentStatus)
                && !Boolean.TRUE.equals(confirm);
    }

    private String buildDisableConfirmMessage(String relationImpactScope) {
        if (StringUtils.isNotBlank(relationImpactScope)) {
            return String.format("禁用后关联协议不可使用。%s。确认禁用？", relationImpactScope);
        }
        return "禁用后关联协议不可使用，确认禁用？";
    }

    private String buildStatusChangeSuccessMessage(Integer status, String relationImpactScope) {
        if (Integer.valueOf(1).equals(status)) {
            return "启用成功";
        }
        if (StringUtils.isNotBlank(relationImpactScope)) {
            return String.format("禁用成功。禁用后关联协议不可使用。%s。", relationImpactScope);
        }
        return "禁用成功。禁用后关联协议不可使用。";
    }

    private ProtocolTypeStatusChangeVO buildStatusChangeVO(ProtocolType protocolType,
                                                           Integer currentStatus,
                                                           Integer targetStatus,
                                                           boolean statusChanged,
                                                           boolean requiresConfirm,
                                                           String message) {
        ProtocolTypeStatusChangeVO result = new ProtocolTypeStatusChangeVO();
        result.setId(protocolType.getId());
        result.setProtocolName(protocolType.getProtocolName());
        result.setCurrentStatus(currentStatus);
        result.setTargetStatus(targetStatus);
        result.setStatusChanged(statusChanged);
        result.setRequiresConfirm(requiresConfirm);
        result.setMessage(message);
        result.setRelatedProjectCount(protocolType.getRelatedProjectCount());
        result.setRelatedTemplateCount(protocolType.getRelatedTemplateCount());
        result.setRelationImpactScope(protocolType.getRelationImpactScope());
        return result;
    }

    private LambdaQueryWrapper<ProtocolType> buildQueryWrapper(ProtocolType protocolType) {
        LambdaQueryWrapper<ProtocolType> lambdaQuery = new LambdaQueryWrapper<>();
        if (protocolType != null) {
            if (StringUtils.isNotBlank(protocolType.getProtocolName())) {
                lambdaQuery.like(ProtocolType::getProtocolName, protocolType.getProtocolName());
            }

            if (StringUtils.isNotBlank(protocolType.getApplicableSystem())) {
                lambdaQuery.eq(ProtocolType::getApplicableSystem, protocolType.getApplicableSystem());
            }

            if (protocolType.getStatus() != null) {
                lambdaQuery.eq(ProtocolType::getStatus, protocolType.getStatus());
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
        exportVO.setProtocolIdentifier(protocolType.getProtocolIdentifier());
        exportVO.setProtocolName(protocolType.getProtocolName());
        exportVO.setClassification(defaultString(protocolType.getApplicableSystem()));
        exportVO.setStatus(getStatusText(protocolType.getStatus()));
        exportVO.setCreateUserName(resolveCreateUserName(protocolType.getCreateId()));
        exportVO.setCreateTime(formatDateTime(protocolType.getCreateTime()));
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

    private String getStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return Integer.valueOf(1).equals(status) ? "启用" : "禁用";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : EXPORT_TIME_FORMATTER.format(dateTime);
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
            setCellValue(row, 0, exportRow.getProtocolIdentifier(), dataStyle);
            setCellValue(row, 1, exportRow.getProtocolName(), dataStyle);
            setCellValue(row, 2, exportRow.getClassification(), dataStyle);
            setCellValue(row, 3, exportRow.getStatus(), dataStyle);
            setCellValue(row, 4, exportRow.getCreateUserName(), dataStyle);
            setCellValue(row, 5, exportRow.getCreateTime(), dataStyle);
            setCellValue(row, 6, exportRow.getDescription(), dataStyle);
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

    private void fillRelationImpactFields(ProtocolType protocolType, long relatedProjectCount, long relatedTemplateCount) {
        protocolType.setRelatedProjectCount(relatedProjectCount);
        protocolType.setRelatedTemplateCount(relatedTemplateCount);
        protocolType.setRelationImpactScope(buildRelationImpactScope(relatedProjectCount, relatedTemplateCount));
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
        private String protocolIdentifier;
        private String protocolName;
        private String applicableSystem;
        private String statusText;
        private Integer status;
        private String description;

    }

    private record RowFailure(RowImportData row, String reason) {
    }
}
