package com.example.space_operators_java.services;

import com.example.space_operators_java.models.ConnectionData;
import com.example.space_operators_java.models.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

public class WebSocketService {
    private static WebSocketService instance;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String SERVER_URL = "ws://26.34.233.167:8080/ws";

    private WebSocketService() {
        initializeWebSocket();
    }

    private void initializeWebSocket() {
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    public void sendConnectRequest(String gameId, String playerId, String playerName) {
        if (stompSession != null && stompSession.isConnected()) {
            System.out.println("Préparation de la requête de connexion");

            try {
                // Créer l'objet message
                ConnectionData connectData = new ConnectionData(gameId, playerId, playerName);
                System.out.println("Message à envoyer: " + connectData);
                stompSession.send("/app/connect", connectData);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la requête de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Session STOMP non connectée");
        }
    }

    public void connect() {
        try {
            StompSessionHandler sessionHandler = new CustomStompSessionHandler();
            stompClient.connect(SERVER_URL, sessionHandler);
        } catch (Exception e) {
            System.err.println("Erreur de connexion WebSocket: " + e.getMessage());
        }
    }

    private class CustomStompSessionHandler implements StompSessionHandler {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connecté au serveur WebSocket");
            session.subscribe("/topic/connections", this);
            stompSession = session;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {

        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {

        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return null;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {

        }
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }
}