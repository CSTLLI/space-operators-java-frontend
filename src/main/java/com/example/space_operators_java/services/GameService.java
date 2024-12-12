package com.example.space_operators_java.services;

import com.example.space_operators_java.models.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;

public class GameService {
    private static GameService instance;
    private final Player currentPlayer;
    private String gameId;
    private final ObservableList<Player> playerData = FXCollections.observableArrayList();
    private final StringProperty role = new SimpleStringProperty();

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