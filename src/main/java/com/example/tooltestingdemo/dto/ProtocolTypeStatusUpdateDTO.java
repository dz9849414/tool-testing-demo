package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议类型状态更新 DTO
 */
@Data
public class ProtocolTypeStatusUpdateDTO {
    /**
     * 协议类型ID
     */
    @NotNull(message = "协议类型ID不能为空")
    private Long id;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 禁用时二次确认
     */
    private Boolean confirm;
}
