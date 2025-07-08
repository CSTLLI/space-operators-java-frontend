package com.example.space_operators_java.controllers;

import com.example.space_operators_java.dtos.PlayerProfileDTO;
import com.example.space_operators_java.services.ApiService;
import com.example.space_operators_java.services.GameService;
import com.example.space_operators_java.services.WebSocketService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.Objects;

public class SpaceOperatorsController {
    @FXML
    private TextField pseudoField;
    @FXML
    private Label uuidLabel;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView titleImage;
    @FXML
    private Button createGameButton;
    @FXML
    private Button joinGameButton;

    private ApiService apiService;
    private GameService gameService;
    private WebSocketService webSocketService;

    @FXML
    public void initialize() {
        // Initialize services
        apiService = ApiService.getInstance();
        gameService = GameService.getInstance();
        webSocketService = WebSocketService.getInstance();

        // Load background image
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_home.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }

        // Connect to WebSocket
        webSocketService.connect();

        // Load player profile and populate the pseudo field
        loadPlayerProfile();

        // Listen to pseudo field changes to update GameService
        pseudoField.textProperty().addListener((obs, old, newVal) -> {
            if (gameService.getCurrentPlayer() != null) {
                gameService.getCurrentPlayer().setName(newVal);
            }
        });
    }

    private void loadPlayerProfile() {
        if (!apiService.isAuthenticated()) {
            System.err.println("User not authenticated, redirecting to login");
            SceneNavigator.navigateTo("login-view.fxml");
            return;
        }

        // Show loading in UUID label
        if (uuidLabel != null) {
            uuidLabel.setText("Chargement du profil...");
        }

        // Disable create/join buttons while loading
        setButtonsEnabled(false);

        apiService.getPlayerProfile()
                .thenAccept(profile -> {
                    // Profile loaded successfully - run on JavaFX thread
                    Platform.runLater(() -> {
                        System.out.println("Profile loaded: " + profile.getPlayerId() + " (games: " + profile.getGamesPlayed() + ")");

                        // Set player name in the pseudo field (si disponible, sinon garder le champ vide)
                        if (pseudoField != null) {
                            if (profile.getPlayerName() != null && !profile.getPlayerName().trim().isEmpty()) {
                                pseudoField.setText(profile.getPlayerName());
                            } else {
                                // Le playerName n'est pas dans cette réponse, garder le champ vide pour que l'utilisateur le saisisse
                                pseudoField.setPromptText("Saisissez votre pseudo");
                            }
                        }

                        // Display player ID and games played in UUID label
                        if (uuidLabel != null) {
                            String displayText = "ID: " + profile.getPlayerId();
                            if (profile.getGamesPlayed() != null) {
                                displayText += " | Parties jouées: " + profile.getGamesPlayed();
                            }
                            uuidLabel.setText(displayText);
                        }

                        // Update GameService with current player info
                        if (gameService.getCurrentPlayer() != null) {
                            // Garder le nom existant si le profil n'en contient pas
                            if (profile.getPlayerName() != null && !profile.getPlayerName().trim().isEmpty()) {
                                gameService.getCurrentPlayer().setName(profile.getPlayerName());
                            }
                            gameService.getCurrentPlayer().setId(profile.getPlayerId());
                        }

                        // Re-enable buttons
                        setButtonsEnabled(true);
                    });
                })
                .exceptionally(throwable -> {
                    // Profile loading failed - run on JavaFX thread
                    Platform.runLater(() -> {
                        System.err.println("Failed to load profile: " + throwable.getMessage());

                        // Show error in UUID label
                        if (uuidLabel != null) {
                            uuidLabel.setText("Erreur de chargement du profil");
                        }

                        // Re-enable buttons
                        setButtonsEnabled(true);

                        // If authentication failed, redirect to login
                        if (throwable.getCause() != null && throwable.getCause().getMessage().contains("Not authenticated")) {
                            showAlert("Session expirée", "Votre session a expiré. Veuillez vous reconnecter.");
                            apiService.logout();
                            SceneNavigator.navigateTo("login-view.fxml");
                        } else {
                            // For other errors, show alert but allow continued use
                            showAlert("Erreur", "Impossible de charger le profil. Veuillez saisir votre pseudo manuellement.");
                        }
                    });
                    return null;
                });
    }

    private void setButtonsEnabled(boolean enabled) {
        if (createGameButton != null) createGameButton.setDisable(!enabled);
        if (joinGameButton != null) joinGameButton.setDisable(!enabled);
    }

    @FXML
    private void onCreateGameClick() {
        String playerName = pseudoField.getText().trim();

        if (playerName.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir votre pseudo.");
            return;
        }

        // Update player name in case it was changed
        if (gameService.getCurrentPlayer() != null) {
            gameService.getCurrentPlayer().setName(playerName);
        }

        // Disable button during game creation
        if (createGameButton != null) createGameButton.setDisable(true);
        if (uuidLabel != null) {
            uuidLabel.setText("Création de la partie...");
        }

        try {
            System.out.println("Creating game... (btn clicked)");
            gameService.createGame();

            // Navigate to session/lobby
            Platform.runLater(() -> {
                SceneNavigator.navigateTo("session-view.fxml");
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                if (createGameButton != null) createGameButton.setDisable(false);
                if (uuidLabel != null) {
                    uuidLabel.setText("Erreur lors de la création");
                }
                showAlert("Erreur", "Impossible de créer la partie: " + e.getMessage());
            });
        }
    }

    @FXML
    private void onJoinGameClick() {
        String playerName = pseudoField.getText().trim();

        if (playerName.isEmpty()) {
            showAlert("Erreur", "Veuillez saisir votre pseudo.");
            return;
        }

        // Update player name in case it was changed
        if (gameService.getCurrentPlayer() != null) {
            gameService.getCurrentPlayer().setName(playerName);
        }

        // Show join dialog
        joinDialog();
    }

    @FXML
    private void onHistoryClick() {
        System.out.println("History clicked - fonctionnalité à implémenter");
        showAlert("Information", "L'historique sera disponible dans une prochaine version.");
    }

    @FXML
    private void onQuitButtonClick() {
        // Confirm quit
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Quitter");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir quitter l'application ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Logout and exit
                apiService.logout();
                gameService.cleanGameState();

                // Exit application
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void joinDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Rejoindre une partie");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField gameIdField = new TextField();
        gameIdField.setPromptText("ID de la partie");

        grid.add(new Label("ID de la partie:"), 0, 0);
        grid.add(gameIdField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Enable/disable OK button based on input
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        gameIdField.textProperty().addListener((obs, old, newVal) -> {
            okButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return gameIdField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(gameId -> {
            if (gameId.isEmpty()) {
                showAlert("Erreur", "L'ID de la partie ne peut pas être vide.");
                return;
            }

            // Disable button during joining
            if (joinGameButton != null) joinGameButton.setDisable(true);
            if (uuidLabel != null) {
                uuidLabel.setText("Connexion à la partie...");
            }

            try {
                gameService.joinGame(gameId);

                // Navigate to session/lobby
                Platform.runLater(() -> {
                    SceneNavigator.navigateTo("session-view.fxml");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (joinGameButton != null) joinGameButton.setDisable(false);
                    if (uuidLabel != null) {
                        uuidLabel.setText("Erreur lors de la connexion");
                    }
                    showAlert("Erreur", "Impossible de rejoindre la partie: " + e.getMessage());
                });
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}