package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 协议参数配置新增 DTO
 */
@Data
public class ProtocolParameterConfigCreateDTO {

    private Long protocolId;

    private String parameterName;

    private String parameterValue;

    private Integer isSensitive;

    private String encryptedValue;
}