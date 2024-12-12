package com.example.space_operators_java.controllers;

import com.example.space_operators_java.models.Operation;
import com.example.space_operators_java.models.operation.Element;
import com.example.space_operators_java.services.GameService;
import com.example.space_operators_java.services.WebSocketService;
import com.example.space_operators_java.utils.SceneNavigator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

public class SpaceOperatorsGameController {
    @FXML private ImageView backgroundImage;
    @FXML private Label turnNumber;
    @FXML private Label operatorId;
    @FXML private ProgressBar shipIntegrity;
    @FXML private ProgressBar timeRemaining;
    @FXML private Label taskDescription;
    @FXML private HBox elementsContainer;

    private Dialog<ButtonType> endGameDialog;

    private Image operatorBackground;
    private Image instructorBackground;
    private final String OPERATOR_BG = "/assets/game/bg_game_operator.png";
    private final String INSTRUCTOR_BG = "/assets/game/bg_game_instructor.png";

    private Timeline countdownTimeline;
    private final GameService gameService = GameService.getInstance();

    @FXML
    public void initialize() {
        // Charger les images de fond
        try {
            operatorBackground = new Image(Objects.requireNonNull(getClass().getResourceAsStream(OPERATOR_BG)));
            instructorBackground = new Image(Objects.requireNonNull(getClass().getResourceAsStream(INSTRUCTOR_BG)));

            backgroundImage.setImage(operatorBackground);

            shipIntegrity.setStyle("-fx-accent: #00ff00; -fx-control-inner-background: #333333;");
            timeRemaining.setStyle("-fx-accent: #0099ff; -fx-control-inner-background: #333333;");

            createEndGameDialog();

            gameService.gameEndedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(this::showEndGameDialog);
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur chargement images: " + e.getMessage());
        }

        // Observer les changements d'opération
        gameService.currentOperationProperty().addListener((obs, oldOp, newOp) -> {
            if (newOp != null) {
                Platform.runLater(() -> handleNewOperation(newOp));
            }
        });

        // Observer l'intégrité du vaisseau
        gameService.shipIntegrityProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> updateShipIntegrity(newVal.doubleValue()));
        });

        // Initialiser le timer et l'intégrité
        timeRemaining.setProgress(1.0);
        shipIntegrity.setProgress(1.0);

        Operation currentOp = gameService.getCurrentOperation();
        if (currentOp != null) {
            handleNewOperation(currentOp);
        }
    }

    private void handleNewOperation(Operation operation) {
        turnNumber.setText("Tour " + operation.getTurn());

        startCountdown(operation.getDuration());

        elementsContainer.getChildren().clear();

        if ("operator".equals(operation.getRole())) {
            backgroundImage.setImage(operatorBackground);
            operatorId.setText("Votre ID: " + operation.getId());
//            taskDescription.setText("");

            if (operation.getElements() != null && !operation.getElements().isEmpty()) {
                operation.getElements().forEach(element -> {
                    Node node = createInteractiveElement(element);
                    if (node != null) {
                        elementsContainer.getChildren().add(node);
                    }
                });
            }
        } else {
            backgroundImage.setImage(instructorBackground);
            operatorId.setText("Opérateur à contacter: " + operation.getId());
            taskDescription.setText(operation.getDescription());
        }
    }

    private Node createInteractiveElement(Element element) {
        return switch (element.getType()) {
            case "button" -> createButton(element);
            default -> null;
        };
    }

    private Button createButton(Element element) {
        Button button = new Button();
        button.setPrefWidth(200);
        button.setPrefHeight(50);

        if ("color".equals(element.getValueType())) {
            button.setStyle("-fx-background-color: " + element.getValue());
        } else {
            button.setText(element.getValue().toString());
        }

        button.setOnAction(e -> {
            // Logique lors du clic
            System.out.println("Button clicked: " + element.getId());
        });

        return button;
    }

    private void createEndGameDialog() {
        endGameDialog = new Dialog<>();
        endGameDialog.setTitle("Fin de la partie");
        endGameDialog.setHeaderText(null);

        // Créer le contenu personnalisé
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label messageLabel = new Label("La partie est terminée!");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffffff;");

        Label scoreLabel = new Label("Nombre de tours complétés: " + gameService.getTurnsCompleted());
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");

        content.getChildren().addAll(messageLabel, scoreLabel);

        // Personnaliser le style
        DialogPane dialogPane = endGameDialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setStyle("-fx-background-color: #2c3e50;");

        // Ajouter le bouton retour
        ButtonType returnButton = new ButtonType("Retour au menu", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(returnButton);

        // Gérer le clic sur le bouton
        endGameDialog.setResultConverter(buttonType -> {
            if (buttonType == returnButton) {
                onBackButtonClick();
            }
            return null;
        });
    }

    private void showEndGameDialog() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        endGameDialog.show();
    }

    private void updateShipIntegrity(double value) {
        shipIntegrity.setProgress(value / 100.0);
        // Changer la couleur en fonction de la valeur
        String color = value > 50 ? "#00ff00" : "#ff0000";
        shipIntegrity.setStyle("-fx-accent: " + color + "; -fx-control-inner-background: #333333;");
    }

    private void startCountdown(int duration) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        duration = 10;

        timeRemaining.setProgress(1.0);

        Duration time = Duration.seconds(duration);
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timeRemaining.progressProperty(), 1.0)),
                new KeyFrame(time, new KeyValue(timeRemaining.progressProperty(), 0))
        );

        countdownTimeline.setOnFinished(event -> handleTimeOut());
        countdownTimeline.play();
    }

    private void handleTimeOut() {
        Operation currentOp = gameService.getCurrentOperation();
        if (currentOp != null && "operator".equals(currentOp.getRole())) {
            try {
                WebSocketService.getInstance().sendFinishOperation(
                        currentOp.getId(),
                        false  // échec
                );
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du message de fin: " + e.getMessage());
            }
        }
    }

    public void onBackButtonClick() {
        WebSocketService.getInstance().unsubscribeFromTopics();
        SceneNavigator.navigateTo("home-view.fxml");
    }
}