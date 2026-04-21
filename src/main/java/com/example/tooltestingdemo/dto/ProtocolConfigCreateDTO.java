package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议配置新增 DTO
 */
@Data
public class ProtocolConfigCreateDTO {

    /**
     * 关联协议类型 ID
     */
    @NotNull(message = "关联协议类型ID不能为空")
    private Long protocolId;

    /**
     * 配置名称
     */
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    /**
     * 访问 URL
     */
    @NotBlank(message = "访问URL不能为空")
    private String url;

    /**
     * 端口号（1-65535）
     */
    @NotNull(message = "端口号不能为空")
    @Min(value = 1, message = "端口号必须在 1-65535 范围内")
    @Max(value = 65535, message = "端口号必须在 1-65535 范围内")
    private Integer port;

    /**
     * 认证方式：NONE/BASIC/TOKEN/OAUTH2/CERT
     */
    @NotBlank(message = "认证方式不能为空")
    private String authType;

    /**
     * 认证配置（JSON 字符串）
     */
    private String authConfig;

    /**
     * 连接超时时间（毫秒）
     */
    @NotNull(message = "连接超时时间不能为空")
    @Min(value = 1, message = "连接超时时间必须大于0")
    private Integer timeoutConnect;

    /**
     * 读取超时时间（毫秒）
     */
    @NotNull(message = "读取超时时间不能为空")
    @Min(value = 1, message = "读取超时时间必须大于0")
    private Integer timeoutRead;

    /**
     * 重试次数（0-10）
     */
    @NotNull(message = "重试次数不能为空")
    @Min(value = 0, message = "重试次数必须在 0-10 范围内")
    @Max(value = 10, message = "重试次数必须在 0-10 范围内")
    private Integer retryCount;

    /**
     * 重试间隔（毫秒）
     */
    @NotNull(message = "重试间隔不能为空")
    @Min(value = 0, message = "重试间隔不能小于0")
    private Integer retryInterval;

    /**
     * 数据格式：JSON/XML/FORM/TEXT/BINARY
     */
    @NotBlank(message = "数据格式不能为空")
    private String dataFormat;

    /**
     * 格式校验配置（JSON 字符串）
     */
    private String formatConfig;

    /**
     * 额外参数（JSON 字符串）
     */
    private String additionalParams;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
