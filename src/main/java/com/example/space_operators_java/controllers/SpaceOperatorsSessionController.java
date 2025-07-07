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
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_session.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }

        gameId.setText("ID de la partie: " + gameService.getGameId());

        gameService.getCurrentPlayer().setReady(false);
        readyBtn.setText("Pas Prêt");

        gameService.getPlayers().addListener((ListChangeListener<Player>) c -> {
            System.out.println("LISTENER - Liste joueurs changée");
            Platform.runLater(() -> {
                System.out.println("REFRESH - Début rafraîchissement liste");
                playersContainer.getChildren().clear();
                gameService.getPlayers().forEach(player -> {
                    System.out.println("REFRESH - Joueur: " + player.getName() + ", Ready: " + player.isReady());
                    HBox playerRow = createPlayerRow(player);
                    playersContainer.getChildren().add(playerRow);
                });
                System.out.println("REFRESH - Fin rafraîchissement liste");
            });
        });

        gameService.getPlayers().forEach(player -> {
            HBox playerRow = createPlayerRow(player);
            playersContainer.getChildren().add(playerRow);
        });

        if (!gameService.getCurrentPlayer().isHost()) {
            startBtn.setVisible(false);
        }
    }

    public void onBackButtonClick() {
        GameService.getInstance().disconnectGame();
        WebSocketService.getInstance().disconnect();
        SceneNavigator.navigateTo("home-view.fxml");
    }

    public void onStartGameButtonClick() {
        try {
            WebSocketService.getInstance().sendStartRequest(gameService.getGameId());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la demande de demarrage: " + e.getMessage());
        }
    }

    private HBox createPlayerRow(Player player) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("player-name");

        Circle statusIndicator = new Circle(8);
        if (player.getId() != null) {
            statusIndicator.setId("status-" + player.getId());
        }
        statusIndicator.getStyleClass().add("status-indicator");

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

        WebSocketService webSocketService = WebSocketService.getInstance();
        if (webSocketService.getStompSession() == null || !webSocketService.getStompSession().isConnected()) {
            return;
        }

        System.out.println("AVANT API - Statut: " + currentPlayer.isReady());

        ApiService.getInstance().setPlayerReady(currentPlayer.getId(), newReadyStatus)
                .thenAccept(success -> {
                    System.out.println("REPONSE API - Success: " + success);
                    if (success) {
                        Platform.runLater(() -> {
                            currentPlayer.setReady(newReadyStatus);
                            readyBtn.setText(newReadyStatus ? "Prêt" : "Pas Prêt");

                            // FORCER le rafraîchissement car WebSocket ne fonctionne plus en session 2
                            playersContainer.getChildren().clear();
                            gameService.getPlayers().forEach(player -> {
                                HBox playerRow = createPlayerRow(player);
                                playersContainer.getChildren().add(playerRow);
                            });
                        });
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Erreur API: " + throwable.getMessage());
                    return null;
                });
    }
}