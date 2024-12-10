package com.example.space_operators_java.controllers;

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

    private String playerId;

    private final GameService gameService = GameService.getInstance();
    private final WebSocketService webSocketService = WebSocketService.getInstance();

//    private WebSocketClient webSocket;

    @FXML
    public void initialize() {

        // Générer un UUID pour le joueur
        playerId = gameService.getCurrentPlayer().getId();
        uuidLabel.setText("ID: " + playerId);
        pseudoField.textProperty().addListener((obs, old, newVal) ->
                gameService.getCurrentPlayer().setName(newVal)
        );

        // Charger les images
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
        try {
//            String gameId = ApiClient.createGame();
//            gameState.setGameId(gameId);
//            gameState.setIsHost(true);
//
//            // Envoyer la requête de connexion via WebSocket
//            JSONObject connectData = new JSONObject();
//            connectData.put("gameId", gameId);
//            connectData.put("playerId", playerId);
//            connectData.put("playerName", pseudoField.getText());
//
//            webSocket.send("connect", connectData);
//
//            // Naviguer vers l'écran de création
//            Navigator.navigateTo("create-game.fxml");
        } catch (Exception e) {
            showError("Erreur lors de la création de la partie", e.getMessage());
        }
    }

    @FXML
    private void onJoinGameClick() {
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
//                gameState.setGameId(gameId);
//                gameState.setIsHost(false);
//
//                JSONObject connectData = new JSONObject();
//                connectData.put("gameId", gameId);
//                connectData.put("playerId", playerId);
//                connectData.put("playerName", pseudoField.getText());
//
//                webSocket.send("connect", connectData);
            } catch (Exception e) {
                showError("Erreur lors de la connexion", e.getMessage());
            }
        });
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
}