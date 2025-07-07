package com.example.space_operators_java.controllers;

import com.example.space_operators_java.utils.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class SpaceOperatorsRegisterController {
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView titleImage;

    @FXML
    public void initialize() {
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
        System.out.println("Login... (btn clicked)");
    }

    @FXML
    private void onGoingToLoginPageClick() {
        System.out.println("Create account... (btn clicked)");
        SceneNavigator.navigateTo("login-view.fxml");
    }
}