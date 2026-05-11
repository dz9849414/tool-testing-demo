package com.example.tooltestingdemo.service.protocol.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.tooltestingdemo.dto.ProtocolConfigCreateDTO;
import com.example.tooltestingdemo.dto.ProtocolConnectivityResultDTO;
import com.example.tooltestingdemo.dto.ProtocolConnectivityTestDTO;
import com.example.tooltestingdemo.dto.ProtocolTestRecordQueryDTO;
import com.example.tooltestingdemo.dto.ProtocolTestTransferDTO;
import com.example.tooltestingdemo.entity.protocol.ProtocolConfig;
import com.example.tooltestingdemo.entity.protocol.ProtocolTestRecord;
import com.example.tooltestingdemo.mapper.protocol.ProtocolTestRecordMapper;
import com.example.tooltestingdemo.service.protocol.IProtocolConfigService;
import com.example.tooltestingdemo.service.protocol.IProtocolTestRecordService;
import com.example.tooltestingdemo.service.template.InterfaceTemplateService;
import com.example.tooltestingdemo.vo.InterfaceTemplateVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String EXPORT_FILE_NAME = "协议测试记录导出";

    private final IProtocolConfigService protocolConfigService;
    private final InterfaceTemplateService templateService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public ProtocolTestRecord testConnect(Long configId) {
        log.info("连通性测试开始: configId={}", configId);
        ProtocolConfig config = getRequiredProtocolConfig(configId);
        String url = resolvePrimaryUrl(config);
        int connectTimeoutMs = config.getTimeoutConnect() == null ? 5000 : config.getTimeoutConnect();
        int readTimeoutMs = config.getTimeoutRead() == null ? 30000 : config.getTimeoutRead();

        LinkedHashMap<String, Object> requestMeta = new LinkedHashMap<>();
        requestMeta.put("url", url);
        requestMeta.put("method", HttpMethod.GET.name());
        requestMeta.put("timeoutConnect", connectTimeoutMs);
        requestMeta.put("timeoutRead", readTimeoutMs);

        ProtocolTestRecord record = executeAndSave(config, ProtocolTestRecord.TestType.CONNECT.name(), ProtocolTestRecord.TestScenario.NETWORK.name(),
            url, HttpMethod.GET, null, requestMeta, connectTimeoutMs, readTimeoutMs);
        log.info("连通性测试结束: configId={}, recordId={}, resultStatus={}", configId, record.getId(), record.getResultStatus());
        return record;
    }

    @Override
    public ProtocolTestRecord testTransfer(ProtocolTestTransferDTO dto) {
        if (dto == null || dto.getConfigId() == null) {
            throw new IllegalArgumentException("协议配置ID不能为空");
        }
        ProtocolConfig config = getRequiredProtocolConfig(dto.getConfigId());
        String baseUrl = resolvePrimaryUrl(config);
        String fullUrl = buildTransferUrl(baseUrl, dto.getPath(), dto.getQueryParams());
        HttpMethod httpMethod = resolveHttpMethod(dto.getMethod());
        log.info("协议转发测试开始: configId={}, method={}, path={}",
            dto.getConfigId(), httpMethod.name(), Objects.toString(dto.getPath(), ""));
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

        ProtocolTestRecord record = executeAndSave(config, ProtocolTestRecord.TestType.TRANSFER.name(), ProtocolTestRecord.TestScenario.PROTOCOL.name(),
            fullUrl, httpMethod, entity, requestMeta, connectTimeoutMs, readTimeoutMs);
        log.info("协议转发测试结束: configId={}, recordId={}, resultStatus={}, responseCode={}",
            dto.getConfigId(), record.getId(), record.getResultStatus(), record.getResponseCode());
        return record;
    }

    @Override
    public ProtocolConnectivityResultDTO testConnectivity(ProtocolConnectivityTestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("诊断参数不能为空");
        }
        ConnectivityTarget target = resolveConnectivityTarget(dto);
        log.info("协议网络诊断开始: templateId={}, configId={}, protocolType={}, host={}, port={}, ping={}, telnet={}",
            target.templateId, target.configId, target.protocolType, target.host, target.port, dto.getPing(), dto.getTelnet());

        ProtocolConnectivityResultDTO result = new ProtocolConnectivityResultDTO();
        result.setTemplateId(target.templateId);
        result.setConfigId(target.configId);
        result.setProtocolType(target.protocolType);
        result.setTargetUrl(target.targetUrl);
        result.setHost(target.host);
        result.setPort(target.port);
        result.setTimeoutMs(target.timeoutMs);

        if (!Boolean.FALSE.equals(dto.getPing())) {
            result.setPing(doPing(target.host, target.timeoutMs));
        } else {
            result.setPing(skipped("已跳过ping"));
        }

        if (!Boolean.FALSE.equals(dto.getTelnet())) {
            result.setTelnet(doTelnet(target));
        } else {
            result.setTelnet(skipped("已跳过telnet"));
        }

        log.info("协议网络诊断结束: templateId={}, configId={}, host={}, port={}, pingSuccess={}, telnetSuccess={}",
            target.templateId, target.configId, target.host, target.port,
            result.getPing() == null ? null : result.getPing().getSuccess(),
            result.getTelnet() == null ? null : result.getTelnet().getSuccess());
        return result;
    }

    @Override
    public IPage<ProtocolTestRecord> getProtocolTestRecordList(ProtocolTestRecordQueryDTO dto) {
        ProtocolTestRecordQueryDTO query = dto == null ? new ProtocolTestRecordQueryDTO() : dto;
        log.info("分页查询协议测试记录: protocolId={}, configId={}, testType={}, testScenario={}, resultStatus={}, createTimeStart={}, createTimeEnd={}, current={}, size={}",
            query.getProtocolId(), query.getConfigId(), query.getTestType(), query.getTestScenario(), query.getResultStatus(),
            query.getCreateTimeStart(), query.getCreateTimeEnd(), query.getCurrent(), query.getSize());
        IPage<ProtocolTestRecord> page = this.page(query.toPage(), buildQueryWrapper(query));
        log.info("分页查询协议测试记录完成: total={}, currentSize={}", page.getTotal(), page.getRecords().size());
        return page;
    }

    @Override
    public void exportProtocolTestRecords(ProtocolTestRecordQueryDTO dto, HttpServletResponse response) throws IOException {
        ProtocolTestRecordQueryDTO query = dto == null ? new ProtocolTestRecordQueryDTO() : dto;
        log.info("导出协议测试记录开始: protocolId={}, configId={}, testType={}, testScenario={}, resultStatus={}, createTime范围=[{}, {}]",
            query.getProtocolId(), query.getConfigId(), query.getTestType(), query.getTestScenario(), query.getResultStatus(),
            query.getCreateTimeStart(), query.getCreateTimeEnd());
        List<ProtocolTestRecord> records = this.list(buildQueryWrapper(query));
        log.info("导出协议测试记录待写入行数: {}", records.size());
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("协议测试记录");
            XSSFCellStyle headerStyle = createHeaderStyle(workbook);
            XSSFCellStyle dataStyle = createDataStyle(workbook);

            String[] headers = {"ID", "协议ID", "配置ID", "测试类型", "测试场景", "结果状态", "响应码", "响应时长(ms)", "错误信息", "创建时间"};
            int[] widths = {10, 12, 12, 14, 14, 14, 12, 16, 48, 24};
            writeHeaderRow(sheet, headerStyle, headers, widths);
            writeDataRows(sheet, dataStyle, records);

            workbook.write(outputStream);
            String exportFileName = EXPORT_FILE_NAME + "_" + LocalDateTime.now().format(FILE_TIME_FORMATTER) + ".xlsx";
            writeExcelResponse(response, exportFileName, outputStream.toByteArray());
            log.info("导出协议测试记录完成: fileName={}, rows={}", exportFileName, records.size());
        }
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
        log.info("协议测试HTTP调用开始: configId={}, protocolId={}, testType={}, testScenario={}, method={}, url={}",
            config.getId(), config.getProtocolId(), testType, testScenario, method.name(), url);
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
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("协议测试HTTP调用成功: configId={}, httpStatus={}, responseTimeMs={}", config.getId(), response.getStatusCode().value(), elapsed);
            } else {
                log.warn("协议测试HTTP返回非2xx: configId={}, httpStatus={}, responseTimeMs={}", config.getId(), response.getStatusCode().value(), elapsed);
            }
        } catch (HttpStatusCodeException ex) {
            long elapsed = System.currentTimeMillis() - start;
            record.setResponseTime((int) elapsed);
            record.setResponseCode(String.valueOf(ex.getStatusCode().value()));
            record.setResultStatus(ProtocolTestRecord.ResultStatus.FAILED.name());
            record.setErrorMessage(truncate(ex.getStatusText()));
            record.setComparisonResult(buildResponseSummary(ex.getResponseBodyAsString(), ex.getStatusCode().value()));
            log.warn("协议测试HTTP客户端异常(仍落库): configId={}, url={}, httpStatus={}, responseTimeMs={}, message={}",
                config.getId(), url, ex.getStatusCode().value(), elapsed, truncate(ex.getMessage()));
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            record.setResponseTime((int) elapsed);
            record.setResponseCode("0");
            record.setResultStatus(ProtocolTestRecord.ResultStatus.FAILED.name());
            record.setErrorMessage(truncate(ex.getMessage()));
            log.error("协议测试HTTP调用失败: configId={}, method={}, url={}, responseTimeMs={}",
                config.getId(), method.name(), url, elapsed, ex);
        }

        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);
        log.info("协议测试记录已保存: recordId={}, configId={}, resultStatus={}, responseCode={}, responseTimeMs={}",
            record.getId(), config.getId(), record.getResultStatus(), record.getResponseCode(), record.getResponseTime());
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

    private LambdaQueryWrapper<ProtocolTestRecord> buildQueryWrapper(ProtocolTestRecordQueryDTO dto) {
        LambdaQueryWrapper<ProtocolTestRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getProtocolId() != null) {
            queryWrapper.eq(ProtocolTestRecord::getProtocolId, dto.getProtocolId());
        }
        if (dto.getConfigId() != null) {
            queryWrapper.eq(ProtocolTestRecord::getConfigId, dto.getConfigId());
        }
        if (StringUtils.isNotBlank(dto.getTestType())) {
            queryWrapper.eq(ProtocolTestRecord::getTestType, dto.getTestType().trim().toUpperCase());
        }
        if (StringUtils.isNotBlank(dto.getTestScenario())) {
            queryWrapper.eq(ProtocolTestRecord::getTestScenario, dto.getTestScenario().trim().toUpperCase());
        }
        if (StringUtils.isNotBlank(dto.getResultStatus())) {
            queryWrapper.eq(ProtocolTestRecord::getResultStatus, dto.getResultStatus().trim().toUpperCase());
        }
        applyDateTimeRange(queryWrapper, dto.getCreateTimeStart(), dto.getCreateTimeEnd(), ProtocolTestRecord::getCreateTime);
        queryWrapper.orderByDesc(ProtocolTestRecord::getCreateTime).orderByDesc(ProtocolTestRecord::getId);
        return queryWrapper;
    }

    private void applyDateTimeRange(LambdaQueryWrapper<ProtocolTestRecord> queryWrapper,
                                    LocalDateTime start,
                                    LocalDateTime end,
                                    SFunction<ProtocolTestRecord, ?> column) {
        Optional.ofNullable(start).ifPresent(value -> queryWrapper.ge(column, value));
        Optional.ofNullable(end).ifPresent(value -> queryWrapper.le(column, value));
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

    private InterfaceTemplateVO getRequiredTemplate(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        InterfaceTemplateVO templateDetail = templateService.getTemplateDetail(templateId);
        if (templateDetail == null) {
            log.warn("接口模板不存在: templateId={}", templateId);
            throw new IllegalArgumentException("接口模板不存在");
        }
        return templateDetail;
    }

    private ProtocolConfig getRequiredProtocolConfig(Long configId) {
        if (configId == null) {
            throw new IllegalArgumentException("协议配置ID不能为空");
        }
        ProtocolConfig config = protocolConfigService.getById(configId);
        if (config == null) {
            log.warn("协议配置不存在: configId={}", configId);
            throw new IllegalArgumentException("协议配置不存在");
        }
        return config;
    }


    private String resolvePrimaryUrl(ProtocolConfig config) {
        List<ProtocolConfigCreateDTO.UrlConfigItemDTO> urlConfigList = parseUrlConfigList(config.getUrlConfig());
        ProtocolConfigCreateDTO.UrlConfigItemDTO primaryUrlItem = pickPrimaryUrl(urlConfigList);
        return buildFinalUrl(primaryUrlItem);
    }

    private ConnectivityTarget resolveConnectivityTarget(ProtocolConnectivityTestDTO dto) {
        InterfaceTemplateVO template = null;
        String targetUrl = StringUtils.trimToNull(dto.getUrl());
        String protocolType = StringUtils.trimToNull(dto.getProtocolType());
        Integer timeoutMs = dto.getTimeoutMs();
        Long templateId = dto.getConfigId();

        // 优先使用模板ID
        if (templateId != null) {
            template = getRequiredTemplate(templateId);
            if (StringUtils.isBlank(targetUrl)) {
                targetUrl = resolveTemplateUrl(template);
            }
            if (StringUtils.isBlank(protocolType)) {
                protocolType = template.getProtocolType();
            }
            if (timeoutMs == null && template.getConnectTimeout() != null) {
                timeoutMs = template.getConnectTimeout();
            }
        }

        URI uri = parseTargetUri(targetUrl);
        if (StringUtils.isBlank(protocolType) && uri != null) {
            protocolType = uri.getScheme();
        }
        protocolType = StringUtils.defaultIfBlank(protocolType, "TCP").trim().toUpperCase();
        String templateHost = resolveTemplateHost(template, protocolType);
        Integer templatePort = resolveTemplatePort(template, protocolType);
        String host = StringUtils.trimToNull(dto.getHost());
        if (StringUtils.isBlank(host)) {
            host = templateHost;
        }
        if (StringUtils.isBlank(host) && uri != null) {
            host = uri.getHost();
        }
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("目标主机不能为空");
        }

        Integer port = dto.getPort();
        if (port == null) {
            port = templatePort;
        }
        if (port == null && uri != null && uri.getPort() > 0) {
            port = uri.getPort();
        }
        if (port == null) {
            port = defaultPort(protocolType);
        }

        ConnectivityTarget target = new ConnectivityTarget();
        target.templateId = templateId;
        target.protocolType = protocolType;
        target.targetUrl = targetUrl;
        target.host = host;
        target.port = port;
        target.timeoutMs = Math.max(timeoutMs == null ? 5000 : timeoutMs, 1);
        return target;
    }

    private String resolveTemplateUrl(InterfaceTemplateVO template) {
        if (template == null) {
            return null;
        }
        if (StringUtils.isNotBlank(template.getFullUrl())) {
            return template.getFullUrl().trim();
        }
        String baseUrl = StringUtils.trimToNull(template.getBaseUrl());
        String path = StringUtils.trimToNull(template.getPath());
        if (baseUrl != null && path != null) {
            String separator = baseUrl.endsWith("/") || path.startsWith("/") ? "" : "/";
            return baseUrl + separator + path;
        }
        return baseUrl != null ? baseUrl : path;
    }

    private String resolveTemplateHost(InterfaceTemplateVO template, String protocolType) {
        if (template == null) {
            return null;
        }
        if (isUdpProtocol(protocolType)) {
            return getJsonStringOption(template.getExtField5(), "udp-hostname", "udpPortHost", "hostname", "Hostname",
                "host", "Host", "目标地址");
        }
        return getJsonStringOption(template.getExtField5(), "tcp-servername", "serverNameOrIp", "服务器名称或IP",
            "hostname", "Hostname", "host", "Host", "目标地址");
    }

    private Integer resolveTemplatePort(InterfaceTemplateVO template, String protocolType) {
        if (template == null) {
            return null;
        }
        if (isUdpProtocol(protocolType)) {
            return getJsonIntegerOption(template.getExtField5(), "udp-port", "udpPort", "UDP Port", "port", "Port",
                "端口号", "目标端口");
        }
        return getJsonIntegerOption(template.getExtField5(), "tcp-port", "tcpPort", "TCP Port", "port", "Port",
            "端口号", "目标端口");
    }

    private String getJsonStringOption(String json, String... keys) {
        Object value = getJsonOption(json, keys);
        return value == null || StringUtils.isBlank(value.toString()) ? null : value.toString().trim();
    }

    private Integer getJsonIntegerOption(String json, String... keys) {
        Object value = getJsonOption(json, keys);
        if (value == null || StringUtils.isBlank(value.toString())) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private Object getJsonOption(String json, String... keys) {
        if (StringUtils.isBlank(json) || keys == null || keys.length == 0) {
            return null;
        }
        try {
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            for (String key : keys) {
                if (data.containsKey(key)) {
                    return data.get(key);
                }
            }
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                for (String key : keys) {
                    if (StringUtils.equalsIgnoreCase(entry.getKey(), key)) {
                        return entry.getValue();
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("解析模板网络诊断扩展参数失败: {}", ex.getMessage());
        }
        return null;
    }

    private URI parseTargetUri(String rawUrl) {
        if (StringUtils.isBlank(rawUrl)) {
            return null;
        }
        String normalized = rawUrl.trim();
        if (!normalized.contains("://")) {
            normalized = "tcp://" + normalized;
        }
        try {
            return new URI(normalized);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("目标URL格式不正确");
        }
    }

    private ProtocolConnectivityResultDTO.CheckResult doPing(String host, int timeoutMs) {
        long start = System.currentTimeMillis();
        ProtocolConnectivityResultDTO.CheckResult result = new ProtocolConnectivityResultDTO.CheckResult();
        result.setExecuted(true);
        try {
            boolean reachable = InetAddress.getByName(host).isReachable(timeoutMs);
            long elapsed = System.currentTimeMillis() - start;
            result.setSuccess(reachable);
            result.setResponseTimeMs(elapsed);
            result.setMessage(reachable ? "ping可达" : "ping不可达或超时");
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            result.setSuccess(false);
            result.setResponseTimeMs(elapsed);
            result.setMessage(truncate(ex.getMessage()));
        }
        return result;
    }

    private ProtocolConnectivityResultDTO.CheckResult doTelnet(ConnectivityTarget target) {
        if (isUdpProtocol(target.protocolType)) {
            return skipped("UDP协议无TCP连接语义，已跳过telnet");
        }
        if (target.port == null) {
            return failed("telnet需要目标端口");
        }
        long start = System.currentTimeMillis();
        ProtocolConnectivityResultDTO.CheckResult result = new ProtocolConnectivityResultDTO.CheckResult();
        result.setExecuted(true);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(target.host, target.port), target.timeoutMs);
            long elapsed = System.currentTimeMillis() - start;
            result.setSuccess(true);
            result.setResponseTimeMs(elapsed);
            result.setMessage("telnet连接成功");
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            result.setSuccess(false);
            result.setResponseTimeMs(elapsed);
            result.setMessage(truncate(ex.getMessage()));
        }
        return result;
    }

    private ProtocolConnectivityResultDTO.CheckResult skipped(String message) {
        ProtocolConnectivityResultDTO.CheckResult result = new ProtocolConnectivityResultDTO.CheckResult();
        result.setExecuted(false);
        result.setSuccess(null);
        result.setResponseTimeMs(0L);
        result.setMessage(message);
        return result;
    }

    private ProtocolConnectivityResultDTO.CheckResult failed(String message) {
        ProtocolConnectivityResultDTO.CheckResult result = new ProtocolConnectivityResultDTO.CheckResult();
        result.setExecuted(true);
        result.setSuccess(false);
        result.setResponseTimeMs(0L);
        result.setMessage(message);
        return result;
    }

    private boolean isUdpProtocol(String protocolType) {
        return StringUtils.equalsIgnoreCase(protocolType, "UDP");
    }

    private Integer defaultPort(String protocolType) {
        String protocol = StringUtils.defaultString(protocolType).trim().toUpperCase();
        switch (protocol) {
            case "HTTP":
                return 80;
            case "HTTPS":
                return 443;
            case "FTP":
                return 21;
            case "SFTP":
            case "SSH":
                return 22;
            case "TELNET":
                return 23;
            case "SMTP":
                return 25;
            case "POP3":
                return 110;
            case "IMAP":
                return 143;
            case "MYSQL":
                return 3306;
            case "REDIS":
                return 6379;
            case "MQTT":
                return 1883;
            default:
                return null;
        }
    }

    private static class ConnectivityTarget {
        private Long configId;
        private Long templateId;
        private String protocolType;
        private String targetUrl;
        private String host;
        private Integer port;
        private Integer timeoutMs;
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

    private void writeHeaderRow(XSSFSheet sheet, XSSFCellStyle headerStyle, String[] headers, int[] columnWidths) {
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, columnWidths[i] * 256);
        }
    }

    private void writeDataRows(XSSFSheet sheet, XSSFCellStyle dataStyle, List<ProtocolTestRecord> records) {
        int rowIndex = 1;
        for (ProtocolTestRecord record : records) {
            XSSFRow row = sheet.createRow(rowIndex++);
            setCellValue(row, 0, String.valueOf(record.getId()), dataStyle);
            setCellValue(row, 1, String.valueOf(record.getProtocolId()), dataStyle);
            setCellValue(row, 2, String.valueOf(record.getConfigId()), dataStyle);
            setCellValue(row, 3, record.getTestType(), dataStyle);
            setCellValue(row, 4, record.getTestScenario(), dataStyle);
            setCellValue(row, 5, record.getResultStatus(), dataStyle);
            setCellValue(row, 6, record.getResponseCode(), dataStyle);
            setCellValue(row, 7, record.getResponseTime() == null ? "" : String.valueOf(record.getResponseTime()), dataStyle);
            setCellValue(row, 8, record.getErrorMessage(), dataStyle);
            setCellValue(row, 9, record.getCreateTime() == null ? "" : record.getCreateTime().toString(), dataStyle);
        }
    }

    private XSSFCellStyle createHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        return headerStyle;
    }

    private XSSFCellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return dataStyle;
    }

    private void setCellValue(XSSFRow row, int columnIndex, String value, XSSFCellStyle style) {
        XSSFCell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void writeExcelResponse(HttpServletResponse response, String fileName, byte[] content) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-Disposition", "attachment; filename="
            + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(content);
            outputStream.flush();
        }
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
