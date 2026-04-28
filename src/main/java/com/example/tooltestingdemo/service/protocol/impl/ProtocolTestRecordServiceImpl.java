package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolTestTransferDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.entity.protocol.ProtocolTestRecord;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTestRecordMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.service.protocol.IProtocolTestRecordService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 协议测试记录表 服务实现类
 * </p>
 *
 * @author aixiaojun
 * @since 2026-04-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProtocolTestRecordServiceImpl extends ServiceImpl<ProtocolTestRecordMapper, ProtocolTestRecord> implements IProtocolTestRecordService {

    private final IProtocolConfigService protocolConfigService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public ProtocolTestRecord testConnect(Long configId) {
        ProtocolConfig config = getRequiredConfig(configId);
        String url = resolvePrimaryUrl(config);
        int connectTimeoutMs = config.getTimeoutConnect() == null ? 5000 : config.getTimeoutConnect();
        int readTimeoutMs = config.getTimeoutRead() == null ? 30000 : config.getTimeoutRead();

        LinkedHashMap<String, Object> requestMeta = new LinkedHashMap<>();
        requestMeta.put("url", url);
        requestMeta.put("method", HttpMethod.GET.name());
        requestMeta.put("timeoutConnect", connectTimeoutMs);
        requestMeta.put("timeoutRead", readTimeoutMs);

        return executeAndSave(config, ProtocolTestRecord.TestType.CONNECT.name(), ProtocolTestRecord.TestScenario.NETWORK.name(),
                url, HttpMethod.GET, null, requestMeta, connectTimeoutMs, readTimeoutMs);
    }

    @Override
    public ProtocolTestRecord testTransfer(ProtocolTestTransferDTO dto) {
        if (dto == null || dto.getConfigId() == null) {
            throw new IllegalArgumentException("协议配置ID不能为空");
        }
        ProtocolConfig config = getRequiredConfig(dto.getConfigId());
        String baseUrl = resolvePrimaryUrl(config);
        String fullUrl = buildTransferUrl(baseUrl, dto.getPath(), dto.getQueryParams());
        HttpMethod httpMethod = resolveHttpMethod(dto.getMethod());
        int connectTimeoutMs = config.getTimeoutConnect() == null ? 5000 : config.getTimeoutConnect();
        int readTimeoutMs = config.getTimeoutRead() == null ? 30000 : config.getTimeoutRead();

        HttpHeaders headers = new HttpHeaders();
        if (dto.getHeaders() != null) {
            dto.getHeaders().forEach((k, v) -> {
                if (StringUtils.isNotBlank(k) && v != null) {
                    headers.set(k.trim(), v);
                }
            });
        }
        HttpEntity<?> entity = new HttpEntity<>(dto.getBody(), headers);

        LinkedHashMap<String, Object> requestMeta = new LinkedHashMap<>();
        requestMeta.put("url", fullUrl);
        requestMeta.put("method", httpMethod.name());
        requestMeta.put("headers", dto.getHeaders());
        requestMeta.put("queryParams", dto.getQueryParams());
        requestMeta.put("body", dto.getBody());
        requestMeta.put("timeoutConnect", connectTimeoutMs);
        requestMeta.put("timeoutRead", readTimeoutMs);

        return executeAndSave(config, ProtocolTestRecord.TestType.TRANSFER.name(), ProtocolTestRecord.TestScenario.PROTOCOL.name(),
                fullUrl, httpMethod, entity, requestMeta, connectTimeoutMs, readTimeoutMs);
    }

    private List<ProtocolConfigCreateDTO.UrlConfigItemDTO> parseUrlConfigList(String urlConfigJson) {
        if (StringUtils.isBlank(urlConfigJson)) {
            throw new IllegalArgumentException("协议配置URL未配置");
        }
        try {
            return objectMapper.readValue(urlConfigJson, new TypeReference<List<ProtocolConfigCreateDTO.UrlConfigItemDTO>>() {
            });
        } catch (Exception e) {
            log.warn("解析协议配置urlConfig失败: {}", e.getMessage());
            throw new IllegalArgumentException("协议配置URL解析失败");
        }
    }

    private ProtocolConfigCreateDTO.UrlConfigItemDTO pickPrimaryUrl(List<ProtocolConfigCreateDTO.UrlConfigItemDTO> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("协议配置URL列表为空");
        }
        ProtocolConfigCreateDTO.UrlConfigItemDTO primary = null;
        for (ProtocolConfigCreateDTO.UrlConfigItemDTO item : list) {
            if (Boolean.TRUE.equals(item.getPrimary())) {
                if (primary != null) {
                    throw new IllegalArgumentException("协议配置存在多个主URL");
                }
                primary = item;
            }
        }
        if (primary == null) {
            throw new IllegalArgumentException("协议配置未设置主URL");
        }
        return primary;
    }

    private String buildFinalUrl(ProtocolConfigCreateDTO.UrlConfigItemDTO item) {
        if (item == null || StringUtils.isBlank(item.getUrl())) {
            throw new IllegalArgumentException("主URL不能为空");
        }
        String rawUrl = item.getUrl().trim();
        if (Boolean.TRUE.equals(item.getUseDefaultPort())) {
            return rawUrl;
        }
        if (item.getPort() == null) {
            return rawUrl;
        }
        try {
            URI uri = new URI(rawUrl);
            URI rebuilt = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    item.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );
            return rebuilt.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("主URL格式不正确");
        }
    }

    private RestTemplate buildRestTemplateWithTimeouts(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.max(connectTimeoutMs, 1));
        factory.setReadTimeout(Math.max(readTimeoutMs, 1));
        RestTemplate template = new RestTemplate(factory);
        template.setMessageConverters(restTemplate.getMessageConverters());
        return template;
    }

    private ProtocolTestRecord executeAndSave(ProtocolConfig config,
                                              String testType,
                                              String testScenario,
                                              String url,
                                              HttpMethod method,
                                              HttpEntity<?> requestEntity,
                                              Map<String, Object> requestMeta,
                                              int connectTimeoutMs,
                                              int readTimeoutMs) {
        ProtocolTestRecord record = initRecord(config, testType, testScenario);
        record.setTestData(toJson(requestMeta));
        long start = System.currentTimeMillis();
        try {
            RestTemplate template = buildRestTemplateWithTimeouts(connectTimeoutMs, readTimeoutMs);
            ResponseEntity<String> response = template.exchange(url, method, requestEntity, String.class);
            long elapsed = System.currentTimeMillis() - start;
            record.setResponseTime((int) elapsed);
            record.setResponseCode(String.valueOf(response.getStatusCode().value()));
            record.setResultStatus(response.getStatusCode().is2xxSuccessful()
                    ? ProtocolTestRecord.ResultStatus.SUCCESS.name()
                    : ProtocolTestRecord.ResultStatus.FAILED.name());
            record.setErrorMessage(response.getStatusCode().is2xxSuccessful() ? null : truncate("HTTP状态非2xx"));
            record.setComparisonResult(buildResponseSummary(response.getBody(), response.getStatusCode().value()));
        } catch (HttpStatusCodeException ex) {
            long elapsed = System.currentTimeMillis() - start;
            record.setResponseTime((int) elapsed);
            record.setResponseCode(String.valueOf(ex.getStatusCode().value()));
            record.setResultStatus(ProtocolTestRecord.ResultStatus.FAILED.name());
            record.setErrorMessage(truncate(ex.getStatusText()));
            record.setComparisonResult(buildResponseSummary(ex.getResponseBodyAsString(), ex.getStatusCode().value()));
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            record.setResponseTime((int) elapsed);
            record.setResponseCode("0");
            record.setResultStatus(ProtocolTestRecord.ResultStatus.FAILED.name());
            record.setErrorMessage(truncate(ex.getMessage()));
        }

        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);
        return record;
    }

    private ProtocolTestRecord initRecord(ProtocolConfig config, String testType, String testScenario) {
        ProtocolTestRecord record = new ProtocolTestRecord();
        record.setProtocolId(config.getProtocolId());
        record.setConfigId(config.getId());
        record.setTestType(testType);
        record.setTestScenario(testScenario);
        record.setIsManual(0);
        return record;
    }

    private String buildTransferUrl(String baseUrl, String path, Map<String, Object> queryParams) {
        String url = baseUrl;
        if (StringUtils.isNotBlank(path)) {
            String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String normalizedPath = path.startsWith("/") ? path : "/" + path;
            url = normalizedBase + normalizedPath;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        if (queryParams != null) {
            queryParams.forEach((k, v) -> {
                if (StringUtils.isNotBlank(k) && v != null) {
                    builder.queryParam(k, v);
                }
            });
        }
        return builder.build(true).toUriString();
    }

    private HttpMethod resolveHttpMethod(String method) {
        if (StringUtils.isBlank(method)) {
            return HttpMethod.POST;
        }
        try {
            return HttpMethod.valueOf(method.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        }
    }

    private ProtocolConfig getRequiredConfig(Long configId) {
        if (configId == null) {
            throw new IllegalArgumentException("协议配置ID不能为空");
        }
        ProtocolConfig config = protocolConfigService.getById(configId);
        if (config == null) {
            throw new IllegalArgumentException("协议配置不存在");
        }
        return config;
    }

    private String resolvePrimaryUrl(ProtocolConfig config) {
        List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList = parseUrlConfigList(config.getUrlConfig());
        ProtocolConfigCreateDTO.UrlConfigItemDTO primaryUrlItem = pickPrimaryUrl(urlConfigList);
        return buildFinalUrl(primaryUrlItem);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildResponseSummary(String responseBody, Integer statusCode) {
        LinkedHashMap<String, Object> summary = new LinkedHashMap<>();
        summary.put("statusCode", statusCode);
        summary.put("responseBodyPreview", truncate(responseBody));
        return toJson(summary);
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        if (t.length() <= 500) {
            return t;
        }
        return t.substring(0, 500);
    }
}
