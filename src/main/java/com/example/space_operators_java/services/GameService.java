package com.example.space_operators_java.services;

import com.example.space_operators_java.models.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.UUID;

public class GameService {
    private static GameService instance;
    private final Player currentPlayer;
    private String gameId;
    private final ObservableList<Player> players = FXCollections.observableArrayList();
    private final StringProperty role = new SimpleStringProperty();

    private GameService() {
        this.currentPlayer = new Player(UUID.randomUUID().toString(), "CSTLLI");

        //mock
        addPlayer(new Player(UUID.randomUUID().toString(), "Wassim"));
        addPlayer(new Player(UUID.randomUUID().toString(), "Ismael"));
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
            String gameId = ApiService.getInstance().createGame();

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
        players.add(currentPlayer);

        // TODO: Send WebSocket connection request
        WebSocketService.getInstance().sendConnectRequest(gameId, currentPlayer.getId(), currentPlayer.getName());
    }

    public ObservableList<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    private void updatePlayers(JsonArray newPlayers) {
        players.clear();
        for (JsonValue value : newPlayers) {
            JsonObject player = (JsonObject) value;
            players.add(new Player(player.getString("id"), player.getString("name")));
        }
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public final StringProperty roleProperty() {
        return role;
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public String getRole() {
        return role.get();
    }
}