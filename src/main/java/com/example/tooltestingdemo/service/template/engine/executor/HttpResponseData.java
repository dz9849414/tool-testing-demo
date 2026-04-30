package com.example.tooltestingdemo.service.template.engine.executor;

import java.util.Map;

public record HttpResponseData(
    Integer statusCode,
    String statusText,
    Map<String, String> headers,
    String body,
    Long responseTime
) {
}
