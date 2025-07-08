package com.example.space_operators_java.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerProfileDTO {
    @JsonProperty("playerId")
    private String playerId;

    private Integer gamesPlayed;

    // Champs optionnels qui pourraient venir d'autres endpoints
    private String email;
    private String playerName;

    // Constructeur par défaut
    public PlayerProfileDTO() {}

    // Constructeur avec paramètres
    public PlayerProfileDTO(String playerId, Integer gamesPlayed) {
        this.playerId = playerId;
        this.gamesPlayed = gamesPlayed;
    }

    // Getters
    public String getPlayerId() {
        return playerId;
    }

    public String getId() {
        return playerId; // Alias pour compatibilité
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public String getEmail() {
        return email;
    }

    public String getPlayerName() {
        return playerName;
    }

    // Setters
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setGamesPlayed(Integer gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        return "PlayerProfileDTO{" +
                "playerId='" + playerId + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", email='" + email + '\'' +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}