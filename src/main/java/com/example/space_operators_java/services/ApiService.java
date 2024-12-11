package com.example.space_operators_java.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiService {
    private static ApiService instance;
    private final HttpClient httpClient;
    private final String BASE_URL = "http://26.34.233.167:8080/api";

    private ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public String createGame() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/create-game"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

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

    public void joinGame(String gameId, String playerId) {
        try {
            String requestBody = String.format("{\"gameId\":\"%s\",\"playerId\":\"%s\"}",
                    gameId, playerId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/join-game"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error joining game: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to join game", e);
        }
    }

    public void getGameState(String gameId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/game/" + gameId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Traiter la réponse si nécessaire
                System.out.println("Game state: " + response.body());
            } else {
                throw new RuntimeException("Error getting game state: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get game state", e);
        }
    }

    public void setPlayerReady(String gameId, String playerId, boolean isReady) {
        try {
            String requestBody = String.format(
                    "{\"gameId\":\"%s\",\"playerId\":\"%s\",\"ready\":%b}",
                    gameId, playerId, isReady);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/player/ready"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error setting player ready: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set player ready", e);
        }
    }
}