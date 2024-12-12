package com.example.space_operators_java.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiService {
    private static ApiService instance;
    private final HttpClient httpClient;
    private final String BASE_URL = "http://26.34.233.167:8080/api";

    private ApiService() {
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(10)).build();
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public String createGame() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/create-game")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response: " + response.body());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("Error creating game: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create game", e);
        }
    }


    public CompletableFuture<Boolean> setPlayerReady(String playerId, boolean status) {
        String requestBody = String.format("{\"ready\": %s}", status);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/player/ready/" + playerId)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() == 200) {
                return true;
            } else {
                throw new RuntimeException("Error setting player ready: " + response.statusCode());
            }
        });
    }
}