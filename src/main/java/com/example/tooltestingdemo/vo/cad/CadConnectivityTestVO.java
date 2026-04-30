package com.example.tooltestingdemo.vo.cad;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CadConnectivityTestVO {
    private Boolean success;
    private String requestMethod;
    private String requestUrl;
    private Integer statusCode;
    private Map<String, List<String>> responseHeaders;
    private String responseBody;
    private Long costTimeMs;
    private String errorMessage;
}
