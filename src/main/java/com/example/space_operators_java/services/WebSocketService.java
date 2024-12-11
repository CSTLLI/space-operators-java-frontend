package com.example.space_operators_java.services;

import com.example.space_operators_java.models.ConnectionData;
import com.example.space_operators_java.models.Message;
import com.example.space_operators_java.models.Player;
import com.example.space_operators_java.models.ServerResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WebSocketService {
    private static WebSocketService instance;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private List<StompSession.Subscription> activeSubscriptions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String SERVER_URL = "ws://26.34.233.167:8080/ws";

    private String currentGameId;

    private WebSocketService() {
        initializeWebSocket();
    }

    private void initializeWebSocket() {
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    public void subscribeToGame(String gameId) {
        if (stompSession != null && stompSession.isConnected()) {
            currentGameId = gameId;
            System.out.println("Abonnement à la partie: " + gameId);
            StompSession.Subscription subscription = stompSession.subscribe("/topic/game/" + gameId, new CustomStompSessionHandler());
            activeSubscriptions.add(subscription);
        }
    }

    public void sendConnectRequest(String gameId, String playerId, String playerName) {
        if (stompSession != null && stompSession.isConnected()) {
            System.out.println("Préparation de la requête de connexion");

            try {
                ConnectionData connectData = new ConnectionData(gameId, playerId, playerName);
                System.out.println("Message à envoyer: " + connectData);
                stompSession.send("/app/connect", connectData);

                subscribeToGame(gameId);
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

    public void unsubscribeFromTopics() {
        System.out.println("Désabonnement des topics");
        for (StompSession.Subscription subscription : activeSubscriptions) {
            subscription.unsubscribe();
        }
        activeSubscriptions.clear();
        currentGameId = null;
    }

    private class CustomStompSessionHandler implements StompSessionHandler {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connecté au serveur WebSocket");
            session.subscribe("/topic/connection", this);
            stompSession = session;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("HandleException->Payload: " + new String(payload));
            System.out.println("HandleException->Exception: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {

        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ServerResponse.class;
        }

        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                Message<Object> message = objectMapper.convertValue(payload,
                        new TypeReference<Message<Object>>() {});

                switch (message.getType()) {
                    case "players" -> handlePlayersMessage(message);
                    case "connection" -> {
                        if (payload instanceof ServerResponse response) {
                            System.out.println("Message de connexion: " + response.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur parsing message: " + e.getMessage());
            }
        }

        private void handlePlayersMessage(Message<Object> message) {
            try {
                JsonNode dataNode = objectMapper.valueToTree(message.getData());
                JsonNode playersNode = dataNode.get("players");

                if (playersNode != null && playersNode.isArray()) {
                    Platform.runLater(() -> {
                        GameService gameService = GameService.getInstance();
                        gameService.getPlayers().clear();

                        for (JsonNode playerNode : playersNode) {
                            String name = playerNode.get("name").asText();
                            boolean status = playerNode.get("status").asBoolean();

                            Player player = new Player(
                                    name, status
                            );
                            player.setReady(status);
                            gameService.addPlayer(player);
                        }
                    });
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du traitement des joueurs: " + e.getMessage());
            }
        }
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }
}