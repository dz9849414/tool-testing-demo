package com.example.tooltestingdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 协议数据传输测试请求体。
 */
@Data
@Schema(name = "ProtocolTestTransferDTO", description = "协议数据传输测试请求体")
public class ProtocolTestTransferDTO {

    @NotNull(message = "协议配置ID不能为空")
    @Schema(description = "协议配置ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long configId;

    @Schema(description = "HTTP方法，默认POST", example = "POST")
    private String method;

    @Schema(description = "相对路径（可选）", example = "/api/v1/order/sync")
    private String path;

    @Schema(description = "请求头（可选）")
    private Map<String, String> headers;

    @Schema(description = "请求参数（Query，可选）")
    private Map<String, Object> queryParams;

    @Schema(description = "请求体（可选，可传对象）")
    private Object body;
}

