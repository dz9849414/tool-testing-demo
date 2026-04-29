package com.example.tooltestingdemo.service.template.engine.executor;

import com.alibaba.fastjson2.JSON;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpExecutor implements TemplateExecutor {

    private final HttpRequestFactory requestFactory;
    private final HttpRequestSender requestSender;
    private final ObjectMapper objectMapper;

    @Override
    public String getType() {
        return "HTTP";
    }

    @Override
    public ExecutionResult execute(TemplateContext context) {
        log.debug("执行http请求: templateInfo={}",
            context.getTemplate() != null ? context.getTemplate().getId() : null);

        LocalDateTime startTime = LocalDateTime.now();
        HttpRequestSpec requestSpec = requestFactory.build(context);
        HttpResponseData responseData = requestSender.send(requestSpec);
        boolean success = isSuccessful(responseData);

        return ExecutionResult.builder()
            .success(success)
            .statusCode(String.valueOf(responseData.statusCode()))
            .message(success ? "执行成功" : "执行失败")
            .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
            .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
            .startTime(startTime)
            .request(requestSpec.requestInfo())
            .response(buildResponseInfo(responseData))
            .variables(context.getAllVariables())
            .build();
    }

    @Override
    public ValidationResult validate(TemplateContext context) {
        if (context.getTemplate() == null) {
            return ValidationResult.failure("模板信息不能为空");
        }
        if (!StringUtils.hasText(context.getTemplate().getMethod())) {
            return ValidationResult.failure("请求方法不能为空");
        }

        String fullUrl = requestFactory.resolveRequestUrl(context);
        if (!StringUtils.hasText(fullUrl)) {
            return ValidationResult.failure("请求URL不能为空");
        }
        try {
            new java.net.URL(fullUrl);
        } catch (Exception e) {
            return ValidationResult.failure("URL格式不正确: " + fullUrl);
        }
        return ValidationResult.success();
    }

    @Override
    public PreviewResult preview(TemplateContext context) {
        HttpRequestSpec requestSpec = requestFactory.build(context);
        ExecutionResult.RequestInfo requestInfo = requestSpec.requestInfo();
        return new PreviewResult(
            requestInfo.getUrl(),
            requestInfo.getMethod(),
            requestInfo.getHeaders(),
            requestInfo.getBody(),
            requestInfo.getParameters()
        );
    }

    private ExecutionResult.ResponseInfo buildResponseInfo(HttpResponseData responseData) {
        String body = responseData.body();
        return ExecutionResult.ResponseInfo.builder()
            .statusCode(responseData.statusCode())
            .statusText(responseData.statusText())
            .headers(responseData.headers())
            .body(parseResponseBody(body))
            .size(body != null ? (long) body.length() : 0L)
            .responseTime(responseData.responseTime())
            .build();
    }

    private boolean isSuccessful(HttpResponseData responseData) {
        if (responseData.statusCode() == null || responseData.statusCode() != 200) {
            return false;
        }
        if (!StringUtils.hasText(responseData.body())) {
            return false;
        }
        try {
            return JSON.parseObject(responseData.body()).getInteger("code") == 200;
        } catch (Exception e) {
            log.debug("Response body does not contain business code");
            return false;
        }
    }

    private Object parseResponseBody(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            String trimBody = body.trim();
            if (trimBody.startsWith("{")) {
                return objectMapper.readValue(trimBody, new TypeReference<Map<String, Object>>() {
                });
            }
            if (trimBody.startsWith("[")) {
                return objectMapper.readValue(trimBody, new TypeReference<List<Object>>() {
                });
            }
        } catch (Exception e) {
            log.debug("Response body is not JSON, returning raw string");
        }
        return body;
    }
}
