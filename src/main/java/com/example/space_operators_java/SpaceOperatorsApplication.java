package com.example.space_operators_java;

import com.example.space_operators_java.utils.SceneNavigator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;

public class SpaceOperatorsApplication extends Application {
    private static void loadCustomFont() {
        try (InputStream fontUrl = SpaceOperatorsApplication.class.getResourceAsStream("/assets/fonts/BowlbyOneSC-Regular.ttf")) {
            if (fontUrl != null) {
                Font.loadFont(fontUrl, 12);
            } else {
                System.err.println("Police personnalisée non trouvée");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la police : " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Debug: vérifions d'abord les chemins
            SceneNavigator.checkPath();

            // Configuration initiale
            stage.setTitle("Space Operators");
            stage.setResizable(false);

            // Configuration du navigateur
            SceneNavigator.setMainStage(stage);

            // Navigation vers la vue initiale
            SceneNavigator.navigateTo("home-view.fxml");

            // Récupération de la scène
            Scene scene = SceneNavigator.getCurrentScene();

            // Chargement de la police
            loadCustomFont();

            // Chargement du CSS
            if (scene != null) {
                String cssPath = "/styles/style.css";
                var cssUrl = getClass().getResource(cssPath);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("Fichier CSS non trouvé: " + cssPath);
                }
            }

            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur au démarrage de l'application");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}