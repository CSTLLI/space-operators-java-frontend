package com.example.space_operators_java.services;

import com.example.space_operators_java.models.Player;
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
//    private WebSocketClient webSocket;

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

//    private void initWebSocket() {
//        webSocket = new WebSocketClient("ws://your-server-url");
//        webSocket.setMessageHandler(message -> {
//            if (message.getType().equals("players")) {
//                Platform.runLater(() -> updatePlayers(message.getData().getPlayers()));
//            }
//        });
//    }

    public void createGame() {
        currentPlayer.setHost(true);

        addPlayer(currentPlayer);
    }

    public void joinGame(String gameId, String playerName) {
        this.gameId = gameId;
        currentPlayer.setName(playerName);
        players.add(currentPlayer);
        // Send WebSocket connection request
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
            players.add(new Player(
                    player.getString("id"),
                    player.getString("name")
            ));
        }
    }

    public void addPlayer(Player player) {
        System.out.println("Adding player: " + player); // Debug
        if (!players.contains(player)) {
            players.add(player);
            System.out.println("Players count: " + players.size()); // Debug
        }
    }
}