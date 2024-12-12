package com.example.space_operators_java.models.request;

public class SessionRequest {
    private String gameId;
    private String playerId;
    private String playerName;

    public SessionRequest(String gameId, String playerId, String playerName) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }
}