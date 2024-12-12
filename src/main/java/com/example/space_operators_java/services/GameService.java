package com.example.space_operators_java.services;

import com.example.space_operators_java.models.Operation;
import com.example.space_operators_java.models.Player;
import com.example.space_operators_java.models.operation.Element;
import com.example.space_operators_java.models.operation.Result;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameService {
    private static GameService instance;
    private final Player currentPlayer;
    private String gameId;
    private final ObservableList<Player> playerData = FXCollections.observableArrayList();
    private final ObjectProperty<String> role = new SimpleObjectProperty<>();
    private final ObjectProperty<Operation> currentOperation = new SimpleObjectProperty<>();
    private final DoubleProperty shipIntegrity = new SimpleDoubleProperty(100);
    private final BooleanProperty gameEnded = new SimpleBooleanProperty(false);
    private int turnsCompleted = 0;

    private GameService() {
        this.currentPlayer = new Player("CSTLLI", UUID.randomUUID().toString(), false);

        System.out.println("Player ID: " + currentPlayer.getId());
    }

    public static GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void createGame() {
        try {
            String gameId = String.valueOf(ApiService.getInstance().createGame());

            setGameId(gameId);
            currentPlayer.setHost(true);

            addPlayer(currentPlayer);

            WebSocketService.getInstance().sendConnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create game", e);
        }
    }

    public void joinGame(String gameId) {
        this.gameId = gameId;
        playerData.add(currentPlayer);

        WebSocketService.getInstance().sendConnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
    }

    public void disconnectGame() {
        WebSocketService.getInstance().sendDisconnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
        getCurrentPlayer().setReady(false);
        cleanSessionPlayers();
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

    public void cleanSessionPlayers() {
        playerData.clear();
    }

    // Propriétés
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
            int turn = data.get("turn").asInt();
            String role = data.get("role").asText();
            String id = data.get("id").asText();
            int duration = data.get("duration").asInt();
            String description = data.get("description").asText();

            // Construire la liste des éléments
            List<Element> elements = new ArrayList<>();
            JsonNode elementsNode = data.get("elements");
            if (elementsNode != null && elementsNode.isArray()) {
                for (JsonNode elementNode : elementsNode) {
                    Element element = new Element(
                            elementNode.get("type").asText(),
                            elementNode.get("id").asInt(),
                            elementNode.get("valueType").asText(),
                            elementNode.get("value").asText()
                    );
                    elements.add(element);
                }
            }

            // Construire l'objet Result
            Result result = parseResult(data.get("result"));

            // Créer et définir la nouvelle opération
            Operation operation = new Operation(turn, role, id, duration, description, elements, result);
            setCurrentOperation(operation);
            setRole(role);

        } catch (Exception e) {
            System.err.println("Erreur lors du parsing de l'opération: " + e.getMessage());
        }
    }

    private Result parseResult(JsonNode resultNode) {
        return new Result();
    }

    public BooleanProperty gameEndedProperty() {
        return gameEnded;
    }

    public int getTurnsCompleted() {
        return turnsCompleted;
    }

    public void handleGameEnd(JsonNode data) {
//        if (data.has("turns")) {
//            turnsCompleted = data.get("turns").asInt();
//        }
        gameEnded.set(true);
    }
}