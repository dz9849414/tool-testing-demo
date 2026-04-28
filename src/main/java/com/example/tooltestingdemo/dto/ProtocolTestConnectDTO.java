package com.example.tooltestingdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议连接测试请求体：仅传协议配置ID。
 */
@Data
@Schema(name = "ProtocolTestConnectDTO", description = "协议连接测试请求体")
public class ProtocolTestConnectDTO {

    @NotNull(message = "协议配置ID不能为空")
    @Schema(description = "协议配置ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long configId;
}

