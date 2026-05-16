package com.questbridge;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class QuestHttpServer {
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running = false;

    public void start(int port) {
        running = true;
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("[QuestBridge] Servidor HTTP em http://localhost:" + port);
                
                while (running) {
                    try (Socket client = serverSocket.accept()) {
                        handleClient(client);
                    } catch (IOException e) {
                        if (running) System.err.println("[QuestBridge] HTTP error: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("[QuestBridge] Falha ao iniciar servidor HTTP: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }

    private void handleClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String line = in.readLine();
        if (line == null) return;

        String[] parts = line.split(" ");
        if (parts.length < 2) return;
        String path = parts[1];

        OutputStream out = client.getOutputStream();
        
        if (path.equals("/") || path.equals("/index.html")) {
            serveResource(out, "/assets/questbridge/controller.html", "text/html");
        } else if (path.equals("/status")) {
            boolean connected = QuestBridgeMod.getControllerState().isConnected();
            String json = String.format("{\"mod\": \"questbridge\", \"connected\": %b}", connected);
            sendResponse(out, 200, "application/json", json.getBytes(StandardCharsets.UTF_8));
        } else {
            sendResponse(out, 404, "text/plain", "Not Found".getBytes());
        }
    }

    private void serveResource(OutputStream out, String resPath, String contentType) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resPath)) {
            if (is == null) {
                sendResponse(out, 404, "text/plain", "Resource not found".getBytes());
                return;
            }
            byte[] data = is.readAllBytes();
            sendResponse(out, 200, contentType, data);
        }
    }

    private void sendResponse(OutputStream out, int code, String contentType, byte[] body) throws IOException {
        String header = "HTTP/1.1 " + code + " OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }
}
