import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class UdpJsonEchoServer {
    private static final int BUFFER_SIZE = 8192;

    public static void main(String[] args) throws Exception {
        String host = "0.0.0.0";
        int port = 9001;
        Charset charset = StandardCharsets.UTF_8;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--encoding" -> charset = Charset.forName(args[++i]);
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        InetAddress bindAddress = InetAddress.getByName(host);
        try (DatagramSocket socket = new DatagramSocket(port, bindAddress)) {
            System.out.printf("UDP JSON echo server listening on %s:%d%n", host, port);

            while (true) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String text = new String(packet.getData(), packet.getOffset(), packet.getLength(), charset);
                String remote = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                System.out.printf("[%s] received from %s: %s%n", Instant.now(), remote, text);

                String response = looksLikeJson(text)
                    ? "{\"code\":200,\"message\":\"ok\",\"echo\":" + text + "}"
                    : "{\"code\":400,\"message\":\"invalid json\",\"raw\":\"" + escapeJson(text) + "\"}";
                byte[] responseBytes = response.getBytes(charset);
                DatagramPacket responsePacket = new DatagramPacket(
                    responseBytes,
                    responseBytes.length,
                    packet.getAddress(),
                    packet.getPort()
                );
                socket.send(responsePacket);
            }
        }
    }

    private static boolean looksLikeJson(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
            || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private static String escapeJson(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> result.append(c);
            }
        }
        return result.toString();
    }
}
