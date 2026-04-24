package com.example.tooltestingdemo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 协议类型批量删除 DTO
 */
@Data
public class ProtocolTypeBatchDeleteDTO {

    @NotEmpty(message = "协议类型ID列表不能为空")
    private Long[] ids;
}
