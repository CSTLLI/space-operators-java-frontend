package com.example.space_operators_java.models.operation;

import java.util.List;

public class Result {
    private ButtonResult buttons;
    private List<Integer> switches;
    private List<List<Integer>> links;

    // Getters et setters
    public ButtonResult getButtons() { return buttons; }
    public List<Integer> getSwitches() { return switches; }
    public List<List<Integer>> getLinks() { return links; }
}
