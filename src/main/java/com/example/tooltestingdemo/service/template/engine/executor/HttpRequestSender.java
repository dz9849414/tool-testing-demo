package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpRequestSender {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HttpResponseData send(HttpRequestSpec requestSpec) {
        ExecutionResult.RequestInfo requestInfo = requestSpec.requestInfo();
        HttpEntity<?> entity = requestSpec.entity();

        try {
            logRequest(requestInfo, entity.getBody());

            long start = System.currentTimeMillis();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                requestInfo.getUrl(),
                HttpMethod.valueOf(requestInfo.getMethod().toUpperCase()),
                entity,
                String.class
            );

            return new HttpResponseData(
                responseEntity.getStatusCode().value(),
                HttpStatus.valueOf(responseEntity.getStatusCode().value()).getReasonPhrase(),
                convertHeaders(responseEntity.getHeaders()),
                responseEntity.getBody(),
                System.currentTimeMillis() - start
            );
        } catch (HttpStatusCodeException e) {
            log.error("HTTP 请求返回错误状态: method={}, url={}, status={}",
                requestInfo.getMethod(), requestInfo.getUrl(), e.getStatusCode().value(), e);
            return new HttpResponseData(
                e.getStatusCode().value(),
                e.getStatusText(),
                convertHeaders(e.getResponseHeaders()),
                e.getResponseBodyAsString(),
                0L
            );
        } catch (Exception e) {
            log.error("HTTP 请求执行失败: method={}, url={}",
                requestInfo.getMethod(), requestInfo.getUrl(), e);
            return new HttpResponseData(
                0,
                "Error",
                Map.of(),
                e.getMessage(),
                0L
            );
        }
    }

    private Map<String, String> convertHeaders(org.springframework.http.HttpHeaders headers) {
        Map<String, String> result = new HashMap<>();
        if (headers != null) {
            headers.forEach((key, values) -> {
                if (!values.isEmpty()) {
                    result.put(key, String.join(", ", values));
                }
            });
        }
        return result;
    }

    private void logRequest(ExecutionResult.RequestInfo requestInfo, Object body) {
        String bodyPreview;
        try {
            bodyPreview = body == null ? null : objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            bodyPreview = String.valueOf(body);
        }
        if (bodyPreview != null && bodyPreview.length() > 2000) {
            bodyPreview = bodyPreview.substring(0, 2000) + "...";
        }
        log.info("发送 HTTP 请求: method={}, url={}, headers={}, body={}",
            requestInfo.getMethod(), requestInfo.getUrl(), requestInfo.getHeaders(), bodyPreview);
    }
}
