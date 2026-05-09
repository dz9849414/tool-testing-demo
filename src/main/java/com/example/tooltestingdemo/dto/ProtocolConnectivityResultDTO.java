package com.example.tooltestingdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 协议网络诊断结果。
 */
@Data
@Schema(name = "ProtocolConnectivityResultDTO", description = "协议网络诊断结果")
public class ProtocolConnectivityResultDTO {

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "协议配置ID")
    private Long configId;

    @Schema(description = "协议类型")
    private String protocolType;

    @Schema(description = "目标URL")
    private String targetUrl;

    @Schema(description = "目标主机")
    private String host;

    @Schema(description = "目标端口")
    private Integer port;

    @Schema(description = "超时时间，单位毫秒")
    private Integer timeoutMs;

    @Schema(description = "ping结果")
    private CheckResult ping;

    @Schema(description = "telnet结果")
    private CheckResult telnet;

    @Data
    @Schema(name = "ProtocolConnectivityCheckResult", description = "单项诊断结果")
    public static class CheckResult {

        @Schema(description = "是否执行")
        private Boolean executed;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "耗时，单位毫秒")
        private Long responseTimeMs;

        @Schema(description = "结果说明")
        private String message;
    }
}
