package com.example.tooltestingdemo.dto.cad;

import lombok.Data;

@Data
public class CadConnectivityTestDTO {
    private Long mockInterfaceId;
    private String baseUrl;
    private String requestMethod;
    private String interfaceUrl;
    private String requestHeaders;
    private String requestParams;
    private String requestBody;
    private String authType;
    private String authConfig;
    private Integer timeoutSeconds;
}
