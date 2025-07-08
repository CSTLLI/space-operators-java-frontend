package com.example.space_operators_java.controllers;

import com.example.space_operators_java.services.ApiService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class SpaceOperatorsRegisterController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField playerNameField;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView titleImage;
    @FXML
    private Button registerButton;
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
    private void onRegisterClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String playerName = playerNameField.getText().trim();

        // Validate input
        if (email.isEmpty() || password.isEmpty() || playerName.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // Validate email format (basic validation)
        if (!isValidEmail(email)) {
            showAlert("Erreur", "Veuillez saisir une adresse email valide.");
            return;
        }

        // Validate password strength (basic validation)
        if (password.length() < 6) {
            showAlert("Erreur", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // Disable register button during registration
        registerButton.setDisable(true);
        if (statusLabel != null) {
            statusLabel.setText("Création du compte en cours...");
        }

        // Perform registration
        apiService.register(email, password, playerName)
                .thenAccept(response -> {
                    // Registration successful - run on JavaFX thread
                    Platform.runLater(() -> {
                        System.out.println("Registration successful!");

                        // Show success message
                        showSuccessAlert("Compte créé", "Votre compte a été créé avec succès ! Vous pouvez maintenant vous connecter.");

                        // Clear form
                        emailField.clear();
                        passwordField.clear();
                        playerNameField.clear();

                        // Navigate to login view
                        SceneNavigator.navigateTo("login-view.fxml");
                    });
                })
                .exceptionally(throwable -> {
                    // Registration failed - run on JavaFX thread
                    Platform.runLater(() -> {
                        System.err.println("Registration failed: " + throwable.getMessage());

                        // Re-enable register button
                        registerButton.setDisable(false);
                        if (statusLabel != null) {
                            statusLabel.setText("");
                        }

                        // Show error message
                        String errorMessage = "Échec de la création du compte. Veuillez réessayer.";
                        if (throwable.getCause() != null) {
                            String causeMessage = throwable.getCause().getMessage();
                            if (causeMessage.contains("already exists") || causeMessage.contains("duplicate")) {
                                errorMessage = "Cette adresse email est déjà utilisée.";
                            } else if (causeMessage.contains("Invalid email")) {
                                errorMessage = "Adresse email invalide.";
                            } else if (causeMessage.contains("Password")) {
                                errorMessage = "Le mot de passe ne respecte pas les critères requis.";
                            }
                        }
                        showAlert("Erreur de création de compte", errorMessage);
                    });
                    return null;
                });
    }

    @FXML
    private void onGoingToLoginPageClick() {
        System.out.println("Go to Login Page (btn clicked)");
        SceneNavigator.navigateTo("login-view.fxml");
    }

    private boolean isValidEmail(String email) {
        // Basic email validation
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}