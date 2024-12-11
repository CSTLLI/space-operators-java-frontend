package com.example.space_operators_java.controllers;

import com.example.space_operators_java.services.GameService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class SpaceOperatorsGameController {
    @FXML
    private ImageView backgroundImage;

    private final String OPERATOR_BG = "/assets/game/bg_game_operator.png";
    private final String INSTRUCTOR_BG = "/assets/game/bg_game_instructor.png";
    private Image operatorBackground;
    private Image instructorBackground;

    private String currentRole; // "operator" ou "instructor"
    private final GameService gameService = GameService.getInstance();

    @FXML
    public void initialize() {
        // Charger les deux images en mémoire
        try {
            operatorBackground = new Image(Objects.requireNonNull(getClass().getResourceAsStream(OPERATOR_BG)));
            instructorBackground = new Image(Objects.requireNonNull(getClass().getResourceAsStream(INSTRUCTOR_BG)));

            // Image par défaut (opérateur ou instructeur selon le rôle initial)
            updateBackground();

            // Observer les changements de rôle
            gameService.roleProperty().addListener((obs, oldRole, newRole) -> {
                currentRole = newRole;
                updateBackground();
            });

        } catch (Exception e) {
            System.err.println("Erreur chargement images: " + e.getMessage());
        }
    }

    private void updateBackground() {
        Platform.runLater(() -> {
            if ("operator".equals(currentRole)) {
                backgroundImage.setImage(operatorBackground);
            } else {
                backgroundImage.setImage(instructorBackground);
            }
        });
    }

    public void onBackButtonClick() {
        SceneNavigator.navigateTo("home-view.fxml");
    }
}