package com.example.space_operators_java.controllers;

import com.example.space_operators_java.models.Player;
import com.example.space_operators_java.services.ApiService;
import com.example.space_operators_java.services.GameService;
import com.example.space_operators_java.services.WebSocketService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
            System.out.println("Players: " + gameService.getPlayers());
            playersContainer.getChildren().clear();
            gameService.getPlayers().forEach(player -> {
                HBox playerRow = createPlayerRow(player);
                playersContainer.getChildren().add(playerRow);
            });
        });

        // Initial display of players
        gameService.getPlayers().forEach(player -> {
            HBox playerRow = createPlayerRow(player);
            playersContainer.getChildren().add(playerRow);
        });

        // Disable startBtn if not host
        if (!gameService.getCurrentPlayer().isHost()) {
            startBtn.setVisible(false);
        }
    }

    public void onBackButtonClick() {
        GameService.getInstance().disconnectGame();
        WebSocketService.getInstance().unsubscribeFromTopics();
        SceneNavigator.navigateTo("home-view.fxml");
    }

    public void onStartGameButtonClick () {
        SceneNavigator.navigateTo("game-view.fxml");
    }

    private HBox createPlayerRow(Player player) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        row.setSpacing(15);

        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("player-name");

        Circle statusIndicator = new Circle(8);
        if (player.getId() != null) {
            statusIndicator.setId("status-" + player.getId());
        }
        statusIndicator.getStyleClass().add("status-indicator");
        System.out.println("Player " + player.getName() + " is ready: " + player.isReady());
        if (player.isReady()) {
            statusIndicator.setFill(Color.GREEN);
            statusIndicator.setStroke(Color.DARKGREEN);
        } else {
            statusIndicator.setFill(Color.RED);
            statusIndicator.setStroke(Color.DARKRED);
        }

        row.getChildren().addAll(nameLabel, statusIndicator);
        return row;
    }

    public void onReadyButtonClick() {
        Player currentPlayer = gameService.getCurrentPlayer();
        boolean newReadyStatus = !currentPlayer.isReady();

        try {
            ApiService.getInstance().setPlayerReady(currentPlayer.getId(), newReadyStatus)
                    .thenAccept(success -> {
                        Platform.runLater(() -> {
                            currentPlayer.setReady(newReadyStatus);
                            readyBtn.setText(newReadyStatus ? "Prêt" : "Pas Prêt");
                        });
                    })
                    .exceptionally(throwable -> {
                        Platform.runLater(() -> {
                            System.err.println("Erreur lors de la mise à jour du statut: " + throwable.getMessage());
                            currentPlayer.setReady(!newReadyStatus);
                        });
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la demande de prêt: " + e.getMessage());
        }
    }
}