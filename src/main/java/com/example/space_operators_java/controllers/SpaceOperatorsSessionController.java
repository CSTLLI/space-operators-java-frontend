package com.example.space_operators_java.controllers;

import com.example.space_operators_java.models.Player;
import com.example.space_operators_java.services.GameService;
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
    private ImageView backgroundImage;

    @FXML
    private Button readyBtn;

    @FXML
    private VBox playersContainer;

    private final GameService gameService = GameService.getInstance();

    @FXML
    public void initialize() {
        // Charger les images
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_session.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }

        // Récupérer les players
        gameService.getPlayers().addListener((ListChangeListener<Player>) c -> {
            System.out.println("Players list changed"); // Debug
            playersContainer.getChildren().clear();
            gameService.getPlayers().forEach(player -> {
                System.out.println("Adding updated player to view: " + player.getId()); // Debug
                HBox playerRow = createPlayerRow(player);
                playersContainer.getChildren().add(playerRow);
            });
        });

        // Initial population
        gameService.getPlayers().forEach(player -> {
            System.out.println("Adding player to view: " + player.getName()); // Debug
            HBox playerRow = createPlayerRow(player);
            playersContainer.getChildren().add(playerRow);
        });
    }

    private HBox createPlayerRow(Player player) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        row.setSpacing(15);

        // Nom du joueur
        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("player-name");

        // Indicateur de statut (cercle)
        Circle statusIndicator = new Circle(8);
        statusIndicator.getStyleClass().add("status-indicator");
        updateStatusIndicator(statusIndicator, player.isReady());

        // Créer un binding pour mettre à jour automatiquement l'indicateur
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
        SceneNavigator.navigateTo("home-view.fxml");
    }

    public void onStartGameButtonClick () {
    }

    public void onReadyButtonClick () {
        gameService.getCurrentPlayer().setReady(!gameService.getCurrentPlayer().isReady());
        readyBtn.setText(gameService.getCurrentPlayer().isReady() ? "Prêt" : "Pas Prêt");
    }
}