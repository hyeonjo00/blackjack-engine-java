package com.blackjackengine.ui;

import com.blackjackengine.Card;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class StartScreenController implements AppScreenController {
    private static final Duration MENU_HOVER_DURATION = Duration.millis(160);
    private static final Duration AMBIENT_CARD_FLIP_HALF = Duration.millis(460);
    private static final Duration AMBIENT_CARD_FACE_DELAY = Duration.seconds(3.0);
    private static final Duration AMBIENT_CARD_BACK_DELAY = Duration.seconds(1.7);
    private static final Duration AMBIENT_CHIP_GLOW_DURATION = Duration.seconds(2.6);

    @FXML
    private StackPane ambientCardPane;

    @FXML
    private ImageView ambientCardImage;

    @FXML
    private StackPane ambientChipPane;

    @FXML
    private Region ambientChipShine;

    @FXML
    private Button startGameButton;

    @FXML
    private TextField startingChipsField;

    @FXML
    private Label startErrorLabel;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button themeButton;

    @FXML
    private Button exitButton;

    private final Card ambientCard = new Card(Card.Suit.SPADES, Card.Rank.ACE);
    private BlackjackApplication application;
    private CardImageMapper cardImageMapper;
    private Image ambientCardFrontImage;
    private Image ambientCardBackImage;
    private Animation ambientCardAnimation;
    private Animation ambientChipAnimation;
    private boolean showingCardFace;

    @FXML
    private void initialize() {
        cardImageMapper = new CardImageMapper();
        preloadAmbientCardImages();
        registerMenuButtonAnimations(startGameButton);
        registerMenuButtonAnimations(statisticsButton);
        registerMenuButtonAnimations(themeButton);
        registerMenuButtonAnimations(exitButton);
        startingChipsField.textProperty().addListener((observable, oldValue, newValue) -> clearStartError());
    }

    @Override
    public void setApplication(BlackjackApplication application) {
        this.application = application;
    }

    @Override
    public void onThemeChanged(UiTheme theme) {
        themeButton.setText("Theme: " + theme.getDisplayName());
    }

    @Override
    public void onViewActivated() {
        startAmbientCardLoop();
        startAmbientChipGlow();
        if (application != null) {
            startingChipsField.setText(String.valueOf(application.getConfiguredStartingChips()));
        }
        clearStartError();
    }

    @Override
    public void onViewDeactivated() {
        stopAnimation(ambientCardAnimation);
        stopAnimation(ambientChipAnimation);
        ambientCardAnimation = null;
        ambientChipAnimation = null;
    }

    @FXML
    private void onStartGame() {
        if (application != null) {
            Integer startingChips = readStartingChips();
            if (startingChips == null) {
                return;
            }
            application.startGameSession(startingChips);
        }
    }

    @FXML
    private void onStatistics() {
        if (application != null) {
            application.showStatisticsDialog();
        }
    }

    @FXML
    private void onTheme() {
        if (application != null) {
            application.cycleTheme();
        }
    }

    @FXML
    private void onExit() {
        if (application != null) {
            application.exitApplication();
        }
    }

    private void preloadAmbientCardImages() {
        ambientCardImage.setPreserveRatio(true);
        ambientCardImage.setFitWidth(112);
        ambientCardImage.setFitHeight(156);

        cardImageMapper.loadCardImage(ambientCard, true, image -> {
            ambientCardBackImage = image;
            if (!showingCardFace) {
                ambientCardImage.setImage(image);
            }
        });

        cardImageMapper.loadCardImage(ambientCard, false, image -> ambientCardFrontImage = image);
    }

    private void registerMenuButtonAnimations(Button button) {
        button.setOnMouseEntered(event -> animateMenuButton(button, 1.03));
        button.setOnMouseExited(event -> animateMenuButton(button, 1.0));
        button.setOnMousePressed(event -> animateMenuButton(button, 0.985));
        button.setOnMouseReleased(event -> animateMenuButton(button, button.isHover() ? 1.03 : 1.0));
    }

    private void animateMenuButton(Button button, double targetScale) {
        ScaleTransition scale = new ScaleTransition(MENU_HOVER_DURATION, button);
        scale.setToX(targetScale);
        scale.setToY(targetScale);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        scale.play();
    }

    private void startAmbientCardLoop() {
        showingCardFace = false;
        if (ambientCardBackImage != null) {
            ambientCardImage.setImage(ambientCardBackImage);
        }
        ambientCardPane.setScaleX(1.0);
        scheduleNextCardFlip(AMBIENT_CARD_BACK_DELAY);
    }

    private void scheduleNextCardFlip(Duration delay) {
        stopAnimation(ambientCardAnimation);

        PauseTransition pause = new PauseTransition(delay);
        pause.setOnFinished(event -> playAmbientCardFlip());
        ambientCardAnimation = pause;
        pause.play();
    }

    private void playAmbientCardFlip() {
        boolean revealFace = !showingCardFace;

        ScaleTransition collapse = new ScaleTransition(AMBIENT_CARD_FLIP_HALF, ambientCardPane);
        collapse.setFromX(1.0);
        collapse.setToX(0.0);
        collapse.setInterpolator(Interpolator.EASE_BOTH);
        collapse.setOnFinished(event -> swapAmbientCardImage(revealFace));

        ScaleTransition expand = new ScaleTransition(AMBIENT_CARD_FLIP_HALF, ambientCardPane);
        expand.setFromX(0.0);
        expand.setToX(1.0);
        expand.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition flip = new SequentialTransition(collapse, expand);
        flip.setOnFinished(event -> {
            showingCardFace = revealFace;
            scheduleNextCardFlip(showingCardFace ? AMBIENT_CARD_FACE_DELAY : AMBIENT_CARD_BACK_DELAY);
        });

        ambientCardAnimation = flip;
        flip.play();
    }

    private void swapAmbientCardImage(boolean revealFace) {
        Image targetImage = revealFace ? ambientCardFrontImage : ambientCardBackImage;
        if (targetImage != null) {
            ambientCardImage.setImage(targetImage);
        }
    }

    private void startAmbientChipGlow() {
        stopAnimation(ambientChipAnimation);

        FadeTransition shine = new FadeTransition(AMBIENT_CHIP_GLOW_DURATION, ambientChipShine);
        shine.setFromValue(0.18);
        shine.setToValue(0.74);
        shine.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition pulse = new ScaleTransition(AMBIENT_CHIP_GLOW_DURATION, ambientChipPane);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition glow = new ParallelTransition(shine, pulse);
        glow.setCycleCount(Animation.INDEFINITE);
        glow.setAutoReverse(true);

        ambientChipAnimation = glow;
        glow.play();
    }

    private void stopAnimation(Animation animation) {
        if (animation != null) {
            animation.stop();
        }
    }

    private Integer readStartingChips() {
        String rawText = startingChipsField.getText();

        try {
            int startingChips = Integer.parseInt(rawText == null ? "" : rawText.trim());
            if (startingChips < 1) {
                showStartError("Choose at least 1 starting chip before entering the table.");
                return null;
            }
            startingChipsField.setText(String.valueOf(startingChips));
            clearStartError();
            return startingChips;
        } catch (NumberFormatException exception) {
            showStartError("Enter a whole-number bankroll such as 100, 500, or 1000.");
            return null;
        }
    }

    private void showStartError(String message) {
        startErrorLabel.setText(message);
        startErrorLabel.setManaged(true);
        startErrorLabel.setVisible(true);
    }

    private void clearStartError() {
        startErrorLabel.setText("");
        startErrorLabel.setManaged(false);
        startErrorLabel.setVisible(false);
    }
}
