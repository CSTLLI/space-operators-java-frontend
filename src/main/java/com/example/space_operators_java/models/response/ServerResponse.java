package com.example.space_operators_java.models.response;

import com.fasterxml.jackson.databind.JsonNode;

public class ServerResponse {
    private String type;
    private JsonNode data;

    public ServerResponse() {}

    public ServerResponse(String type, JsonNode data) {
        this.type = type;
        this.data = data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public JsonNode getData() {
        return data;
    }
}