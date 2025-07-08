package com.example.space_operators_java.dtos;

public class RegisterDTO {
    private String email;
    private String password;
    private String playerName;

    // Constructeur par défaut
    public RegisterDTO() {}

    // Constructeur avec paramètres
    public RegisterDTO(String email, String password, String playerName) {
        this.email = email;
        this.password = password;
        this.playerName = playerName;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPlayerName() {
        return playerName;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public String toString() {
        return "RegisterDTO{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}