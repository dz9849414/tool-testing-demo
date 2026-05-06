import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpLengthHeaderServer {
    private static final int MAX_FRAME_SIZE = 10 * 1024 * 1024;

    public static void main(String[] args) throws Exception {
        String host = "192.168.0.215";
        int port = 9000;
        Charset charset = StandardCharsets.UTF_8;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--encoding" -> charset = Charset.forName(args[++i]);
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }

        ExecutorService pool = Executors.newCachedThreadPool();
        Charset serverCharset = charset;
        try (ServerSocket server = new ServerSocket(port, 50, java.net.InetAddress.getByName(host))) {
            System.out.printf("TCP length-header JSON test server listening on %s:%d%n", host, port);
            System.out.println("Protocol: 4-byte big-endian length header + JSON payload");
            while (true) {
                Socket socket = server.accept();
                pool.submit(() -> handleClient(socket, serverCharset));
            }
        }
    }

    private static void handleClient(Socket socket, Charset charset) {
        String client = socket.getRemoteSocketAddress().toString();
        System.out.printf("[%s] connected %s%n", Instant.now(), client);

        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

            while (true) {
                int frameSize;
                try {
                    frameSize = input.readInt();
                } catch (EOFException eof) {
                    System.out.printf("[%s] disconnected %s%n", Instant.now(), client);
                    return;
                }

                if (frameSize < 0 || frameSize > MAX_FRAME_SIZE) {
                    writeFrame(output, jsonError("invalid frame size: " + frameSize), charset);
                    return;
                }

                byte[] body = input.readNBytes(frameSize);
                if (body.length != frameSize) {
                    System.out.printf("[%s] incomplete frame from %s%n", Instant.now(), client);
                    return;
                }

                String text = new String(body, charset);
                System.out.printf("[%s] received %d bytes: %s%n", Instant.now(), frameSize, text);

                String response = looksLikeJson(text)
                    ? "{\"code\":200,\"message\":\"ok\",\"echo\":" + text
                        + ",\"receivedBytes\":" + frameSize
                        + ",\"timestamp\":" + System.currentTimeMillis() + "}"
                    : "{\"code\":400,\"message\":\"invalid json\",\"raw\":\"" + escapeJson(text) + "\"}";
                writeFrame(output, response, charset);
            }
        } catch (IOException e) {
            System.out.printf("[%s] client error %s: %s%n", Instant.now(), client, e.getMessage());
        }
    }

    private static void writeFrame(DataOutputStream output, String json, Charset charset) throws IOException {
        byte[] payload = json.getBytes(charset);
        output.writeInt(payload.length);
        output.write(payload);
        output.flush();
    }

    private static String jsonError(String message) {
        return "{\"code\":400,\"message\":\"" + escapeJson(message) + "\"}";
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
