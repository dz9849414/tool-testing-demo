package com.example.tooltestingdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 协议配置新增 DTO
 */
@Data
@Schema(name = "ProtocolConfigCreateDTO", description = "协议配置新增请求体")
public class ProtocolConfigCreateDTO {

    /**
     * 关联协议类型ID
     */
    @NotNull(message = "protocolId不能为空")
    @Schema(description = "关联协议类型ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long protocolId;

    /**
     * 配置名称
     */
    @NotBlank(message = "configName不能为空")
    @Schema(description = "配置名称", example = "ERP对接-生产环境", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configName;

    /**
     * URL配置（支持多个）
     */
    @Valid
    @NotEmpty(message = "urlConfigList不能为空")
    @Schema(description = "URL配置列表（支持多个，且主URL只能有一个）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UrlConfigItemDTO> urlConfigList;

    /**
     * 认证配置（支持多个）
     */
    @Valid
    @Schema(description = "认证配置列表（支持多个，不同认证方式字段不同）")
    private List<AuthConfigItemDTO> authConfigList;

    /**
     * 连接超时时间（毫秒）
     */
    @Min(value = 1, message = "timeoutConnect必须大于0")
    @Schema(description = "连接超时时间（毫秒）", example = "5000", minimum = "1")
    private Integer timeoutConnect;

    /**
     * 读取超时时间（毫秒）
     */
    @Min(value = 1, message = "timeoutRead必须大于0")
    @Schema(description = "读取超时时间（毫秒）", example = "30000", minimum = "1")
    private Integer timeoutRead;

    /**
     * 重试次数（0-10）
     */
    @Min(value = 0, message = "retryCount不能小于0")
    @Max(value = 10, message = "retryCount不能大于10")
    @Schema(description = "重试次数（0-10）", example = "3", minimum = "0", maximum = "10")
    private Integer retryCount;

    /**
     * 重试间隔（毫秒）
     */
    @Min(value = 0, message = "retryInterval不能小于0")
    @Schema(description = "重试间隔（毫秒）", example = "1000", minimum = "0")
    private Integer retryInterval;

    /**
     * 重试触发条件：1-链接超时，2-响应超时，3-响应错误码
     */
    @Schema(description = "重试触发条件：1-链接超时，2-响应超时，3-响应错误码", example = "1,2")
    private String retryCondition;

    /**
     * 数据格式：JSON/XML/FORM/TEXT/BINARY
     */
    @Schema(description = "数据格式", example = "JSON", allowableValues = {"JSON", "XML", "FORM", "TEXT", "BINARY"})
    private String dataFormat;

    /**
     * 格式校验配置（JSON）
     */
    @Schema(description = "格式校验配置（JSON字符串，如 JSON Schema、XSD 等）")
    private String formatConfig;

    /**
     * 额外参数（JSON）
     */
    @Schema(description = "额外参数（JSON字符串）")
    private String additionalParams;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态：0-禁用，1-启用", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    /**
     * 协议参数配置描述
     */
    @Schema(description = "协议参数配置描述")
    private Integer description;

    /**
     * 参数模板（一个协议可配置多个模板）
     */
    @Valid
    @Schema(description = "参数模板列表（一个协议可配置多个模板）")
    private List<ProtocolTemplateDTO> templates;

    @Data
    @Schema(name = "UrlConfigItemDTO", description = "URL配置项")
    public static class UrlConfigItemDTO {
        /**
         * 序号
         */
        @NotNull(message = "urlConfigList.seq不能为空")
        @Schema(description = "序号（同一配置内不允许重复）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer seq;

        /**
         * URL
         */
        @NotBlank(message = "urlConfigList.url不能为空")
        @Schema(description = "URL地址", example = "https://api.example.com/v1", requiredMode = Schema.RequiredMode.REQUIRED)
        private String url;

        /**
         * 是否使用默认端口
         */
        @NotNull(message = "urlConfigList.useDefaultPort不能为空")
        @Schema(description = "是否使用默认端口（true=使用URL默认端口；false=使用port字段）", example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean useDefaultPort;

        /**
         * 端口号（1-65535）
         */
        @Min(value = 1, message = "urlConfigList.port端口范围1-65535")
        @Max(value = 65535, message = "urlConfigList.port端口范围1-65535")
        @Schema(description = "端口号（当useDefaultPort=false时必填，范围1-65535）", example = "443", minimum = "1", maximum = "65535")
        private Integer port;

        /**
         * 是否主URL（只会有一个）
         */
        @NotNull(message = "urlConfigList.primary不能为空")
        @Schema(description = "是否主URL（同一配置内必须且只能有一个true）", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean primary;
    }

    @Data
    @Schema(name = "AuthConfigItemDTO", description = "认证配置项（不同type字段不同）")
    public static class AuthConfigItemDTO {
        /**
         * 认证配置名称
         */
        @NotBlank(message = "authConfigList.name不能为空")
        @Schema(description = "认证配置名称（用于前端展示/选择）", example = "默认认证", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        /**
         * 认证方式：NONE/BASIC/TOKEN/OAUTH2/CERT
         */
        @NotBlank(message = "authConfigList.type不能为空")
        @Schema(description = "认证方式", example = "TOKEN", allowableValues = {"NONE", "BASIC", "TOKEN", "OAUTH2", "CERT"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String type;

        // BASIC
        @Schema(description = "Basic认证用户名（type=BASIC必填）", example = "admin")
        private String username;
        @Schema(description = "Basic认证密码（type=BASIC必填）", example = "******")
        private String password;

        // TOKEN
        @Schema(description = "Token值（type=TOKEN必填）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;
        /**
         * token位置：HEADER/QUERY
         */
        @Schema(description = "Token位置（type=TOKEN必填）", example = "HEADER", allowableValues = {"HEADER", "QUERY"})
        private String tokenLocation;
        @Schema(description = "Header名称（tokenLocation=HEADER时必填）", example = "Authorization")
        private String headerName;

        // OAUTH2
        @Schema(description = "OAuth2授权端点（type=OAUTH2必填）", example = "https://auth.example.com/oauth2/token")
        private String authEndpoint;
        @Schema(description = "OAuth2 Client ID（type=OAUTH2必填）", example = "client-id-xxx")
        private String clientId;
        @Schema(description = "OAuth2 Client Secret（type=OAUTH2必填）", example = "client-secret-xxx")
        private String clientSecret;
        @Schema(description = "OAuth2 Scope（可选）", example = "read write")
        private String scope;

        // CERT
        /**
         * 证书文件名（建议与文件存储结合使用）
         */
        @Schema(description = "证书文件名（type=CERT必填）", example = "client.p12")
        private String certFileName;
        /**
         * 证书文件内容（Base64）；如已接入文件服务，可改为 fileId/url
         */
        @Schema(description = "证书文件内容Base64（type=CERT必填）")
        private String certFileBase64;
        @Schema(description = "证书密码（type=CERT必填）", example = "******")
        private String certPassword;
    }

    @Data
    @Schema(name = "ProtocolTemplateDTO", description = "协议参数模板")
    public static class ProtocolTemplateDTO {
        @Schema(description = "关联协议ID字符串（多个用逗号分割）", example = "1001,1002,1003")
        private String protocolIdStr;

        @NotBlank(message = "模板名称不能为空")
        @Schema(description = "模板名称", example = "BOM同步模板", requiredMode = Schema.RequiredMode.REQUIRED)
        private String templateName;

        @NotBlank(message = "模板编码不能为空")
        @Schema(description = "模板编码（同一请求内不可重复）", example = "BOM_SYNC_V1", requiredMode = Schema.RequiredMode.REQUIRED)
        private String templateCode;

        /**
         * 参数快照（JSON字符串，存储完整参数配置）
         */
        @NotBlank(message = "参数快照不能为空")
        @Schema(description = "参数快照（JSON字符串，存储完整参数配置）", example = "{\"version\":1,\"params\":[]}",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String paramsSnapshot;

        /**
         * 是否公开模板：0-私有，1-公开
         */
        @Schema(description = "是否公开：0-私有，1-公开", example = "0", allowableValues = {"0", "1"})
        private Integer isPublic;

        @Valid
        @Schema(description = "模板分组列表")
        private List<ProtocolTemplateGroupDTO> groups;
    }

    @Data
    @Schema(name = "ProtocolTemplateGroupDTO", description = "模板分组")
    public static class ProtocolTemplateGroupDTO {
        @Schema(description = "分组名称", example = "连接参数")
        private String groupName;

        /**
         * 分组参数配置：JSON列表
         */
        @Valid
        @NotEmpty(message = "templates.groups.paramsConfig不能为空")
        @Schema(description = "分组参数配置列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<ProtocolTemplateGroupParamDTO> paramsConfig;
    }

    @Data
    @Schema(name = "ProtocolTemplateGroupParamDTO", description = "模板分组参数项")
    public static class ProtocolTemplateGroupParamDTO {
        @NotBlank(message = "templates.groups.paramsConfig.paramName不能为空")
        @Schema(description = "参数名称", example = "connectTimeout", requiredMode = Schema.RequiredMode.REQUIRED)
        private String paramName;

        /**
         * 类型：STRING/NUMBER/BOOLEAN
         */
        @NotBlank(message = "模板参数类型不能为空")
        @Schema(description = "参数类型", example = "NUMBER", allowableValues = {"STRING", "NUMBER", "BOOLEAN"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String type;

        /**
         * 默认值
         */
        @Schema(description = "默认值（类型随type变化）", example = "3000")
        private Object defaultValue;

        /**
         * 是否必填
         */
        @NotNull(message = "是否必填不能为空")
        @Schema(description = "是否必填", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean required;

        @Schema(description = "描述", example = "连接超时时间（毫秒）")
        private String description;
    }
}

