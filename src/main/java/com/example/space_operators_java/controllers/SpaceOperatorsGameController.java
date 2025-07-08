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

    // Éléments FXML
    @FXML private ImageView backgroundImage;
    @FXML private Label turnNumber;
    @FXML private Label operatorId;
    @FXML private ProgressBar shipIntegrity;
    @FXML private ProgressBar timeRemaining;
    @FXML private Label taskDescription;
    @FXML private Label sectionTitle;         // Nouveau : titre de la section instructions
    @FXML private Label elementsTitle;        // Nouveau : titre de la section éléments
    @FXML private HBox elementsContainer;
    @FXML private VBox instructionsSection;  // Section pour les instructions
    @FXML private VBox elementsSection;      // Section pour les éléments de contrôle

    // Variables de classe
    private Dialog<ButtonType> endGameDialog;
    private Image operatorBackground;
    private Image instructorBackground;
    private Timeline countdownTimeline;
    private final GameService gameService = GameService.getInstance();

    // Constantes pour les chemins des images
    private final String OPERATOR_BG = "/assets/game/bg_game_operator.png";
    private final String INSTRUCTOR_BG = "/assets/game/bg_game_instructor_default.png";

// Dans votre SpaceOperatorsGameController, améliorez la méthode initialize

    @FXML
    public void initialize() {
        System.out.println("GameController initialized");

        // Vérifications de sécurité pour éviter les NullPointerException
        checkFXMLElements();

        // Charger les images de fond
        loadBackgroundImages();

        // Configurer les barres de progression
        configureProgressBars();

        // Créer le dialog de fin de jeu
        createEndGameDialog();

        // Écouter les événements de fin de jeu
        gameService.gameEndedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(this::showEndGameDialog);
            }
        });

        // Observer les changements d'opération AVEC DEBUG DU RÔLE
        gameService.currentOperationProperty().addListener((obs, oldOp, newOp) -> {
            if (newOp != null) {
                System.out.println("🔄 Nouvelle opération reçue:");
                System.out.println("   - Rôle: '" + newOp.getRole() + "'");
                System.out.println("   - ID: " + newOp.getId());
                System.out.println("   - Description: " + newOp.getDescription());

                Platform.runLater(() -> {
                    System.out.println("🎯 Traitement de la nouvelle opération dans le thread JavaFX");
                    handleNewOperation(newOp);
                });
            } else {
                System.out.println("❌ Opération reçue est null");
            }
        });

        // Observer l'intégrité du vaisseau
        gameService.shipIntegrityProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> updateShipIntegrity(newVal.doubleValue()));
        });

        // Vérifier s'il y a déjà une opération en cours
        Operation currentOp = gameService.getCurrentOperation();
        if (currentOp != null) {
            System.out.println("🔄 Opération en cours détectée à l'initialisation:");
            System.out.println("   - Rôle: '" + currentOp.getRole() + "'");
            handleNewOperation(currentOp);
        } else {
            // Initialiser l'interface par défaut
            initializeDefaultInterface();
        }
    }

    private void checkFXMLElements() {
        if (taskDescription == null) System.err.println("ERROR: taskDescription is null");
        if (turnNumber == null) System.err.println("ERROR: turnNumber is null");
        if (operatorId == null) System.err.println("ERROR: operatorId is null");
        if (elementsContainer == null) System.err.println("ERROR: elementsContainer is null");
        if (sectionTitle == null) System.err.println("ERROR: sectionTitle is null");
        if (elementsTitle == null) System.err.println("ERROR: elementsTitle is null");
        if (backgroundImage == null) System.err.println("ERROR: backgroundImage is null");
    }

    private void loadBackgroundImages() {
        try {
            System.out.println("Chargement des images de fond...");

            operatorBackground = new Image(Objects.requireNonNull(getClass().getResourceAsStream(OPERATOR_BG)));
            System.out.println("✅ Image opérateur chargée: " + operatorBackground.getWidth() + "x" + operatorBackground.getHeight());

            instructorBackground = new Image(Objects.requireNonNull(getClass().getResourceAsStream(INSTRUCTOR_BG)));
            System.out.println("✅ Image instructeur chargée: " + instructorBackground.getWidth() + "x" + instructorBackground.getHeight());

            if (backgroundImage != null) {
                backgroundImage.setImage(operatorBackground); // Image par défaut
                System.out.println("✅ Image par défaut (opérateur) appliquée");
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureProgressBars() {
        if (shipIntegrity != null) {
            shipIntegrity.setStyle("-fx-accent: #00ff00; -fx-control-inner-background: #333333;");
            shipIntegrity.setProgress(1.0);
        }
        if (timeRemaining != null) {
            timeRemaining.setStyle("-fx-accent: #0099ff; -fx-control-inner-background: #333333;");
            timeRemaining.setProgress(1.0);
        }
    }

    private void initializeDefaultInterface() {
        // Masquer les deux sections par défaut
        if (instructionsSection != null) {
            instructionsSection.setVisible(false);
            instructionsSection.setManaged(false);
            System.out.println("✅ Section INSTRUCTIONS masquée par défaut");
        }

        if (elementsSection != null) {
            elementsSection.setVisible(false);
            elementsSection.setManaged(false);
            System.out.println("✅ Section ÉLÉMENTS masquée par défaut");
        }

        // Initialiser les valeurs par défaut
        if (turnNumber != null) {
            turnNumber.setText("Tour 0");
        }
        if (operatorId != null) {
            operatorId.setText("En attente...");
        }
        if (sectionTitle != null) {
            sectionTitle.setText("Instructions");
        }
        if (elementsTitle != null) {
            elementsTitle.setText("Éléments de contrôle");
        }
        if (taskDescription != null) {
            taskDescription.setText("En attente d'une opération...");
        }
    }

    private void handleNewOperation(Operation operation) {
        System.out.println("Handling new operation: " + operation.getRole() + " - " + operation.getDescription());

        // Mettre à jour le numéro de tour
        if (turnNumber != null) {
            turnNumber.setText("Tour " + operation.getTurn());
        }

        // Démarrer le compte à rebours
        startCountdown(operation.getDuration());

        // Vider le conteneur d'éléments AVANT de traiter le rôle
        if (elementsContainer != null) {
            elementsContainer.getChildren().clear();
        }

        // Adapter l'interface selon le rôle
        if ("operator".equals(operation.getRole())) {
            handleOperatorMode(operation);
        } else if ("inspector".equals(operation.getRole())) {
            handleInstructorMode(operation);
        } else {
            System.err.println("Rôle non reconnu: " + operation.getRole());
            // Mode par défaut
            handleDefaultMode(operation);
        }
    }

    private void handleOperatorMode(Operation operation) {
        System.out.println("=== MODE OPÉRATEUR ===");

        // Changer le fond vers l'image opérateur
        if (backgroundImage != null && operatorBackground != null) {
            backgroundImage.setImage(operatorBackground);
            System.out.println("✅ Image opérateur appliquée");
        }

        // Afficher l'ID de l'opérateur
        if (operatorId != null) {
            operatorId.setText("Votre ID: " + operation.getId());
        }

        // MASQUER COMPLÈTEMENT la section INSTRUCTIONS (important : toujours masquer d'abord)
        if (instructionsSection != null) {
            instructionsSection.setVisible(false);
            instructionsSection.setManaged(false);
            System.out.println("✅ Section INSTRUCTIONS masquée pour l'opérateur");
        }

        // AFFICHER la section ÉLÉMENTS
        if (elementsSection != null) {
            elementsSection.setVisible(true);
            elementsSection.setManaged(true);
            System.out.println("✅ Section ÉLÉMENTS affichée pour l'opérateur");
        }

        // Mettre à jour le titre des éléments
        if (elementsTitle != null) {
            elementsTitle.setText("Éléments de contrôle");
        }

        // Vider et remplir le conteneur d'éléments avec les éléments INTERACTIFS
        if (elementsContainer != null) {
            elementsContainer.getChildren().clear();

            if (operation.getElements() != null && !operation.getElements().isEmpty()) {
                System.out.println("Création de " + operation.getElements().size() + " éléments interactifs");
                operation.getElements().forEach(element -> {
                    try {
                        Node node = createInteractiveElement(element);
                        if (node != null) {
                            elementsContainer.getChildren().add(node);
                            System.out.println("✅ Élément interactif ajouté: " + element.getId());
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Erreur création élément interactif " + element.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("Aucun élément à afficher pour l'opérateur");
            }
        }
    }

    private void handleInstructorMode(Operation operation) {
        System.out.println("=== MODE INSTRUCTEUR/INSPECTOR ===");

        // Changer le fond vers l'image instructeur
        if (backgroundImage != null && instructorBackground != null) {
            backgroundImage.setImage(instructorBackground);
            System.out.println("✅ Image instructeur appliquée");
        }

        // Afficher l'ID de l'opérateur à contacter
        if (operatorId != null) {
            operatorId.setText(" contacter: " + operation.getOperationId());
        }

        // MASQUER COMPLÈTEMENT la section ÉLÉMENTS (important : toujours masquer d'abord)
        if (elementsSection != null) {
            elementsSection.setVisible(false);
            elementsSection.setManaged(false);
            System.out.println("✅ Section ÉLÉMENTS masquée pour l'instructeur");
        }

        // AFFICHER la section INSTRUCTIONS
        if (instructionsSection != null) {
            instructionsSection.setVisible(true);
            instructionsSection.setManaged(true);
            System.out.println("✅ Section INSTRUCTIONS affichée pour l'instructeur");
        }

        // Mettre à jour le contenu des instructions
        if (sectionTitle != null) {
            sectionTitle.setText("Instructions");
        }
        if (taskDescription != null) {
            String description = operation.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                taskDescription.setText(description);
                System.out.println("✅ Description mise à jour: " + description);
            } else {
                taskDescription.setText("Aucune instruction disponible");
                System.out.println("⚠️ Description vide ou null");
            }
        }

        System.out.println("Mode instructeur configuré - affichage des instructions uniquement");
    }

    private void handleDefaultMode(Operation operation) {
        System.out.println("=== MODE PAR DÉFAUT ===");

        // Image par défaut (opérateur)
        if (backgroundImage != null && operatorBackground != null) {
            backgroundImage.setImage(operatorBackground);
        }

        if (operatorId != null) {
            operatorId.setText("ID: " + operation.getId());
        }

        if (taskDescription != null) {
            taskDescription.setText("Mode non défini - " + operation.getDescription());
        }

        // Pas d'éléments en mode par défaut
    }

    private Node createInteractiveElement(Element element) {
        return switch (element.getType()) {
            case "button" -> createButton(element, true);
            case "switch" -> createSwitch(element, true);
            default -> {
                System.err.println("Type d'élément non supporté: " + element.getType());
                yield null;
            }
        };
    }

    private Node createReferenceElement(Element element) {
        return switch (element.getType()) {
            case "button" -> createReferenceButton(element);
            case "switch" -> createReferenceSwitch(element);
            default -> {
                System.err.println("Type d'élément non supporté: " + element.getType());
                yield null;
            }
        };
    }

    private Button createReferenceButton(Element element) {
        Button button = new Button();
        button.setPrefWidth(150);
        button.setPrefHeight(50);

        // Gestion robuste des différents types de valeurs pour les boutons de référence
        if ("color".equals(element.getValueType()) && element.getValue() != null) {
            // Bouton coloré de référence
            String color = element.getValue().toString();
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #FFD700; -fx-border-width: 3px;");

            // Afficher un nom plus descriptif selon la couleur
            String colorName = getColorName(color);
            button.setText("RÉFÉRENCE\n" + colorName + " (" + element.getId() + ")");

            System.out.println("Bouton de référence créé - Couleur: " + color + " (" + colorName + "), ID: " + element.getId());
        } else if (element.getValue() != null) {
            // Bouton avec texte de référence
            button.setText("RÉFÉRENCE\n" + element.getValue().toString());
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #FFD700; -fx-border-width: 3px;");
        } else {
            // Valeur par défaut de référence
            button.setText("RÉFÉRENCE\nBouton " + element.getId());
            button.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #FFD700; -fx-border-width: 3px;");
        }

        // Les boutons de référence ne sont pas interactifs
        button.setDisable(true);
        button.setStyle(button.getStyle() + "; -fx-opacity: 0.8;");

        return button;
    }

    private Node createReferenceSwitch(Element element) {
        VBox switchContainer = new VBox(5);
        switchContainer.setAlignment(Pos.CENTER);

        Label label = new Label("RÉFÉRENCE - Switch " + element.getId());
        label.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");

        ToggleButton toggle = new ToggleButton();
        toggle.setPrefWidth(100);
        toggle.setPrefHeight(30);

        // Gestion robuste des différents types de valeurs
        boolean isOn = false;
        String displayText = "OFF";

        if (element.getValue() != null) {
            if (element.getValue() instanceof Number) {
                int value = ((Number) element.getValue()).intValue();
                isOn = value == 1;
                displayText = isOn ? "ON" : "OFF";
            } else if (element.getValue() instanceof String) {
                String strValue = (String) element.getValue();
                try {
                    int value = Integer.parseInt(strValue);
                    isOn = value == 1;
                    displayText = isOn ? "ON" : "OFF";
                } catch (NumberFormatException e) {
                    // Si ce n'est pas un nombre, traiter comme boolean
                    isOn = "true".equalsIgnoreCase(strValue) || "on".equalsIgnoreCase(strValue) || "1".equals(strValue);
                    displayText = isOn ? "ON" : "OFF";
                }
            } else if (element.getValue() instanceof Boolean) {
                isOn = (Boolean) element.getValue();
                displayText = isOn ? "ON" : "OFF";
            }
        }

        toggle.setSelected(isOn);
        toggle.setText("REF: " + displayText);
        toggle.setStyle("-fx-border-color: #FFD700; -fx-border-width: 2px;");

        // Les switches de référence ne sont pas interactifs
        toggle.setDisable(true);

        switchContainer.getChildren().addAll(label, toggle);
        return switchContainer;
    }

    private Button createButton(Element element, boolean interactive) {
        Button button = new Button();
        button.setPrefWidth(150);
        button.setPrefHeight(50);

        // Gestion robuste des différents types de valeurs
        if ("color".equals(element.getValueType()) && element.getValue() != null) {
            // Bouton coloré
            String color = element.getValue().toString();
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
            button.setText("Bouton " + element.getId());
        } else if (element.getValue() != null) {
            // Bouton avec texte
            button.setText(element.getValue().toString());
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            // Valeur par défaut
            button.setText("Bouton " + element.getId());
            button.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        }

        if (interactive) {
            button.setOnAction(e -> {
                System.out.println("Button clicked: " + element.getId() + " (value: " + element.getValue() + ")");
                handleElementInteraction(element);
            });

            // Effet hover pour les boutons interactifs
            button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
            button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("; -fx-scale-x: 1.05; -fx-scale-y: 1.05;", "")));
        } else {
            button.setDisable(true);
            button.setStyle(button.getStyle() + "; -fx-opacity: 0.7;");
        }

        return button;
    }

    private Node createSwitch(Element element, boolean interactive) {
        VBox switchContainer = new VBox(5);
        switchContainer.setAlignment(Pos.CENTER);

        Label label = new Label("Switch " + element.getId());
        label.setStyle("-fx-text-fill: white;");

        ToggleButton toggle = new ToggleButton();
        toggle.setPrefWidth(100);
        toggle.setPrefHeight(30);

        // Gestion robuste des différents types de valeurs
        boolean isOn = false;
        String displayText = "OFF";

        if (element.getValue() != null) {
            if (element.getValue() instanceof Number) {
                int value = ((Number) element.getValue()).intValue();
                isOn = value == 1;
                displayText = isOn ? "ON" : "OFF";
            } else if (element.getValue() instanceof String) {
                String strValue = (String) element.getValue();
                try {
                    int value = Integer.parseInt(strValue);
                    isOn = value == 1;
                    displayText = isOn ? "ON" : "OFF";
                } catch (NumberFormatException e) {
                    // Si ce n'est pas un nombre, traiter comme boolean
                    isOn = "true".equalsIgnoreCase(strValue) || "on".equalsIgnoreCase(strValue) || "1".equals(strValue);
                    displayText = isOn ? "ON" : "OFF";
                }
            } else if (element.getValue() instanceof Boolean) {
                isOn = (Boolean) element.getValue();
                displayText = isOn ? "ON" : "OFF";
            }
        }

        toggle.setSelected(isOn);
        toggle.setText(displayText);

        if (interactive) {
            toggle.setOnAction(e -> {
                toggle.setText(toggle.isSelected() ? "ON" : "OFF");
                System.out.println("Switch toggled: " + element.getId() + " = " + toggle.isSelected());
                handleElementInteraction(element);
            });
        } else {
            toggle.setDisable(true);
        }

        switchContainer.getChildren().addAll(label, toggle);
        return switchContainer;
    }

    private void handleElementInteraction(Element element) {
        // Logique d'interaction avec un élément
        System.out.println("Element interaction: " + element.getType() + " " + element.getId());

        // Ici vous pouvez ajouter la logique pour:
        // - Enregistrer l'action de l'utilisateur
        // - Mettre à jour l'état du jeu
        // - Envoyer des données via WebSocket

        // Exemple: terminer l'opération après interaction
        handlePostOperation(element.getId());
    }

    private void createEndGameDialog() {
        endGameDialog = new Dialog<>();
        endGameDialog.setTitle("Fin de la partie");
        endGameDialog.setHeaderText(null);

        DialogPane dialogPane = endGameDialog.getDialogPane();
        dialogPane.getStyleClass().add("custom-dialog");
        dialogPane.setStyle("-fx-background-color: #2c3e50;");

        ButtonType returnButton = new ButtonType("Retour au menu", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().add(returnButton);

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

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label messageLabel = new Label("La partie est terminée!");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffffff;");

        Label scoreLabel = new Label("Nombre de tours complétés: " + gameService.getTurnsCompleted());
        scoreLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff;");

        content.getChildren().addAll(messageLabel, scoreLabel);
        endGameDialog.getDialogPane().setContent(content);
        endGameDialog.show();
    }

    private void updateShipIntegrity(double value) {
        if (shipIntegrity != null) {
            shipIntegrity.setProgress(value / 100.0);
            String color = value > 50 ? "#00ff00" : "#ff0000";
            shipIntegrity.setStyle("-fx-accent: " + color + "; -fx-control-inner-background: #333333;");
        }
    }

    private void startCountdown(int duration) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        if (timeRemaining != null) {
            timeRemaining.setProgress(1.0);

            Duration time = Duration.seconds(duration);
            countdownTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(timeRemaining.progressProperty(), 1.0)),
                    new KeyFrame(time, new KeyValue(timeRemaining.progressProperty(), 0))
            );

            // Optionnel: terminer automatiquement l'opération quand le timer arrive à 0
            // countdownTimeline.setOnFinished(event -> handlePostOperation());
            countdownTimeline.play();
        }
    }

    private void handlePostOperation(int elementId) {
        Operation currentOp = gameService.getCurrentOperation();
        System.out.println("Operation terminée: " + currentOp);

        if (currentOp != null && "operator".equals(currentOp.getRole())) {
            try {
                WebSocketService.getInstance().sendFinishOperation(
                        currentOp.getId(),
                        gameService.getGameId(),
                        elementId
                );
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du message de fin: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onBackButtonClick() {
        WebSocketService.getInstance().unsubscribeFromTopics();
        SceneNavigator.navigateTo("home-view.fxml");
    }

    /**
     * Convertit un code couleur hexadécimal en nom de couleur en français
     */
    private String getColorName(String hexColor) {
        if (hexColor == null) return "COULEUR";

        // Version simplifiée pour les couleurs les plus communes
        return switch (hexColor.toUpperCase()) {
            case "#FF0000" -> "ROUGE";
            case "#00FF00" -> "VERT";
            case "#0000FF" -> "BLEU";
            case "#FFFF00" -> "JAUNE";
            case "#FF00FF" -> "MAGENTA";
            case "#00FFFF" -> "CYAN";
            case "#FFA500" -> "ORANGE";
            case "#800080" -> "VIOLET";
            default -> "COULEUR ";
        };
    }
}