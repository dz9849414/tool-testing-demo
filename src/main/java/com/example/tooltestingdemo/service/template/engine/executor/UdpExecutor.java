package com.example.tooltestingdemo.service.template.engine.executor;

import com.example.tooltestingdemo.service.template.engine.core.ExecutionResult;
import com.example.tooltestingdemo.service.template.engine.core.TemplateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class UdpExecutor implements TemplateExecutor {

    private static final int RESPONSE_BUFFER_SIZE = 8192;

    @Override
    public String getType() {
        return "UDP";
    }

    @Override
    public ExecutionResult execute(TemplateContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        SocketTemplateSupport.SocketRequest request = SocketTemplateSupport.buildRequest(context, getType());
        long start = System.currentTimeMillis();

        try (DatagramSocket socket = createSocket(context)) {
            // UDP 没有连接建立过程；这里的 readTimeout 只影响 socket.receive 等响应的最长等待时间。
            socket.setSoTimeout(request.readTimeout());
            // 按 Data Encode/Decode class 把 Request Data 转成实际 UDP 字节。
            SocketPayloadCodec.Codec codec = resolveCodec(context);
            byte[] payload = SocketPayloadCodec.encode(request.payload(), request.charset(), codec);
            DatagramPacket packet = new DatagramPacket(payload, payload.length,
                InetAddress.getByName(request.host()), request.port());
            socket.send(packet);

            long duration = System.currentTimeMillis() - start;
            // Wait for Response=false 时只发包不阻塞等待，适合单向 UDP 通知类接口。
            boolean waitForResponse = SocketTemplateSupport.getBooleanOption(context, true,
                "waitForResponse", "Wait for Response");
            String response = null;
            if (waitForResponse) {
                // UDP 响应来自服务端主动回包；如果服务端不回包，会在 responseTimeout 后抛超时异常。
                byte[] responseBytes = new byte[RESPONSE_BUFFER_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                socket.receive(responsePacket);
                byte[] responsePayload = java.util.Arrays.copyOfRange(responsePacket.getData(),
                    responsePacket.getOffset(), responsePacket.getOffset() + responsePacket.getLength());
                response = SocketPayloadCodec.decode(responsePayload, request.charset(), codec);
                duration = System.currentTimeMillis() - start;
            }

            return ExecutionResult.builder()
                .success(true)
                .statusCode("200")
                .message(waitForResponse ? "执行成功" : "发送成功")
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(startTime)
                .request(buildRequestInfo(request, context))
                .response(waitForResponse ? SocketTemplateSupport.buildResponseInfo(response, duration) : null)
                .variables(context.getAllVariables())
                .build();
        } catch (Exception e) {
            log.warn("UDP模板执行失败: host={}, port={}", request.host(), request.port(), e);
            return ExecutionResult.builder()
                .success(false)
                .statusCode("ERROR")
                .message("执行失败: " + e.getMessage())
                .templateId(context.getTemplate() != null ? context.getTemplate().getId() : null)
                .templateName(context.getTemplate() != null ? context.getTemplate().getName() : null)
                .startTime(startTime)
                .request(buildRequestInfo(request, context))
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
        return new PreviewResult(request.url(), getType(), null, request.payload(), buildParameters(request, context));
    }

    private DatagramSocket createSocket(TemplateContext context) throws Exception {
        String bindAddress = SocketTemplateSupport.getStringOption(context, null,
            "bindLocalAddress", "Bind Local Address", "localAddress", "Local Address");
        int bindPort = SocketTemplateSupport.getIntOption(context, 0,
            "bindLocalPort", "Bind Local Port", "localPort", "Local Port");
        if (bindAddress == null && bindPort <= 0) {
            // 不指定本地绑定时由系统自动选择源 IP 和源端口。
            return new DatagramSocket();
        }
        // Bind Local Address/Port 用于指定本机发包网卡和源端口；端口 0 表示系统自动分配。
        InetAddress address = bindAddress == null ? InetAddress.getByName("0.0.0.0") : InetAddress.getByName(bindAddress);
        SocketAddress socketAddress = new InetSocketAddress(address, Math.max(bindPort, 0));
        return new DatagramSocket(socketAddress);
    }

    private ExecutionResult.RequestInfo buildRequestInfo(SocketTemplateSupport.SocketRequest request, TemplateContext context) {
        // UDP 没有 HTTP headers，这里用 headers 携带 charset，parameters 展示协议级运行配置。
        return ExecutionResult.RequestInfo.builder()
            .url(request.url())
            .method(getType())
            .headers(Map.of("Charset", request.charset().name()))
            .body(request.payload())
            .parameters(buildParameters(request, context))
            .build();
    }

    private Map<String, String> buildParameters(SocketTemplateSupport.SocketRequest request, TemplateContext context) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("host", request.host());
        parameters.put("udpPort", String.valueOf(request.port()));
        parameters.put("waitForResponse", String.valueOf(SocketTemplateSupport.getBooleanOption(context, true,
            "waitForResponse", "Wait for Response")));
        // 当前执行器每次请求都会释放 DatagramSocket；该字段保留给前端展示和后续复用 socket 扩展。
        parameters.put("closeUdpSocket", String.valueOf(SocketTemplateSupport.getBooleanOption(context, true,
            "closeUdpSocket", "Close UDP Socket")));
        parameters.put("responseTimeout", String.valueOf(request.readTimeout()));
        parameters.put("dataEncodeDecodeClass", resolveCodec(context).name());
        String bindAddress = SocketTemplateSupport.getStringOption(context, "", "bindLocalAddress", "Bind Local Address");
        int bindPort = SocketTemplateSupport.getIntOption(context, 0, "bindLocalPort", "Bind Local Port");
        parameters.put("bindLocalAddress", bindAddress);
        parameters.put("bindLocalPort", String.valueOf(bindPort));
        return parameters;
    }

    private SocketPayloadCodec.Codec resolveCodec(TemplateContext context) {
        // UDP 的 Data Encode/Decode class 直接决定 Request Data 与网络字节之间的转换策略。
        return SocketTemplateSupport.getCodecOption(context, "UTF-8",
            "dataEncodeDecodeClass", "Data Encode/Decode class", "codec", "encoding");
    }
}
