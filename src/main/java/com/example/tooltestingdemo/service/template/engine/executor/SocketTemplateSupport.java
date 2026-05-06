package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.constants.TemplateConstants;
import com.example.tooltestingdemo.entity.template.InterfaceTemplate;
import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SocketTemplateSupport {

    /**
     * 轻量读取 extField5 中的一层 JSON 字段。
     * 当前只用于协议执行参数，不引入额外 JSON 依赖，避免影响执行器初始化。
     */
    private static final Pattern SIMPLE_JSON_FIELD =
        Pattern.compile("\"%s\"\\s*:\\s*(\"([^\"]*)\"|true|false|-?\\d+)", Pattern.CASE_INSENSITIVE);

    private SocketTemplateSupport() {
    }

    static SocketRequest buildRequest(TemplateContext context, String protocol) {
        InterfaceTemplate template = context.getTemplate();
        Map<String, Object> variables = context.getAllVariables();
        // URL 先从 variables/fullUrl/baseUrl/path 中解析出来，再统一执行变量替换。
        // 这样既兼容老模板字段，也支持调用时临时覆盖 url/requestUrl。
        String url = replaceVariables(resolveUrl(template, variables), variables);
        URI uri = parseUri(url, protocol);
        String payload = replaceVariables(resolvePayload(template, variables), variables);
        Charset charset = resolveCharset(template);
        // TCP/UDP 的运行参数允许调用方通过 variables 覆盖模板默认值，便于调试单次请求。
        int connectTimeout = getIntOption(context,
            resolveTimeout(template != null ? template.getConnectTimeout() : null,
                TemplateConstants.DEFAULT_CONNECT_TIMEOUT),
            "connectTimeout", "Connect Timeout", "连接超时");
        int readTimeout = getIntOption(context,
            resolveTimeout(template != null ? template.getReadTimeout() : null,
                TemplateConstants.DEFAULT_READ_TIMEOUT),
            "responseTimeout", "Response Timeout", "响应超时", "readTimeout", "Read Timeout");
        String host = getStringOption(context, uri.getHost(),
            "hostname", "Hostname", "hostName", "HostName", "host", "Host", "ip", "IP",
            "targetAddress", "Target Address", "remoteAddress", "Remote Address",
            "serverNameOrIp", "Server Name or IP", "服务器名称或IP");
        // UDP Port/TCP Port 可单独传入，这样前端不必拼接 udp://host:port。
        int port = getIntOption(context, uri.getPort(),
            "udpPort", "UDP Port", "tcpPort", "TCP Port", "port", "Port",
            "targetPort", "Target Port", "remotePort", "Remote Port", "端口号");

        return new SocketRequest(url, host, port, payload, charset, connectTimeout, readTimeout);
    }

    static TemplateExecutor.ValidationResult validate(TemplateContext context, String protocol) {
        if (context.getTemplate() == null) {
            return TemplateExecutor.ValidationResult.failure("模板信息不能为空");
        }
        try {
            SocketRequest request = buildRequest(context, protocol);
            if (!StringUtils.hasText(request.host())) {
                return TemplateExecutor.ValidationResult.failure(protocol + "目标主机不能为空");
            }
            if (request.port() <= 0 || request.port() > 65535) {
                return TemplateExecutor.ValidationResult.failure(protocol + "目标端口不正确");
            }
            return TemplateExecutor.ValidationResult.success();
        } catch (Exception e) {
            return TemplateExecutor.ValidationResult.failure(e.getMessage());
        }
    }

    static ExecutionResult.RequestInfo buildRequestInfo(SocketRequest request, String protocol) {
        return ExecutionResult.RequestInfo.builder()
            .url(request.url())
            .method(protocol)
            .headers(Map.of("Charset", request.charset().name()))
            .body(request.payload())
            .parameters(Map.of(
                "host", request.host(),
                "port", String.valueOf(request.port())
            ))
            .build();
    }

    static ExecutionResult.ResponseInfo buildResponseInfo(String response, long responseTime) {
        return ExecutionResult.ResponseInfo.builder()
            .statusCode(200)
            .statusText("OK")
            .headers(Map.of())
            .body(response)
            .size(response != null ? (long) response.getBytes(StandardCharsets.UTF_8).length : 0L)
            .responseTime(responseTime)
            .build();
    }

    static boolean getBooleanOption(TemplateContext context, boolean defaultValue, String... keys) {
        Object value = getOptionValue(context, keys);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = value.toString().trim();
        if ("1".equals(text) || "true".equalsIgnoreCase(text) || "yes".equalsIgnoreCase(text)) {
            return true;
        }
        if ("0".equals(text) || "false".equalsIgnoreCase(text) || "no".equalsIgnoreCase(text)) {
            return false;
        }
        return defaultValue;
    }

    static int getIntOption(TemplateContext context, int defaultValue, String... keys) {
        Object value = getOptionValue(context, keys);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static String getStringOption(TemplateContext context, String defaultValue, String... keys) {
        Object value = getOptionValue(context, keys);
        return value == null || !StringUtils.hasText(value.toString()) ? defaultValue : value.toString().trim();
    }

    static SocketPayloadCodec.Codec getCodecOption(TemplateContext context, String defaultValue, String... keys) {
        // 编解码配置本质也是普通协议配置项，复用 getStringOption 的变量/extField5 优先级。
        return SocketPayloadCodec.resolve(getStringOption(context, defaultValue, keys));
    }

    private static String resolveUrl(InterfaceTemplate template, Map<String, Object> variables) {
        // 完整 URL 优先级最高，便于前端直接传 tcp://host:port 或 udp://host:port。
        for (String key : List.of("fullUrl", "url", "requestUrl")) {
            Object value = variables.get(key);
            if (value instanceof String text && StringUtils.hasText(text)) {
                return text.trim();
            }
        }
        if (template != null && StringUtils.hasText(template.getFullUrl())) {
            return template.getFullUrl().trim();
        }
        String baseUrl = template != null ? template.getBaseUrl() : null;
        String path = template != null ? template.getPath() : null;
        if (StringUtils.hasText(baseUrl) && StringUtils.hasText(path)) {
            String separator = baseUrl.endsWith("/") || path.startsWith("/") ? "" : "/";
            return baseUrl.trim() + separator + path.trim();
        }
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl.trim();
        }
        return path;
    }

    private static URI parseUri(String url, String protocol) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException(protocol + "目标地址不能为空");
        }
        String candidate = url.trim();
        // 用户只填 host:port 时自动补协议头，避免前端必须拼完整 URL。
        if (!candidate.toLowerCase(Locale.ROOT).startsWith(protocol.toLowerCase(Locale.ROOT) + "://")) {
            candidate = protocol.toLowerCase(Locale.ROOT) + "://" + candidate;
        }
        try {
            return URI.create(candidate);
        } catch (Exception e) {
            throw new IllegalArgumentException(protocol + "目标地址格式不正确: " + url);
        }
    }

    private static String resolvePayload(InterfaceTemplate template, Map<String, Object> variables) {
        // Request Data 是 UDP/TCP 原始报文入口；body/bodyContent 是兼容现有模板字段。
        for (String key : List.of("textToSend", "要发送的文本", "requestData", "Request Data",
            "body", "bodyContent", "requestBody", "payload", "message", "data")) {
            Object value = variables.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return template != null ? template.getBodyContent() : null;
    }

    private static Charset resolveCharset(InterfaceTemplate template) {
        String charset = template != null ? template.getCharset() : null;
        if (!StringUtils.hasText(charset)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(charset.trim());
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private static int resolveTimeout(Integer value, int defaultValue) {
        return value != null && value > 0 ? value : defaultValue;
    }

    private static String replaceVariables(String content, Map<String, Object> variables) {
        if (!StringUtils.hasText(content) || variables == null || variables.isEmpty()) {
            return content;
        }
        String result = content;
        // 同时兼容 ${name} 和 $name 两种历史占位符写法。
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace("${" + entry.getKey() + "}", value);
            result = result.replace("$" + entry.getKey(), value);
        }
        return result;
    }

    private static Object getOptionValue(TemplateContext context, String... keys) {
        if (context == null || keys == null) {
            return null;
        }
        // 优先使用本次执行传入的变量，适合临时覆盖目标地址、端口、超时等参数。
        Map<String, Object> variables = context.getAllVariables();
        for (String key : keys) {
            if (variables != null && variables.containsKey(key)) {
                return variables.get(key);
            }
        }
        // 其次读取模板扩展字段 extField5，作为协议高级配置的持久化位置。
        String extField5 = context.getTemplate() != null ? context.getTemplate().getExtField5() : null;
        if (!StringUtils.hasText(extField5)) {
            return null;
        }
        for (String key : keys) {
            Object value = readSimpleJsonField(extField5, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object readSimpleJsonField(String json, String key) {
        // extField5 只按一层 key/value 配置读取，不支持嵌套对象。
        // 这里用正则是为了避免给执行器增加 JSON 解析依赖路径；复杂结构后续应升级为明确 DTO。
        Matcher matcher = Pattern.compile(String.format(SIMPLE_JSON_FIELD.pattern(), Pattern.quote(key)),
            Pattern.CASE_INSENSITIVE).matcher(json);
        if (!matcher.find()) {
            return null;
        }
        String raw = matcher.group(1);
        String quoted = matcher.group(2);
        if (quoted != null) {
            return quoted;
        }
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return Boolean.parseBoolean(raw);
        }
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return raw;
        }
    }

    record SocketRequest(String url, String host, int port, String payload, Charset charset,
                         int connectTimeout, int readTimeout) {
    }
}
