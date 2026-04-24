package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigModifyDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolConfigStatusUpdateDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.mapper.protocol.ProtocolConfigMapper;
import com.example.tooltestingdemo.service.SecurityService;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.service.protocol.support.ProtocolConfigImportFailureReportStore;
import com.example.tooltestingdemo.util.LocalDateUtil;
import com.example.tooltestingdemo.vo.ProtocolConfigImportResultVO;
import com.example.tooltestingdemo.vo.ProtocolConfigVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

/**
 * 协议参数配置表服务实现：新增场景仅持久化主表及 JSON 字段（url_config、auth_config），不再联动参数模板表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolConfigServiceImpl extends ServiceImpl<ProtocolConfigMapper, ProtocolConfig> implements IProtocolConfigService {

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String IMPORT_TEMPLATE_FILE_NAME = "协议配置导入模板.xlsx";
    private static final String IMPORT_TEMPLATE_PATH = "templates/协议配置导入模板.xlsx";
    private static final String FAILURE_REPORT_FILE_PREFIX = "协议配置导入失败原因_";
    private static final String EXPORT_FILE_NAME = "协议配置导出";

    private final ObjectMapper objectMapper;
    private final SecurityService securityService;
    private final ProtocolConfigImportFailureReportStore failureReportStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfig createProtocolConfig(ProtocolConfigCreateDTO dto) {
        validateUrlConfig(dto.getUrlConfigList());
        validateAuthConfig(dto.getAuthConfigList());

        ProtocolConfig entity = new ProtocolConfig()
                .setProtocolId(dto.getProtocolId())
                .setProtocolName(dto.getProtocolName())
                .setConfigName(dto.getConfigName())
                .setTimeoutConnect(defaultInt(dto.getTimeoutConnect(), 5000))
                .setTimeoutRead(defaultInt(dto.getTimeoutRead(), 30000))
                .setRetryCount(defaultInt(dto.getRetryCount(), 3))
                .setRetryInterval(defaultInt(dto.getRetryInterval(), 1000))
                .setRetryCondition(dto.getRetryCondition())
                .setDataFormat(defaultString(dto.getDataFormat(), "JSON"))
                .setStatus(defaultInt(dto.getStatus(), 0))
                .setFormatConfig(dto.getFormatConfig())
                .setAdditionalParams(dto.getAdditionalParams())
                .setDescription(dto.getDescription());

        entity.setUrlConfig(toJsonOrNull(dto.getUrlConfigList()));
        entity.setAuthConfig(toJsonOrNull(dto.getAuthConfigList()));

        this.save(entity);
        return entity;
    }

    @Override
    public IPage<ProtocolConfigVO> getProtocolConfigList(ProtocolConfigQueryDTO dto) {
        ProtocolConfigQueryDTO query = dto == null ? new ProtocolConfigQueryDTO() : dto;
        IPage<ProtocolConfig> page = this.page(query.toPage(), buildQueryWrapper(query));
        return page.convert(this::toVO);
    }

    @Override
    public ProtocolConfigVO getProtocolConfigDetail(Long id) {
        ProtocolConfig config = this.getById(id);
        if (config == null) {
            throw new RuntimeException("协议配置不存在");
        }
        return toVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfigVO modifyProtocolConfig(ProtocolConfigModifyDTO dto) {
        ProtocolConfig existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("协议配置不存在");
        }
        if (dto.getUrlConfigList() != null) {
            validateUrlConfig(dto.getUrlConfigList());
        }
        if (dto.getAuthConfigList() != null) {
            validateAuthConfig(dto.getAuthConfigList());
        }

        ProtocolConfig updateEntity = new ProtocolConfig();
        updateEntity.setId(existing.getId());
        updateEntity.setProtocolId(dto.getProtocolId() == null ? existing.getProtocolId() : dto.getProtocolId());
        updateEntity.setProtocolName(dto.getProtocolName() == null ? existing.getProtocolName() : dto.getProtocolName());
        updateEntity.setConfigName(resolveNullableText(dto.getConfigName(), existing.getConfigName()));
        updateEntity.setTimeoutConnect(dto.getTimeoutConnect() == null ? existing.getTimeoutConnect() : dto.getTimeoutConnect());
        updateEntity.setTimeoutRead(dto.getTimeoutRead() == null ? existing.getTimeoutRead() : dto.getTimeoutRead());
        updateEntity.setRetryCount(dto.getRetryCount() == null ? existing.getRetryCount() : dto.getRetryCount());
        updateEntity.setRetryInterval(dto.getRetryInterval() == null ? existing.getRetryInterval() : dto.getRetryInterval());
        updateEntity.setRetryCondition(resolveNullableText(dto.getRetryCondition(), existing.getRetryCondition()));
        updateEntity.setDataFormat(resolveNullableText(dto.getDataFormat(), existing.getDataFormat()));
        updateEntity.setFormatConfig(resolveNullableText(dto.getFormatConfig(), existing.getFormatConfig()));
        updateEntity.setAdditionalParams(resolveNullableText(dto.getAdditionalParams(), existing.getAdditionalParams()));
        updateEntity.setDescription(resolveNullableText(dto.getDescription(), existing.getDescription()));
        updateEntity.setStatus(resolveStatus(dto.getStatus(), existing.getStatus()));
        updateEntity.setUrlConfig(dto.getUrlConfigList() == null ? existing.getUrlConfig() : toJsonOrNull(dto.getUrlConfigList()));
        updateEntity.setAuthConfig(dto.getAuthConfigList() == null ? existing.getAuthConfig() : toJsonOrNull(dto.getAuthConfigList()));

        if (!this.updateById(updateEntity)) {
            throw new RuntimeException("协议配置编辑失败");
        }
        return toVO(this.getById(existing.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfigVO updateProtocolConfigStatus(ProtocolConfigStatusUpdateDTO dto) {
        ProtocolConfig existing = this.getById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("协议配置不存在");
        }
        Integer targetStatus = resolveStatus(dto.getStatus(), null);
        if (Objects.equals(existing.getStatus(), targetStatus)) {
            return toVO(existing);
        }

        ProtocolConfig updateEntity = new ProtocolConfig();
        updateEntity.setId(existing.getId());
        updateEntity.setStatus(targetStatus);
        if (!this.updateById(updateEntity)) {
            throw new RuntimeException("协议配置状态更新失败");
        }
        return toVO(this.getById(existing.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProtocolConfig(Long id) {
        ProtocolConfig existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("协议配置不存在");
        }

        Long operatorId = getCurrentOperatorId();
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<ProtocolConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProtocolConfig::getId, id)
                .eq(ProtocolConfig::getIsDeleted, 0)
                .set(ProtocolConfig::getIsDeleted, 1)
                .set(ProtocolConfig::getDeletedBy, operatorId)
                .set(ProtocolConfig::getDeletedTime, now)
                .set(ProtocolConfig::getUpdateTime, now);

        if (this.baseMapper.update(null, updateWrapper) <= 0) {
            throw new RuntimeException("协议配置删除失败");
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        ClassPathResource resource = new ClassPathResource(IMPORT_TEMPLATE_PATH);
        if (!resource.exists()) {
            throw new RuntimeException("协议配置导入模板不存在");
        }
        byte[] fileBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        writeExcelResponse(response, IMPORT_TEMPLATE_FILE_NAME, fileBytes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProtocolConfigImportResultVO importProtocolConfigs(MultipartFile file) throws IOException {
        validateImportFile(file);
        List<RowImportData> importRows = parseImportRows(file);
        if (importRows.isEmpty()) {
            throw new RuntimeException("导入文件中没有可处理的数据");
        }

        int successCount = 0;
        List<RowFailure> failures = new ArrayList<>();
        for (RowImportData row : importRows) {
            try {
                ProtocolConfigCreateDTO dto = toCreateDTO(row);
                createProtocolConfig(dto);
                successCount++;
            } catch (Exception ex) {
                failures.add(new RowFailure(row, defaultString(ex.getMessage(), "导入失败")));
            }
        }

        String failureReportId = null;
        if (!failures.isEmpty()) {
            failureReportId = failureReportStore.save(buildFailureReportFileName(), buildFailureReportContent(failures));
        }

        String message = String.format("导入完成：成功 %d 条，失败 %d 条", successCount, failures.size());
        return ProtocolConfigImportResultVO.builder()
                .success(failures.isEmpty())
                .message(message)
                .totalCount(importRows.size())
                .successCount(successCount)
                .failCount(failures.size())
                .failureReportId(failureReportId)
                .failureReportDownloadUrl(failureReportId == null ? null : "/api/protocol/protocolConfig/import/failures/" + failureReportId)
                .importTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void downloadImportFailureReport(String reportId, HttpServletResponse response) throws IOException {
        ProtocolConfigImportFailureReportStore.FailureReportResource reportResource = failureReportStore.get(reportId);
        if (reportResource == null) {
            throw new RuntimeException("失败原因文件不存在或已过期");
        }
        writeExcelResponse(response, reportResource.getFileName(), reportResource.getContent());
    }

    @Override
    public void exportProtocolConfigs(ProtocolConfigQueryDTO dto, HttpServletResponse response) throws IOException {
        List<ProtocolConfig> configs = this.list(buildQueryWrapper(dto == null ? new ProtocolConfigQueryDTO() : dto));
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("协议配置");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"配置名称", "协议类型ID", "协议类型名称", "URL配置(JSON)", "认证配置(JSON)", "连接超时(ms)",
                    "读取超时(ms)", "重试次数", "重试间隔(ms)", "重试触发条件(1，2，3)", "数据格式", "格式校验配置", "额外参数", "状态", "描述"};
            int[] widths = {24, 14, 20, 48, 48, 16, 16, 12, 14, 24, 16, 24, 24, 10, 30};
            writeHeaderRow(sheet, headerStyle, headers, widths);
            writeExportRows(sheet, dataStyle, configs);

            workbook.write(outputStream);
            writeExcelResponse(response, EXPORT_FILE_NAME + ".xlsx", outputStream.toByteArray());
        }
    }

    /**
     * URL 列表业务规则：非空、主 URL 恰好一个、序号唯一、非默认端口时必须带合法端口。
     */
    private void validateUrlConfig(List<ProtocolConfigCreateDTO.UrlConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("urlConfigList不能为空");
        }
        long primaryTrue = list.stream().filter(i -> Boolean.TRUE.equals(i.getPrimary())).count();
        if (primaryTrue != 1) {
            throw new IllegalArgumentException("主URL必须且只能有一个");
        }
        Set<Integer> seqSeen = new HashSet<>();
        for (ProtocolConfigCreateDTO.UrlConfigItemDTO item : list) {
            if (!seqSeen.add(item.getSeq())) {
                throw new IllegalArgumentException("urlConfigList.seq不能重复");
            }
            if (Boolean.FALSE.equals(item.getUseDefaultPort())) {
                if (item.getPort() == null) {
                    throw new IllegalArgumentException("未使用默认端口时，端口号不能为空");
                }
                if (item.getPort() < 1 || item.getPort() > 65535) {
                    throw new IllegalArgumentException("端口范围必须为1-65535");
                }
            }
        }
    }

    /**
     * 认证列表按类型做字段级校验；空列表表示「无额外认证配置」，允许不写 auth_config。
     */
    private void validateAuthConfig(List<ProtocolConfigCreateDTO.AuthConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ProtocolConfigCreateDTO.AuthConfigItemDTO item : list) {
            String type = upper(item.getType());
            switch (type) {
                case "NONE" -> { /* 无附加字段 */ }
                case "BASIC" -> {
                    requireText(item.getUsername(), "Basic认证用户名不能为空");
                    requireText(item.getPassword(), "Basic认证密码不能为空");
                }
                case "TOKEN" -> {
                    requireText(item.getToken(), "Token不能为空");
                    requireText(item.getTokenLocation(), "Token位置不能为空");
                    String loc = upper(item.getTokenLocation());
                    if (!"HEADER".equals(loc) && !"QUERY".equals(loc)) {
                        throw new IllegalArgumentException("Token位置仅支持HEADER/QUERY");
                    }
                    if ("HEADER".equals(loc)) {
                        requireText(item.getHeaderName(), "Token位置为HEADER时headerName不能为空");
                    }
                }
                case "OAUTH2" -> {
                    requireText(item.getAuthEndpoint(), "OAuth2授权端点不能为空");
                    requireText(item.getClientId(), "OAuth2 clientId不能为空");
                    requireText(item.getClientSecret(), "OAuth2 clientSecret不能为空");
                }
                case "CERT" -> {
                    requireText(item.getCertFileName(), "证书文件名不能为空");
                    requireText(item.getCertFileBase64(), "证书文件内容不能为空");
                    requireText(item.getCertPassword(), "证书密码不能为空");
                }
                default -> throw new IllegalArgumentException("不支持的认证方式：" + item.getType());
            }
        }
    }

    private String toJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON序列化失败：" + e.getMessage(), e);
        }
    }

    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static int defaultInt(Integer v, int defaultValue) {
        return v == null ? defaultValue : v;
    }

    private static String defaultString(String v, String defaultValue) {
        if (v == null || "[]".equals(v) || v.trim().isEmpty()) {
            return defaultValue;
        }
        return v;
    }

    private Integer resolveStatus(Integer status, Integer defaultValue) {
        if (status == null) {
            return defaultValue;
        }
        if (status == 0 || status == 1) {
            return status;
        }
        throw new RuntimeException("状态不合法，仅支持 0（禁用）或 1（启用）");
    }

    private String resolveNullableText(String incomingValue, String oldValue) {
        return StringUtils.isBlank(incomingValue) ? oldValue : incomingValue.trim();
    }

    private LambdaQueryWrapper<ProtocolConfig> buildQueryWrapper(ProtocolConfigQueryDTO dto) {
        LambdaQueryWrapper<ProtocolConfig> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getProtocolId() != null) {
            queryWrapper.eq(ProtocolConfig::getProtocolId, dto.getProtocolId());
        }
        if (StringUtils.isNotBlank(dto.getConfigName())) {
            queryWrapper.like(ProtocolConfig::getConfigName, dto.getConfigName());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq(ProtocolConfig::getStatus, dto.getStatus());
        }
        applyDateTimeRange(queryWrapper, dto.getCreateTimeStart(), dto.getCreateTimeEnd(), ProtocolConfig::getCreateTime);
        applyDateTimeRange(queryWrapper, dto.getUpdateTimeStart(), dto.getUpdateTimeEnd(), ProtocolConfig::getUpdateTime);
        queryWrapper.orderByDesc(ProtocolConfig::getCreateTime).orderByDesc(ProtocolConfig::getId);
        return queryWrapper;
    }

    private void applyDateTimeRange(LambdaQueryWrapper<ProtocolConfig> queryWrapper,
                                    LocalDateTime start,
                                    LocalDateTime end,
                                    SFunction<ProtocolConfig, ?> column) {
        Optional.ofNullable(start).ifPresent(value -> queryWrapper.ge(column, value));
        Optional.ofNullable(end).ifPresent(value -> queryWrapper.le(column, value));
    }

    private ProtocolConfigVO toVO(ProtocolConfig entity) {
        ProtocolConfigVO vo = new ProtocolConfigVO();
        vo.setId(entity.getId());
        vo.setProtocolId(entity.getProtocolId());
        vo.setProtocolName(entity.getProtocolName());
        vo.setConfigName(entity.getConfigName());
        vo.setTimeoutConnect(entity.getTimeoutConnect());
        vo.setTimeoutRead(entity.getTimeoutRead());
        vo.setRetryCount(entity.getRetryCount());
        vo.setRetryInterval(entity.getRetryInterval());
        vo.setRetryCondition(entity.getRetryCondition());
        vo.setDataFormat(entity.getDataFormat());
        vo.setFormatConfig(entity.getFormatConfig());
        vo.setAdditionalParams(entity.getAdditionalParams());
        vo.setStatus(entity.getStatus());
        vo.setDescription(entity.getDescription());
        vo.setCreateId(entity.getCreateId());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateId(entity.getUpdateId());
        vo.setUpdateTime(entity.getUpdateTime());

        List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList = parseList(
                entity.getUrlConfig(),
                new TypeReference<List<ProtocolConfigCreateDTO.UrlConfigItemDTO>>() {
                }
        );
        if (urlConfigList != null) {
            urlConfigList.sort(Comparator.comparing(
                    ProtocolConfigCreateDTO.UrlConfigItemDTO::getSeq,
                    Comparator.nullsLast(Integer::compareTo)
            ));
        }
        vo.setUrlConfigList(urlConfigList);
        vo.setAuthConfigList(parseList(
                entity.getAuthConfig(),
                new TypeReference<List<ProtocolConfigCreateDTO.AuthConfigItemDTO>>() {
                }
        ));
        return vo;
    }

    private <T> List<T> parseList(String json, TypeReference<List<T>> typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.warn("协议配置JSON反序列化失败: {}", e.getMessage());
            return null;
        }
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
            List<RowImportData> rows = new ArrayList<>();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmptyRow(row, formatter)) {
                    continue;
                }
                RowImportData rowData = new RowImportData();
                rowData.setRowNumber(rowIndex + 1);
                rowData.setConfigName(readCellValue(row, formatter, 0));
                rowData.setProtocolIdText(readCellValue(row, formatter, 1));
                rowData.setProtocolName(readCellValue(row, formatter, 2));
                rowData.setUrlConfigJson(readCellValue(row, formatter, 3));
                rowData.setAuthConfigJson(readCellValue(row, formatter, 4));
                rowData.setTimeoutConnectText(readCellValue(row, formatter, 5));
                rowData.setTimeoutReadText(readCellValue(row, formatter, 6));
                rowData.setRetryCountText(readCellValue(row, formatter, 7));
                rowData.setRetryIntervalText(readCellValue(row, formatter, 8));
                rowData.setRetryConditionText(readCellValue(row, formatter, 9));
                rowData.setDataFormat(readCellValue(row, formatter, 10));
                rowData.setFormatConfig(readCellValue(row, formatter, 11));
                rowData.setAdditionalParams(readCellValue(row, formatter, 12));
                rowData.setStatusText(readCellValue(row, formatter, 13));
                rowData.setDescription(readCellValue(row, formatter, 14));
                rows.add(rowData);
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("解析导入文件失败: " + e.getMessage(), e);
        }
    }

    private ProtocolConfigCreateDTO toCreateDTO(RowImportData row) {
        ProtocolConfigCreateDTO dto = new ProtocolConfigCreateDTO();
        dto.setConfigName(requireText(row.getConfigName(), "配置名称不能为空"));
        dto.setProtocolId(parseLong(row.getProtocolIdText(), "协议类型ID不能为空且必须为数字"));
        dto.setProtocolName(requireText(row.getProtocolName(), "协议类型名称不能为空"));
        dto.setUrlConfigList(parseJsonList(row.getUrlConfigJson(), new TypeReference<List<ProtocolConfigCreateDTO.UrlConfigItemDTO>>() {
        }, "URL配置(JSON)格式不正确"));
        dto.setAuthConfigList(parseJsonList(row.getAuthConfigJson(), new TypeReference<List<ProtocolConfigCreateDTO.AuthConfigItemDTO>>() {
        }, "认证配置(JSON)格式不正确"));
        dto.setTimeoutConnect(parseIntegerOrNull(row.getTimeoutConnectText(), "连接超时必须为整数"));
        dto.setTimeoutRead(parseIntegerOrNull(row.getTimeoutReadText(), "读取超时必须为整数"));
        dto.setRetryCount(parseIntegerOrNull(row.getRetryCountText(), "重试次数必须为整数"));
        dto.setRetryInterval(parseIntegerOrNull(row.getRetryIntervalText(), "重试间隔必须为整数"));
        dto.setRetryCondition(normalizeRetryCondition(row.getRetryConditionText()));
        dto.setDataFormat(normalizeDataFormat(row.getDataFormat()));
        dto.setFormatConfig(blankToNull(row.getFormatConfig()));
        dto.setAdditionalParams(blankToNull(row.getAdditionalParams()));
        dto.setStatus(parseStatusOrNull(row.getStatusText()));
        dto.setDescription(blankToNull(row.getDescription()));
        return dto;
    }

    private <T> List<T> parseJsonList(String json, TypeReference<List<T>> typeRef, String errorMessage) {
        if (StringUtils.isBlank(json)) {
            log.debug("warning warning parseJsonList-json is empty");
            return null;
        }
        try {
            List<T> list = objectMapper.readValue(json, typeRef);
            if (list == null || list.isEmpty()) {
                throw new RuntimeException(errorMessage);
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException(errorMessage);
        }
    }

    private String readCellValue(Row row, DataFormatter formatter, int index) {
        return StringUtils.trimToEmpty(formatter.formatCellValue(row.getCell(index)));
    }

    private boolean isEmptyRow(Row row, DataFormatter formatter) {
        for (int i = 0; i <= 14; i++) {
            if (StringUtils.isNotBlank(formatter.formatCellValue(row.getCell(i)))) {
                return false;
            }
        }
        return true;
    }

    private String requireText(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }

    private Long parseLong(String value, String message) {
        if (StringUtils.isBlank(value) || !StringUtils.isNumeric(value.trim())) {
            throw new RuntimeException(message);
        }
        return Long.valueOf(value.trim());
    }

    private Integer parseIntegerOrNull(String value, String message) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (!trimmed.matches("^-?\\d+$")) {
            throw new RuntimeException(message);
        }
        return Integer.valueOf(trimmed);
    }

    private Integer parseStatusOrNull(String statusText) {
        if (StringUtils.isBlank(statusText)) {
            return null;
        }
        String normalized = statusText.trim().toUpperCase(Locale.ROOT);
        if (Arrays.asList("1", "启用", "ENABLED", "TRUE").contains(normalized)) {
            return 1;
        }
        if (Arrays.asList("0", "禁用", "DISABLED", "FALSE").contains(normalized)) {
            return 0;
        }
        throw new RuntimeException("状态不合法，仅支持 0/1、启用/禁用、ENABLED/DISABLED");
    }

    private String normalizeRetryCondition(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String normalized = value.replace('，', ',');
        String[] items = normalized.split(",");
        LinkedHashSet<String> validSet = new LinkedHashSet<>();
        for (String item : items) {
            String trimmed = item.trim();
            if (StringUtils.isBlank(trimmed)) {
                continue;
            }
            if (!Arrays.asList("1", "2", "3").contains(trimmed)) {
                throw new RuntimeException("重试触发条件仅支持 1，2，3（可用中文逗号分隔）");
            }
            validSet.add(trimmed);
        }
        return validSet.isEmpty() ? null : String.join(",", validSet);
    }

    private String normalizeDataFormat(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!Arrays.asList("JSON", "XML", "FORM", "TEXT", "BINARY").contains(normalized)) {
            throw new RuntimeException("数据格式仅支持 JSON/XML/FORM/TEXT/BINARY");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private byte[] buildFailureReportContent(List<RowFailure> failures) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("导入失败原因");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);
            String[] headers = {"行号", "配置名称", "协议类型ID", "协议类型名称", "状态", "失败原因"};
            int[] widths = {10, 24, 14, 20, 10, 50};
            writeHeaderRow(sheet, headerStyle, headers, widths);

            int rowIndex = 1;
            for (RowFailure failure : failures) {
                XSSFRow row = sheet.createRow(rowIndex++);
                setCellValue(row, 0, String.valueOf(failure.row.getRowNumber()), dataStyle);
                setCellValue(row, 1, failure.row.getConfigName(), dataStyle);
                setCellValue(row, 2, failure.row.getProtocolIdText(), dataStyle);
                setCellValue(row, 3, failure.row.getProtocolName(), dataStyle);
                setCellValue(row, 4, failure.row.getStatusText(), dataStyle);
                setCellValue(row, 5, failure.reason, dataStyle);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String buildFailureReportFileName() {
        return FAILURE_REPORT_FILE_PREFIX + LocalDateUtil.formatDateTime(LocalDateTime.now(), FILE_TIME_FORMATTER) + ".xlsx";
    }

    private void writeExportRows(XSSFSheet sheet, XSSFCellStyle dataStyle, List<ProtocolConfig> configs) {
        int rowIndex = 1;
        for (ProtocolConfig config : configs) {
            XSSFRow row = sheet.createRow(rowIndex++);
            setCellValue(row, 0, config.getConfigName(), dataStyle);
            setCellValue(row, 1, config.getProtocolId() == null ? "" : String.valueOf(config.getProtocolId()), dataStyle);
            setCellValue(row, 2, config.getProtocolName(), dataStyle);
            setCellValue(row, 3, defaultString(config.getUrlConfig(), ""), dataStyle);
            setCellValue(row, 4, defaultString(config.getAuthConfig(), ""), dataStyle);
            setCellValue(row, 5, config.getTimeoutConnect() == null ? "" : String.valueOf(config.getTimeoutConnect()), dataStyle);
            setCellValue(row, 6, config.getTimeoutRead() == null ? "" : String.valueOf(config.getTimeoutRead()), dataStyle);
            setCellValue(row, 7, config.getRetryCount() == null ? "" : String.valueOf(config.getRetryCount()), dataStyle);
            setCellValue(row, 8, config.getRetryInterval() == null ? "" : String.valueOf(config.getRetryInterval()), dataStyle);
            setCellValue(row, 9, formatRetryConditionForExport(config.getRetryCondition()), dataStyle);
            setCellValue(row, 10, config.getDataFormat(), dataStyle);
            setCellValue(row, 11, config.getFormatConfig(), dataStyle);
            setCellValue(row, 12, config.getAdditionalParams(), dataStyle);
            setCellValue(row, 13, toStatusText(config.getStatus()), dataStyle);
            setCellValue(row, 14, config.getDescription(), dataStyle);
        }
    }

    private String formatRetryConditionForExport(String retryCondition) {
        if (StringUtils.isBlank(retryCondition)) {
            return "";
        }
        return retryCondition.replace(",", "，");
    }

    private String toStatusText(Integer status) {
        if (status == null) {
            return "";
        }
        return status == 1 ? "启用" : "禁用";
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

    private void writeHeaderRow(XSSFSheet sheet, XSSFCellStyle headerStyle, String[] headers, int[] widths) {
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, widths[i] * 256);
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
        cell.setCellValue(defaultString(value, ""));
        cell.setCellStyle(style);
    }

    @lombok.Data
    private static class RowImportData {
        private Integer rowNumber;
        private String configName;
        private String protocolIdText;
        private String protocolName;
        private String urlConfigJson;
        private String authConfigJson;
        private String timeoutConnectText;
        private String timeoutReadText;
        private String retryCountText;
        private String retryIntervalText;
        private String retryConditionText;
        private String dataFormat;
        private String formatConfig;
        private String additionalParams;
        private String statusText;
        private String description;
    }

    private record RowFailure(RowImportData row, String reason) {
    }

    private Long getCurrentOperatorId() {
        Long currentUserId = securityService.getCurrentUserId();
        return currentUserId == null ? 1L : currentUserId;
    }
}
