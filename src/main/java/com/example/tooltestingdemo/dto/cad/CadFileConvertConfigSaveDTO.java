package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CadFileConvertConfigSaveDTO {
    private Long id;
    private Long mockInterfaceId;
    @NotBlank(message = "CAD类型不能为空")
    private String cadType;
    @NotBlank(message = "适用流程不能为空")
    private String applyFlow;
    @NotBlank(message = "源文件格式不能为空")
    private String sourceFormat;
    @NotBlank(message = "目标文件格式不能为空")
    private String targetFormat;
    @NotBlank(message = "转换服务地址不能为空")
    private String convertUrl;
    private Integer asyncConvert;
    private Integer timeoutSeconds;
    private Integer generatePreview;
    private Integer keepSourceFile;
    private Integer overwriteTargetFile;
    private String requestTemplate;
    private String responseTemplate;
    private Integer status;
    private String remark;
}
