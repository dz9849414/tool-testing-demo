package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议配置状态更新 DTO
 */
@Data
public class ProtocolConfigStatusUpdateDTO {
    /**
     * 协议配置ID
     */
    @NotNull(message = "协议配置ID不能为空")
    private Long id;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
