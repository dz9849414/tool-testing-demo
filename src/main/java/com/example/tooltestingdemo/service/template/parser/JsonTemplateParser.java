package com.example.tooltestingdemo.service.template.parser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.tooltestingdemo.dto.*;
import com.example.tooltestingdemo.enums.TemplateEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON格式模板解析器
 */
@Slf4j
@Component
public class JsonTemplateParser implements TemplateParser {

    // 格式名称常量
    private static final String FORMAT_NAME = "JSON";
    
    // Postman body mode 常量
    private static final String POSTMAN_MODE_RAW = "raw";
    private static final String POSTMAN_MODE_FORMDATA = "formdata";
    private static final String POSTMAN_MODE_URLENCODED = "urlencoded";
    
    // Postman language 常量
    private static final String LANGUAGE_JSON = "json";
    
    // FormData 字段类型常量
    private static final String FIELD_TYPE_TEXT = "TEXT";
    private static final String FIELD_TYPE_FILE = "FILE";
    private static final String FORM_DATA_TYPE_TEXT = "text";
    
    // 参数类型常量
    private static final String PARAM_TYPE_QUERY = "QUERY";
    
    // 数据类型常量
    private static final String DATA_TYPE_STRING = "STRING";
    
    // 启用状态常量
    private static final int ENABLED_YES = 1;
    private static final int ENABLED_NO = 0;

    @Override
    public String getFormat() {
        return FORMAT_NAME;
    }

    @Override
    public String getDescription() {
        return "系统标准JSON格式";
    }

    @Override
    public boolean validate(String content) {
        try {
            JSON.parse(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<InterfaceTemplateDTO> parse(String content) {
        List<InterfaceTemplateDTO> templates = new ArrayList<>();
        
        try {
            // 尝试解析为数组
            JSONArray array = JSON.parseArray(content);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                templates.add(parseTemplate(obj));
            }
        } catch (Exception e) {
            // 尝试解析为单个对象
            try {
                JSONObject obj = JSON.parseObject(content);
                // 检查是否是Postman格式
                if (obj.containsKey("info")) {
                    templates.addAll(parsePostmanCollection(obj));
                } else {
                    templates.add(parseTemplate(obj));
                }
            } catch (Exception ex) {
                log.error("解析JSON失败", ex);
                throw new RuntimeException("JSON格式不正确: " + ex.getMessage());
            }
        }
        
        return templates;
    }

    /**
     * 解析单个模板
     */
    private InterfaceTemplateDTO parseTemplate(JSONObject obj) {
        InterfaceTemplateDTO dto = new InterfaceTemplateDTO();
        
        // 基本信息
        dto.setName(obj.getString("name"));
        dto.setDescription(obj.getString("description"));
        dto.setProtocolType(obj.getString("protocolType"));
        dto.setMethod(obj.getString("method"));
        dto.setBaseUrl(obj.getString("baseUrl"));
        dto.setPath(obj.getString("path"));
        
        // 认证配置
        dto.setAuthType(obj.getString("authType"));
        dto.setAuthConfig(obj.getString("authConfig"));
        
        // 内容配置
        dto.setContentType(obj.getString("contentType"));
        dto.setCharset(obj.getString("charset"));
        dto.setBodyType(obj.getString("bodyType"));
        dto.setBodyContent(obj.getString("bodyContent"));
        dto.setBodyRawType(obj.getString("bodyRawType"));
        
        // 超时配置
        dto.setConnectTimeout(obj.getInteger("connectTimeout"));
        dto.setReadTimeout(obj.getInteger("readTimeout"));
        
        // 重试配置
        dto.setRetryCount(obj.getInteger("retryCount"));
        dto.setRetryInterval(obj.getInteger("retryInterval"));
        
        // 标签和分类
        dto.setTags(obj.getString("tags"));
        dto.setPdmSystemType(obj.getString("pdmSystemType"));
        dto.setPdmModule(obj.getString("pdmModule"));
        dto.setBusinessScene(obj.getString("businessScene"));
        dto.setVisibility(obj.getInteger("visibility"));
        
        // 解析关联数据
        dto.setHeaders(parseHeaders(obj.getJSONArray("headers")));
        dto.setParameters(parseParameters(obj.getJSONArray("parameters")));
        dto.setFormDataList(parseFormData(obj.getJSONArray("formDataList")));
        dto.setAssertions(parseAssertions(obj.getJSONArray("assertions")));
        dto.setPreProcessors(parsePreProcessors(obj.getJSONArray("preProcessors")));
        dto.setPostProcessors(parsePostProcessors(obj.getJSONArray("postProcessors")));
        dto.setVariables(parseVariables(obj.getJSONArray("variables")));
        
        return dto;
    }

    /**
     * 解析Postman Collection格式
     */
    private List<InterfaceTemplateDTO> parsePostmanCollection(JSONObject collection) {
        List<InterfaceTemplateDTO> templates = new ArrayList<>();
        
        JSONObject info = collection.getJSONObject("info");
        String collectionName = info != null ? info.getString("name") : "Imported Collection";
        
        JSONArray items = collection.getJSONArray("item");
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                parsePostmanItem(items.getJSONObject(i), templates, collectionName);
            }
        }
        
        return templates;
    }

    private void parsePostmanItem(JSONObject item, List<InterfaceTemplateDTO> templates, String folderName) {
        // 检查是否是文件夹
        if (item.containsKey("item")) {
            JSONArray subItems = item.getJSONArray("item");
            String subFolderName = folderName + "/" + item.getString("name");
            for (int i = 0; i < subItems.size(); i++) {
                parsePostmanItem(subItems.getJSONObject(i), templates, subFolderName);
            }
        } else {
            // 解析请求
            InterfaceTemplateDTO dto = new InterfaceTemplateDTO();
            dto.setName(item.getString("name"));
            dto.setDescription(item.getString("description"));
            
            JSONObject request = item.getJSONObject("request");
            if (request != null) {
                // 解析URL
                Object urlObj = request.get("url");
                if (urlObj instanceof JSONObject) {
                    JSONObject url = (JSONObject) urlObj;
                    dto.setBaseUrl(url.getString("protocol") + "://" + url.getString("host"));
                    dto.setPath(url.getString("path"));
                } else if (urlObj instanceof String) {
                    String urlStr = (String) urlObj;
                    // 简单解析URL
                    if (urlStr.startsWith("http")) {
                        int pathStart = urlStr.indexOf("/", 8);
                        if (pathStart > 0) {
                            dto.setBaseUrl(urlStr.substring(0, pathStart));
                            dto.setPath(urlStr.substring(pathStart));
                        } else {
                            dto.setBaseUrl(urlStr);
                            dto.setPath("/");
                        }
                    } else {
                        dto.setPath(urlStr);
                    }
                }
                
                // 解析方法
                dto.setMethod(request.getString("method"));
                dto.setProtocolType(TemplateEnums.ProtocolType.HTTP.getCode());
                
                // 解析Header
                dto.setHeaders(parsePostmanHeaders(request.getJSONArray("header")));
                
                // 解析Body
                JSONObject body = request.getJSONObject("body");
                if (body != null) {
                    String mode = body.getString("mode");
                    if (POSTMAN_MODE_RAW.equals(mode)) {
                        dto.setBodyType(TemplateEnums.BodyType.RAW.getCode());
                        dto.setBodyContent(body.getString("raw"));
                        JSONObject options = body.getJSONObject("options");
                        if (options != null) {
                            JSONObject raw = options.getJSONObject("raw");
                            if (raw != null) {
                                String language = raw.getString("language");
                                if (LANGUAGE_JSON.equals(language)) {
                                    dto.setBodyRawType(TemplateEnums.BodyType.JSON.getCode());
                                    dto.setContentType("application/json");
                                }
                            }
                        }
                    } else if (POSTMAN_MODE_FORMDATA.equals(mode)) {
                        dto.setBodyType(TemplateEnums.BodyType.FORM_DATA.getCode());
                        dto.setFormDataList(parsePostmanFormData(body.getJSONArray("formdata")));
                    } else if (POSTMAN_MODE_URLENCODED.equals(mode)) {
                        dto.setBodyType(TemplateEnums.BodyType.X_WWW_FORM_URLENCODED.getCode());
                        dto.setParameters(parsePostmanUrlEncoded(body.getJSONArray("urlencoded")));
                    }
                }
            }
            
            templates.add(dto);
        }
    }

    private List<TemplateHeaderDTO> parsePostmanHeaders(JSONArray headers) {
        List<TemplateHeaderDTO> list = new ArrayList<>();
        if (headers == null) return list;
        
        for (int i = 0; i < headers.size(); i++) {
            JSONObject h = headers.getJSONObject(i);
            TemplateHeaderDTO dto = new TemplateHeaderDTO();
            dto.setHeaderName(h.getString("key"));
            dto.setHeaderValue(h.getString("value"));
            dto.setDescription(h.getString("description"));
            Boolean disabled = h.getBoolean("disabled");
            dto.setIsEnabled(disabled == null || !disabled ? ENABLED_YES : ENABLED_NO);
            list.add(dto);
        }
        return list;
    }

    private List<TemplateFormDataDTO> parsePostmanFormData(JSONArray formdata) {
        List<TemplateFormDataDTO> list = new ArrayList<>();
        if (formdata == null) return list;
        
        for (int i = 0; i < formdata.size(); i++) {
            JSONObject f = formdata.getJSONObject(i);
            TemplateFormDataDTO dto = new TemplateFormDataDTO();
            dto.setFieldName(f.getString("key"));
            dto.setFieldValue(f.getString("value"));
            dto.setFieldType(FORM_DATA_TYPE_TEXT.equals(f.getString("type")) ? FIELD_TYPE_TEXT : FIELD_TYPE_FILE);
            dto.setDescription(f.getString("description"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplateParameterDTO> parsePostmanUrlEncoded(JSONArray urlencoded) {
        List<TemplateParameterDTO> list = new ArrayList<>();
        if (urlencoded == null) return list;
        
        for (int i = 0; i < urlencoded.size(); i++) {
            JSONObject p = urlencoded.getJSONObject(i);
            TemplateParameterDTO dto = new TemplateParameterDTO();
            dto.setParamName(p.getString("key"));
            dto.setParamValue(p.getString("value"));
            dto.setParamType(PARAM_TYPE_QUERY);
            dto.setDataType(DATA_TYPE_STRING);
            list.add(dto);
        }
        return list;
    }

    private List<TemplateHeaderDTO> parseHeaders(JSONArray array) {
        List<TemplateHeaderDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplateHeaderDTO dto = new TemplateHeaderDTO();
            dto.setHeaderName(obj.getString("headerName"));
            dto.setHeaderValue(obj.getString("headerValue"));
            dto.setDescription(obj.getString("description"));
            dto.setIsEnabled(obj.getInteger("isEnabled"));
            dto.setIsRequired(obj.getInteger("isRequired"));
            dto.setIsVariable(obj.getInteger("isVariable"));
            dto.setVariableName(obj.getString("variableName"));
            dto.setSortOrder(obj.getInteger("sortOrder"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplateParameterDTO> parseParameters(JSONArray array) {
        List<TemplateParameterDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplateParameterDTO dto = new TemplateParameterDTO();
            dto.setParamType(obj.getString("paramType"));
            dto.setParamName(obj.getString("paramName"));
            dto.setParamValue(obj.getString("paramValue"));
            dto.setDataType(obj.getString("dataType"));
            dto.setDescription(obj.getString("description"));
            dto.setExampleValue(obj.getString("exampleValue"));
            dto.setIsRequired(obj.getInteger("isRequired"));
            dto.setIsEnabled(obj.getInteger("isEnabled"));
            dto.setIsVariable(obj.getInteger("isVariable"));
            dto.setVariableName(obj.getString("variableName"));
            dto.setSortOrder(obj.getInteger("sortOrder"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplateFormDataDTO> parseFormData(JSONArray array) {
        List<TemplateFormDataDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplateFormDataDTO dto = new TemplateFormDataDTO();
            dto.setFieldName(obj.getString("fieldName"));
            dto.setFieldType(obj.getString("fieldType"));
            dto.setFieldValue(obj.getString("fieldValue"));
            dto.setFilePath(obj.getString("filePath"));
            dto.setFileName(obj.getString("fileName"));
            dto.setContentType(obj.getString("contentType"));
            dto.setDescription(obj.getString("description"));
            dto.setIsRequired(obj.getInteger("isRequired"));
            dto.setIsEnabled(obj.getInteger("isEnabled"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplateAssertionDTO> parseAssertions(JSONArray array) {
        List<TemplateAssertionDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplateAssertionDTO dto = new TemplateAssertionDTO();
            dto.setAssertName(obj.getString("assertName"));
            dto.setAssertType(obj.getString("assertType"));
            dto.setExtractPath(obj.getString("extractPath"));
            dto.setExpectedValue(obj.getString("expectedValue"));
            dto.setOperator(obj.getString("operator"));
            dto.setDataType(obj.getString("dataType"));
            dto.setErrorMessage(obj.getString("errorMessage"));
            dto.setIsEnabled(obj.getInteger("isEnabled"));
            dto.setAssertGroup(obj.getString("assertGroup"));
            dto.setLogicType(obj.getString("logicType"));
            dto.setSortOrder(obj.getInteger("sortOrder"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplatePreProcessorDTO> parsePreProcessors(JSONArray array) {
        List<TemplatePreProcessorDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplatePreProcessorDTO dto = new TemplatePreProcessorDTO();
            dto.setProcessorName(obj.getString("processorName"));
            dto.setProcessorType(obj.getString("processorType"));
            dto.setConfig(obj.getString("config"));
            dto.setScriptContent(obj.getString("scriptContent"));
            dto.setTargetVariable(obj.getString("targetVariable"));
            dto.setVariableScope(obj.getString("variableScope"));
            dto.setDescription(obj.getString("description"));
            dto.setIsEnabled(obj.getInteger("isEnabled"));
            dto.setSortOrder(obj.getInteger("sortOrder"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplatePostProcessorDTO> parsePostProcessors(JSONArray array) {
        List<TemplatePostProcessorDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplatePostProcessorDTO dto = new TemplatePostProcessorDTO();
            dto.setProcessorName(obj.getString("processorName"));
            dto.setProcessorType(obj.getString("processorType"));
            dto.setExtractType(obj.getString("extractType"));
            dto.setExtractExpression(obj.getString("extractExpression"));
            dto.setExtractMatchNo(obj.getInteger("extractMatchNo"));
            dto.setTargetVariable(obj.getString("targetVariable"));
            dto.setVariableScope(obj.getString("variableScope"));
            dto.setDefaultValue(obj.getString("defaultValue"));
            dto.setConfig(obj.getString("config"));
            dto.setScriptContent(obj.getString("scriptContent"));
            dto.setDescription(obj.getString("description"));
            dto.setIsEnabled(obj.getInteger("isEnabled"));
            dto.setSortOrder(obj.getInteger("sortOrder"));
            list.add(dto);
        }
        return list;
    }

    private List<TemplateVariableDTO> parseVariables(JSONArray array) {
        List<TemplateVariableDTO> list = new ArrayList<>();
        if (array == null) return list;
        
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            TemplateVariableDTO dto = new TemplateVariableDTO();
            dto.setVariableName(obj.getString("variableName"));
            dto.setVariableType(obj.getString("variableType"));
            dto.setDefaultValue(obj.getString("defaultValue"));
            dto.setCurrentValue(obj.getString("currentValue"));
            dto.setDescription(obj.getString("description"));
            dto.setExampleValue(obj.getString("exampleValue"));
            dto.setIsRequired(obj.getInteger("isRequired"));
            dto.setIsEditable(obj.getInteger("isEditable"));
            dto.setSourceType(obj.getString("sourceType"));
            dto.setSourceConfig(obj.getString("sourceConfig"));
            dto.setSortOrder(obj.getInteger("sortOrder"));
            list.add(dto);
        }
        return list;
    }
}
