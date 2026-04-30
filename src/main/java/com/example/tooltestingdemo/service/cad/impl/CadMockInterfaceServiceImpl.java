package com.example.tooltestingdemo.service.cad.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.tooltestingdemo.dto.cad.*;
import com.example.tooltestingdemo.entity.cad.*;
import com.example.tooltestingdemo.enums.cad.CadAuthTypeEnum;
import com.example.tooltestingdemo.enums.cad.CadConvertTypeEnum;
import com.example.tooltestingdemo.enums.cad.CadFileConvertTaskStatusEnum;
import com.example.tooltestingdemo.enums.cad.CadTypeEnum;
import com.example.tooltestingdemo.exception.BusinessException;
import com.example.tooltestingdemo.mapper.cad.*;
import com.example.tooltestingdemo.service.cad.CadMockInterfaceService;
import com.example.tooltestingdemo.vo.cad.CadFileConvertTaskVO;
import com.example.tooltestingdemo.vo.cad.CadConnectivityTestVO;
import com.example.tooltestingdemo.vo.cad.CadMockTestVO;
import com.example.tooltestingdemo.vo.cad.CadUnifiedExecuteVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CadMockInterfaceServiceImpl implements CadMockInterfaceService {

    private static final DateTimeFormatter TASK_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CadMockInterfaceMapper cadMockInterfaceMapper;
    private final CadDataConvertMappingMapper cadDataConvertMappingMapper;
    private final CadFileConvertConfigMapper cadFileConvertConfigMapper;
    private final CadFileConvertTaskMapper cadFileConvertTaskMapper;
    private final CadMockTestLogMapper cadMockTestLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<CadMockInterface> page(CadMockInterfaceQueryDTO dto) {
        CadMockInterfaceQueryDTO query = dto == null ? new CadMockInterfaceQueryDTO() : dto;
        String interfaceName = StringUtils.trimToNull(query.getInterfaceName());
        LambdaQueryWrapper<CadMockInterface> wrapper = new LambdaQueryWrapper<CadMockInterface>()
                .eq(StringUtils.isNotBlank(query.getCadType()), CadMockInterface::getCadType, normalizeUpper(query.getCadType()))
                .eq(StringUtils.isNotBlank(query.getConvertType()), CadMockInterface::getConvertType, normalizeUpper(query.getConvertType()))
                .eq(StringUtils.isNotBlank(query.getApplyFlow()), CadMockInterface::getApplyFlow, normalizeUpper(query.getApplyFlow()))
                .eq(query.getStatus() != null, CadMockInterface::getStatus, query.getStatus())
                .like(interfaceName != null, CadMockInterface::getInterfaceName, interfaceName)
                .orderByDesc(CadMockInterface::getUpdateTime)
                .orderByDesc(CadMockInterface::getId);
        return cadMockInterfaceMapper.selectPage(query.toPage(), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadMockInterface saveMockInterface(CadMockInterfaceCreateDTO dto) {
        validateCadType(dto.getCadType());
        validateConvertType(dto.getConvertType());
        validateJson(dto.getResponseBody(), "模拟响应内容必须是合法JSON");
        validateOptionalJson(dto.getRequestHeaders(), "请求头必须是合法JSON");
        validateOptionalJson(dto.getRequestParams(), "请求参数必须是合法JSON");
        validateOptionalJson(dto.getRequestBodyTemplate(), "请求体模板必须是合法JSON");
        validateAuthType(defaultIfBlank(dto.getAuthType(), CadAuthTypeEnum.NONE.getCode()));
        validateOptionalJson(dto.getAuthConfig(), "认证配置必须是合法JSON");

        CadMockInterface entity = dto.getId() == null ? new CadMockInterface() : requireMockInterface(dto.getId());
        entity.setInterfaceName(dto.getInterfaceName().trim());
        entity.setCadType(normalizeUpper(dto.getCadType()));
        entity.setConvertType(normalizeUpper(dto.getConvertType()));
        entity.setApplyFlow(normalizeUpper(dto.getApplyFlow()));
        entity.setRequestMethod(normalizeUpper(dto.getRequestMethod()));
        entity.setInterfaceUrl(dto.getInterfaceUrl().trim());
        entity.setRequestHeaders(trimToNull(dto.getRequestHeaders()));
        entity.setRequestParams(trimToNull(dto.getRequestParams()));
        entity.setRequestBodyTemplate(trimToNull(dto.getRequestBodyTemplate()));
        entity.setResponseBody(dto.getResponseBody().trim());
        entity.setSuccessField(defaultIfBlank(dto.getSuccessField(), "code"));
        entity.setSuccessValue(defaultIfBlank(dto.getSuccessValue(), "200"));
        String authType = defaultIfBlank(dto.getAuthType(), CadAuthTypeEnum.NONE.getCode()).toUpperCase(Locale.ROOT);
        entity.setAuthType(authType);
        entity.setAuthConfig(CadAuthTypeEnum.NONE.getCode().equals(authType) ? null : trimToNull(dto.getAuthConfig()));
        entity.setStatus(defaultIfNull(dto.getStatus(), 1));
        entity.setVersionNo(defaultIfBlank(dto.getVersionNo(), "V1.0"));
        entity.setOwnerId(dto.getOwnerId());
        entity.setOwnerName(trimToNull(dto.getOwnerName()));
        entity.setRemark(trimToNull(dto.getRemark()));

        if (entity.getId() == null) {
            cadMockInterfaceMapper.insert(entity);
        } else {
            cadMockInterfaceMapper.updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadMockInterface saveAuthConfig(CadAuthConfigSaveDTO dto) {
        CadMockInterface entity = requireMockInterface(dto.getMockInterfaceId());
        String authType = normalizeUpper(dto.getAuthType());
        validateAuthType(authType);
        validateOptionalJson(dto.getAuthConfig(), "认证配置必须是合法JSON");
        entity.setAuthType(authType);
        entity.setAuthConfig(CadAuthTypeEnum.NONE.getCode().equals(authType) ? null : trimToNull(dto.getAuthConfig()));
        cadMockInterfaceMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CadDataConvertMapping> saveDataMappings(CadDataConvertSaveBatchDTO dto) {
        CadMockInterface mockInterface = requireMockInterface(dto.getMockInterfaceId());
        Set<String> targetKeys = new HashSet<>();
        for (CadDataConvertMappingDTO mapping : dto.getMappings()) {
            String targetKey = normalizeUpper(mapping.getTargetModule()) + "|" + mapping.getTargetField().trim();
            if (!targetKeys.add(targetKey)) {
                throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "同一接口下目标模块+目标字段不能重复");
            }
        }

        cadDataConvertMappingMapper.delete(new LambdaQueryWrapper<CadDataConvertMapping>()
                .eq(CadDataConvertMapping::getMockInterfaceId, dto.getMockInterfaceId()));

        List<CadDataConvertMapping> saved = new ArrayList<>();
        for (CadDataConvertMappingDTO item : dto.getMappings()) {
            CadDataConvertMapping entity = new CadDataConvertMapping();
            entity.setMockInterfaceId(dto.getMockInterfaceId());
            entity.setCadType(mockInterface.getCadType());
            entity.setApplyFlow(mockInterface.getApplyFlow());
            entity.setConvertDirection(defaultIfBlank(item.getConvertDirection(), "RESPONSE"));
            entity.setSourceField(item.getSourceField().trim());
            entity.setSourceFieldName(trimToNull(item.getSourceFieldName()));
            entity.setTargetModule(normalizeUpper(item.getTargetModule()));
            entity.setTargetField(item.getTargetField().trim());
            entity.setTargetFieldName(trimToNull(item.getTargetFieldName()));
            entity.setFieldType(defaultIfBlank(item.getFieldType(), "STRING"));
            entity.setRequired(defaultIfNull(item.getRequired(), 0));
            entity.setDefaultValue(trimToNull(item.getDefaultValue()));
            entity.setTransformRule(trimToNull(item.getTransformRule()));
            entity.setSortNo(defaultIfNull(item.getSortNo(), 0));
            entity.setStatus(defaultIfNull(item.getStatus(), 1));
            cadDataConvertMappingMapper.insert(entity);
            saved.add(entity);
        }
        return saved;
    }

    @Override
    public List<CadDataConvertMapping> listDataMappings(Long mockInterfaceId) {
        requireMockInterface(mockInterfaceId);
        return cadDataConvertMappingMapper.selectList(new LambdaQueryWrapper<CadDataConvertMapping>()
                .eq(CadDataConvertMapping::getMockInterfaceId, mockInterfaceId)
                .orderByAsc(CadDataConvertMapping::getSortNo)
                .orderByAsc(CadDataConvertMapping::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadFileConvertConfig saveFileConvertConfig(CadFileConvertConfigSaveDTO dto) {
        validateCadType(dto.getCadType());
        validateOptionalJson(dto.getRequestTemplate(), "转换请求模板必须是合法JSON");
        validateOptionalJson(dto.getResponseTemplate(), "转换响应模板必须是合法JSON");

        String cadType = normalizeUpper(dto.getCadType());
        String applyFlow = normalizeUpper(dto.getApplyFlow());
        String sourceFormat = normalizeFormat(dto.getSourceFormat());
        String targetFormat = normalizeFormat(dto.getTargetFormat());

        LambdaQueryWrapper<CadFileConvertConfig> uniqueWrapper = new LambdaQueryWrapper<CadFileConvertConfig>()
                .eq(CadFileConvertConfig::getCadType, cadType)
                .eq(CadFileConvertConfig::getApplyFlow, applyFlow)
                .eq(CadFileConvertConfig::getSourceFormat, sourceFormat)
                .eq(CadFileConvertConfig::getTargetFormat, targetFormat)
                .eq(CadFileConvertConfig::getStatus, 1);
        if (dto.getId() != null) {
            uniqueWrapper.ne(CadFileConvertConfig::getId, dto.getId());
        }
        if (cadFileConvertConfigMapper.selectCount(uniqueWrapper) > 0 && defaultIfNull(dto.getStatus(), 1) == 1) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "同一CAD类型+流程+源格式+目标格式下只能有一条启用配置");
        }

        CadFileConvertConfig entity = dto.getId() == null ? new CadFileConvertConfig() : requireFileConvertConfig(dto.getId());
        entity.setMockInterfaceId(dto.getMockInterfaceId());
        entity.setCadType(cadType);
        entity.setApplyFlow(applyFlow);
        entity.setSourceFormat(sourceFormat);
        entity.setTargetFormat(targetFormat);
        entity.setConvertUrl(dto.getConvertUrl().trim());
        entity.setAsyncConvert(defaultIfNull(dto.getAsyncConvert(), 1));
        entity.setTimeoutSeconds(defaultIfNull(dto.getTimeoutSeconds(), 60));
        entity.setGeneratePreview(defaultIfNull(dto.getGeneratePreview(), 0));
        entity.setKeepSourceFile(defaultIfNull(dto.getKeepSourceFile(), 1));
        entity.setOverwriteTargetFile(defaultIfNull(dto.getOverwriteTargetFile(), 0));
        entity.setRequestTemplate(trimToNull(dto.getRequestTemplate()));
        entity.setResponseTemplate(trimToNull(dto.getResponseTemplate()));
        entity.setStatus(defaultIfNull(dto.getStatus(), 1));
        entity.setRemark(trimToNull(dto.getRemark()));

        if (entity.getId() == null) {
            cadFileConvertConfigMapper.insert(entity);
        } else {
            cadFileConvertConfigMapper.updateById(entity);
        }
        return entity;
    }

    @Override
    public List<CadFileConvertConfig> listFileConvertConfigs(CadFileConvertConfigQueryDTO dto) {
        CadFileConvertConfigQueryDTO query = dto == null ? new CadFileConvertConfigQueryDTO() : dto;
        String sourceFormat = StringUtils.isNotBlank(query.getSourceFormat()) ? normalizeFormat(query.getSourceFormat()) : null;
        String targetFormat = StringUtils.isNotBlank(query.getTargetFormat()) ? normalizeFormat(query.getTargetFormat()) : null;
        return cadFileConvertConfigMapper.selectList(new LambdaQueryWrapper<CadFileConvertConfig>()
                .eq(StringUtils.isNotBlank(query.getCadType()), CadFileConvertConfig::getCadType, normalizeUpper(query.getCadType()))
                .eq(StringUtils.isNotBlank(query.getApplyFlow()), CadFileConvertConfig::getApplyFlow, normalizeUpper(query.getApplyFlow()))
                .eq(sourceFormat != null, CadFileConvertConfig::getSourceFormat, sourceFormat)
                .eq(targetFormat != null, CadFileConvertConfig::getTargetFormat, targetFormat)
                .eq(query.getStatus() != null, CadFileConvertConfig::getStatus, query.getStatus())
                .orderByDesc(CadFileConvertConfig::getUpdateTime)
                .orderByDesc(CadFileConvertConfig::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadFileConvertTaskVO executeFileConvert(CadFileConvertExecuteDTO dto) {
        String sourceFormat = normalizeFormat(dto.getSourceFormat());
        assertSourceFileMatches(dto.getSourceFileName(), sourceFormat);
        CadFileConvertConfig config = findFileConvertConfig(
                normalizeUpper(dto.getCadType()),
                normalizeUpper(dto.getApplyFlow()),
                sourceFormat,
                normalizeFormat(dto.getTargetFormat()),
                true
        );
        CadFileConvertTask task = createTask(config, dto.getSourceFileId(), dto.getSourceFileName(), sourceFormat, normalizeFormat(dto.getTargetFormat()));
        if (Objects.equals(config.getAsyncConvert(), 0)) {
            finishTask(task, true, null);
        }
        return toTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadFileConvertTaskVO getTask(String taskNo) {
        CadFileConvertTask task = cadFileConvertTaskMapper.selectOne(new LambdaQueryWrapper<CadFileConvertTask>()
                .eq(CadFileConvertTask::getTaskNo, taskNo)
                .last("limit 1"));
        if (task == null) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.NOT_FOUND, "转换任务不存在");
        }
        if (Objects.equals(task.getTaskStatus(), CadFileConvertTaskStatusEnum.WAITING.getCode())) {
            finishTask(task, true, null);
        }
        return toTaskVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadUnifiedExecuteVO executeUnified(CadMockUnifiedExecuteDTO dto) {
        CadMockInterface mockInterface = findMockInterface(normalizeUpper(dto.getCadType()), normalizeUpper(dto.getApplyFlow()));
        Object rawResponse = parseJson(mockInterface.getResponseBody());
        Map<String, Object> mappedData = applyDataMappings(mockInterface, rawResponse);

        CadUnifiedExecuteVO vo = new CadUnifiedExecuteVO();
        vo.setMappedData(mappedData);

        if (shouldHandleFile(mockInterface.getConvertType()) && StringUtils.isNotBlank(dto.getSourceFileName())) {
            String sourceFormat = StringUtils.isNotBlank(dto.getSourceFormat())
                    ? normalizeFormat(dto.getSourceFormat())
                    : extractFormat(dto.getSourceFileName());
            assertSourceFileMatches(dto.getSourceFileName(), sourceFormat);
            CadFileConvertConfig config = findFileConvertConfig(
                    mockInterface.getCadType(),
                    mockInterface.getApplyFlow(),
                    sourceFormat,
                    StringUtils.isNotBlank(dto.getTargetFormat()) ? normalizeFormat(dto.getTargetFormat()) : null,
                    false
            );
            if (config != null) {
                CadFileConvertTask task = createTask(config, defaultIfNull(dto.getSourceFileId(), 0L), dto.getSourceFileName(), sourceFormat, config.getTargetFormat());
                finishTask(task, true, null);
                vo.setFileConvert(toTaskVO(task));
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CadMockTestVO test(CadMockTestDTO dto) {
        LocalDateTime start = LocalDateTime.now();
        CadMockInterface mockInterface = dto.getMockInterfaceId() != null
                ? requireMockInterface(dto.getMockInterfaceId())
                : findMockInterface(normalizeUpper(dto.getCadType()), normalizeUpper(dto.getApplyFlow()));

        CadMockTestLog logEntity = new CadMockTestLog();
        logEntity.setMockInterfaceId(mockInterface.getId());
        logEntity.setCadType(mockInterface.getCadType());
        logEntity.setConvertType(mockInterface.getConvertType());
        logEntity.setRequestMethod(mockInterface.getRequestMethod());
        logEntity.setInterfaceUrl(mockInterface.getInterfaceUrl());
        logEntity.setRequestContent(writeJsonSilently(dto.getRequestData()));

        CadMockTestVO vo = new CadMockTestVO();
        vo.setMockInterfaceId(mockInterface.getId());
        try {
            Object rawResponse = parseJson(mockInterface.getResponseBody());
            Map<String, Object> converted = applyDataMappings(mockInterface, rawResponse);
            vo.setSuccess(Boolean.TRUE);
            vo.setRawResponse(rawResponse);
            vo.setConvertedData(converted);
            logEntity.setResponseContent(writeJsonSilently(rawResponse));
            logEntity.setConvertedContent(writeJsonSilently(converted));

            if (StringUtils.isNotBlank(dto.getSourceFileName()) && shouldHandleFile(mockInterface.getConvertType())) {
                CadMockUnifiedExecuteDTO executeDTO = new CadMockUnifiedExecuteDTO();
                executeDTO.setCadType(mockInterface.getCadType());
                executeDTO.setApplyFlow(mockInterface.getApplyFlow());
                executeDTO.setSourceFileId(dto.getSourceFileId());
                executeDTO.setSourceFileName(dto.getSourceFileName());
                executeDTO.setSourceFormat(dto.getSourceFormat());
                executeDTO.setTargetFormat(dto.getTargetFormat());
                executeDTO.setRequestData(dto.getRequestData());
                CadUnifiedExecuteVO executeVO = executeUnified(executeDTO);
                vo.setFileConvert(executeVO.getFileConvert());
                logEntity.setFileConvertResult(writeJsonSilently(executeVO.getFileConvert()));
            }

            logEntity.setTestResult(1);
        } catch (Exception ex) {
            vo.setSuccess(Boolean.FALSE);
            vo.setErrorMessage(ex.getMessage());
            logEntity.setTestResult(0);
            logEntity.setErrorMessage(ex.getMessage());
        }

        logEntity.setCostTimeMs(Math.max(1L, java.time.Duration.between(start, LocalDateTime.now()).toMillis()));
        cadMockTestLogMapper.insert(logEntity);
        return vo;
    }

    @Override
    public CadConnectivityTestVO connectivityTest(CadConnectivityTestDTO dto) {
        CadMockInterface mockInterface = dto.getMockInterfaceId() == null ? null : requireMockInterface(dto.getMockInterfaceId());
        String requestMethod = normalizeUpper(firstNotBlank(dto.getRequestMethod(), mockInterface == null ? null : mockInterface.getRequestMethod(), "GET"));
        String interfaceUrl = firstNotBlank(dto.getInterfaceUrl(), mockInterface == null ? null : mockInterface.getInterfaceUrl(), null);
        if (StringUtils.isBlank(interfaceUrl)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "接口地址不能为空");
        }

        Map<String, String> headers = parseStringMap(firstNotBlank(dto.getRequestHeaders(), mockInterface == null ? null : mockInterface.getRequestHeaders(), null), "请求头必须是合法JSON对象");
        Map<String, String> params = parseStringMap(firstNotBlank(dto.getRequestParams(), mockInterface == null ? null : mockInterface.getRequestParams(), null), "请求参数必须是合法JSON对象");
        String authType = normalizeUpper(firstNotBlank(dto.getAuthType(), mockInterface == null ? null : mockInterface.getAuthType(), CadAuthTypeEnum.NONE.getCode()));
        String authConfig = firstNotBlank(dto.getAuthConfig(), mockInterface == null ? null : mockInterface.getAuthConfig(), null);
        validateAuthType(authType);
        applyAuth(headers, params, authType, authConfig);

        URI uri = buildRequestUri(dto.getBaseUrl(), interfaceUrl, params);
        int timeoutSeconds = defaultIfNull(dto.getTimeoutSeconds(), 10);
        if (timeoutSeconds <= 0 || timeoutSeconds > 300) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "超时时间必须在1到300秒之间");
        }

        String requestBody = firstNotBlank(dto.getRequestBody(), mockInterface == null ? null : mockInterface.getRequestBodyTemplate(), null);
        HttpRequest request = buildHttpRequest(uri, requestMethod, headers, requestBody, timeoutSeconds);

        CadConnectivityTestVO vo = new CadConnectivityTestVO();
        vo.setRequestMethod(requestMethod);
        vo.setRequestUrl(uri.toString());
        long start = System.currentTimeMillis();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            vo.setStatusCode(response.statusCode());
            vo.setResponseHeaders(response.headers().map());
            vo.setResponseBody(response.body());
            vo.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);
        } catch (Exception ex) {
            vo.setSuccess(Boolean.FALSE);
            vo.setErrorMessage(ex.getMessage());
        } finally {
            vo.setCostTimeMs(Math.max(1L, System.currentTimeMillis() - start));
        }
        return vo;
    }

    private CadMockInterface requireMockInterface(Long id) {
        CadMockInterface entity = cadMockInterfaceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.NOT_FOUND, "模拟接口不存在");
        }
        return entity;
    }

    private CadFileConvertConfig requireFileConvertConfig(Long id) {
        CadFileConvertConfig entity = cadFileConvertConfigMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.NOT_FOUND, "文件转换配置不存在");
        }
        return entity;
    }

    private CadMockInterface findMockInterface(String cadType, String applyFlow) {
        List<CadMockInterface> list = cadMockInterfaceMapper.selectList(new LambdaQueryWrapper<CadMockInterface>()
                .eq(CadMockInterface::getCadType, cadType)
                .eq(CadMockInterface::getApplyFlow, applyFlow)
                .eq(CadMockInterface::getStatus, 1)
                .orderByDesc(CadMockInterface::getUpdateTime)
                .orderByDesc(CadMockInterface::getId));
        if (list.isEmpty()) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.NOT_FOUND, "未找到启用的CAD模拟接口配置");
        }
        return list.stream()
                .sorted(Comparator.comparingInt(this::convertTypePriority))
                .findFirst()
                .orElse(list.get(0));
    }

    private CadFileConvertConfig findFileConvertConfig(String cadType, String applyFlow, String sourceFormat, String targetFormat, boolean required) {
        LambdaQueryWrapper<CadFileConvertConfig> wrapper = new LambdaQueryWrapper<CadFileConvertConfig>()
                .eq(CadFileConvertConfig::getCadType, cadType)
                .eq(CadFileConvertConfig::getApplyFlow, applyFlow)
                .eq(CadFileConvertConfig::getSourceFormat, sourceFormat)
                .eq(CadFileConvertConfig::getStatus, 1)
                .orderByDesc(CadFileConvertConfig::getUpdateTime)
                .orderByDesc(CadFileConvertConfig::getId);
        if (StringUtils.isNotBlank(targetFormat)) {
            wrapper.eq(CadFileConvertConfig::getTargetFormat, targetFormat);
        }
        List<CadFileConvertConfig> list = cadFileConvertConfigMapper.selectList(wrapper);
        if (list.isEmpty()) {
            if (required) {
                throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.NOT_FOUND, "未找到匹配的文件转换配置");
            }
            return null;
        }
        return list.get(0);
    }

    private CadFileConvertTask createTask(CadFileConvertConfig config, Long sourceFileId, String sourceFileName, String sourceFormat, String targetFormat) {
        CadFileConvertTask task = new CadFileConvertTask();
        task.setTaskNo(buildTaskNo());
        task.setFileConvertConfigId(config.getId());
        task.setCadType(config.getCadType());
        task.setApplyFlow(config.getApplyFlow());
        task.setSourceFileId(sourceFileId);
        task.setSourceFileName(sourceFileName);
        task.setSourceFormat(sourceFormat);
        task.setTargetFormat(targetFormat);
        task.setTaskStatus(CadFileConvertTaskStatusEnum.WAITING.getCode());
        task.setRequestContent(writeJsonSilently(Map.of(
                "sourceFileId", sourceFileId,
                "sourceFileName", sourceFileName,
                "sourceFormat", sourceFormat,
                "targetFormat", targetFormat,
                "convertUrl", config.getConvertUrl()
        )));
        cadFileConvertTaskMapper.insert(task);
        return task;
    }

    private void finishTask(CadFileConvertTask task, boolean success, String errorMessage) {
        task.setTaskStatus(success ? CadFileConvertTaskStatusEnum.SUCCESS.getCode() : CadFileConvertTaskStatusEnum.FAILED.getCode());
        if (success) {
            task.setTargetFileId(Math.abs(ThreadLocalRandom.current().nextLong(10000L, 999999999L)));
            task.setTargetFileName(buildTargetFileName(task.getSourceFileName(), task.getTargetFormat()));
            task.setResponseContent(writeJsonSilently(Map.of(
                    "targetFileId", task.getTargetFileId(),
                    "targetFileName", task.getTargetFileName(),
                    "targetFormat", task.getTargetFormat()
            )));
            task.setCostTimeMs(ThreadLocalRandom.current().nextLong(50L, 300L));
        } else {
            task.setErrorMessage(errorMessage);
            task.setResponseContent(writeJsonSilently(Map.of("errorMessage", errorMessage)));
        }
        cadFileConvertTaskMapper.updateById(task);
    }

    private Map<String, Object> applyDataMappings(CadMockInterface mockInterface, Object rawResponse) {
        if (!shouldHandleData(mockInterface.getConvertType())) {
            return rawResponse instanceof Map<?, ?> map ? castMap(map) : new LinkedHashMap<>();
        }
        List<CadDataConvertMapping> mappings = cadDataConvertMappingMapper.selectList(new LambdaQueryWrapper<CadDataConvertMapping>()
                .eq(CadDataConvertMapping::getMockInterfaceId, mockInterface.getId())
                .eq(CadDataConvertMapping::getStatus, 1)
                .orderByAsc(CadDataConvertMapping::getSortNo)
                .orderByAsc(CadDataConvertMapping::getId));
        if (mappings.isEmpty()) {
            return rawResponse instanceof Map<?, ?> map ? castMap(map) : new LinkedHashMap<>();
        }

        JsonNode rootNode = objectMapper.valueToTree(rawResponse);
        Map<String, Object> result = new LinkedHashMap<>();
        for (CadDataConvertMapping mapping : mappings) {
            JsonNode valueNode = rootNode.at(toJsonPointer(mapping.getSourceField()));
            Object value = valueNode.isMissingNode() || valueNode.isNull() ? null : objectMapper.convertValue(valueNode, Object.class);
            if (value == null && StringUtils.isNotBlank(mapping.getDefaultValue())) {
                value = mapping.getDefaultValue();
            }
            value = transformValue(value, mapping.getFieldType(), mapping.getTransformRule());
            if (Objects.equals(mapping.getRequired(), 1) && isEmptyValue(value)) {
                throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "必填字段转换后不能为空: " + mapping.getTargetField());
            }
            result.put(mapping.getTargetField(), value);
        }
        return result;
    }

    private Object transformValue(Object value, String fieldType, String transformRule) {
        if (value == null) {
            return null;
        }
        String fieldTypeCode = defaultIfBlank(fieldType, "STRING").toUpperCase(Locale.ROOT);
        Object converted = switch (fieldTypeCode) {
            case "NUMBER" -> value instanceof Number ? value : Double.valueOf(String.valueOf(value));
            case "BOOLEAN" -> value instanceof Boolean ? value : Boolean.valueOf(String.valueOf(value));
            case "JSON" -> value;
            default -> String.valueOf(value);
        };
        if (!(converted instanceof String text) || StringUtils.isBlank(transformRule)) {
            return converted;
        }
        return applyTransformRule(text, transformRule);
    }

    private String applyTransformRule(String value, String transformRule) {
        String result = value;
        String[] rules = transformRule.split(",");
        for (String rule : rules) {
            String normalized = rule.trim().toUpperCase(Locale.ROOT);
            if ("TRIM".equals(normalized)) {
                result = result.trim();
            } else if ("UPPERCASE".equals(normalized)) {
                result = result.toUpperCase(Locale.ROOT);
            } else if ("LOWERCASE".equals(normalized)) {
                result = result.toLowerCase(Locale.ROOT);
            }
        }
        return result;
    }

    private Object parseJson(String content) {
        try {
            return objectMapper.readValue(content, new TypeReference<>() { });
        } catch (JsonProcessingException e) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "JSON解析失败: " + e.getOriginalMessage(), e);
        }
    }

    private void validateJson(String content, String errorMessage) {
        try {
            objectMapper.readTree(content);
        } catch (Exception ex) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, errorMessage);
        }
    }

    private void validateOptionalJson(String content, String errorMessage) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        validateJson(content, errorMessage);
    }

    private void validateCadType(String cadType) {
        if (!CadTypeEnum.contains(cadType)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "不支持的CAD类型: " + cadType);
        }
    }

    private void validateConvertType(String convertType) {
        if (!CadConvertTypeEnum.contains(convertType)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "不支持的转换类型: " + convertType);
        }
    }

    private void validateAuthType(String authType) {
        if (!CadAuthTypeEnum.contains(authType)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "不支持的认证方式: " + authType);
        }
    }

    private URI buildRequestUri(String baseUrl, String interfaceUrl, Map<String, String> params) {
        String targetUrl = interfaceUrl.trim();
        URI rawUri = URI.create(targetUrl);
        if (!rawUri.isAbsolute()) {
            if (StringUtils.isBlank(baseUrl)) {
                throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "相对接口地址需要提供baseUrl");
            }
            targetUrl = joinUrl(baseUrl.trim(), targetUrl);
            rawUri = URI.create(targetUrl);
        }
        String scheme = rawUri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "接口地址仅支持HTTP或HTTPS");
        }
        String query = buildQuery(rawUri.getRawQuery(), params);
        return URI.create(rawUri.getScheme() + "://" + rawUri.getRawAuthority()
                + defaultIfBlank(rawUri.getRawPath(), "/")
                + (StringUtils.isBlank(query) ? "" : "?" + query));
    }

    private HttpRequest buildHttpRequest(URI uri, String method, Map<String, String> headers, String requestBody, int timeoutSeconds) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(timeoutSeconds));
        headers.forEach(builder::header);
        boolean hasBody = StringUtils.isNotBlank(requestBody);
        if (hasBody && headers.keySet().stream().noneMatch(key -> "content-type".equalsIgnoreCase(key))) {
            builder.header("Content-Type", "application/json");
        }
        if (Set.of("GET", "DELETE").contains(method)) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else if (Set.of("POST", "PUT", "PATCH").contains(method)) {
            builder.method(method, hasBody ? HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8) : HttpRequest.BodyPublishers.noBody());
        } else {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "不支持的请求方式: " + method);
        }
        return builder.build();
    }

    private void applyAuth(Map<String, String> headers, Map<String, String> params, String authType, String authConfig) {
        if (CadAuthTypeEnum.NONE.getCode().equals(authType)) {
            return;
        }
        Map<String, String> config = parseStringMap(authConfig, "认证配置必须是合法JSON对象");
        switch (authType) {
            case "BASIC" -> {
                String username = requiredConfig(config, "username");
                String password = requiredConfig(config, "password");
                String credential = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
                headers.put("Authorization", "Basic " + credential);
            }
            case "BEARER" -> headers.put("Authorization", "Bearer " + requiredConfig(config, "token"));
            case "API_KEY" -> {
                String name = requiredConfig(config, "name");
                String value = requiredConfig(config, "value");
                String location = defaultIfBlank(config.get("location"), "HEADER").toUpperCase(Locale.ROOT);
                if ("QUERY".equals(location)) {
                    params.put(name, value);
                } else {
                    headers.put(name, value);
                }
            }
            case "CUSTOM_HEADERS" -> headers.putAll(config);
            default -> throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "不支持的认证方式: " + authType);
        }
    }

    private String requiredConfig(Map<String, String> config, String key) {
        String value = config.get(key);
        if (StringUtils.isBlank(value)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "认证配置缺少字段: " + key);
        }
        return value;
    }

    private Map<String, String> parseStringMap(String content, String errorMessage) {
        if (StringUtils.isBlank(content)) {
            return new LinkedHashMap<>();
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            if (!node.isObject()) {
                throw new IllegalArgumentException(errorMessage);
            }
            Map<String, String> result = new LinkedHashMap<>();
            node.fields().forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue().isTextual() ? entry.getValue().asText() : entry.getValue().toString()));
            return result;
        } catch (Exception ex) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, errorMessage);
        }
    }

    private String buildQuery(String rawQuery, Map<String, String> params) {
        StringBuilder builder = new StringBuilder(StringUtils.defaultString(rawQuery));
        params.forEach((key, value) -> {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(urlEncode(key)).append('=').append(urlEncode(value));
        });
        return builder.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8);
    }

    private String joinUrl(String baseUrl, String path) {
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean shouldHandleData(String convertType) {
        String normalized = normalizeUpper(convertType);
        return CadConvertTypeEnum.DATA.getCode().equals(normalized) || CadConvertTypeEnum.BOTH.getCode().equals(normalized);
    }

    private boolean shouldHandleFile(String convertType) {
        String normalized = normalizeUpper(convertType);
        return CadConvertTypeEnum.FILE.getCode().equals(normalized) || CadConvertTypeEnum.BOTH.getCode().equals(normalized);
    }

    private int convertTypePriority(CadMockInterface item) {
        return switch (normalizeUpper(item.getConvertType())) {
            case "BOTH" -> 0;
            case "DATA" -> 1;
            case "FILE" -> 2;
            default -> 9;
        };
    }

    private CadFileConvertTaskVO toTaskVO(CadFileConvertTask task) {
        CadFileConvertTaskVO vo = new CadFileConvertTaskVO();
        vo.setTaskNo(task.getTaskNo());
        vo.setTaskStatus(task.getTaskStatus());
        vo.setTargetFileId(task.getTargetFileId());
        vo.setTargetFileName(task.getTargetFileName());
        vo.setTargetFormat(task.getTargetFormat());
        vo.setErrorMessage(task.getErrorMessage());
        return vo;
    }

    private String buildTaskNo() {
        return "CAD_CONVERT_" + LocalDateTime.now().format(TASK_TIME_FORMATTER) + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private String buildTargetFileName(String sourceFileName, String targetFormat) {
        int dotIndex = sourceFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? sourceFileName.substring(0, dotIndex) : sourceFileName;
        return baseName + normalizeFormat(targetFormat);
    }

    private void assertSourceFileMatches(String sourceFileName, String sourceFormat) {
        if (!extractFormat(sourceFileName).equalsIgnoreCase(sourceFormat)) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "文件后缀与源格式不匹配");
        }
    }

    private String extractFormat(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new BusinessException(com.example.tooltestingdemo.common.ErrorStatus.BAD_REQUEST, "源文件缺少扩展名");
        }
        return normalizeFormat(fileName.substring(dotIndex));
    }

    private String normalizeFormat(String format) {
        String value = format.trim();
        return value.startsWith(".") ? value : "." + value;
    }

    private String normalizeUpper(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value.trim();
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Long defaultIfNull(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    private boolean isEmptyValue(Object value) {
        return value == null || (value instanceof String text && StringUtils.isBlank(text));
    }

    private String toJsonPointer(String path) {
        if (StringUtils.isBlank(path)) {
            return "";
        }
        String[] parts = path.trim().split("\\.");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                builder.append('/').append(part);
            }
        }
        return builder.toString();
    }

    private String writeJsonSilently(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败: {}", e.getOriginalMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> source) {
        return (Map<String, Object>) source;
    }
}
