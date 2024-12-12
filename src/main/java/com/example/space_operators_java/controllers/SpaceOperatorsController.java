package com.example.space_operators_java.controllers;

import com.example.space_operators_java.services.ApiService;
import com.example.space_operators_java.services.GameService;
import com.example.space_operators_java.services.WebSocketService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.util.Objects;
import java.util.UUID;

public class SpaceOperatorsController {
    @FXML
    private TextField pseudoField;
    @FXML
    private Label uuidLabel;
    @FXML
    private VBox mainContainer;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView titleImage;

    private final GameService gameService = GameService.getInstance();
    private final WebSocketService webSocketService = WebSocketService.getInstance();
    private final ApiService apiService = ApiService.getInstance();

    @FXML
    public void initialize() {
        webSocketService.connect();

        // Generate a UUID for the player
        uuidLabel.setText("ID: " + gameService.getCurrentPlayer().getId());

        pseudoField.textProperty().addListener((obs, old, newVal) ->
                gameService.getCurrentPlayer().setName(newVal)
        );

        // Load background image
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_home.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }
    }

    @FXML
    private void onCreateGameClick() {
        gameService.createGame();
        SceneNavigator.navigateTo("session-view.fxml");
    }

    @FXML
    private void onJoinGameClick() {
        joinDialog();
    }

    @FXML
    private void onHistoryClick() {
        SceneNavigator.navigateTo("history-view.fxml");
    }

    @FXML
    private void onQuitButtonClick() {
        System.exit(0);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void joinDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Rejoindre une partie");

        GridPane grid = new GridPane();
        TextField gameIdField = new TextField();
        grid.add(new Label("ID de la partie:"), 0, 0);
        grid.add(gameIdField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return gameIdField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(gameId -> {
            try {
                gameService.joinGame(gameId);
                SceneNavigator.navigateTo("session-view.fxml");
            } catch (Exception e) {
                showError("Erreur lors de la connexion", e.getMessage());
            }
        });
    }
}