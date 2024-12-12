package com.example.space_operators_java.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Player {
    private String id;
    private String name;
    private boolean status;
    private boolean isHost;

    public Player(String name, String id, boolean status) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.isHost = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReady(boolean status) {
        this.status = status;
    }

    public boolean isReady() {
        return status;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}
