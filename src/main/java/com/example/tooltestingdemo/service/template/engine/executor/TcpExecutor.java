package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class TcpExecutor implements TemplateExecutor {

    @Override
    public String getType() {
        return "TCP";
    }

    @Override
    public ExecutionResult execute(TemplateContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        SocketTemplateSupport.SocketRequest request = SocketTemplateSupport.buildRequest(context, getType());
        long start = System.currentTimeMillis();

        try (Socket socket = new Socket()) {
            // connectTimeout 只控制建立 TCP 连接的等待时间；连接建立后读取响应由 SO_TIMEOUT 控制。
            socket.connect(new InetSocketAddress(request.host(), request.port()), request.connectTimeout());
            socket.setSoTimeout(request.readTimeout());
            // TCP_NODELAY=true 会关闭 Nagle 算法，小包会立即发送，适合请求/响应式协议调试。
            socket.setTcpNoDelay(SocketTemplateSupport.getBooleanOption(context, false,
                "tcpNoDelay", "setNoDelay", "设置无延迟"));
            applySoLinger(socket, context);

            if (StringUtils.hasText(request.payload())) {
                // TCP 是流协议；JSON 报文默认按约定加 4 字节大端长度头，接收方据此拆包。
                socket.getOutputStream().write(buildPayload(request, context));
                socket.getOutputStream().flush();
            }

            // 响应侧同样优先按 4 字节长度头解析，便于和测试服务/真实 TCP 服务互通。
            String response = readResponse(socket, request.charset(), shouldUseLengthHeader(context),
                resolveEolByte(context), resolveCodec(context));
            long duration = System.currentTimeMillis() - start;
            return ExecutionResult.builder()
                .success(true)
                .statusCode("200")
                .message("执行成功")
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(startTime)
                .request(SocketTemplateSupport.buildRequestInfo(request, getType()))
                .response(SocketTemplateSupport.buildResponseInfo(response, duration))
                .variables(context.getAllVariables())
                .build();
        } catch (Exception e) {
            log.warn("TCP模板执行失败: host={}, port={}", request.host(), request.port(), e);
            return ExecutionResult.builder()
                .success(false)
                .statusCode("ERROR")
                .message("执行失败: " + e.getMessage())
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(startTime)
                .request(SocketTemplateSupport.buildRequestInfo(request, getType()))
                .variables(context.getAllVariables())
                .build();
        }
    }

    @Override
    public ValidationResult validate(TemplateContext context) {
        return SocketTemplateSupport.validate(context, getType());
    }

    @Override
    public PreviewResult preview(TemplateContext context) {
        SocketTemplateSupport.SocketRequest request = SocketTemplateSupport.buildRequest(context, getType());
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("host", request.host());
        parameters.put("port", String.valueOf(request.port()));
        parameters.put("lengthHeader", shouldUseLengthHeader(context) ? "4" : "0");
        parameters.put("tcpClientClassname", resolveClientClassname(context));
        parameters.put("codec", resolveCodec(context).name());
        parameters.put("connectTimeout", String.valueOf(request.connectTimeout()));
        parameters.put("responseTimeout", String.valueOf(request.readTimeout()));
        parameters.put("reuseConnection", String.valueOf(SocketTemplateSupport.getBooleanOption(context, false,
            "reuseConnection", "Re-use connection")));
        parameters.put("closeConnection", String.valueOf(SocketTemplateSupport.getBooleanOption(context, true,
            "closeConnection", "关闭连接")));
        parameters.put("tcpNoDelay", String.valueOf(SocketTemplateSupport.getBooleanOption(context, false,
            "tcpNoDelay", "setNoDelay", "设置无延迟")));
        parameters.put("soLinger", SocketTemplateSupport.getStringOption(context, "",
            "soLinger", "SO_LINGER"));
        parameters.put("eolByteValue", String.valueOf(resolveEolByte(context)));
        return new PreviewResult(request.url(), getType(), null, request.payload(),
            parameters);
    }

    private byte[] buildPayload(SocketTemplateSupport.SocketRequest request, TemplateContext context) {
        // 发送前先按 TCPClient classname / codec 把“要发送的文本”转换成真实字节。
        // 文本模式保持原字符串；HEX/BASE64 模式会先解码成二进制。
        byte[] payload = SocketPayloadCodec.encode(request.payload(), request.charset(), resolveCodec(context));
        if (!shouldUseLengthHeader(context)) {
            return payload;
        }
        // ByteBuffer 默认使用大端序，等价于网络字节序。
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + payload.length);
        buffer.putInt(payload.length);
        buffer.put(payload);
        return buffer.array();
    }

    private boolean shouldUseLengthHeader(TemplateContext context) {
        if (context.getTemplate() == null) {
            return false;
        }
        String rawType = context.getTemplate().getBodyRawType();
        String contentType = context.getTemplate().getContentType();
        Boolean configured = resolveLengthHeaderOption(context);
        if (configured != null) {
            return configured;
        }
        // 兼容现有模板字段：选择 JSON 时默认启用 4 字节长度头。
        return isTextClient(context) && ("JSON".equalsIgnoreCase(rawType)
            || (contentType != null && contentType.toLowerCase(Locale.ROOT).contains("json")));
    }

    private Boolean resolveLengthHeaderOption(TemplateContext context) {
        String value = SocketTemplateSupport.getStringOption(context, null,
            "lengthHeader", "Length Header", "useLengthHeader", "Use Length Header", "4字节长度头");
        if (!StringUtils.hasText(value)) {
            return null;
        }
        // 允许前端传 4/true/yes/1 开启；传 false/0 时这里返回 false，覆盖 JSON 自动开启逻辑。
        return "4".equals(value.trim())
            || "true".equalsIgnoreCase(value.trim())
            || "yes".equalsIgnoreCase(value.trim())
            || "1".equals(value.trim());
    }

    private String readResponse(Socket socket, java.nio.charset.Charset charset, boolean useLengthHeader,
                                int eolByte, SocketPayloadCodec.Codec codec) throws Exception {
        InputStream input = socket.getInputStream();
        if (useLengthHeader) {
            // 使用长度头时，响应必须是 4 字节大端长度 + body；读取完成后不会继续等连接关闭。
            String framedResponse = readLengthHeaderResponse(input, charset, codec);
            if (framedResponse != null) {
                return framedResponse;
            }
        }

        if (eolByte >= 0) {
            // 未启用长度头时，EOL 字节可作为响应结束标识，常见值是 10(\\n) 或 13(\\r)。
            return readUntilEol(input, charset, eolByte, codec);
        }

        // 最后兜底：读取当前可用数据。该模式适合服务端回包后不立即关闭连接但数据已到达的场景。
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read = input.read(chunk);
        while (read > -1) {
            buffer.write(chunk, 0, read);
            if (input.available() <= 0) {
                break;
            }
            read = input.read(chunk);
        }
        return SocketPayloadCodec.decode(buffer.toByteArray(), charset, codec);
    }

    private String readLengthHeaderResponse(InputStream input, java.nio.charset.Charset charset,
                                            SocketPayloadCodec.Codec codec) throws Exception {
        byte[] header = input.readNBytes(Integer.BYTES);
        if (header.length < Integer.BYTES) {
            return header.length == 0 ? null : SocketPayloadCodec.decode(header, charset, codec);
        }

        int length = ByteBuffer.wrap(header).getInt();
        if (length < 0 || length > 10 * 1024 * 1024) {
            // 如果前 4 字节不像合法长度，退回普通文本响应，避免误吞非分帧服务的响应。
            ByteArrayOutputStream fallback = new ByteArrayOutputStream();
            fallback.write(header);
            while (input.available() > 0) {
                fallback.write(input.read());
            }
            return SocketPayloadCodec.decode(fallback.toByteArray(), charset, codec);
        }

        byte[] body = input.readNBytes(length);
        return SocketPayloadCodec.decode(body, charset, codec);
    }

    private String readUntilEol(InputStream input, Charset charset, int eolByte,
                                SocketPayloadCodec.Codec codec) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        while ((read = input.read()) > -1) {
            if (read == eolByte) {
                break;
            }
            buffer.write(read);
        }
        return SocketPayloadCodec.decode(buffer.toByteArray(), charset, codec);
    }

    private int resolveEolByte(TemplateContext context) {
        String configured = SocketTemplateSupport.getStringOption(context, "",
            "eolByteValue", "EOL byte value", "EOL字节值");
        if (!StringUtils.hasText(configured)) {
            return -1;
        }
        try {
            String value = configured.trim();
            // 支持 0x0A 这类十六进制写法，也支持 10 这类十进制写法。
            if (value.startsWith("0x") || value.startsWith("0X")) {
                return Integer.parseInt(value.substring(2), 16) & 0xFF;
            }
            return Integer.parseInt(value) & 0xFF;
        } catch (Exception e) {
            return -1;
        }
    }

    private void applySoLinger(Socket socket, TemplateContext context) throws Exception {
        String configured = SocketTemplateSupport.getStringOption(context, "",
            "soLinger", "SO_LINGER");
        if (!StringUtils.hasText(configured)) {
            return;
        }
        // SO_LINGER 留空时不干预系统默认关闭策略；非空时按秒设置 linger。
        // seconds < 0 会关闭 linger；seconds >= 0 会等待指定秒数发送剩余数据。
        int seconds = Integer.parseInt(configured.trim());
        socket.setSoLinger(seconds >= 0, Math.max(seconds, 0));
    }

    private boolean isTextClient(TemplateContext context) {
        // 只有文本模式默认根据 JSON 自动加长度头；二进制 HEX/BASE64 不做 JSON 推断。
        return SocketPayloadCodec.Codec.TEXT.equals(resolveCodec(context));
    }

    private String resolveClientClassname(TemplateContext context) {
        return SocketTemplateSupport.getStringOption(context, "TCPClientImpl",
            "tcpClientClassname", "TCPClient classname", "clientClassname", "clientClass", "tcpClientClass");
    }

    private SocketPayloadCodec.Codec resolveCodec(TemplateContext context) {
        // 显式 Data Encode/Decode class 优先；否则从 TCPClient classname 推断 TEXT/HEX/BASE64。
        String configuredCodec = SocketTemplateSupport.getStringOption(context, null,
            "dataEncodeDecodeClass", "Data Encode/Decode class", "codec", "encoding");
        if (StringUtils.hasText(configuredCodec)) {
            return SocketPayloadCodec.resolve(configuredCodec);
        }
        return SocketPayloadCodec.resolve(resolveClientClassname(context));
    }
}
