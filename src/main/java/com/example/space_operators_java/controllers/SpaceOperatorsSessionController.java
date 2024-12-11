package com.example.space_operators_java.controllers;

import com.example.space_operators_java.models.Player;
import com.example.space_operators_java.services.GameService;
import com.example.space_operators_java.services.WebSocketService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Objects;

public class SpaceOperatorsSessionController {
    @FXML
    public Label gameId;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private Button startBtn;
    @FXML
    private Button readyBtn;
    @FXML
    private VBox playersContainer;
    private final GameService gameService = GameService.getInstance();

    @FXML
    public void initialize() {
        // Load background image
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_session.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }
        gameId.setText("ID de la partie: " + gameService.getGameId());

        // Event for players list changes
        gameService.getPlayers().addListener((ListChangeListener<Player>) c -> {
            System.out.println("Players list changed");
            playersContainer.getChildren().clear();
            gameService.getPlayers().forEach(player -> {
                HBox playerRow = createPlayerRow(player);
                playersContainer.getChildren().add(playerRow);
            });
        });

        // Initial display of players
        gameService.getPlayers().forEach(player -> {
            System.out.println("Adding player to view: " + player.getName());
            HBox playerRow = createPlayerRow(player);
            playersContainer.getChildren().add(playerRow);
        });

        // Disable startBtn if not host
        if (!gameService.getCurrentPlayer().isHost()) {
            startBtn.setVisible(false);
        }
    }

    private HBox createPlayerRow(Player player) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        row.setSpacing(15);

        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("player-name");

        Circle statusIndicator = new Circle(8);
        statusIndicator.getStyleClass().add("status-indicator");
        updateStatusIndicator(statusIndicator, player.isReady());

        player.readyProperty().addListener((obs, oldVal, newVal) ->
                updateStatusIndicator(statusIndicator, newVal));

        row.getChildren().addAll(nameLabel, statusIndicator);
        return row;
    }

    private void updateStatusIndicator(Circle indicator, boolean isReady) {
        if (isReady) {
            indicator.setFill(Color.GREEN);
            indicator.setStroke(Color.DARKGREEN);
        } else {
            indicator.setFill(Color.RED);
            indicator.setStroke(Color.DARKRED);
        }
    }

    public void onBackButtonClick() {
        WebSocketService.getInstance().unsubscribeFromTopics();
        SceneNavigator.navigateTo("home-view.fxml");
    }

    public void onStartGameButtonClick () {
        SceneNavigator.navigateTo("game-view.fxml");
    }

    public void onReadyButtonClick () {
        gameService.getCurrentPlayer().setReady(!gameService.getCurrentPlayer().isReady());
        readyBtn.setText(gameService.getCurrentPlayer().isReady() ? "Prêt" : "Pas Prêt");
    }
}