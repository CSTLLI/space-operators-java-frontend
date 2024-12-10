package com.example.space_operators_java.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;

public class SceneNavigator {
    private static Stage mainStage;
    private static Scene currentScene;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static Scene getCurrentScene() {
        return currentScene;
    }

    public static void checkPath() {
        System.out.println(SceneNavigator.class.getResource("/com/example/space_operators_java/"));
    }

    public static void navigateTo(String path) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(SceneNavigator.class.getResource("/com/example/space_operators_java/" + path));
            Parent root = fxmlLoader.load();

            if (currentScene == null) {
                currentScene = new Scene(root);
            } else {
                currentScene.setRoot(root);
            }

            mainStage.setWidth(460);
            mainStage.setHeight(720);

            mainStage.setScene(currentScene);
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la navigation vers " + path);
        }
    }
}
