package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 协议类型批量状态更新 DTO
 */
@Data
public class ProtocolTypeBatchStatusUpdateDTO {

    /**
     * 协议类型ID列表
     */
    @NotEmpty(message = "协议类型ID列表不能为空")
    private Long[] ids;

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
