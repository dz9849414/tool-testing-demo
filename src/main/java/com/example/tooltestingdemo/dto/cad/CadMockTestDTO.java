package com.example.tooltestingdemo.dto.cad;

import lombok.Data;

import java.util.Map;

@Data
public class CadMockTestDTO {
    private Long mockInterfaceId;
    private String cadType;
    private String applyFlow;
    private Long sourceFileId;
    private String sourceFileName;
    private String sourceFormat;
    private String targetFormat;
    private Map<String, Object> requestData;
}
