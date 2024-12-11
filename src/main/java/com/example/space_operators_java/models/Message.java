package com.example.space_operators_java.models;

public class Message<T> {
    private String type;
    private T data;

    public Message(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public T getData() {
        return data;
    }
}