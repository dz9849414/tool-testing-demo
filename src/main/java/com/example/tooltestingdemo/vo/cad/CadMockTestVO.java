package com.example.tooltestingdemo.vo.cad;

import lombok.Data;

import java.util.Map;

@Data
public class CadMockTestVO {
    private Long mockInterfaceId;
    private Boolean success;
    private Object rawResponse;
    private Map<String, Object> convertedData;
    private CadFileConvertTaskVO fileConvert;
    private String errorMessage;
}
