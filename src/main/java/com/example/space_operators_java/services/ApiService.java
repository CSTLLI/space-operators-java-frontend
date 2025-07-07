package com.example.space_operators_java.services;

import com.example.space_operators_java.dtos.LoginDTO;
import com.example.space_operators_java.dtos.LoginResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ApiService {
    private static ApiService instance;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private String jwtToken; // Store the JWT token after login

    //    private final String BASE_URL = "http://26.34.233.167:8080/api"; // PC PORTABLE WASSIM
    private final String BASE_URL = "http://26.195.1.69:8080/api"; // PC FIXE WASSIM

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

    /**
     * Login method - returns JWT token on success
     */
    public CompletableFuture<String> login(String email, String password) {
        try {
            LoginDTO loginDTO = new LoginDTO(email, password);
            String requestBody = mapper.writeValueAsString(loginDTO);

            System.out.println("=== LOGIN REQUEST ===");
            System.out.println("URL: " + BASE_URL + "/auth/login");
            System.out.println("Email: " + email);
            System.out.println("Request Body: " + requestBody);
            System.out.println("====================");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("=== LOGIN RESPONSE ===");
                        System.out.println("Status Code: " + response.statusCode());
                        System.out.println("Response Body: " + response.body());
                        System.out.println("=====================");

                        if (response.statusCode() == 200) {
                            try {
                                // Parse the JSON response to extract the token
                                LoginResponseDTO tokenResponse = mapper.readValue(response.body(), LoginResponseDTO.class);
                                String token = tokenResponse.getToken();

                                if (token != null && !token.isEmpty() && !token.equals("null")) {
                                    this.jwtToken = token;
                                    System.out.println("Extracted token: " + token);
                                    return token;
                                } else {
                                    throw new RuntimeException("Invalid credentials - no token received");
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to parse token response: " + e.getMessage());
                                throw new RuntimeException("Failed to parse login response", e);
                            }
                        } else {
                            throw new RuntimeException("Login failed: " + response.statusCode());
                        }
                    })
                    .exceptionally(throwable -> {
                        System.err.println("=== LOGIN ERROR ===");
                        System.err.println("Error: " + throwable.getMessage());
                        throwable.printStackTrace();
                        System.err.println("==================");
                        throw new RuntimeException("Login failed", throwable);
                    });
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * Get current user information
     */
    public CompletableFuture<String> getCurrentUser() {
        if (jwtToken == null) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Not authenticated"));
            return future;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + jwtToken)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        throw new RuntimeException("Failed to get user info: " + response.statusCode());
                    }
                });
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return jwtToken != null;
    }

    /**
     * Logout - clear the token
     */
    public void logout() {
        this.jwtToken = null;
    }

    /**
     * Get the current JWT token
     */
    public String getJwtToken() {
        return jwtToken;
    }

    public String createGame() {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/create-game"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody());

            System.out.println("=== API REQUEST ===");
            System.out.println("URL: " + BASE_URL + "/create-game");
            System.out.println("Payload: {}");
            System.out.println("JWT Token: " + (jwtToken != null ? jwtToken : "null"));
            System.out.println("==================");

            // Add authorization header if authenticated
            if (jwtToken != null) {
                requestBuilder.header("Authorization", "Bearer " + jwtToken);
            }

            HttpRequest request = requestBuilder.build();
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
        String url = BASE_URL + "/player/ready/" + playerId;

        System.out.println("=== API REQUEST ===");
        System.out.println("URL: " + url);
        System.out.println("Player ID: " + playerId);
        System.out.println("Status: " + status);
        System.out.println("Payload: " + requestBody);
        System.out.println("==================");

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        // Add authorization header if authenticated
        if (jwtToken != null) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest request = requestBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("=== API RESPONSE ===");
                    System.out.println("Status Code: " + response.statusCode());
                    System.out.println("Response Body: " + response.body());
                    System.out.println("===================");

                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        System.err.println("Error setting player ready: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("=== API ERROR ===");
                    System.err.println("Error: " + throwable.getMessage());
                    throwable.printStackTrace();
                    System.err.println("================");
                    return false;
                });
    }
}