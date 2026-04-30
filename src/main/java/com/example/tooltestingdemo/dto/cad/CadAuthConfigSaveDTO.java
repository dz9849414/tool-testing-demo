package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CadAuthConfigSaveDTO {
    @NotNull(message = "模拟接口ID不能为空")
    private Long mockInterfaceId;
    @NotBlank(message = "认证方式不能为空")
    private String authType;
    private String authConfig;
}
