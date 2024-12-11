package com.example.space_operators_java.models.response;

import com.example.space_operators_java.models.Player;

public class PlayerListResponse {
    private final String type;
    private final Player data;

    public PlayerListResponse(String type, Player data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Player getData() {
        return data;
    }
}
