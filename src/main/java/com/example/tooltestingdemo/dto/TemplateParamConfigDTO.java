package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 模板参数配置页专用 DTO。
 *
 * <p>这个 DTO 只承载“参数管理页面”会编辑的字段，避免前端单独保存参数时误覆盖
 * 断言、处理器、附件、历史版本等模板其他配置。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateParamConfigDTO {

    private Long id;
    private String name;
    private String protocolType;
    private String method;
    private String fullUrl;
    private String baseUrl;
    private String path;
    private String contentType;
    private String charset;
    private String bodyType;
    private String bodyRawType;
    private String bodyContent;
    private Integer connectTimeout;
    private Integer readTimeout;
    private String extField5;

    private List<TemplateHeaderDTO> headers;
    private List<TemplateParameterDTO> parameters;
    private List<TemplateFormDataDTO> formDataList;
    private List<TemplateAssertionDTO> assertions;
    private List<TemplatePreProcessorDTO> preProcessors;
    private List<TemplatePostProcessorDTO> postProcessors;
    private List<TemplateVariableDTO> variables;
}
