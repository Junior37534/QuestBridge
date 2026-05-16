package com.questbridge;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced WebSocket server that supports bidirectional communication.
 * Now tracks active clients to allow sending haptic feedback commands back to the browser.
 */
public class QuestWebSocketServer {
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running = false;
    private final ControllerState state;
    private final Gson gson = new Gson();
    
    // Track active client output streams for broadcasting haptics
    private final Set<OutputStream> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public QuestWebSocketServer(ControllerState state) {
        this.state = state;
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            acceptThread = new Thread(() -> {
                while (running) {
                    try {
                        Socket client = serverSocket.accept();
                        new Thread(() -> handleClient(client)).start();
                    } catch (IOException e) {
                        if (running) {
                            System.err.println("[QuestBridge] WebSocket accept error: " + e.getMessage());
                        }
                    }
                }
            });
            acceptThread.setDaemon(true);
            acceptThread.start();
            System.out.println("[QuestBridge] WebSocket em ws://localhost:" + port);
        } catch (IOException e) {
            System.err.println("[QuestBridge] Falha ao iniciar WebSocket: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a haptic pulse command to all connected WebXR clients.
     */
    public void broadcastHaptic(float intensity, int durationMs) {
        if (clients.isEmpty()) return;
        
        JsonObject json = new JsonObject();
        json.addProperty("type", "haptic");
        json.addProperty("intensity", intensity);
        json.addProperty("duration", durationMs);
        
        String payload = gson.toJson(json);
        byte[] frame = encodeTextFrame(payload);
        
        for (OutputStream out : clients) {
            try {
                out.write(frame);
                out.flush();
            } catch (IOException e) {
                // client disconnected
                clients.remove(out);
            }
        }
    }

    private void handleClient(Socket client) {
        OutputStream out = null;
        try {
            InputStream in = client.getInputStream();
            out = client.getOutputStream();

            Scanner s = new Scanner(in, "UTF-8");
            String data = s.useDelimiter("\\r\\n\\r\\n").next();

            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            if (!match.find()) {
                client.close();
                return;
            }

            String key = match.group(1).trim();
            String acceptKey = generateAcceptKey(key);

            String response = "HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

            out.write(response.getBytes("UTF-8"));
            out.flush();

            // Register client for haptics
            clients.add(out);
            
            readFrames(in);

        } catch (Exception e) {
            // connection lost or parsing error
        } finally {
            if (out != null) clients.remove(out);
            state.setDisconnected();
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private String generateAcceptKey(String key) throws NoSuchAlgorithmException {
        String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String combined = key + magic;
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] hash = sha1.digest(combined.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Simple WebSocket text frame encoder (server-to-client).
     * Does not use masking (optional for server-to-client).
     */
    private byte[] encodeTextFrame(String text) {
        byte[] rawData = text.getBytes();
        int length = rawData.length;
        int frameSize = 2 + (length > 125 ? (length > 65535 ? 8 : 2) : 0) + length;
        byte[] frame = new byte[frameSize];
        
        frame[0] = (byte) 0x81; // FIN + Text
        int offset = 2;
        if (length <= 125) {
            frame[1] = (byte) length;
        } else if (length <= 65535) {
            frame[1] = (byte) 126;
            frame[2] = (byte) ((length >> 8) & 0xFF);
            frame[3] = (byte) (length & 0xFF);
            offset = 4;
        } else {
            // Long payloads (rare for haptics)
            frame[1] = (byte) 127;
            // set bytes 2-9...
            offset = 10;
        }
        
        System.arraycopy(rawData, 0, frame, offset, length);
        return frame;
    }

    private void readFrames(InputStream in) throws IOException {
        while (true) {
            int b1 = in.read();
            if (b1 == -1) break;

            int opcode = b1 & 0x0F;
            if (opcode == 8) { // Close
                break;
            }

            int b2 = in.read();
            boolean masked = (b2 & 0x80) != 0;
            long payloadLength = b2 & 0x7F;

            if (payloadLength == 126) {
                payloadLength = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
            } else if (payloadLength == 127) {
                payloadLength = 0;
                for (int i = 0; i < 8; i++) {
                    payloadLength = (payloadLength << 8) | (in.read() & 0xFF);
                }
            }

            byte[] maskingKey = new byte[4];
            if (masked) {
                in.readNBytes(maskingKey, 0, 4);
            }

            byte[] payload = new byte[(int) payloadLength];
            in.readNBytes(payload, 0, payload.length);

            if (masked) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= maskingKey[i % 4];
                }
            }

            if (opcode == 1) { // Text frame
                String text = new String(payload, "UTF-8");
                try {
                    JsonObject json = gson.fromJson(text, JsonObject.class);
                    state.update(json);
                } catch (Exception e) {
                    // ignore malformed JSON
                }
            }
        }
    }
}
