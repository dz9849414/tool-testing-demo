package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CadDataConvertSaveBatchDTO {
    @NotNull(message = "模拟接口ID不能为空")
    private Long mockInterfaceId;
    @Valid
    @NotEmpty(message = "字段映射不能为空")
    private List<CadDataConvertMappingDTO> mappings;
}
