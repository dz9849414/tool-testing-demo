package com.example.tooltestingdemo.service.impl.template;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.dto.InterfaceTemplateDTO;
import com.example.tooltestingdemo.dto.TemplateImportDTO;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.entity.template.TemplateImportExport;
import com.example.tooltestingdemo.mapper.template.InterfaceTemplateMapper;
import com.example.tooltestingdemo.mapper.template.TemplateImportExportMapper;
import com.example.tooltestingdemo.enums.TemplateEnums;
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

/**
 * 模板导入导出服务实现类
 * 
 * <p>提供模板的导入导出功能，支持多种格式：</p>
 * <ul>
 *   <li>系统标准JSON格式 - 完整的模板数据交换格式</li>
 *   <li>Postman Collection (v2.1) - 与Postman工具兼容</li>
 * </ul>
 * 
 * <p>导入策略：</p>
 * <ul>
 *   <li>SKIP - 跳过已存在的模板（默认）</li>
 *   <li>OVERWRITE - 覆盖更新已有模板</li>
 *   <li>RENAME - 自动重命名后导入</li>
 * </ul>
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
/**
 * 模板导入导出服务实现类
 * 
 * <p>提供模板的导入导出功能，支持多种格式：</p>
 * <ul>
 *   <li>系统标准JSON格式 - 完整的模板数据交换格式</li>
 *   <li>Postman Collection (v2.1) - 与Postman工具兼容</li>
 * </ul>
 * 
 * <p>导入策略：</p>
 * <ul>
 *   <li>SKIP - 跳过已存在的模板（默认）</li>
 *   <li>OVERWRITE - 覆盖更新已有模板</li>
 *   <li>RENAME - 自动重命名后导入</li>
 * </ul>
 * 
 * @author PDM接口测试工具
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateImportServiceImpl implements TemplateImportService {

    // 文件扩展名常量
    private static final String EXTENSION_JSON = "json";
    private static final String EXTENSION_YAML = "yaml";
    private static final String EXTENSION_YML = "yml";
    
    // Postman Collection Key 常量
    private static final String POSTMAN_KEY_INFO = "info";
    private static final String POSTMAN_KEY_ITEM = "item";
    private static final String POSTMAN_KEY_NAME = "name";
    private static final String POSTMAN_KEY_REQUEST = "request";
    private static final String POSTMAN_KEY_URL = "url";
    private static final String POSTMAN_KEY_METHOD = "method";
    private static final String POSTMAN_KEY_HEADER = "header";
    private static final String POSTMAN_KEY_BODY = "body";
    private static final String POSTMAN_KEY_MODE = "mode";
    private static final String POSTMAN_KEY_RAW = "raw";
    private static final String POSTMAN_KEY_OPTIONS = "options";
    private static final String POSTMAN_KEY_LANGUAGE = "language";
    private static final String POSTMAN_KEY_PROTOCOL = "protocol";
    private static final String POSTMAN_KEY_HOST = "host";
    private static final String POSTMAN_KEY_PATH = "path";
    
    // Postman Body Mode 常量
    private static final String POSTMAN_MODE_RAW = "raw";
    private static final String POSTMAN_MODE_FORMDATA = "formdata";
    private static final String POSTMAN_MODE_URLENCODED = "urlencoded";
    
    // Postman Language 常量
    private static final String LANGUAGE_JSON = "json";
    
    // Body Raw Type 常量
    private static final String BODY_RAW_TYPE_JSON = "JSON";
    
    // 模板名称分隔符
    private static final String NAME_SEPARATOR = "_";
    
    // 默认创建者信息
    private static final Long DEFAULT_CREATE_ID = 1L;
    private static final String DEFAULT_CREATE_NAME = "管理员";

    /** 模板主服务 */
    private final InterfaceTemplateService templateService;
    
    /** 模板数据访问层 */
    private final InterfaceTemplateMapper templateMapper;
    
    /** 导入导出记录数据访问层 */
    private final TemplateImportExportMapper importExportMapper;
    
    /** JSON格式解析器 */
    private final JsonTemplateParser jsonTemplateParser;

    /**
     * 导入模板
     * 
     * <p>主导入流程：</p>
     * <ol>
     *   <li>记录导入操作到数据库</li>
     *   <li>读取并解析上传的文件</li>
     *   <li>根据策略处理每个模板（创建/更新/跳过）</li>
     *   <li>更新导入记录状态</li>
     *   <li>返回导入结果</li>
     * </ol>
     *
     * @param dto 导入请求参数，包含文件、格式、目标文件夹、导入策略等
     * @return 导入结果，包含成功/失败数量、错误信息等
     * @throws RuntimeException 当文件格式不正确或解析失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TemplateImportResultVO importTemplates(TemplateImportDTO dto) {
        long startTime = System.currentTimeMillis();
        
        // 记录导入操作到历史表
        TemplateImportExport record = new TemplateImportExport();
        record.setOperationType(TemplateEnums.OperationType.IMPORT.getCode());
        record.setFileName(dto.getFile().getOriginalFilename());
        record.setFileFormat(getFormatFromDTO(dto));
        record.setStatus(TemplateEnums.ImportExportStatus.PROCESSING.getCode()); // 处理中
        record.setStartTime(LocalDateTime.now());
        record.setCreateId(DEFAULT_CREATE_ID);  // TODO: 从当前登录用户获取
        record.setCreateName(DEFAULT_CREATE_NAME);
        importExportMapper.insert(record);
        
        try {
            // 读取文件内容
            String content = readFileContent(dto.getFile());
            
            // 获取对应格式的解析器
            TemplateParser parser = getParser(dto.getFormat(), content);
            
            // 验证文件格式
            if (!parser.validate(content)) {
                throw new RuntimeException("文件格式不正确: " + parser.getDescription());
            }
            
            // 解析模板列表
            List<InterfaceTemplateDTO> templates = parser.parse(content);
            
            if (CollectionUtils.isEmpty(templates)) {
                return buildErrorResult("未找到可导入的模板");
            }
            
            // 获取导入策略和目标文件夹
            String strategy = dto.getStrategy() != null ? dto.getStrategy() : TemplateEnums.ImportStrategy.SKIP.getCode();
            Long folderId = dto.getFolderId();
            
            // 执行导入
            List<TemplateImportResultVO.ImportedTemplateVO> importedList = new ArrayList<>();
            List<TemplateImportResultVO.ImportErrorVO> errors = new ArrayList<>();
            
            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;
            
            Set<Long> importedIds = new HashSet<>();
            
            // 逐个处理模板
            for (int i = 0; i < templates.size(); i++) {
                InterfaceTemplateDTO templateDTO = templates.get(i);
                
                try {
                    // 设置目标文件夹
                    if (folderId != null) {
                        templateDTO.setFolderId(folderId);
                    }
                    
                    // 检查是否已存在相同模板
                    InterfaceTemplate existing = findExistingTemplate(templateDTO);
                    
                    TemplateImportResultVO.ImportedTemplateVO.ImportedTemplateVOBuilder resultBuilder = 
                            TemplateImportResultVO.ImportedTemplateVO.builder()
                                    .originalName(templateDTO.getName());
                    
                    if (existing != null) {
                        // 根据策略处理重复模板
                        switch (strategy) {
                            case "SKIP":
                            case "skip":
                                // 跳过重复模板
                                skipCount++;
                                resultBuilder.status("SKIPPED").templateId(existing.getId());
                                importedList.add(resultBuilder.build());
                                continue;
                            case "OVERWRITE":
                            case "overwrite":
                                // 覆盖更新已有模板
                                templateService.updateTemplate(existing.getId(), templateDTO);
                                successCount++;
                                resultBuilder.status("UPDATED").templateId(existing.getId());
                                importedList.add(resultBuilder.build());
                                importedIds.add(existing.getId());
                                continue;
                            case "RENAME":
                            case "rename":
                                // 重命名后导入
                                templateDTO.setName(generateUniqueName(templateDTO.getName()));
                                break;
                            default:
                                skipCount++;
                                resultBuilder.status("SKIPPED").templateId(existing.getId());
                                importedList.add(resultBuilder.build());
                                continue;
                        }
                    }
                    
                    // 创建新模板
                    InterfaceTemplateVO vo = templateService.createTemplate(templateDTO);
                    successCount++;
                    resultBuilder.status("CREATED").templateId(vo.getId()).templateName(vo.getName());
                    importedList.add(resultBuilder.build());
                    importedIds.add(vo.getId());
                    
                } catch (Exception e) {
                    log.error("导入模板失败: {}", templateDTO.getName(), e);
                    failCount++;
                    errors.add(TemplateImportResultVO.ImportErrorVO.builder()
                            .templateName(templateDTO.getName())
                            .errorMessage(e.getMessage())
                            .rowNumber(i + 1)
                            .build());
                }
            }
            
            // 更新导入记录
            record.setStatus(failCount == 0 ? TemplateEnums.ImportExportStatus.SUCCESS.getCode() : TemplateEnums.ImportExportStatus.PARTIAL_SUCCESS.getCode()); // 1-成功 2-部分成功/失败
            record.setSuccessCount(successCount);
            record.setFailCount(failCount);
            record.setTemplateIds(importedIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            record.setEndTime(LocalDateTime.now());
            importExportMapper.updateById(record);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("模板导入完成: 总计={}, 成功={}, 跳过={}, 失败={}, 耗时={}ms", 
                    templates.size(), successCount, skipCount, failCount, duration);
            
            return TemplateImportResultVO.builder()
                    .success(failCount == 0)
                    .message(buildMessage(successCount, skipCount, failCount))
                    .totalCount(templates.size())
                    .successCount(successCount)
                    .failCount(failCount)
                    .skipCount(skipCount)
                    .importedTemplates(importedList)
                    .errors(errors)
                    .importTime(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("模板导入失败", e);
            
            // 更新记录为失败状态
            record.setStatus(TemplateEnums.ImportExportStatus.FAILED.getCode());
            record.setErrorMessage(e.getMessage());
            record.setEndTime(LocalDateTime.now());
            importExportMapper.updateById(record);
            
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * 验证导入文件
     * 
     * <p>预检查导入文件的有效性，不实际执行导入操作</p>
     *
     * @param dto 导入请求参数，包含要验证的文件
     * @return 验证结果，包含发现的模板数量或错误信息
     */
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

    /**
     * 导出模板为JSON格式
     * 
     * <p>将选中的模板导出为系统标准JSON格式，包含完整的模板数据</p>
     *
     * @param templateIds 要导出的模板ID数组
     * @return JSON格式字符串，包含所有模板数据
     */
    @Override
    public String exportToJson(Long[] templateIds) {
        if (templateIds == null || templateIds.length == 0) {
            return "[]";
        }
        
        List<JSONObject> templates = new ArrayList<>();
        
        for (Long id : templateIds) {
            InterfaceTemplateVO vo = templateService.getTemplateDetail(id);
            if (vo != null) {
                templates.add(convertToJsonObject(vo));
            }
        }
        
        return JSON.toJSONString(templates);
    }

    /**
     * 导出为Postman Collection格式
     * 
     * <p>将选中的模板导出为Postman Collection (v2.1) 格式，
     * 可直接导入Postman工具使用</p>
     *
     * @param templateIds 要导出的模板ID数组
     * @return Postman Collection JSON字符串
     */
    @Override
    public String exportToPostman(Long[] templateIds) {
        JSONObject collection = new JSONObject();
        
        // Postman Collection基本信息
        JSONObject info = new JSONObject();
        info.put("_postman_id", UUID.randomUUID().toString());
        info.put(POSTMAN_KEY_NAME, "PDM接口测试模板集合");
        info.put("description", "从PDM接口测试工具导出的模板集合");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put(POSTMAN_KEY_INFO, info);
        
        JSONArray items = new JSONArray();
        
        // 转换每个模板为Postman Item
        if (templateIds != null) {
            for (Long id : templateIds) {
                InterfaceTemplateVO vo = templateService.getTemplateDetail(id);
                if (vo != null) {
                    items.add(convertToPostmanItem(vo));
                }
            }
        }
        
        collection.put(POSTMAN_KEY_ITEM, items);
        
        return JSON.toJSONString(collection);
    }

    /**
     * 读取上传文件内容
     * 
     * <p>以UTF-8编码读取MultipartFile的内容</p>
     *
     * @param file 上传的文件
     * @return 文件内容字符串
     * @throws Exception 读取失败时抛出
     */
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

    /**
     * 从DTO获取文件格式
     * 
     * <p>优先使用用户指定的格式，否则根据文件扩展名自动识别</p>
     *
     * @param dto 导入请求DTO
     * @return 格式名称（JSON/YAML等）
     */
    private String getFormatFromDTO(TemplateImportDTO dto) {
        if (StringUtils.hasText(dto.getFormat())) {
            return dto.getFormat().toUpperCase();
        }
        
        String extension = FilenameUtils.getExtension(dto.getFile().getOriginalFilename());
        if (EXTENSION_JSON.equalsIgnoreCase(extension)) {
            return TemplateEnums.ProtocolType.HTTP.getCode();  // 默认返回HTTP格式标识
        } else if (EXTENSION_YAML.equalsIgnoreCase(extension) || EXTENSION_YML.equalsIgnoreCase(extension)) {
            return "YAML";
        }
        
        return TemplateEnums.ProtocolType.HTTP.getCode();
    }

    /**
     * 获取对应的模板解析器
     * 
     * <p>根据格式或内容自动识别选择合适的解析器</p>
     *
     * @param format  指定的格式
     * @param content 文件内容（用于自动识别）
     * @return 对应的模板解析器
     * @throws RuntimeException 当格式不支持时抛出
     */
    private TemplateParser getParser(String format, String content) {
        if (!StringUtils.hasText(format)) {
            // 根据内容特征自动识别
            if (content.contains("\"" + POSTMAN_KEY_INFO + "\"")) {
                return jsonTemplateParser; // Postman格式
            }
            return jsonTemplateParser;
        }
        
        String upperFormat = format.toUpperCase();
        if (TemplateEnums.ProtocolType.HTTP.getCode().equals(upperFormat) || "POSTMAN".equals(upperFormat)) {
            return jsonTemplateParser;
        }
        
        throw new RuntimeException("不支持的导入格式: " + format);
    }

    /**
     * 查找已存在的模板
     * 
     * <p>根据模板名称和请求方法匹配已有模板</p>
     *
     * @param dto 要检查的模板DTO
     * @return 已存在的模板，不存在则返回null
     */
    private InterfaceTemplate findExistingTemplate(InterfaceTemplateDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            return null;
        }
        return templateMapper.selectByNameAndMethod(dto.getName(), dto.getMethod());
    }

    /**
     * 生成唯一名称
     * 
     * <p>在原名后添加时间戳后缀，避免名称冲突</p>
     *
     * @param originalName 原始名称
     * @return 生成的唯一名称
     */
    private String generateUniqueName(String originalName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return originalName + "_" + timestamp.substring(timestamp.length() - 6);
    }

    /**
     * 构建导入结果消息
     *
     * @param success 成功数量
     * @param skip    跳过数量
     * @param fail    失败数量
     * @return 结果消息字符串
     */
    private String buildMessage(int success, int skip, int fail) {
        StringBuilder msg = new StringBuilder();
        msg.append("导入完成：成功").append(success).append("个");
        if (skip > 0) {
            msg.append("，跳过").append(skip).append("个");
        }
        if (fail > 0) {
            msg.append("，失败").append(fail).append("个");
        }
        return msg.toString();
    }

    /**
     * 构建错误结果
     *
     * @param message 错误消息
     * @return 导入结果VO，标记为失败
     */
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

    /**
     * 将模板VO转换为JSON对象
     * 
     * <p>用于导出系统标准JSON格式</p>
     *
     * @param vo 模板视图对象
     * @return JSON对象
     */
    private JSONObject convertToJsonObject(InterfaceTemplateVO vo) {
        JSONObject obj = new JSONObject();
        obj.put(POSTMAN_KEY_NAME, vo.getName());
        obj.put("description", vo.getDescription());
        obj.put("protocolType", vo.getProtocolType());
        obj.put(POSTMAN_KEY_METHOD, vo.getMethod());
        obj.put("baseUrl", vo.getBaseUrl());
        obj.put(POSTMAN_KEY_PATH, vo.getPath());
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
        
        // 关联数据
        obj.put("headers", vo.getHeaders());
        obj.put("parameters", vo.getParameters());
        obj.put("formDataList", vo.getFormDataList());
        obj.put("assertions", vo.getAssertions());
        obj.put("preProcessors", vo.getPreProcessors());
        obj.put("postProcessors", vo.getPostProcessors());
        obj.put("variables", vo.getVariables());
        
        return obj;
    }

    /**
     * 将模板VO转换为Postman Item对象
     * 
     * <p>用于导出Postman Collection格式</p>
     *
     * @param vo 模板视图对象
     * @return Postman Item JSON对象
     */
    private JSONObject convertToPostmanItem(InterfaceTemplateVO vo) {
        JSONObject item = new JSONObject();
        item.put(POSTMAN_KEY_NAME, vo.getName());
        
        JSONObject request = new JSONObject();
        request.put(POSTMAN_KEY_METHOD, vo.getMethod());
        request.put("description", vo.getDescription());
        
        // 构建URL对象
        JSONObject url = new JSONObject();
        String fullUrl = (vo.getBaseUrl() != null ? vo.getBaseUrl() : "") + 
                (vo.getPath() != null ? vo.getPath() : "");
        url.put(POSTMAN_KEY_RAW, fullUrl);
        url.put(POSTMAN_KEY_PROTOCOL, "http");
        if (vo.getBaseUrl() != null) {
            String host = vo.getBaseUrl().replace("http://", "").replace("https://", "");
            url.put(POSTMAN_KEY_HOST, new JSONArray().fluentAdd(host));
        }
        if (vo.getPath() != null) {
            url.put(POSTMAN_KEY_PATH, Arrays.asList(vo.getPath().split("/")));
        }
        request.put(POSTMAN_KEY_URL, url);
        
        // 转换请求头
        if (!CollectionUtils.isEmpty(vo.getHeaders())) {
            JSONArray headers = new JSONArray();
            for (var header : vo.getHeaders()) {
                JSONObject h = new JSONObject();
                h.put("key", header.getHeaderName());
                h.put("value", header.getHeaderValue());
                h.put("description", header.getDescription());
                headers.add(h);
            }
            request.put(POSTMAN_KEY_HEADER, headers);
        }
        
        // 转换请求体
        if (vo.getBodyContent() != null) {
            JSONObject body = new JSONObject();
            body.put(POSTMAN_KEY_MODE, POSTMAN_MODE_RAW);
            body.put(POSTMAN_KEY_RAW, vo.getBodyContent());
            if (BODY_RAW_TYPE_JSON.equals(vo.getBodyRawType())) {
                JSONObject options = new JSONObject();
                JSONObject raw = new JSONObject();
                raw.put(POSTMAN_KEY_LANGUAGE, LANGUAGE_JSON);
                options.put(POSTMAN_KEY_RAW, raw);
                body.put("options", options);
            }
            request.put(POSTMAN_KEY_BODY, body);
        }
        
        item.put(POSTMAN_KEY_REQUEST, request);
        
        return item;
    }
}
