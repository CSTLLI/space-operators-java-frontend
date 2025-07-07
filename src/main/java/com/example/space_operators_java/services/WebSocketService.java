package com.example.space_operators_java.services;

import com.example.space_operators_java.models.*;
import com.example.space_operators_java.models.operation.Result;
import com.example.space_operators_java.models.request.SessionRequest;
import com.example.space_operators_java.models.response.ServerResponse;
import com.example.space_operators_java.utils.SceneNavigator;
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

public class WebSocketService {
    private static WebSocketService instance;
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private List<StompSession.Subscription> activeSubscriptions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CustomStompSessionHandler sessionHandler = new CustomStompSessionHandler(this);

    //    private final String SERVER_URL = "ws://26.34.233.167:8080/ws"; // PC PORTABLE WASSIM
    private final String SERVER_URL = "ws://26.195.1.69:8080/ws"; // PC FIXE WASSIM

    private String currentGameId;

    private WebSocketService() {
        initializeWebSocket();
    }

    public void subscribeToGame(String gameId) {
        if (stompSession != null && stompSession.isConnected()) {
            currentGameId = gameId;
            System.out.println("Abonnement à la partie: " + gameId);
            StompSession.Subscription subscription = stompSession.subscribe("/topic/game/" + gameId, sessionHandler);
            activeSubscriptions.add(subscription);
        }
    }

    public void sendConnectRequest(String gameId, String playerId, String playerName) {
        if (stompSession != null && stompSession.isConnected()) {
            try {
                GameService.getInstance().setCurrentPlayerId(playerId);
                System.out.println("Player ID local défini: " + playerId);

                subscribeToGame(gameId);

                SessionRequest session = new SessionRequest(gameId, playerId, playerName);
                stompSession.send("/app/connect", session);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la requête de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Session STOMP non connectee");
        }
    }

    public void sendStartRequest(String gameId) {
        System.out.println("Envoi de la demande de demarrage");
        if (stompSession != null && stompSession.isConnected()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.createObjectNode()
                        .put("gameId", gameId);

                ServerResponse response = new ServerResponse(
                        "start",
                        jsonNode
                );

                System.out.println("Response " + response);
                stompSession.send("/app/start", response);

            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la requête de demarrage: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Session STOMP non connectee");
        }
    }

    public void disconnect() {
        System.out.println("=== AVANT DÉCONNEXION ===");
        if (stompSession != null) {
            System.out.println("Session ID: " + stompSession.getSessionId());
            System.out.println("Souscriptions: " + activeSubscriptions.size());
        }

        try {
            System.out.println("Désabonnement forcé de TOUS les topics");
            if (stompSession != null) {
                for (StompSession.Subscription sub : activeSubscriptions) {
                    try {
                        System.out.println("  Désabonnement de: " + sub.getSubscriptionId());
                        sub.unsubscribe();
                    } catch (Exception e) {
                        System.err.println("  Erreur désabonnement: " + e.getMessage());
                    }
                }
                activeSubscriptions.clear();

                try {
                    System.out.println("Déconnexion de la session STOMP");
                    stompSession.disconnect();
                } catch (Exception e) {
                    System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
                }
            }
        } finally {
            stompSession = null;
            currentGameId = null;
            System.out.println("=== DÉCONNEXION TERMINÉE ===");
        }

        initializeWebSocket();
    }

    public void connect() {
        try {
            stompClient.connect(SERVER_URL, sessionHandler);
        } catch (Exception e) {
            System.err.println("Erreur de connexion WebSocket: " + e.getMessage());
        }
    }

    private void initializeWebSocket() {
        System.out.println("Création d'un NOUVEAU client WebSocket");
        WebSocketClient client = new StandardWebSocketClient();
        stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    public void unsubscribeFromTopics() {
        System.out.println("Desabonnement des topics");
        for (StompSession.Subscription subscription : activeSubscriptions) {
            subscription.unsubscribe();
        }
        activeSubscriptions.clear();
        currentGameId = null;
    }

    public void sendDisconnectRequest(String gameId, String playerId, String playerName) {
        if (stompSession != null && stompSession.isConnected()) {
            System.out.println("Deconnexion de la session");

            try {
                SessionRequest session = new SessionRequest(gameId, playerId, playerName);
                stompSession.send("/app/disconnect", session);

            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la requête de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Session STOMP non connectee");
        }
    }

    public void sendFinishOperation(String operatorId, String gameId, Result result) {
        if (stompSession != null && stompSession.isConnected()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.createObjectNode()
                        .put("operator", operatorId)
                        .put("gameId", gameId)
                        .put("result", result.toString());

                ServerResponse response = new ServerResponse(
                        "finish-operation",
                        jsonNode
                );

                stompSession.send("/app/finish-operation", response);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de la fin d'operation: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Session STOMP non connectee");
        }
    }

    private static class CustomStompSessionHandler implements StompSessionHandler {
        private final WebSocketService service;

        public CustomStompSessionHandler(WebSocketService service) {
            this.service = service;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connecte au serveur WebSocket");
            session.subscribe("/topic/connection", this);
            service.stompSession = session;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("HandleException->Payload: " + new String(payload));
            System.out.println("HandleException->Exception: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.err.println("=== ERREUR DE TRANSPORT STOMP ===");
            System.err.println("- Session ID: " + (session != null ? session.getSessionId() : "null"));
            System.err.println("- Exception: " + exception.getMessage());
            exception.printStackTrace();

            if (session != null && session.equals(service.stompSession)) {
                System.err.println("- Reinitialisation de la reference stompSession");
                service.stompSession = null;
            }
            System.err.println("=== FIN D'ERREUR DE TRANSPORT STOMP ===");
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ServerResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            try {
                ServerResponse response = (ServerResponse) payload;

                System.out.println("=== FRAME STOMP REÇUE ===");
                System.out.println("- En-têtes: " + headers);
                System.out.println("- Type de payload: " + (payload != null ? payload.getClass().getName() : "null"));

                switch (response.getType()) {
                    case "players" -> handlePlayersMessage(response.getData());
                    case "message" -> System.out.println("Message reçu: " + response.getData());
                    case "start" -> handleStartMessage(response.getData());
                    case "operation" -> handleOperationMessage(response.getData());
                    case "integrity" -> {
                        double integrity = response.getData().get("integrity").asDouble();
                        Platform.runLater(() -> GameService.getInstance().setShipIntegrity(integrity));
                    }
                    case "destroyed", "victory" ->
                            Platform.runLater(() -> GameService.getInstance().handleGameEnd(response.getData()));
                    default -> System.out.println("Type non gere: " + response.getType());
                }
            } catch (Exception e) {
                System.err.println("Erreur dans handleFrame: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleOperationMessage(JsonNode dataNode) {
            try {
                System.out.println("=== MESSAGE OPÉRATION WEBSOCKET ===");
                System.out.println("JSON brut reçu: " + dataNode.toString());

                // Récupérer le player ID du message
                String messagePlayerId = null;
                if (dataNode.has("playerId")) {
                    messagePlayerId = dataNode.get("playerId").asText();
                    System.out.println("Player ID du message: " + messagePlayerId);
                }

                // Récupérer le player ID local
                String localPlayerId = getCurrentPlayerId();
                System.out.println("Player ID local: " + localPlayerId);

                // Vérifier si le message nous est destiné
                if (messagePlayerId != null && localPlayerId != null) {
                    if (!messagePlayerId.equals(localPlayerId)) {
                        System.out.println("❌ Message ignoré - Player ID différent");
                        System.out.println("   Message pour: " + messagePlayerId);
                        System.out.println("   Player local: " + localPlayerId);
                        return; // Ignorer ce message
                    } else {
                        System.out.println("✅ Message accepté - Player ID correspond");
                    }
                }

                // Afficher les détails de l'opération
                if (dataNode.has("id")) {
                    System.out.println("ID opération: " + dataNode.get("id").asText());
                }
                if (dataNode.has("role")) {
                    System.out.println("Rôle: " + dataNode.get("role").asText());
                }
                if (dataNode.has("turn")) {
                    System.out.println("Tour: " + dataNode.get("turn").asInt());
                }
                if (dataNode.has("duration")) {
                    System.out.println("Durée: " + dataNode.get("duration").asInt());
                }
                if (dataNode.has("description")) {
                    System.out.println("Description: " + dataNode.get("description").asText());
                }
                if (dataNode.has("elements")) {
                    JsonNode elementsNode = dataNode.get("elements");
                    System.out.println("Éléments (" + elementsNode.size() + "):");
                    elementsNode.forEach(elementNode -> {
                        System.out.println("  - Type: " + elementNode.get("type").asText() +
                                ", ID: " + elementNode.get("id").asInt() +
                                ", Value: " + elementNode.get("value") +
                                ", ValueType: " + elementNode.get("valueType").asText());
                    });
                }
                System.out.println("===================================");

                // Déléguer au GameService seulement si le message nous concerne
                GameService.getInstance().handleOperationMessage(dataNode);
            } catch (Exception e) {
                System.err.println("Erreur traitement operation: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Méthode pour récupérer le player ID local
        private String getCurrentPlayerId() {
            try {
                return GameService.getInstance().getCurrentPlayer().getId();
            } catch (Exception e) {
                System.err.println("Erreur récupération player ID: " + e.getMessage());
                return null;
            }
        }

        private void handlePlayersMessage(JsonNode dataNode) {
            try {
                JsonNode playersNode = dataNode.get("players");
                if (playersNode != null && playersNode.isArray()) {
                    Platform.runLater(() -> {
                        GameService gameService = GameService.getInstance();
                        gameService.getPlayers().clear();

                        playersNode.forEach(playerNode -> {
                            String name = playerNode.get("playerName").asText();
                            String id = playerNode.get("playerId").asText();
                            boolean status = playerNode.get("ready").asBoolean();
                            Player player = new Player(name, id, status);
                            gameService.addPlayer(player);
                        });
                    });
                }
            } catch (Exception e) {
                System.err.println("Erreur traitement players: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleStartMessage(JsonNode dataNode) {
            try {
                Platform.runLater(() -> {
                    SceneNavigator.navigateTo("game-view.fxml");
                });
            } catch (Exception e) {
                System.err.println("Erreur traitement start: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public StompSession getStompSession() {
        return stompSession;
    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }
}