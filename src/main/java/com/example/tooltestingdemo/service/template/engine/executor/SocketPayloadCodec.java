package com.example.tooltestingdemo.service.template.engine.executor;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Locale;

/**
 * TCP/UDP payload 编解码策略入口。
 * <ul>
 *   <li>TEXT：请求数据就是普通文本，按模板 charset 编码成字节；响应字节也按 charset 转回文本。</li>
 *   <li>HEX：请求数据是十六进制字符串，例如 7B226D7367223A226869227D，发送前转成真实字节。</li>
 *   <li>BASE64：请求数据是 Base64 字符串，发送前解码成真实字节。</li>
 * </ul>
 */
final class SocketPayloadCodec {

    private SocketPayloadCodec() {
    }

    static Codec resolve(String configuredName) {
        if (configuredName == null) {
            return Codec.TEXT;
        }
        String normalized = configuredName.trim().toUpperCase(Locale.ROOT);
        // 兼容前端传入的 class name，而不是强制要求只传 TEXT/HEX/BASE64。
        // 例如 TCPBinaryClientImpl 会被识别为 HEX，因为它表达的是二进制字节输入。
        if (normalized.contains("HEX") || normalized.contains("BINARY")) {
            return Codec.HEX;
        }
        if (normalized.contains("BASE64")) {
            return Codec.BASE64;
        }
        return Codec.TEXT;
    }

    static byte[] encode(String payload, Charset charset, Codec codec) {
        if (payload == null) {
            return new byte[0];
        }
        // encode 是“界面/模板里配置的字符串”到“网络层真实字节”的转换。
        // TCP/UDP 执行器统一调用这里，避免两个执行器各自处理 HEX 的边界条件。
        return switch (codec) {
            case HEX -> decodeHex(payload);
            case BASE64 -> Base64.getDecoder().decode(payload);
            case TEXT -> payload.getBytes(charset);
        };
    }

    static String decode(byte[] payload, Charset charset, Codec codec) {
        if (payload == null) {
            return null;
        }
        // decode 是“网络层收到的真实字节”到“执行结果展示字符串”的转换。
        // HEX/BASE64 响应用编码文本展示，方便用户复制后再次作为 Request Data 使用。
        return switch (codec) {
            case HEX -> encodeHex(payload);
            case BASE64 -> Base64.getEncoder().encodeToString(payload);
            case TEXT -> new String(payload, charset);
        };
    }

    static byte[] decodeHex(String text) {
        String normalized = text == null ? "" : text.replaceAll("\\s+", "");
        if (normalized.length() % 2 != 0) {
            throw new IllegalArgumentException("HEX数据长度必须为偶数");
        }
        // 每两个十六进制字符表示一个字节；Integer.parseInt(..., 16) 负责校验非法字符。
        byte[] bytes = new byte[normalized.length() / 2];
        for (int i = 0; i < normalized.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(normalized.substring(i, i + 2), 16);
        }
        return bytes;
    }

    static String encodeHex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    enum Codec {
        TEXT,
        HEX,
        BASE64
    }
}
