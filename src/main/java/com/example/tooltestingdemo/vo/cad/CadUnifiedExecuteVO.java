package com.example.tooltestingdemo.vo.cad;

import lombok.Data;

import java.util.Map;

@Data
public class CadUnifiedExecuteVO {
    private Map<String, Object> mappedData;
    private CadFileConvertTaskVO fileConvert;
}
