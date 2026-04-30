package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CadFileConvertExecuteDTO {
    @NotBlank(message = "CAD类型不能为空")
    private String cadType;
    @NotBlank(message = "适用流程不能为空")
    private String applyFlow;
    @NotNull(message = "源文件ID不能为空")
    private Long sourceFileId;
    @NotBlank(message = "源文件名称不能为空")
    private String sourceFileName;
    @NotBlank(message = "源文件格式不能为空")
    private String sourceFormat;
    @NotBlank(message = "目标文件格式不能为空")
    private String targetFormat;
}
