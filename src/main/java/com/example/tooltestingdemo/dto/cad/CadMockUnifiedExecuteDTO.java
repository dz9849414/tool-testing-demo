package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CadMockUnifiedExecuteDTO {
    @NotBlank(message = "CAD类型不能为空")
    private String cadType;
    @NotBlank(message = "适用流程不能为空")
    private String applyFlow;
    private Long sourceFileId;
    private String sourceFileName;
    private String sourceFormat;
    private String targetFormat;
    private Map<String, Object> requestData;
}
