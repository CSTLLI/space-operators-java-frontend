package com.example.space_operators_java.controllers;

import com.example.space_operators_java.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class SpaceOperatorsHistoryController {
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView titleImage;

    @FXML
    public void initialize() {
        // Load background image
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/game/bg_game_instructor_default.png")));
            backgroundImage.setImage(background);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des images: " + e.getMessage());
        }
    }

    public void onBackButtonClick() {
        SceneNavigator.navigateTo("home-view.fxml");
    }
}