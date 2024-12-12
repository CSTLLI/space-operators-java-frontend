package com.example.space_operators_java.models;

import com.example.space_operators_java.models.operation.Element;
import com.example.space_operators_java.models.operation.Result;

import java.util.List;

public class Operation {
    private int turn;
    private String role;
    private String id;
    private int duration;
    private String description;
    private List<Element> elements;
    private Result result;

    public Operation(int turn, String role, String id, int duration, String description, List<Element> elements, Result result) {
        this.turn = turn;
        this.role = role;
        this.id = id;
        this.duration = duration;
        this.description = description;
        this.elements = elements;
        this.result = result;
    }

    // Getters
    public int getTurn() { return turn; }
    public String getRole() { return role; }
    public String getId() { return id; }
    public int getDuration() { return duration; }
    public String getDescription() { return description; }
    public List<Element> getElements() { return elements; }
    public Result getResult() { return result; }
}

