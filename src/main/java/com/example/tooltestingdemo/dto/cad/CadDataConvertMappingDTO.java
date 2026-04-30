package com.example.tooltestingdemo.dto.cad;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CadDataConvertMappingDTO {
    private Long id;
    @NotBlank(message = "源字段不能为空")
    private String sourceField;
    private String sourceFieldName;
    @NotBlank(message = "目标模块不能为空")
    private String targetModule;
    @NotBlank(message = "目标字段不能为空")
    private String targetField;
    private String targetFieldName;
    private String fieldType;
    private Integer required;
    private String defaultValue;
    private String transformRule;
    private Integer sortNo;
    private Integer status;
    private String convertDirection;
}
