package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.entity.SysUser;
import com.example.tooltestingdemo.entity.protocol.ProtocolType;
import com.example.tooltestingdemo.mapper.SysUserMapper;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTypeMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.protocol.IProtocolTypeService;
import com.example.tooltestingdemo.vo.ProtocolTypeDeleteResultVO;
import com.example.tooltestingdemo.vo.ProtocolTypeExportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
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

    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String EXPORT_FILE_NAME = "协议类型导出";

    private final ProtocolTypeMapper protocolTypeMapper;
    private final SysUserMapper sysUserMapper;
    private final SecurityService securityService;

    public ProtocolTypeServiceImpl(ProtocolTypeMapper protocolTypeMapper,
                                   SysUserMapper sysUserMapper,
                                   SecurityService securityService) {
        this.protocolTypeMapper = protocolTypeMapper;
        this.sysUserMapper = sysUserMapper;
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
    public void exportProtocolTypes(ProtocolType protocolType, HttpServletResponse response) throws IOException {
        List<ProtocolType> protocolTypes = listProtocolTypes(protocolType);
        List<ProtocolTypeExportVO> exportRows = buildExportRows(protocolTypes);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename="
                + URLEncoder.encode(EXPORT_FILE_NAME + ".xlsx", "UTF-8").replace("+", "%20"));

        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream outputStream = response.getOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("协议类型");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"类型编码", "名称", "分类", "状态", "创建人", "创建时间", "描述"};
            int[] columnWidths = {20, 24, 18, 12, 18, 24, 40};
            writeHeaderRow(sheet, headerStyle, headers, columnWidths);
            writeDataRows(sheet, dataStyle, exportRows);

            workbook.write(outputStream);
            outputStream.flush();
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

    private List<ProtocolType> listProtocolTypes(ProtocolType protocolType) {
        return protocolTypeMapper.selectList(buildQueryWrapper(protocolType));
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

    private static class RelationStats {
        private final long relatedProjectCount;
        private final long relatedTemplateCount;

        private RelationStats(long relatedProjectCount, long relatedTemplateCount) {
            this.relatedProjectCount = relatedProjectCount;
            this.relatedTemplateCount = relatedTemplateCount;
        }

        private boolean hasRelatedData() {
            return relatedProjectCount > 0 || relatedTemplateCount > 0;
        }
    }
}
