package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议类型编辑 DTO
 */
@Data
public class ProtocolTypeModifyDTO {

    @NotNull(message = "协议类型ID不能为空")
    private Long id;

    private String protocolCode;

    private String protocolName;

    private String protocolCategory;

    private String systemType;

    private Integer status;

    private String description;

    private Integer version;
}