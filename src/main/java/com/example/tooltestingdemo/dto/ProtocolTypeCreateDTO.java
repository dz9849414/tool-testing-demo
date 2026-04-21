package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议类型新增请求 DTO
 */
@Data
public class ProtocolTypeCreateDTO {

    @NotBlank(message = "协议编码不能为空")
    private String protocolCode;

    @NotBlank(message = "协议名称不能为空")
    private String protocolName;

    private String protocolCategory;

    private String systemType;

    @NotNull(message = "协议状态不能为空")
    private Integer status;

    private String description;
}
