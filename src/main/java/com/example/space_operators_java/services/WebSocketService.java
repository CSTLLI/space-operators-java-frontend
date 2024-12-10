package com.example.space_operators_java.services;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketService {
    private static WebSocketService instance;
    private CustomWebSocketClient client;
    private final String SERVER_URL = "ws://26.34.233.167:8080";

    private class CustomWebSocketClient extends WebSocketClient {
        public CustomWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("Connected to server");
        }

        @Override
        public void onMessage(String message) {
            System.out.println("Received message: " + message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("Connection closed: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            System.err.println("Error occurred: " + ex.getMessage());
        }
    }

    private WebSocketService() {
        connectToServer();
    }

    private void connectToServer() {
        try {
            System.out.println("Trying to connect to: " + SERVER_URL);
            client = new CustomWebSocketClient(new URI(SERVER_URL));
            client.connect();
        } catch (Exception e) {
            System.err.println("WebSocket connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }
}