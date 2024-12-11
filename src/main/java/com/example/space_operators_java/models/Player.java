package com.example.space_operators_java.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Player {
    private String id;
    private String name;
    private boolean isHost;
    private final BooleanProperty ready = new SimpleBooleanProperty(false);

    public Player(String name) {
        this.id = id;
        this.name = name;
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

    public BooleanProperty readyProperty() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready.set(ready);
    }

    public boolean isReady() {
        return ready.get();
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }
}
