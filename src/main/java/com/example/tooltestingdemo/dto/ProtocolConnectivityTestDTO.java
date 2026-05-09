package com.example.tooltestingdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 协议网络诊断请求体。
 */
@Data
@Schema(name = "ProtocolConnectivityTestDTO", description = "协议网络诊断请求体")
public class ProtocolConnectivityTestDTO {

    @Schema(description = "模板ID；传入后从模板URL解析目标，优先级最高", example = "1000")
    private Long configId;

    @Schema(description = "协议类型；不传时使用模板协议类型、配置协议名称或URL协议头", example = "TCP")
    private String protocolType;

    @Schema(description = "目标地址；传入后覆盖模板/配置解析出的主机", example = "127.0.0.1")
    private String host;

    @Min(value = 1, message = "端口范围1-65535")
    @Max(value = 65535, message = "端口范围1-65535")
    @Schema(description = "目标端口；传入后覆盖模板/配置解析出的端口", example = "8080")
    private Integer port;

    @Schema(description = "目标URL；未传templateId/configId时可直接用URL解析目标", example = "tcp://127.0.0.1:8080")
    private String url;

    @Min(value = 1, message = "超时时间必须大于0")
    @Schema(description = "诊断超时时间，单位毫秒；不传默认使用模板/配置连接超时或5000", example = "5000")
    private Integer timeoutMs;

    @Schema(description = "是否执行ping；默认true", example = "true")
    private Boolean ping;

    @Schema(description = "是否执行telnet；默认true", example = "true")
    private Boolean telnet;
}
