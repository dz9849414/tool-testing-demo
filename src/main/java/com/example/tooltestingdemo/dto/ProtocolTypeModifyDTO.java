package com.example.tooltestingdemo.dto;

import lombok.Data;

/**
 * 协议类型编辑 DTO
 */
@Data
public class ProtocolTypeModifyDTO {

    private Long id;

    private String protocolName;

    private String description;
}