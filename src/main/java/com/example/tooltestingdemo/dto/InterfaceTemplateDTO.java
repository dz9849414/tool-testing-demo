package com.example.tooltestingdemo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口模板 DTO（用于创建/更新）
 * 
 * 文件位置：src/main/java/com/example/tooltestingdemo/dto/InterfaceTemplateDTO.java
 */
@Data
public class InterfaceTemplateDTO {

    /**
     * 模板ID（更新时必填）
     */
    private Long id;

    /**
     * 所属分类ID
     */
    private Long folderId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述/备注
     */
    private String description;

    /**
     * 协议类型：HTTP/HTTPS/WEBSOCKET/SOAP/REST/MQTT/TCP/UDP
     */
    private String protocolType;

    /**
     * 请求方法：GET/POST/PUT/DELETE/PATCH/HEAD/OPTIONS
     */
    private String method;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 认证类型：NONE/BASIC/DIGEST/OAUTH1/OAUTH2/BEARER/APIKEY/JWT
     */
    private String authType;

    /**
     * 认证配置详情（JSON格式）
     */
    private String authConfig;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 字符编码，默认UTF-8
     */
    private String charset;

    /**
     * 请求体类型：NONE/FORM_DATA/X_WWW_FORM_URLENCODED/RAW/BINARY/GRAPHQL
     */
    private String bodyType;

    /**
     * 请求体内容
     */
    private String bodyContent;

    /**
     * RAW类型：JSON/XML/HTML/TEXT/JavaScript
     */
    private String bodyRawType;

    /**
     * 连接超时时间（毫秒），默认30000
     */
    private Integer connectTimeout;

    /**
     * 读取超时时间（毫秒），默认30000
     */
    private Integer readTimeout;

    /**
     * 重试次数，默认0
     */
    private Integer retryCount;

    /**
     * 重试间隔（毫秒），默认1000
     */
    private Integer retryInterval;

    /**
     * 可见性：1-私有 2-团队 3-公开
     */
    private Integer visibility;

    /**
     * 标签，逗号分隔
     */
    private String tags;

    /**
     * PDM系统类型：CAD/ERP/PLM/CAM/CAE
     */
    private String pdmSystemType;

    /**
     * PDM模块：物料管理/BOM管理/变更管理/图纸管理
     */
    private String pdmModule;

    /**
     * 业务场景描述
     */
    private String businessScene;

    // ==================== 关联数据 ====================

    /**
     * 请求头列表
     */
    private List<TemplateHeaderDTO> headers;

    /**
     * 请求参数列表
     */
    private List<TemplateParameterDTO> parameters;

    /**
     * FormData参数列表
     */
    private List<TemplateFormDataDTO> formDataList;

    /**
     * 断言规则列表
     */
    private List<TemplateAssertionDTO> assertions;

    /**
     * 前置处理器列表
     */
    private List<TemplatePreProcessorDTO> preProcessors;

    /**
     * 后置处理器列表
     */
    private List<TemplatePostProcessorDTO> postProcessors;

    /**
     * 变量定义列表
     */
    private List<TemplateVariableDTO> variables;
}
