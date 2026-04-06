package com.blackjackengine.ui;

import com.blackjackengine.GameStatistics;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class BlackjackApplication extends Application {
    private static final double SCENE_WIDTH = 1180;
    private static final double SCENE_HEIGHT = 860;
    private static final double MIN_WIDTH = 1040;
    private static final double MIN_HEIGHT = 780;
    private static final String STYLESHEET_PATH = "/ui/blackjack.css";

    private Stage stage;
    private Scene scene;
    private UiTheme currentTheme = UiTheme.CLASSIC_GREEN;
    private AppScreenController activeController;
    private int configuredStartingChips = 100;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(new VBox(), SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add(
            BlackjackApplication.class.getResource(STYLESHEET_PATH).toExternalForm()
        );

        stage.setTitle("Blackjack Engine");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        showStartScreen();
        stage.show();
    }

    public void showStartScreen() {
        switchView("/ui/start-screen.fxml");
    }

    public void showGameScreen() {
        switchView("/ui/blackjack-table.fxml");
    }

    public void startGameSession(int startingChips) {
        configuredStartingChips = startingChips;
        showGameScreen();
    }

    public void cycleTheme() {
        currentTheme = currentTheme.next();
        applyTheme(scene.getRoot());

        if (activeController != null) {
            activeController.onThemeChanged(currentTheme);
        }
    }

    public UiTheme getCurrentTheme() {
        return currentTheme;
    }

    public void setTheme(UiTheme theme) {
        if (theme == null) {
            return;
        }

        currentTheme = theme;
        if (scene.getRoot() != null) {
            applyTheme(scene.getRoot());
        }

        if (activeController != null) {
            activeController.onThemeChanged(currentTheme);
        }
    }

    public int getConfiguredStartingChips() {
        return configuredStartingChips;
    }

    public void showStatisticsDialog() {
        GameStatistics statistics = new GameStatistics();

        VBox root = new VBox(16);
        root.getStyleClass().addAll("table-root", "stats-modal-root", currentTheme.getStyleClass());
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Session Statistics");
        title.getStyleClass().add("stats-modal-title");

        Label subtitle = new Label(
            "A fresh table session begins when you press Start Game. Live wins, losses, pushes, "
                + "and recommendation analytics continue on the main table screen."
        );
        subtitle.getStyleClass().add("stats-modal-subtitle");
        subtitle.setWrapText(true);

        HBox topRow = new HBox(12);
        topRow.getChildren().addAll(
            createStatCard("Rounds", String.valueOf(statistics.getTotalRoundsPlayed())),
            createStatCard("Wins", String.valueOf(statistics.getWins())),
            createStatCard("Losses", String.valueOf(statistics.getLosses()))
        );

        HBox bottomRow = new HBox(12);
        bottomRow.getChildren().addAll(
            createStatCard("Pushes", String.valueOf(statistics.getPushes())),
            createStatCard("Blackjacks", String.valueOf(statistics.getBlackjacks())),
            createStatCard("Theme", currentTheme.getDisplayName())
        );

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("menu-button", "secondary-menu-button");

        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Blackjack Statistics");

        closeButton.setOnAction(event -> dialog.close());

        root.getChildren().addAll(title, subtitle, topRow, bottomRow, closeButton);

        Scene dialogScene = new Scene(root, 520, 360);
        dialogScene.getStylesheets().add(
            BlackjackApplication.class.getResource(STYLESHEET_PATH).toExternalForm()
        );
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public void exitApplication() {
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void switchView(String fxmlPath) {
        try {
            if (activeController != null) {
                activeController.onViewDeactivated();
            }

            FXMLLoader loader = new FXMLLoader(BlackjackApplication.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof AppScreenController) {
                activeController = (AppScreenController) controller;
                activeController.setApplication(this);
            } else {
                activeController = null;
            }

            applyTheme(root);
            root.setOpacity(0.0);
            scene.setRoot(root);

            if (activeController != null) {
                activeController.onThemeChanged(currentTheme);
                activeController.onViewActivated();
            }

            FadeTransition fadeIn = new FadeTransition(Duration.millis(420), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view: " + fxmlPath, exception);
        }
    }

    private void applyTheme(Parent root) {
        root.getStyleClass().removeAll(
            UiTheme.CLASSIC_GREEN.getStyleClass(),
            UiTheme.LUXURY_GOLD.getStyleClass(),
            UiTheme.NEON_CASINO.getStyleClass()
        );
        root.getStyleClass().add(currentTheme.getStyleClass());
    }

    private VBox createStatCard(String labelText, String valueText) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stats-modal-card");
        card.setPadding(new Insets(16));
        card.setPrefWidth(148);
        card.setAlignment(Pos.TOP_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("stats-modal-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("stats-modal-value");
        value.setWrapText(true);

        Region spacer = new Region();
        spacer.setMinHeight(8);

        card.getChildren().addAll(label, spacer, value);
        return card;
    }
}
