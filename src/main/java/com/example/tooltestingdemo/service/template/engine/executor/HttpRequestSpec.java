package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import org.springframework.http.HttpEntity;

public record HttpRequestSpec(
    ExecutionResult.RequestInfo requestInfo,
    HttpEntity<?> entity
) {
}
