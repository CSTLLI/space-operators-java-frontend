package com.example.space_operators_java.services;

import com.example.space_operators_java.models.Operation;
import com.example.space_operators_java.models.Player;
import com.example.space_operators_java.models.operation.Element;
import com.example.space_operators_java.models.operation.Result;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameService {
    private static GameService instance;
    private Player currentPlayer;
    private String gameId;
    private final ObservableList<Player> playerData = FXCollections.observableArrayList();
    private final ObjectProperty<String> role = new SimpleObjectProperty<>();
    private final ObjectProperty<Operation> currentOperation = new SimpleObjectProperty<>();
    private final DoubleProperty shipIntegrity = new SimpleDoubleProperty(100);
    private final BooleanProperty gameEnded = new SimpleBooleanProperty(false);
    private int turnsCompleted = 0;

    private GameService() {
        ensureCurrentPlayer();
    }

    public static GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    public void cleanGameState() {
        this.gameId = null;
        this.turnsCompleted = 0;

        Platform.runLater(() -> {
            this.role.set(null);
            this.currentOperation.set(null);
            this.shipIntegrity.set(100.0);
            this.gameEnded.set(false);
        });

        this.playerData.clear();

        // S'assurer qu'on a toujours un joueur valide
        ensureCurrentPlayer();
    }

    private void ensureCurrentPlayer() {
        if (this.currentPlayer == null) {
            this.currentPlayer = new Player("CSTLLI", UUID.randomUUID().toString(), false);
        } else {
            this.currentPlayer.setReady(false);
            this.currentPlayer.setHost(false);
        }
    }

    public String getCurrentPlayerId() {
        return currentPlayer.getId();
    }
    public void setCurrentPlayerId(String id) {
        if (currentPlayer != null) {
            currentPlayer.setId(id);
        } else {
            ensureCurrentPlayer();
            currentPlayer.setId(id);
        }
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void createGame() {
        try {
            forceCleanState();

            String response = String.valueOf(ApiService.getInstance().createGame());
            setGameId(response);

            currentPlayer.setHost(true);
            currentPlayer.setReady(false);
            addPlayer(currentPlayer);

            WebSocketService.getInstance().sendConnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create game", e);
        }
    }

    public void joinGame(String gameId) {
        try {
            forceCleanState();

            this.gameId = gameId;
            currentPlayer.setReady(false);
            addPlayer(currentPlayer);

            WebSocketService webSocketService = WebSocketService.getInstance();
            if (webSocketService.getStompSession() == null || !webSocketService.getStompSession().isConnected()) {
                webSocketService.connect();
            }

            webSocketService.sendConnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to join game", e);
        }
    }

    private void forceCleanState() {
        this.gameId = null;
        this.turnsCompleted = 0;
        this.playerData.clear();

        this.role.set(null);
        this.currentOperation.set(null);
        this.shipIntegrity.set(100.0);
        this.gameEnded.set(false);

        if (this.currentPlayer != null) {
            this.currentPlayer.setReady(false);
            this.currentPlayer.setHost(false);
        } else {
            ensureCurrentPlayer();
        }
    }

    public void disconnectGame() {
        try {
            if (gameId != null && currentPlayer != null) {
                WebSocketService.getInstance().sendDisconnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    public ObservableList<Player> getPlayers() {
        return playerData;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void addPlayer(Player player) {
        if (!this.playerData.contains(player)) {
            this.playerData.add(player);
        }
    }

    public void updatePlayerInList(String playerId, boolean isReady) {
        Platform.runLater(() -> {
            for (Player player : playerData) {
                if (player.getId().equals(playerId)) {
                    player.setReady(isReady);
                    break;
                }
            }

            if (currentPlayer != null && currentPlayer.getId().equals(playerId)) {
                currentPlayer.setReady(isReady);
            }
        });
    }

    public void replacePlayersList(List<Player> newPlayers) {
        Platform.runLater(() -> {
            playerData.clear();
            playerData.addAll(newPlayers);

            if (currentPlayer != null) {
                for (Player serverPlayer : newPlayers) {
                    if (serverPlayer.getId().equals(currentPlayer.getId())) {
                        currentPlayer.setName(serverPlayer.getName());
                        break;
                    }
                }
            }
        });
    }

    public void cleanSessionPlayers() {
        playerData.clear();
    }

    public ObjectProperty<Operation> currentOperationProperty() {
        return currentOperation;
    }

    public Operation getCurrentOperation() {
        return currentOperation.get();
    }

    public void setCurrentOperation(Operation operation) {
        this.currentOperation.set(operation);
    }

    public DoubleProperty shipIntegrityProperty() {
        return shipIntegrity;
    }

    public double getShipIntegrity() {
        return shipIntegrity.get();
    }

    public void setShipIntegrity(double integrity) {
        this.shipIntegrity.set(integrity);
    }

    public ObjectProperty<String> roleProperty() {
        return role;
    }

    public String getRole() {
        return role.get();
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public void handleOperationMessage(JsonNode data) {
        try {
            System.out.println("=== GAMESERVICE: TRAITEMENT OPÉRATION ===");

            int turn = data.get("turn").asInt();
            String role = data.get("role").asText();
            String id = data.get("id").asText();
            String operatorId = data.get("operatorId").asText();
            int duration = data.get("duration").asInt();
            String description = data.get("description").asText();

            System.out.println("Turn: " + turn + ", Role: '" + role + "', ID: " + id);
            System.out.println("Duration: " + duration + ", Description: " + description);

            List<Element> elements = new ArrayList<>();
            JsonNode elementsNode = data.get("elements");
            if (elementsNode != null && elementsNode.isArray()) {
                System.out.println("Parsing " + elementsNode.size() + " éléments...");

                for (JsonNode elementNode : elementsNode) {
                    try {
                        String type = elementNode.get("type").asText();
                        int elementId = elementNode.get("id").asInt();
                        String valueType = elementNode.get("valueType").asText();

                        // Parsing robuste de la valeur selon son type
                        Object value = parseElementValue(elementNode.get("value"), valueType);

                        System.out.println("  Élément: type=" + type + ", id=" + elementId +
                                ", valueType=" + valueType + ", value=" + value +
                                " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");

                        Element element = new Element(type, elementId, valueType, value);
                        elements.add(element);
                    } catch (Exception e) {
                        System.err.println("Erreur parsing élément: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            Result result = parseResult(data.get("result"));

            Operation operation = new Operation(turn, role, id, operatorId, duration, description, elements, result);

            System.out.println("=== OPÉRATION CRÉÉE ===");
            System.out.println("Rôle final: '" + operation.getRole() + "'");
            System.out.println("Nombre d'éléments: " + operation.getElements().size());

            // Mettre à jour sur le thread JavaFX
            Platform.runLater(() -> {
                setCurrentOperation(operation);
                setRole(role);
                System.out.println("✅ Opération définie dans GameService avec rôle: " + role);
            });

        } catch (Exception e) {
            System.err.println("Erreur lors du parsing de l'operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parse la valeur d'un élément selon son type
     */
    private Object parseElementValue(JsonNode valueNode, String valueType) {
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        try {
            return switch (valueType.toLowerCase()) {
                case "color" -> {
                    // Pour les couleurs, retourner la chaîne directement
                    if (valueNode.isTextual()) {
                        yield valueNode.asText();
                    } else {
                        yield valueNode.toString();
                    }
                }
                case "boolean" -> {
                    if (valueNode.isBoolean()) {
                        yield valueNode.asBoolean();
                    } else if (valueNode.isTextual()) {
                        String text = valueNode.asText().toLowerCase();
                        yield "true".equals(text) || "on".equals(text) || "1".equals(text);
                    } else if (valueNode.isNumber()) {
                        yield valueNode.asInt() == 1;
                    } else {
                        yield false;
                    }
                }
                case "number", "integer" -> {
                    if (valueNode.isNumber()) {
                        yield valueNode.asInt();
                    } else if (valueNode.isTextual()) {
                        try {
                            yield Integer.parseInt(valueNode.asText());
                        } catch (NumberFormatException e) {
                            System.err.println("Impossible de parser '" + valueNode.asText() + "' comme nombre");
                            yield 0;
                        }
                    } else {
                        yield 0;
                    }
                }
                case "string", "text" -> {
                    yield valueNode.asText();
                }
                default -> {
                    // Par défaut, retourner la valeur comme string
                    if (valueNode.isTextual()) {
                        yield valueNode.asText();
                    } else {
                        yield valueNode.toString();
                    }
                }
            };
        } catch (Exception e) {
            System.err.println("Erreur parsing valeur: " + e.getMessage());
            return valueNode.asText(); // Fallback
        }
    }

    public int getTurnsCompleted() {
        return turnsCompleted;
    }

    private Result parseResult(JsonNode resultNode) {
        return new Result();
    }

    public BooleanProperty gameEndedProperty() {
        return gameEnded;
    }

    public void handleGameEnd(JsonNode data) {
        if (data.has("turns")) {
            turnsCompleted = data.get("turns").asInt();
        }
        gameEnded.set(true);
    }
}