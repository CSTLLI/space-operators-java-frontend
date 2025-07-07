package com.example.space_operators_java.controllers;

import com.example.space_operators_java.services.ApiService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class SpaceOperatorsLoginController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView titleImage;
    @FXML
    private Button loginButton;
    @FXML
    private Label statusLabel; // Add this to your FXML if you want to show status messages

    private ApiService apiService;

    @FXML
    public void initialize() {
        // Initialize API service
        apiService = ApiService.getInstance();

        // Load background image
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_home.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }
    }

    @FXML
    private void onLoginClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir un nom d'utilisateur et un mot de passe.");
            return;
        }

        // Disable login button during authentication
        loginButton.setDisable(true);
        if (statusLabel != null) {
            statusLabel.setText("Connexion en cours...");
        }

        // Perform login
        apiService.login(email, password)
                .thenAccept(token -> {
                    // Login successful - run on JavaFX thread
                    Platform.runLater(() -> {
                        System.out.println("Login successful! Token: " + token);

                        // Clear form
                        emailField.clear();
                        passwordField.clear();

                        // Navigate to home view
                        SceneNavigator.navigateTo("home-view.fxml");
                    });
                })
                .exceptionally(throwable -> {
                    // Login failed - run on JavaFX thread
                    Platform.runLater(() -> {
                        System.err.println("Login failed: " + throwable.getMessage());

                        // Re-enable login button
                        loginButton.setDisable(false);
                        if (statusLabel != null) {
                            statusLabel.setText("");
                        }

                        // Show error message
                        String errorMessage = "Échec de la connexion. Veuillez vérifier vos identifiants.";
                        if (throwable.getCause() != null && throwable.getCause().getMessage().contains("Invalid credentials")) {
                            errorMessage = "Nom d'utilisateur ou mot de passe incorrect.";
                        }
                        showAlert("Erreur de connexion", errorMessage);
                    });
                    return null;
                });
    }

    @FXML
    private void onGoingToRegisterPageClick() {
        System.out.println("Create account... (btn clicked)");
        SceneNavigator.navigateTo("register-view.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}