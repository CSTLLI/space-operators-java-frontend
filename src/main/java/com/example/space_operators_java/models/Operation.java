package com.example.space_operators_java.models;

import com.example.space_operators_java.models.operation.Element;
import com.example.space_operators_java.models.operation.Result;

import java.util.List;

public class Operation {
    private int turn;
    private String role;
    private String id;
    private String operatorId;
    private int duration;
    private String description;
    private List<Element> elements;
    private Result result;

    public Operation(int turn, String role, String id, String operatorId, int duration, String description, List<Element> elements, Result result) {
        this.turn = turn;
        this.role = role;
        this.id = id;
        this.operatorId = operatorId;
        this.duration = duration;
        this.description = description;
        this.elements = elements;
        this.result = result;
    }

    // Getters
    public int getTurn() { return turn; }
    public String getRole() { return role; }
    public String getId() { return id; }
    public String getOperationId() { return operatorId; } // Alias for getId()
    public int getDuration() { return duration; }
    public String getDescription() { return description; }
    public List<Element> getElements() { return elements; }
    public Result getResult() { return result; }

    // Serializer
    public String toString() {
        return String.format("{\"turn\": %d, \"role\": \"%s\", \"id\": \"%s\", \"duration\": %d, \"description\": \"%s\", \"elements\": %s, \"result\": %s}",
                turn, role, id, duration, description, elements, result);
    }
}

