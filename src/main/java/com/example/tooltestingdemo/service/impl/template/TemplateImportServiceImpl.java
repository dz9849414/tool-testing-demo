package com.example.tooltestingdemo.service.impl.template;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.dto.TemplateImportDTO;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateImportExport;
import com.example.tooltestingdemo.enums.TemplateEnums;
import com.example.tooltestingdemo.mapper.template.InterfaceTemplateMapper;
import com.example.tooltestingdemo.mapper.template.TemplateImportExportMapper;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.service.template.TemplateImportService;
import com.example.tooltestingdemo.service.template.parser.JsonTemplateParser;
import com.example.tooltestingdemo.service.template.parser.TemplateParser;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.example.tooltestingdemo.vo.TemplateImportResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 模板导入导出服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateImportServiceImpl implements TemplateImportService {

    private final InterfaceTemplateService templateService;
    private final InterfaceTemplateMapper templateMapper;
    private final TemplateImportExportMapper importExportMapper;
    private final JsonTemplateParser jsonTemplateParser;

    private static final Long DEFAULT_CREATE_ID = 1L;
    private static final String DEFAULT_CREATE_NAME = "管理员";
    private static final String EXTENSION_JSON = "json";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateImportResultVO importTemplates(TemplateImportDTO dto) {
        long startTime = System.currentTimeMillis();
        TemplateImportExport record = createImportRecord(dto);
        
        try {
            String content = readFileContent(dto.getFile());
            TemplateParser parser = getParser(dto.getFormat(), content);
            
            if (!parser.validate(content)) {
                return buildErrorResult("文件格式不正确: " + parser.getDescription());
            }
            
            List<InterfaceTemplateDTO> templates = parser.parse(content);
            if (CollectionUtils.isEmpty(templates)) {
                return buildErrorResult("未找到可导入的模板");
            }
            
            return processImport(templates, dto, record, startTime);
            
        } catch (Exception e) {
            log.error("模板导入失败", e);
            updateRecordFailed(record, e.getMessage());
            return buildErrorResult(e.getMessage());
        }
    }

    @Override
    public TemplateImportResultVO validateImport(TemplateImportDTO dto) {
        try {
            String content = readFileContent(dto.getFile());
            TemplateParser parser = getParser(dto.getFormat(), content);
            
            if (!parser.validate(content)) {
                return buildErrorResult("文件格式不正确: " + parser.getDescription());
            }
            
            List<InterfaceTemplateDTO> templates = parser.parse(content);
            return TemplateImportResultVO.builder()
                .success(true)
                .message("验证通过，共发现 " + templates.size() + " 个模板")
                .totalCount(templates.size())
                .importTime(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            return buildErrorResult(e.getMessage());
        }
    }

    @Override
    public String exportToJson(Long[] templateIds) {
        if (templateIds == null || templateIds.length == 0) return "[]";
        
        return JSON.toJSONString(
            Arrays.stream(templateIds)
                .map(templateService::getTemplateDetail)
                .filter(Objects::nonNull)
                .map(this::convertToJsonObject)
                .collect(Collectors.toList())
        );
    }

    @Override
    public String exportToPostman(Long[] templateIds) {
        JSONObject collection = new JSONObject();
        collection.put("info", buildPostmanInfo());
        
        JSONArray items = new JSONArray();
        if (templateIds != null) {
            Arrays.stream(templateIds)
                .map(templateService::getTemplateDetail)
                .filter(Objects::nonNull)
                .forEach(vo -> items.add(convertToPostmanItem(vo)));
        }
        collection.put("item", items);
        
        return JSON.toJSONString(collection);
    }

    // ========== 私有方法 ==========

    private TemplateImportExport createImportRecord(TemplateImportDTO dto) {
        TemplateImportExport record = new TemplateImportExport();
        record.setOperationType(TemplateEnums.OperationType.IMPORT.getCode());
        record.setFileName(dto.getFile().getOriginalFilename());
        record.setFileFormat(getFormatFromDTO(dto));
        record.setStatus(TemplateEnums.ImportExportStatus.PROCESSING.getCode());
        record.setStartTime(LocalDateTime.now());
        record.setCreateId(DEFAULT_CREATE_ID);
        record.setCreateName(DEFAULT_CREATE_NAME);
        importExportMapper.insert(record);
        return record;
    }

    private TemplateImportResultVO processImport(List<InterfaceTemplateDTO> templates, TemplateImportDTO dto, 
                                                  TemplateImportExport record, long startTime) {
        String strategy = Optional.ofNullable(dto.getStrategy()).orElse(TemplateEnums.ImportStrategy.SKIP.getCode()).toLowerCase();
        Long folderId = dto.getFolderId();
        
        List<TemplateImportResultVO.ImportedTemplateVO> importedList = new ArrayList<>();
        List<TemplateImportResultVO.ImportErrorVO> errors = new ArrayList<>();
        Set<Long> importedIds = new HashSet<>();
        
        int[] counts = {0, 0, 0}; // success, skip, fail
        
        IntStream.range(0, templates.size()).forEach(i -> {
            InterfaceTemplateDTO templateDTO = templates.get(i);
            try {
                processSingleTemplate(templateDTO, folderId, strategy, importedList, importedIds, counts);
            } catch (Exception e) {
                log.error("导入模板失败: {}", templateDTO.getName(), e);
                counts[2]++;
                errors.add(TemplateImportResultVO.ImportErrorVO.builder()
                    .templateName(templateDTO.getName())
                    .errorMessage(e.getMessage())
                    .rowNumber(i + 1)
                    .build());
            }
        });
        
        updateRecordSuccess(record, counts, importedIds);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("模板导入完成: 总计={}, 成功={}, 跳过={}, 失败={}, 耗时={}ms", 
            templates.size(), counts[0], counts[1], counts[2], duration);
        
        return TemplateImportResultVO.builder()
            .success(counts[2] == 0)
            .message(buildMessage(counts))
            .totalCount(templates.size())
            .successCount(counts[0])
            .failCount(counts[2])
            .skipCount(counts[1])
            .importedTemplates(importedList)
            .errors(errors)
            .importTime(LocalDateTime.now())
            .build();
    }

    private void processSingleTemplate(InterfaceTemplateDTO dto, Long folderId, String strategy,
                                        List<TemplateImportResultVO.ImportedTemplateVO> importedList,
                                        Set<Long> importedIds, int[] counts) {
        Optional.ofNullable(folderId).ifPresent(dto::setFolderId);
        
        InterfaceTemplate existing = findExistingTemplate(dto);
        var builder = TemplateImportResultVO.ImportedTemplateVO.builder().originalName(dto.getName());
        
        if (existing != null) {
            switch (strategy) {
                case "skip":
                    counts[1]++;
                    importedList.add(builder.status("SKIPPED").templateId(existing.getId()).build());
                    return;
                case "overwrite":
                    templateService.updateTemplate(existing.getId(), dto);
                    counts[0]++;
                    importedList.add(builder.status("UPDATED").templateId(existing.getId()).build());
                    importedIds.add(existing.getId());
                    return;
                case "rename":
                    dto.setName(generateUniqueName(dto.getName()));
                    break;
                default:
                    counts[1]++;
                    importedList.add(builder.status("SKIPPED").templateId(existing.getId()).build());
                    return;
            }
        }
        
        InterfaceTemplateVO vo = templateService.createTemplate(dto);
        counts[0]++;
        importedList.add(builder.status("CREATED").templateId(vo.getId()).templateName(vo.getName()).build());
        importedIds.add(vo.getId());
    }

    private void updateRecordSuccess(TemplateImportExport record, int[] counts, Set<Long> importedIds) {
        record.setStatus(counts[2] == 0 ? TemplateEnums.ImportExportStatus.SUCCESS.getCode() 
            : TemplateEnums.ImportExportStatus.PARTIAL_SUCCESS.getCode());
        record.setSuccessCount(counts[0]);
        record.setFailCount(counts[2]);
        record.setTemplateIds(importedIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        record.setEndTime(LocalDateTime.now());
        importExportMapper.updateById(record);
    }

    private void updateRecordFailed(TemplateImportExport record, String errorMsg) {
        record.setStatus(TemplateEnums.ImportExportStatus.FAILED.getCode());
        record.setErrorMessage(errorMsg);
        record.setEndTime(LocalDateTime.now());
        importExportMapper.updateById(record);
    }

    private String readFileContent(org.springframework.web.multipart.MultipartFile file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String getFormatFromDTO(TemplateImportDTO dto) {
        if (StringUtils.hasText(dto.getFormat())) {
            return dto.getFormat().toUpperCase();
        }
        return EXTENSION_JSON.equalsIgnoreCase(FilenameUtils.getExtension(dto.getFile().getOriginalFilename()))
            ? "JSON" : "UNKNOWN";
    }

    private TemplateParser getParser(String format, String content) {
        if (!StringUtils.hasText(format) && content.contains("\"info\"")) {
            return jsonTemplateParser;
        }
        return jsonTemplateParser;
    }

    private InterfaceTemplate findExistingTemplate(InterfaceTemplateDTO dto) {
        return StringUtils.hasText(dto.getName())
            ? templateMapper.selectByName(dto.getName())
            : null;
    }

    private String generateUniqueName(String originalName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return originalName + "_" + timestamp.substring(timestamp.length() - 6);
    }

    private String buildMessage(int[] counts) {
        StringBuilder msg = new StringBuilder("导入完成：成功").append(counts[0]).append("个");
        if (counts[1] > 0) msg.append("，跳过").append(counts[1]).append("个");
        if (counts[2] > 0) msg.append("，失败").append(counts[2]).append("个");
        return msg.toString();
    }

    private TemplateImportResultVO buildErrorResult(String message) {
        return TemplateImportResultVO.builder()
            .success(false)
            .message(message)
            .totalCount(0)
            .successCount(0)
            .failCount(0)
            .importTime(LocalDateTime.now())
            .build();
    }

    private JSONObject buildPostmanInfo() {
        JSONObject info = new JSONObject();
        info.put("_postman_id", UUID.randomUUID().toString());
        info.put("name", "PDM接口测试模板集合");
        info.put("description", "从PDM接口测试工具导出的模板集合");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        return info;
    }

    private JSONObject convertToJsonObject(InterfaceTemplateVO vo) {
        JSONObject obj = new JSONObject();
        obj.put("name", vo.getName());
        obj.put("description", vo.getDescription());
        obj.put("protocolType", vo.getProtocolType());
        obj.put("method", vo.getMethod());
        obj.put("baseUrl", vo.getBaseUrl());
        obj.put("path", vo.getPath());
        obj.put("authType", vo.getAuthType());
        obj.put("authConfig", vo.getAuthConfig());
        obj.put("contentType", vo.getContentType());
        obj.put("charset", vo.getCharset());
        obj.put("bodyType", vo.getBodyType());
        obj.put("bodyContent", vo.getBodyContent());
        obj.put("bodyRawType", vo.getBodyRawType());
        obj.put("connectTimeout", vo.getConnectTimeout());
        obj.put("readTimeout", vo.getReadTimeout());
        obj.put("retryCount", vo.getRetryCount());
        obj.put("retryInterval", vo.getRetryInterval());
        obj.put("tags", vo.getTags());
        obj.put("pdmSystemType", vo.getPdmSystemType());
        obj.put("pdmModule", vo.getPdmModule());
        obj.put("businessScene", vo.getBusinessScene());
        obj.put("visibility", vo.getVisibility());
        obj.put("headers", vo.getHeaders());
        obj.put("parameters", vo.getParameters());
        obj.put("formDataList", vo.getFormDataList());
        obj.put("assertions", vo.getAssertions());
        obj.put("preProcessors", vo.getPreProcessors());
        obj.put("postProcessors", vo.getPostProcessors());
        obj.put("variables", vo.getVariables());
        return obj;
    }

    private JSONObject convertToPostmanItem(InterfaceTemplateVO vo) {
        JSONObject item = new JSONObject();
        item.put("name", vo.getName());
        
        JSONObject request = new JSONObject();
        request.put("method", vo.getMethod());
        request.put("description", vo.getDescription());
        request.put("url", buildPostmanUrl(vo));
        request.put("header", buildPostmanHeaders(vo));
        request.put("body", buildPostmanBody(vo));
        
        item.put("request", request);
        return item;
    }

    private JSONObject buildPostmanUrl(InterfaceTemplateVO vo) {
        JSONObject url = new JSONObject();
        String fullUrl = Optional.ofNullable(vo.getBaseUrl()).orElse("") + Optional.ofNullable(vo.getPath()).orElse("");
        url.put("raw", fullUrl);
        url.put("protocol", "http");
        Optional.ofNullable(vo.getBaseUrl()).ifPresent(baseUrl -> 
            url.put("host", new JSONArray().fluentAdd(baseUrl.replace("http://", "").replace("https://", ""))));
        Optional.ofNullable(vo.getPath()).ifPresent(path -> 
            url.put("path", Arrays.asList(path.split("/"))));
        return url;
    }

    private JSONArray buildPostmanHeaders(InterfaceTemplateVO vo) {
        JSONArray headers = new JSONArray();
        if (!CollectionUtils.isEmpty(vo.getHeaders())) {
            vo.getHeaders().forEach(h -> {
                JSONObject header = new JSONObject();
                header.put("key", h.getHeaderName());
                header.put("value", h.getHeaderValue());
                header.put("description", h.getDescription());
                headers.add(header);
            });
        }
        return headers;
    }

    private JSONObject buildPostmanBody(InterfaceTemplateVO vo) {
        JSONObject body = new JSONObject();
        if (vo.getBodyContent() != null) {
            body.put("mode", "raw");
            body.put("raw", vo.getBodyContent());
            if ("JSON".equals(vo.getBodyRawType())) {
                JSONObject options = new JSONObject();
                JSONObject raw = new JSONObject();
                raw.put("language", "json");
                options.put("raw", raw);
                body.put("options", options);
            }
        }
        return body;
    }
}
