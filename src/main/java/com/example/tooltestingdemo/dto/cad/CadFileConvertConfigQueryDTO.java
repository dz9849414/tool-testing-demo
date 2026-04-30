package com.example.tooltestingdemo.dto.cad;

import lombok.Data;

@Data
public class CadFileConvertConfigQueryDTO {
    private String cadType;
    private String applyFlow;
    private String sourceFormat;
    private String targetFormat;
    private Integer status;
}
