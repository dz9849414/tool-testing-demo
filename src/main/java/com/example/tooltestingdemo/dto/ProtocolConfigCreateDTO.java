package com.example.tooltestingdemo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 协议配置新增请求体：仅包含协议配置主数据、多 URL 与多认证（结构化入参，落库时序列化为 JSON）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "ProtocolConfigCreateDTO", description = "协议配置新增请求体（不含参数模板）")
public class ProtocolConfigCreateDTO {

    @NotNull(message = "协议类型ID不能为空")
    @Schema(description = "关联协议类型ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long protocolId;

    @NotNull(message = "协议类型名称不能为空")
    @Schema(description = "关联协议类型名称", example = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocolName;

    @NotBlank(message = "configName不能为空")
    @Schema(description = "配置名称", example = "ERP对接-生产环境", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configName;

    @Valid
    @NotEmpty(message = "URL 配置列表不能为空")
    @Schema(description = "URL 配置列表；全局必须且只能有一个主 URL（primary=true）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UrlConfigItemDTO> urlConfigList;

    @Valid
    @Schema(description = "认证配置列表（可选）；不同 type 使用不同字段组合")
    private List<AuthConfigItemDTO> authConfigList;

    @Min(value = 1, message = "连接超时时间必须大于0")
    @Schema(description = "连接超时（毫秒）；不传则服务端使用表默认值", example = "5000", minimum = "1")
    private Integer timeoutConnect;

    @Min(value = 1, message = "读取超时时间必须大于0")
    @Schema(description = "读取超时（毫秒）；不传则服务端使用表默认值", example = "30000", minimum = "1")
    private Integer timeoutRead;

    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 10, message = "重试次数不能大于10")
    @Schema(description = "重试次数 0-10；不传则服务端使用表默认值", example = "3", minimum = "0", maximum = "10")
    private Integer retryCount;

    @Min(value = 0, message = "重试间隔时间不能小于0")
    @Schema(description = "重试间隔（毫秒）；不传则服务端使用表默认值", example = "1000", minimum = "0")
    private Integer retryInterval;

    @Schema(description = "重试触发条件：1-链接超时，2-响应超时，3-响应错误码；可组合如 1,2", example = "1,2")
    private String retryCondition;

    @Schema(description = "数据格式", example = "JSON", allowableValues = {"JSON", "XML", "FORM", "TEXT", "BINARY"})
    private String dataFormat;

    @Schema(description = "格式校验配置（JSON 字符串，如 JSON Schema、XSD）")
    private String formatConfig;

    @Schema(description = "额外参数（JSON 字符串）")
    private String additionalParams;

    @Schema(description = "状态：0-禁用，1-启用；不传默认 0", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Size(max = 500, message = "描述长度不能超过500")
    @Schema(description = "协议参数配置说明（纯文本）", example = "用于生产环境 ERP 订单同步")
    private String description;

    @Data
    @Schema(name = "UrlConfigItemDTO", description = "单条 URL 配置")
    public static class UrlConfigItemDTO {

        @NotNull(message = "URL配置-序号不能为空")
        @Schema(description = "展示/排序序号，同一配置内不可重复", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer seq;

        @NotBlank(message = "URL配置-URL不能为空")
        @Schema(description = "完整 URL", example = "https://api.example.com/v1", requiredMode = Schema.RequiredMode.REQUIRED)
        private String url;

        @NotNull(message = "URL配置-是否默认端口不能为空（false/true）")
        @Schema(description = "true：使用 URL 自带默认端口；false：必须填写 port", example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean useDefaultPort;

        @Min(value = 1, message = "URL配置-端口范围1-65535")
        @Max(value = 65535, message = "URL配置-端口范围1-65535")
        @Schema(description = "端口；仅当 useDefaultPort=false 时必填，范围 1-65535", example = "443")
        private Integer port;

        @NotNull(message = "URL配置-是否主URL不能为空")
        @Schema(description = "是否主 URL；同一请求内必须且只能有一条为 true", example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Boolean primary;
    }

    @Data
    @Schema(name = "AuthConfigItemDTO", description = "单条认证配置（按 type 分支校验）")
    public static class AuthConfigItemDTO {

        @NotBlank(message = "认证配置-认证配置名称不能为空")
        @Schema(description = "认证配置名称，便于前端展示或多套认证切换", example = "生产 Token", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @NotBlank(message = "认证配置-认证类型不能为空")
        @Schema(description = "认证类型", example = "TOKEN", allowableValues = {"NONE", "BASIC", "TOKEN", "OAUTH2", "CERT"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String type;

        @Schema(description = "Basic：用户名（type=BASIC 必填）", example = "admin")
        private String username;

        @Schema(description = "Basic：密码（type=BASIC 必填）")
        private String password;

        @Schema(description = "Token：令牌值（type=TOKEN 必填）")
        private String token;

        @Schema(description = "Token：放置位置 HEADER 或 QUERY（type=TOKEN 必填）", allowableValues = {"HEADER", "QUERY"})
        private String tokenLocation;

        @Schema(description = "Token：当 tokenLocation=HEADER 时，HTTP 头名（必填）", example = "Authorization")
        private String headerName;

        @Schema(description = "OAuth2：授权端点 URL（type=OAUTH2 必填）")
        private String authEndpoint;

        @Schema(description = "OAuth2：Client ID（type=OAUTH2 必填）")
        private String clientId;

        @Schema(description = "OAuth2：Client Secret（type=OAUTH2 必填）")
        private String clientSecret;

        @Schema(description = "OAuth2：Scope（可选）", example = "read write")
        private String scope;

        @Schema(description = "证书：文件名（type=CERT 必填）", example = "client.p12")
        private String certFileName;

        @Schema(description = "证书：文件内容 Base64（type=CERT 必填）；若接对象存储可改为存 fileId/url")
        private String certFileBase64;

        @Schema(description = "证书：存储/使用密码（type=CERT 必填）")
        private String certPassword;
    }
}
