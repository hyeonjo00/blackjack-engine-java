package com.blackjackengine.ui;

import com.blackjackengine.Card;
import com.blackjackengine.GameEngine;
import com.blackjackengine.GameStatistics;
import com.blackjackengine.Hand;
import com.blackjackengine.MoveRecommendation;
import com.blackjackengine.PlayerAction;
import com.blackjackengine.PlayerHand;
import com.blackjackengine.RecommendationAnalytics;
import com.blackjackengine.RoundResult;
import com.blackjackengine.RoundOutcome;
import com.blackjackengine.SessionPersistence;
import com.blackjackengine.SessionSnapshot;
import com.blackjackengine.StrategyAdvisor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

public class BlackjackController implements AppScreenController {
    private static final String CARD_IMAGE_VIEW_KEY = "cardImageView";
    private static final String HAND_CARDS_ROW_KEY = "handCardsRow";
    private static final String HAND_SCORE_LABEL_KEY = "handScoreLabel";
    private static final double CARD_WIDTH = 96;
    private static final double CARD_HEIGHT = 136;
    private static final Duration INITIAL_DEAL_STEP_DELAY = Duration.millis(110);
    private static final Duration INITIAL_DEAL_FLIGHT_DURATION = Duration.millis(250);
    private static final Duration INITIAL_DEAL_FADE_DURATION = Duration.millis(140);
    private static final Duration DEALER_FLIP_HALF_DURATION = Duration.millis(220);
    private static final Duration DEALER_DRAW_DELAY = Duration.millis(180);
    private static final Duration DEALER_DRAW_DURATION = Duration.millis(220);
    private static final Duration CHIP_FLIGHT_DURATION = Duration.millis(340);
    private static final Duration CHIP_SETTLE_DURATION = Duration.millis(120);
    private static final Duration BET_SPOT_PULSE_DURATION = Duration.millis(360);
    private static final Duration CHIP_BALANCE_COUNT_DURATION = Duration.millis(780);
    private static final double CHIP_SIZE = 68;
    private static final double CHIP_FLIGHT_SCALE = 1.12;
    private static final double BET_STACK_CHIP_SIZE = 42;
    private static final double BET_STACK_BASELINE = 128;
    private static final double BET_STACK_COLUMN_SPREAD = 28;
    private static final double BET_STACK_MIN_STEP = 4;
    private static final double BET_STACK_MAX_STEP = 12;
    private static final int MAX_SETTLEMENT_ANIMATION_CHIPS = 8;

    @FXML
    private Label chipBalanceLabel;
    @FXML
    private Label currentBetLabel;
    @FXML
    private Label handsOnTableLabel;
    @FXML
    private Label sessionProfitLossLabel;
    @FXML
    private Label roundStatusLabel;
    @FXML
    private HBox topInfoBar;
    @FXML
    private Label tableBetLabel;
    @FXML
    private Spinner<Integer> betSpinner;
    @FXML
    private Button quickDealButton;
    @FXML
    private Label betErrorLabel;
    @FXML
    private VBox chipControlsContainer;
    @FXML
    private Button chip1Button;
    @FXML
    private Button chip5Button;
    @FXML
    private Button chip10Button;
    @FXML
    private Button chip25Button;
    @FXML
    private VBox dealerZoneBox;
    @FXML
    private HBox dealerCardsContainer;
    @FXML
    private Label dealerScoreLabel;
    @FXML
    private Label bestMoveLabel;
    @FXML
    private Label reasonLabel;
    @FXML
    private Label confidenceLabel;
    @FXML
    private Label expectedEvLabel;
    @FXML
    private Label bustRiskLabel;
    @FXML
    private Label dealerBustChanceLabel;
    @FXML
    private VBox playerZoneBox;
    @FXML
    private HBox playerHandsContainer;
    @FXML
    private Label playerScoreLabel;
    @FXML
    private StackPane activeRoundActionBar;
    @FXML
    private StackPane betSpotPane;
    @FXML
    private Pane betChipStackPane;
    @FXML
    private Pane tableAnimationLayer;
    @FXML
    private Button hitButton;
    @FXML
    private Button standButton;
    @FXML
    private Button doubleButton;
    @FXML
    private Button splitButton;
    @FXML
    private Button nextRoundButton;
    @FXML
    private Button clearBetButton;
    @FXML
    private Button resetSessionButton;
    @FXML
    private Button saveSessionButton;
    @FXML
    private Button loadSessionButton;
    @FXML
    private Button exportStatsButton;
    @FXML
    private Button quitButton;
    @FXML
    private Label winsLabel;
    @FXML
    private Label lossesLabel;
    @FXML
    private Label pushesLabel;
    @FXML
    private Label winRateLabel;
    @FXML
    private Label recommendationAccuracyLabel;
    @FXML
    private Label bestWinStreakLabel;
    @FXML
    private Label sessionProfitLabel;
    @FXML
    private StackPane roundResultOverlay;
    @FXML
    private Label roundResultOverlayTitleLabel;
    @FXML
    private Label roundResultOverlayDeltaLabel;
    @FXML
    private Label roundResultOverlayMessageLabel;

    private final List<Integer> placedBetChips = new ArrayList<>();
    private BlackjackApplication application;
    private GameEngine gameEngine;
    private StrategyAdvisor strategyAdvisor;
    private RecommendationAnalytics recommendationAnalytics;
    private CardImageMapper cardImageMapper;
    private MoveRecommendation currentRecommendation;
    private RoundResult lastStyledRoundResult;
    private Timeline chipBalanceCountTimeline;
    private Animation outcomePulseAnimation;
    private Animation roundResultOverlayAnimation;
    private boolean animationInProgress;
    private boolean chipBalanceAnimationInProgress;
    private int chipAnimationsInFlight;
    private int queuedChipBetTotal;
    private int chipLandingCount;
    private int displayedChipBalance;
    private int sessionStartingChips = 100;
    private boolean initialDealAnimationInProgress;
    private String lastCompletedRoundResultText = "Waiting to deal";
    private MoveRecommendation lastLiveRecommendation;
    private int selectedChipAmount;

    @FXML
    private void initialize() {
        strategyAdvisor = new StrategyAdvisor();
        cardImageMapper = new CardImageMapper();
        gameEngine = new GameEngine("Player");
        recommendationAnalytics = new RecommendationAnalytics();
        displayedChipBalance = gameEngine.getPlayer().getChips();
        configureBetSpinner();
        clearBetError();
        ensureBankrollPanelVisible();
        ensureActionBarVisible();
        updateUi();
    }

    @Override
    public void setApplication(BlackjackApplication application) {
        this.application = application;
        initializeSession(application.getConfiguredStartingChips());
    }

    @Override
    public void onViewActivated() {
        clearBetError();
    }

    private void initializeSession(int startingChips) {
        sessionStartingChips = startingChips;
        gameEngine = new GameEngine("Player", startingChips);
        recommendationAnalytics = new RecommendationAnalytics();
        currentRecommendation = null;
        lastLiveRecommendation = null;
        lastStyledRoundResult = null;
        animationInProgress = false;
        initialDealAnimationInProgress = false;
        chipAnimationsInFlight = 0;
        queuedChipBetTotal = 0;
        chipLandingCount = 0;
        selectedChipAmount = 0;
        lastCompletedRoundResultText = "Waiting to deal";
        placedBetChips.clear();
        stopOutcomePulse();
        hideRoundResultOverlay();
        stopChipBalanceAnimation(false);

        if (tableAnimationLayer != null) {
            tableAnimationLayer.getChildren().clear();
        }

        displayedChipBalance = gameEngine.getPlayer().getChips();

        if (betSpinner != null && betSpinner.getValueFactory() != null) {
            betSpinner.getValueFactory().setValue(0);
            betSpinner.getEditor().setText("0");
        }

        clearBetError();
        updateUi();
    }

    @FXML
    private void onNextRound() {
        startRoundFromCurrentBet();
    }

    private void startRoundFromCurrentBet() {
        if (gameEngine.isRoundInProgress() || animationInProgress || chipAnimationsInFlight > 0) {
            return;
        }

        if (gameEngine.getPlayer().getChips() <= 0) {
            updateUi();
            return;
        }

        try {
            if (!commitManualBetInput(false)) {
                return;
            }

            resetRoundVisualState();
            int bet = readBetAmount();
            chipLandingCount = 0;
            syncPlacedBetChipsToAmount(bet);
            setSelectedChipAmount(0);
            gameEngine.startRound(bet);
            currentRecommendation = null;
            lastLiveRecommendation = null;
            clearBetError();
            if (canPlayInitialDealSequence()) {
                playInitialDealSequence();
            } else {
                updateUi();
            }
        } catch (IllegalArgumentException exception) {
            showBetError(exception.getMessage());
            updateUi();
        } catch (IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void onClearBet() {
        if (gameEngine.isRoundInProgress() || animationInProgress || chipAnimationsInFlight > 0) {
            return;
        }

        queuedChipBetTotal = 0;
        chipLandingCount = 0;
        setSelectedChipAmount(0);
        placedBetChips.clear();
        setSpinnerBetValue(0);
        clearBetError();
        updateUi();
    }

    @FXML
    private void onResetSession() {
        if (animationInProgress || chipAnimationsInFlight > 0) {
            return;
        }

        initializeSession(sessionStartingChips);
    }

    @FXML
    private void onSaveSession() {
        if (gameEngine.isRoundInProgress() || animationInProgress || chipAnimationsInFlight > 0) {
            showError("Finish the current hand before saving the session.");
            return;
        }

        FileChooser fileChooser = createJsonFileChooser("Save Session", "blackjack-session.json");
        Window window = getCurrentWindow();
        java.io.File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        try {
            SessionPersistence.saveSession(file.toPath(), buildSessionSnapshot());
            showInfo("Session saved", "Saved the current table session to:\n" + file.getAbsolutePath());
        } catch (IOException exception) {
            showError("Unable to save the session file.");
        }
    }

    @FXML
    private void onLoadSession() {
        if (gameEngine.isRoundInProgress() || animationInProgress || chipAnimationsInFlight > 0) {
            showError("Finish the current hand before loading another session.");
            return;
        }

        FileChooser fileChooser = createJsonFileChooser("Load Session", "blackjack-session.json");
        Window window = getCurrentWindow();
        java.io.File file = fileChooser.showOpenDialog(window);
        if (file == null) {
            return;
        }

        try {
            applyLoadedSession(SessionPersistence.loadSession(file.toPath()));
            showInfo("Session loaded", "Loaded the saved session successfully.");
        } catch (IOException | IllegalArgumentException exception) {
            showError("Unable to load that session file.");
        }
    }

    @FXML
    private void onExportStatsJson() {
        FileChooser fileChooser = createJsonFileChooser("Export Statistics", "blackjack-stats.json");
        Window window = getCurrentWindow();
        java.io.File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        try {
            SessionPersistence.exportStatisticsJson(
                file.toPath(),
                gameEngine.getStatistics().toSnapshot(),
                recommendationAnalytics.toSnapshot(),
                gameEngine.getPlayer().getChips(),
                resolveThemeName()
            );
            showInfo("Statistics exported", "Exported the session statistics to:\n" + file.getAbsolutePath());
        } catch (IOException exception) {
            showError("Unable to export the statistics file.");
        }
    }

    @FXML
    private void onHit() {
        handleHit();
    }

    @FXML
    private void onStand() {
        handleStand();
    }

    @FXML
    private void onDouble() {
        handleDouble();
    }

    @FXML
    private void onSplit() {
        handleSplit();
    }

    @FXML
    private void handleHit() {
        handlePlayerAction(PlayerAction.HIT);
    }

    @FXML
    private void handleStand() {
        handlePlayerAction(PlayerAction.STAND);
    }

    @FXML
    private void handleDouble() {
        handlePlayerAction(PlayerAction.DOUBLE_DOWN);
    }

    @FXML
    private void handleSplit() {
        handlePlayerAction(PlayerAction.SPLIT);
    }

    @FXML
    private void onQuit() {
        if (application != null) {
            application.exitApplication();
        } else {
            Platform.exit();
        }
    }

    @FXML
    private void onBetChip(ActionEvent event) {
        if (gameEngine.isRoundInProgress() || animationInProgress) {
            return;
        }

        if (!(event.getSource() instanceof Button)) {
            return;
        }

        Button chipButton = (Button) event.getSource();
        int chipAmount = parseChipAmount(chipButton);

        if (chipAmount <= 0) {
            return;
        }

        if (!commitManualBetInput(true)) {
            return;
        }

        int nextBet = readSpinnerValueOrDefault() + queuedChipBetTotal + chipAmount;
        if (nextBet > gameEngine.getPlayer().getChips()) {
            showBetError("That chip would push the bet above your chip balance.");
            updateControlState();
            return;
        }

        clearBetError();
        setSelectedChipAmount(chipAmount);
        animateChipToBetCircle(chipButton, chipAmount);
    }

    private void handlePlayerAction(PlayerAction action) {
        if (!gameEngine.isRoundInProgress() || animationInProgress) {
            return;
        }

        MoveRecommendation recommendation = currentRecommendation == null
            ? createRecommendationForActiveHand()
            : currentRecommendation;

        if (!isRecommendationAvailable(recommendation)) {
            recommendation = new MoveRecommendation(
                PlayerAction.STAND,
                "Recommendation unavailable for the current hand."
            );
        }

        recommendationAnalytics.recordDecision(
            gameEngine.getActiveHandIndex() + 1,
            recommendation,
            action
        );

        int previousActiveHandIndex = gameEngine.getActiveHandIndex();
        PlayerHand previousActiveHand = gameEngine.getActiveHand();
        int previousCardCount = previousActiveHand == null ? 0 : previousActiveHand.getHand().size();
        boolean shouldAnimateDealerReveal = shouldAnimateDealerReveal();
        try {
            switch (action) {
                case HIT:
                    gameEngine.playerHit();
                    break;
                case STAND:
                    gameEngine.playerStand();
                    break;
                case DOUBLE_DOWN:
                    gameEngine.playerDoubleDown();
                    break;
                case SPLIT:
                    gameEngine.playerSplit();
                    break;
                default:
                    throw new IllegalStateException("Unsupported action: " + action);
            }

            if (!gameEngine.isRoundInProgress()) {
                recommendationAnalytics.resolveRound(gameEngine.getLastResult());
            }

            currentRecommendation = null;

            if ((action == PlayerAction.HIT || action == PlayerAction.DOUBLE_DOWN)
                && playPlayerDrawAnimation(previousActiveHandIndex, previousCardCount, shouldAnimateDealerReveal)) {
                return;
            }

            if (!gameEngine.isRoundInProgress() && shouldAnimateDealerReveal) {
                playDealerRevealFlip();
                return;
            }

            updateUi();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private void configureBetSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        betSpinner.setValueFactory(valueFactory);
        betSpinner.setEditable(true);
        betSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!gameEngine.isRoundInProgress() && chipAnimationsInFlight == 0) {
                clearBetError();
                updateHeader();
                updateControlState();
            }
        });
        betSpinner.getEditor().focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (wasFocused && !isFocused && !gameEngine.isRoundInProgress()) {
                commitManualBetInput(true);
                updateHeader();
                updateControlState();
            }
        });
        betSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!gameEngine.isRoundInProgress() && chipAnimationsInFlight == 0) {
                syncPlacedBetChipsToAmount(newValue == null ? 0 : newValue);
                updateHeader();
                updateControlState();
            }
        });
    }

    private void updateUi() {
        updateBetSpinnerRange();
        ensureBankrollPanelVisible();
        ensureActionBarVisible();
        updateHeader();
        updateDealerArea();
        updatePlayerArea();
        refreshRecommendation();
        updateFooter();
        updateControlState();
        refreshRoundOutcomeEffects();
    }

    private void updateBetSpinnerRange() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
            (SpinnerValueFactory.IntegerSpinnerValueFactory) betSpinner.getValueFactory();
        int chips = Math.max(gameEngine.getPlayer().getChips(), 0);
        int currentValue = readSpinnerValueOrDefault();
        valueFactory.setMin(0);
        valueFactory.setMax(chips);
        valueFactory.setValue(Math.max(0, Math.min(currentValue, chips)));

        if (!gameEngine.isRoundInProgress() && chipAnimationsInFlight == 0 && betSpinner.getEditor().isFocused()) {
            return;
        }

        betSpinner.getEditor().setText(String.valueOf(valueFactory.getValue()));
    }

    private void updateHeader() {
        updateChipBalanceDisplay();
        int displayedBetAmount = getDisplayedBetAmount();
        String betText = formatChipMetric(displayedBetAmount);
        currentBetLabel.setText(betText);
        tableBetLabel.setText(String.valueOf(displayedBetAmount));
        handsOnTableLabel.setText(String.valueOf(gameEngine.getPlayerHands().size()));
        sessionProfitLossLabel.setText(formatSignedChipMetric(gameEngine.getStatistics().getTotalChipProfitLoss()));
        applyProfitLabelStyle(sessionProfitLossLabel, gameEngine.getStatistics().getTotalChipProfitLoss());
        syncPlacedBetChipsToAmount(displayedBetAmount);
        refreshBetChipStack(displayedBetAmount);
        updateBetAmountGlow(displayedBetAmount);
        roundStatusLabel.setText(resolveDisplayedRoundResultText());
    }

    private void updateDealerArea() {
        dealerCardsContainer.getChildren().clear();
        Hand dealerHand = gameEngine.getDealer().getHand();
        boolean hideHoleCard = gameEngine.isRoundInProgress();

        if (dealerHand.size() == 0) {
            dealerCardsContainer.getChildren().add(createPlaceholderCard("Waiting"));
            dealerScoreLabel.setText("Score: --");
            return;
        }

        for (int index = 0; index < dealerHand.size(); index++) {
            boolean hidden = hideHoleCard && index > 0;
            dealerCardsContainer.getChildren().add(createCardView(dealerHand.getCard(index), hidden));
        }

        dealerScoreLabel.setText(
            hideHoleCard
                ? "Visible score: " + calculateDealerScore(1)
                : "Score: " + dealerHand.getBestValue()
        );
    }

    private void updatePlayerArea() {
        playerHandsContainer.getChildren().clear();
        List<PlayerHand> playerHands = gameEngine.getPlayerHands();

        if (playerHands.isEmpty()) {
            playerHandsContainer.getChildren().add(createEmptyHandPanel());
            playerScoreLabel.setText("Active score: --");
            return;
        }

        for (int index = 0; index < playerHands.size(); index++) {
            playerHandsContainer.getChildren().add(createHandPanel(playerHands.get(index), index));
        }

        PlayerHand activeHand = gameEngine.getActiveHand();
        playerScoreLabel.setText(
            activeHand == null
                ? "Hands on table: " + playerHands.size()
                : "Active score: " + activeHand.getHand().getBestValue()
        );
    }

    private void refreshRecommendation() {
        RoundResult roundResult = gameEngine.getLastResult();

        if (initialDealAnimationInProgress) {
            currentRecommendation = null;
            lastLiveRecommendation = null;
            setRecommendationText(
                "Waiting",
                "Dealing the opening cards. Recommendation appears when your turn begins.",
                "--",
                "--",
                "--",
                "--"
            );
            return;
        }

        if (roundResult != null && roundResult.isRoundOver()) {
            currentRecommendation = null;
            showResolvedRecommendation(roundResult);
            return;
        }

        if (!gameEngine.isRoundInProgress()) {
            currentRecommendation = null;
            lastLiveRecommendation = null;
            setRecommendationText(
                "Waiting",
                "Choose a bet and press Deal Cards to start the next hand.",
                "--",
                "--",
                "--",
                "--"
            );
            return;
        }

        MoveRecommendation recommendation = createRecommendationForActiveHand();
        currentRecommendation = recommendation;

        if (!isRecommendationAvailable(recommendation)) {
            lastLiveRecommendation = null;
            setRecommendationText(
                "Stand",
                "Recommendation unavailable for the current hand.",
                "Unavailable",
                "--",
                "--",
                "--"
            );
            return;
        }

        lastLiveRecommendation = recommendation;

        setRecommendationText(
            recommendation.getAction().getLabel(),
            recommendation.getReason() == null || recommendation.getReason().isBlank()
                ? "Recommendation unavailable for the current hand."
                : recommendation.getReason(),
            recommendation.getConfidence().getLabel(),
            formatExpectedValue(recommendation.getExpectedValue()),
            formatPercent(recommendation.getBustRisk()),
            formatPercent(recommendation.getDealerBustChance())
        );
    }

    private void updateFooter() {
        GameStatistics statistics = gameEngine.getStatistics();
        winsLabel.setText(String.valueOf(statistics.getWins()));
        lossesLabel.setText(String.valueOf(statistics.getLosses()));
        pushesLabel.setText(String.valueOf(statistics.getPushes()));
        winRateLabel.setText(formatRate(statistics.getWinRate()));
        recommendationAccuracyLabel.setText(formatRate(recommendationAnalytics.getRecommendationAccuracy()));
        bestWinStreakLabel.setText(String.valueOf(statistics.getBestWinStreak()));
        sessionProfitLabel.setText(formatSignedChipMetric(statistics.getTotalChipProfitLoss()));
        applyProfitLabelStyle(sessionProfitLabel, statistics.getTotalChipProfitLoss());
    }

    private void updateControlState() {
        ensureActionBarVisible();
        List<PlayerAction> availableActions = gameEngine.getAvailableActions();
        boolean roundInProgress = gameEngine.isRoundInProgress();
        boolean controlsLocked = animationInProgress;
        boolean bettingLocked = roundInProgress || animationInProgress || chipAnimationsInFlight > 0;
        boolean outOfChips = gameEngine.getPlayer().getChips() <= 0;
        int pendingBetAmount = getPreviewBetAmount();
        boolean validPendingBet = hasValidPendingBet();
        boolean hasPreviewBet = pendingBetAmount > 0;
        boolean canStartRound = !controlsLocked
            && !roundInProgress
            && !outOfChips
            && chipAnimationsInFlight == 0
            && validPendingBet;
        int remainingBetCapacity = gameEngine.getPlayer().getChips()
            - (pendingBetAmount + queuedChipBetTotal);

        hitButton.setDisable(controlsLocked || !availableActions.contains(PlayerAction.HIT));
        standButton.setDisable(controlsLocked || !availableActions.contains(PlayerAction.STAND));
        doubleButton.setDisable(controlsLocked || !availableActions.contains(PlayerAction.DOUBLE_DOWN));
        splitButton.setDisable(controlsLocked || !availableActions.contains(PlayerAction.SPLIT));
        if (nextRoundButton != null) {
            nextRoundButton.setDisable(!canStartRound);
        }
        if (quickDealButton != null) {
            quickDealButton.setDisable(!canStartRound);
        }
        clearBetButton.setDisable(roundInProgress || animationInProgress || chipAnimationsInFlight > 0 || !hasPreviewBet);
        resetSessionButton.setDisable(animationInProgress || chipAnimationsInFlight > 0);
        saveSessionButton.setDisable(roundInProgress || animationInProgress || chipAnimationsInFlight > 0);
        loadSessionButton.setDisable(roundInProgress || animationInProgress || chipAnimationsInFlight > 0);
        exportStatsButton.setDisable(animationInProgress || chipAnimationsInFlight > 0);
        betSpinner.setDisable(bettingLocked || outOfChips);
        chipControlsContainer.setDisable(false);
        chip1Button.setDisable(roundInProgress || animationInProgress || outOfChips || 1 > remainingBetCapacity);
        chip5Button.setDisable(roundInProgress || animationInProgress || outOfChips || 5 > remainingBetCapacity);
        chip10Button.setDisable(roundInProgress || animationInProgress || outOfChips || 10 > remainingBetCapacity);
        chip25Button.setDisable(roundInProgress || animationInProgress || outOfChips || 25 > remainingBetCapacity);
        updateSelectedChipButtonStyles();
    }

    private MoveRecommendation createRecommendationForActiveHand() {
        return strategyAdvisor.recommend(
            gameEngine.getActiveHand(),
            gameEngine.getDealerUpCard(),
            gameEngine.getAvailableActions()
        );
    }

    private VBox createHandPanel(PlayerHand playerHand, int handIndex) {
        VBox handPanel = new VBox(12);
        handPanel.getStyleClass().add("hand-panel");
        handPanel.setPadding(new Insets(14));
        handPanel.setPrefWidth(260);
        handPanel.setMaxWidth(320);

        if (gameEngine.isRoundInProgress() && handIndex == gameEngine.getActiveHandIndex()) {
            handPanel.getStyleClass().add("active-hand");
        }

        Label handTitle = new Label("Hand " + (handIndex + 1));
        handTitle.getStyleClass().add("hand-title");

        Label handMeta = new Label(buildHandMeta(playerHand));
        handMeta.getStyleClass().add("hand-meta");

        HBox cardsBox = new HBox(10);
        cardsBox.setAlignment(Pos.CENTER_LEFT);
        for (Card card : playerHand.getHand().getCards()) {
            cardsBox.getChildren().add(createCardView(card, false));
        }

        Label handScore = new Label("Score: " + playerHand.getHand().getBestValue());
        handScore.getStyleClass().add("score-label");

        handPanel.getProperties().put(HAND_CARDS_ROW_KEY, cardsBox);
        handPanel.getProperties().put(HAND_SCORE_LABEL_KEY, handScore);
        handPanel.getChildren().addAll(handTitle, handMeta, cardsBox, handScore);
        HBox.setHgrow(handPanel, Priority.ALWAYS);
        return handPanel;
    }

    private VBox createEmptyHandPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("hand-panel");
        panel.setPadding(new Insets(16));
        panel.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Player cards will appear here.");
        label.getStyleClass().add("placeholder-label");
        panel.getChildren().add(label);
        return panel;
    }

    private StackPane createCardView(Card card, boolean hidden) {
        StackPane cardPane = new StackPane();
        cardPane.getStyleClass().add("card-node");
        cardPane.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        cardPane.setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        try {
            ImageView cardImageView = cardImageMapper.createCardImageView(card, hidden);
            cardImageView.getStyleClass().add("card-image");
            cardPane.getProperties().put(CARD_IMAGE_VIEW_KEY, cardImageView);
            cardPane.getChildren().add(cardImageView);
        } catch (IllegalStateException exception) {
            return createPlaceholderCard(hidden ? "Hidden" : "Missing");
        }
        return cardPane;
    }

    private StackPane createPlaceholderCard(String text) {
        StackPane placeholder = new StackPane();
        placeholder.getStyleClass().addAll("card-node", "card-back");
        placeholder.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        placeholder.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        placeholder.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        Label label = new Label(text);
        label.getStyleClass().add("card-back-text");
        placeholder.getChildren().add(label);
        return placeholder;
    }

    private String buildHandMeta(PlayerHand playerHand) {
        StringBuilder builder = new StringBuilder("Bet: ");
        builder.append(playerHand.getBet());
        if (playerHand.isFromSplit()) {
            builder.append(" | Split");
        }
        if (playerHand.isDoubledDown()) {
            builder.append(" | Doubled");
        }
        return builder.toString();
    }

    private String buildRoundResultText(RoundResult roundResult) {
        if (roundResult == null) {
            return "Waiting to deal";
        }

        if (roundResult.getChipDelta() > 0) {
            return "Win +" + roundResult.getChipDelta();
        }

        if (roundResult.getChipDelta() < 0) {
            return "Lose " + roundResult.getChipDelta();
        }

        return "Push";
    }

    private String resolveDisplayedRoundResultText() {
        RoundResult roundResult = gameEngine.getLastResult();

        if (roundResult != null && roundResult.isRoundOver()) {
            lastCompletedRoundResultText = buildRoundResultText(roundResult);
        }

        if (gameEngine.getPlayer().getChips() <= 0 && "Waiting to deal".equals(lastCompletedRoundResultText)) {
            return "Out of chips";
        }

        return lastCompletedRoundResultText;
    }

    private int readBetAmount() {
        int bet = readSpinnerValueOrDefault();

        if (!isValidBetAmount(bet)) {
            throw new IllegalArgumentException(
                "Place at least one chip and keep the total bet within your chip balance."
            );
        }

        return bet;
    }

    private int readSpinnerValueOrDefault() {
        Integer spinnerValue = betSpinner.getValue();
        return spinnerValue == null ? 0 : spinnerValue;
    }

    private int getDisplayedBetAmount() {
        return gameEngine.isRoundInProgress()
            ? gameEngine.getTotalCommittedBet()
            : getPreviewBetAmount();
    }

    private String formatRate(double rate) {
        if (rate < 0.0) {
            return "N/A";
        }
        return String.format("%.1f%%", rate * 100.0);
    }

    private void setRecommendationText(
        String actionText,
        String reasonText,
        String confidenceText,
        String expectedEvText,
        String bustRiskText,
        String dealerBustChanceText
    ) {
        bestMoveLabel.setText(actionText == null || actionText.isBlank() ? "Stand" : actionText);
        reasonLabel.setText(
            reasonText == null || reasonText.isBlank()
                ? "Recommendation unavailable for the current hand."
                : reasonText
        );
        confidenceLabel.setText(
            confidenceText == null || confidenceText.isBlank() ? "--" : confidenceText
        );
        expectedEvLabel.setText(
            expectedEvText == null || expectedEvText.isBlank() ? "--" : expectedEvText
        );
        bustRiskLabel.setText(
            bustRiskText == null || bustRiskText.isBlank() ? "--" : bustRiskText
        );
        dealerBustChanceLabel.setText(
            dealerBustChanceText == null || dealerBustChanceText.isBlank() ? "--" : dealerBustChanceText
        );
    }

    private void showResolvedRecommendation(RoundResult roundResult) {
        String reasonText = animationInProgress
            ? "Dealer reveal in progress. " + roundResult.getMessage()
            : roundResult.getMessage();
        String confidenceText = animationInProgress ? "Settling" : "Round complete";
        Card dealerUpCard = gameEngine.getDealerUpCard();

        setRecommendationText(
            "Round complete",
            reasonText,
            confidenceText,
            formatExpectedValue(calculateRoundResultExpectedValue(roundResult)),
            formatPercent(calculateResolvedBustRisk(roundResult)),
            dealerUpCard == null ? "--" : formatPercent(strategyAdvisor.estimateDealerBustChance(dealerUpCard))
        );
    }

    private boolean isRecommendationAvailable(MoveRecommendation recommendation) {
        return recommendation != null
            && recommendation.getAction() != null
            && recommendation.getReason() != null
            && !recommendation.getReason().isBlank();
    }

    private double calculateRoundResultExpectedValue(RoundResult roundResult) {
        if (roundResult == null || roundResult.getTotalBetAmount() <= 0) {
            return 0.0;
        }

        return (double) roundResult.getChipDelta() / roundResult.getTotalBetAmount();
    }

    private double calculateResolvedBustRisk(RoundResult roundResult) {
        if (roundResult == null || roundResult.getHandResults().isEmpty()) {
            return 0.0;
        }

        long bustHands = roundResult.getHandResults().stream()
            .filter(handResult -> handResult.getOutcome() == RoundOutcome.PLAYER_BUST)
            .count();

        return (double) bustHands / roundResult.getHandResults().size();
    }

    private String formatExpectedValue(double expectedValue) {
        return String.format("%+.2f units", expectedValue);
    }

    private String formatPercent(double value) {
        if (value < 0.0) {
            return "N/A";
        }

        return String.format("%.0f%%", value * 100.0);
    }

    private String formatSignedChipMetric(int amount) {
        if (amount > 0) {
            return "+" + amount;
        }

        if (amount < 0) {
            return String.valueOf(amount);
        }

        return "0";
    }

    private void applyProfitLabelStyle(Label label, int amount) {
        if (label == null) {
            return;
        }

        removeStyleClass(label, "metric-profit-positive");
        removeStyleClass(label, "metric-profit-negative");
        removeStyleClass(label, "metric-profit-neutral");

        if (amount > 0) {
            addStyleClass(label, "metric-profit-positive");
        } else if (amount < 0) {
            addStyleClass(label, "metric-profit-negative");
        } else {
            addStyleClass(label, "metric-profit-neutral");
        }
    }

    private FileChooser createJsonFileChooser(String title, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON files", "*.json")
        );
        return fileChooser;
    }

    private Window getCurrentWindow() {
        if (topInfoBar != null && topInfoBar.getScene() != null) {
            return topInfoBar.getScene().getWindow();
        }

        return null;
    }

    private SessionSnapshot buildSessionSnapshot() {
        return new SessionSnapshot(
            sessionStartingChips,
            gameEngine.getPlayer().getChips(),
            Math.min(getPreviewBetAmount(), gameEngine.getPlayer().getChips()),
            lastCompletedRoundResultText,
            resolveThemeName(),
            gameEngine.getStatistics().toSnapshot(),
            recommendationAnalytics.toSnapshot()
        );
    }

    private void applyLoadedSession(SessionSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Loaded session data is unavailable.");
        }

        sessionStartingChips = snapshot.getStartingChips();
        gameEngine = new GameEngine("Player", sessionStartingChips);
        gameEngine.applySessionSnapshot(snapshot);
        recommendationAnalytics = new RecommendationAnalytics();
        recommendationAnalytics.restore(snapshot.getRecommendationAnalyticsSnapshot());
        currentRecommendation = null;
        lastLiveRecommendation = null;
        lastStyledRoundResult = null;
        animationInProgress = false;
        initialDealAnimationInProgress = false;
        chipAnimationsInFlight = 0;
        queuedChipBetTotal = 0;
        chipLandingCount = 0;
        selectedChipAmount = 0;
        placedBetChips.clear();
        clearOutcomeEffects();
        hideRoundResultOverlay();
        stopChipBalanceAnimation(false);

        if (tableAnimationLayer != null) {
            tableAnimationLayer.getChildren().clear();
        }

        displayedChipBalance = gameEngine.getPlayer().getChips();
        lastCompletedRoundResultText = snapshot.getLastRoundResultText() == null
            || snapshot.getLastRoundResultText().isBlank()
            ? "Waiting to deal"
            : snapshot.getLastRoundResultText();

        int previewBetAmount = Math.max(0, Math.min(snapshot.getPreviewBetAmount(), gameEngine.getPlayer().getChips()));
        setSpinnerBetValue(previewBetAmount);
        syncPlacedBetChipsToAmount(previewBetAmount);
        clearBetError();

        if (application != null) {
            application.setTheme(UiTheme.fromName(snapshot.getThemeName()));
        }

        updateUi();
    }

    private String resolveThemeName() {
        return application == null ? UiTheme.CLASSIC_GREEN.name() : application.getCurrentTheme().name();
    }

    private void showInfo(String headerText, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Blackjack");
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Blackjack");
        alert.setHeaderText("Unable to complete that action");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean commitManualBetInput(boolean allowZeroBet) {
        String rawText = betSpinner.getEditor().getText();

        if (rawText == null || rawText.trim().isEmpty()) {
            setSpinnerBetValue(0);
            if (allowZeroBet) {
                clearBetError();
                return true;
            }

            showBetError("Enter a whole-number bet before starting the round.");
            return false;
        }

        Integer parsedBet = parseBetText(rawText);
        if (parsedBet == null) {
            showBetError("Enter a whole-number bet before starting the round.");
            return false;
        }

        if (parsedBet == 0 && allowZeroBet) {
            setSpinnerBetValue(0);
            clearBetError();
            return true;
        }

        if (!isValidBetAmount(parsedBet)) {
            showBetError("Place at least one chip and keep the total bet within your chip balance.");
            return false;
        }

        setSpinnerBetValue(parsedBet);
        clearBetError();
        return true;
    }

    private Integer parseBetText(String rawText) {
        try {
            return Integer.parseInt(rawText.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private int getPreviewBetAmount() {
        Integer parsedBet = parseCurrentEditorBet();
        if (parsedBet != null && parsedBet >= 0 && parsedBet <= gameEngine.getPlayer().getChips()) {
            return parsedBet;
        }

        return readSpinnerValueOrDefault();
    }

    private boolean hasValidPendingBet() {
        String rawText = betSpinner.getEditor().getText();
        if (rawText == null || rawText.trim().isEmpty()) {
            return false;
        }

        Integer parsedBet = parseCurrentEditorBet();
        return parsedBet != null && isValidBetAmount(parsedBet);
    }

    private Integer parseCurrentEditorBet() {
        return parseBetText(betSpinner.getEditor().getText());
    }

    private boolean isValidBetAmount(int bet) {
        return bet > 0 && bet <= gameEngine.getPlayer().getChips();
    }

    private void setSpinnerBetValue(int bet) {
        betSpinner.getValueFactory().setValue(bet);
        betSpinner.getEditor().setText(String.valueOf(bet));
    }

    private void showBetError(String message) {
        betErrorLabel.setText(message);
        betErrorLabel.setManaged(true);
        betErrorLabel.setVisible(true);
    }

    private void clearBetError() {
        if (betErrorLabel == null) {
            return;
        }

        betErrorLabel.setText("");
        betErrorLabel.setManaged(false);
        betErrorLabel.setVisible(false);
    }

    private boolean canPlayInitialDealSequence() {
        return tableAnimationLayer != null
            && tableAnimationLayer.getScene() != null
            && !gameEngine.getPlayerHands().isEmpty()
            && gameEngine.getDealer().getHand().size() >= 2;
    }

    private void playInitialDealSequence() {
        PlayerHand openingHand = gameEngine.getPlayerHands().get(0);
        Hand playerHand = openingHand.getHand();
        Hand dealerHand = gameEngine.getDealer().getHand();

        if (playerHand.size() < 2 || dealerHand.size() < 2) {
            animationInProgress = false;
            initialDealAnimationInProgress = false;
            updateUi();
            return;
        }

        animationInProgress = true;
        initialDealAnimationInProgress = true;
        currentRecommendation = null;

        dealerCardsContainer.getChildren().clear();
        playerHandsContainer.getChildren().clear();

        HBox dealingPlayerCardsRow = createDealingPlayerCardsRow();
        playerHandsContainer.getChildren().add(createDealingHandPanel(openingHand, dealingPlayerCardsRow));
        dealerScoreLabel.setText("Score: --");
        playerScoreLabel.setText("Active score: --");

        updateHeader();
        updateFooter();
        refreshRecommendation();
        ensureActionBarVisible();
        updateControlState();

        SequentialTransition initialDealSequence = new SequentialTransition(
            animateDealToDealer(dealerHand.getCard(0), false, 0),
            animateDealToPlayer(dealingPlayerCardsRow, playerHand.getCard(0), 0),
            animateDealToDealer(dealerHand.getCard(1), true, 1),
            animateDealToPlayer(dealingPlayerCardsRow, playerHand.getCard(1), 1)
        );
        initialDealSequence.setOnFinished(event -> finishInitialDealSequence());
        initialDealSequence.play();
    }

    private HBox createDealingPlayerCardsRow() {
        HBox cardsRow = new HBox(10);
        cardsRow.setAlignment(Pos.CENTER_LEFT);
        return cardsRow;
    }

    private VBox createDealingHandPanel(PlayerHand playerHand, HBox cardsRow) {
        VBox handPanel = new VBox(12);
        handPanel.getStyleClass().addAll("hand-panel", "active-hand");
        handPanel.setPadding(new Insets(14));
        handPanel.setPrefWidth(260);
        handPanel.setMaxWidth(320);

        Label handTitle = new Label("Hand 1");
        handTitle.getStyleClass().add("hand-title");

        Label handMeta = new Label(buildHandMeta(playerHand));
        handMeta.getStyleClass().add("hand-meta");

        Label handScore = new Label("Score: --");
        handScore.getStyleClass().add("score-label");

        handPanel.getProperties().put(HAND_CARDS_ROW_KEY, cardsRow);
        handPanel.getProperties().put(HAND_SCORE_LABEL_KEY, handScore);
        handPanel.getChildren().addAll(handTitle, handMeta, cardsRow, handScore);
        HBox.setHgrow(handPanel, Priority.ALWAYS);
        return handPanel;
    }

    private SequentialTransition animateDealToDealer(Card card, boolean hidden, int insertionIndex) {
        return animateDealCard(dealerCardsContainer, card, hidden, insertionIndex);
    }

    private SequentialTransition animateDealToPlayer(HBox playerCardsRow, Card card, int insertionIndex) {
        return animateDealCard(playerCardsRow, card, false, insertionIndex);
    }

    private SequentialTransition animateDealCard(
        Pane targetContainer,
        Card card,
        boolean hidden,
        int insertionIndex
    ) {
        Region targetSlot = createDealTargetSlot();
        targetContainer.getChildren().add(insertionIndex, targetSlot);
        targetContainer.applyCss();
        targetContainer.layout();

        StackPane animatedCard = createCardView(card, hidden);
        Point2D startCenter = getDealSourceCenter();
        Point2D targetCenter = toAnimationLayerCenter(targetSlot);
        animatedCard.setOpacity(0.0);
        animatedCard.setLayoutX(startCenter.getX() - (CARD_WIDTH / 2.0));
        animatedCard.setLayoutY(startCenter.getY() - (CARD_HEIGHT / 2.0));
        tableAnimationLayer.getChildren().add(animatedCard);

        PauseTransition delay = new PauseTransition(INITIAL_DEAL_STEP_DELAY);

        FadeTransition fadeIn = new FadeTransition(INITIAL_DEAL_FADE_DURATION, animatedCard);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition move = new TranslateTransition(INITIAL_DEAL_FLIGHT_DURATION, animatedCard);
        move.setToX(targetCenter.getX() - startCenter.getX());
        move.setToY(targetCenter.getY() - startCenter.getY());
        move.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition arrival = new ParallelTransition(fadeIn, move);
        SequentialTransition dealStep = new SequentialTransition(delay, arrival);
        dealStep.setOnFinished(event -> {
            tableAnimationLayer.getChildren().remove(animatedCard);
            targetContainer.getChildren().remove(targetSlot);
            targetContainer.getChildren().add(insertionIndex, createCardView(card, hidden));
        });
        return dealStep;
    }

    private Region createDealTargetSlot() {
        Region targetSlot = new Region();
        targetSlot.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        targetSlot.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        targetSlot.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        targetSlot.setOpacity(0.0);
        return targetSlot;
    }

    private Point2D getDealSourceCenter() {
        Bounds dealerZoneBounds = dealerZoneBox.localToScene(dealerZoneBox.getBoundsInLocal());
        double centerX = dealerZoneBounds.getMinX() + (dealerZoneBounds.getWidth() / 2.0);
        double centerY = dealerZoneBounds.getMinY() + 28.0;
        return tableAnimationLayer.sceneToLocal(centerX, centerY);
    }

    private boolean playPlayerDrawAnimation(int handIndex, int previousCardCount, boolean shouldAnimateDealerReveal) {
        if (tableAnimationLayer == null
            || tableAnimationLayer.getScene() == null
            || handIndex < 0
            || handIndex >= gameEngine.getPlayerHands().size()
            || handIndex >= playerHandsContainer.getChildren().size()) {
            return false;
        }

        PlayerHand updatedHand = gameEngine.getPlayerHands().get(handIndex);
        if (updatedHand.getHand().size() <= previousCardCount) {
            return false;
        }

        Node handNode = playerHandsContainer.getChildren().get(handIndex);
        if (!(handNode instanceof VBox)) {
            return false;
        }

        VBox handPanel = (VBox) handNode;
        Object cardsRowObject = handPanel.getProperties().get(HAND_CARDS_ROW_KEY);
        Object scoreLabelObject = handPanel.getProperties().get(HAND_SCORE_LABEL_KEY);

        if (!(cardsRowObject instanceof HBox) || !(scoreLabelObject instanceof Label)) {
            return false;
        }

        HBox cardsRow = (HBox) cardsRowObject;
        Label handScoreLabel = (Label) scoreLabelObject;
        Card drawnCard = updatedHand.getHand().getCard(previousCardCount);

        animationInProgress = true;
        updateHeader();
        updateDealerArea();
        updateFooter();
        updateControlState();

        Region targetSlot = createDealTargetSlot();
        cardsRow.getChildren().add(previousCardCount, targetSlot);
        cardsRow.applyCss();
        cardsRow.layout();

        StackPane animatedCard = createCardView(drawnCard, false);
        Point2D startCenter = getDealSourceCenter();
        Point2D targetCenter = toAnimationLayerCenter(targetSlot);
        animatedCard.setOpacity(0.0);
        animatedCard.setLayoutX(startCenter.getX() - (CARD_WIDTH / 2.0));
        animatedCard.setLayoutY(startCenter.getY() - (CARD_HEIGHT / 2.0));
        tableAnimationLayer.getChildren().add(animatedCard);

        PauseTransition delay = new PauseTransition(Duration.millis(70));
        FadeTransition fadeIn = new FadeTransition(Duration.millis(130), animatedCard);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition move = new TranslateTransition(Duration.millis(210), animatedCard);
        move.setToX(targetCenter.getX() - startCenter.getX());
        move.setToY(targetCenter.getY() - startCenter.getY());
        move.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition arrival = new ParallelTransition(fadeIn, move);
        SequentialTransition playerDrawSequence = new SequentialTransition(delay, arrival);
        playerDrawSequence.setOnFinished(event -> {
            tableAnimationLayer.getChildren().remove(animatedCard);
            cardsRow.getChildren().remove(targetSlot);
            cardsRow.getChildren().add(previousCardCount, createCardView(drawnCard, false));
            handScoreLabel.setText("Score: " + updatedHand.getHand().getBestValue());
            finishPlayerDrawAnimation(shouldAnimateDealerReveal);
        });
        playerDrawSequence.play();
        return true;
    }

    private void finishPlayerDrawAnimation(boolean shouldAnimateDealerReveal) {
        animationInProgress = false;

        if (!gameEngine.isRoundInProgress() && shouldAnimateDealerReveal) {
            playDealerRevealFlip();
            return;
        }

        updateUi();
    }

    private void finishInitialDealSequence() {
        initialDealAnimationInProgress = false;
        ensureActionBarVisible();

        if (!gameEngine.isRoundInProgress() && shouldAnimateDealerReveal()) {
            playDealerRevealFlip();
            return;
        }

        animationInProgress = false;
        updateUi();
    }

    private boolean shouldAnimateDealerReveal() {
        return dealerCardsContainer.getChildren().size() > 1
            && dealerCardsContainer.getChildren().get(1) instanceof StackPane
            && getCardImageView((StackPane) dealerCardsContainer.getChildren().get(1)) != null;
    }

    private void playDealerRevealFlip() {
        if (gameEngine.getDealer().getHand().size() < 2 || dealerCardsContainer.getChildren().size() < 2) {
            updateUi();
            return;
        }

        StackPane holeCardPane = (StackPane) dealerCardsContainer.getChildren().get(1);
        ImageView holeCardImageView = getCardImageView(holeCardPane);

        if (holeCardImageView == null) {
            updateUi();
            return;
        }

        animationInProgress = true;
        updateUiWithoutDealerArea();
        dealerScoreLabel.setText("Visible score: " + calculateDealerScore(1));

        Card holeCard = gameEngine.getDealer().getHand().getCard(1);
        try {
            cardImageMapper.loadCardImage(
                holeCard,
                false,
                revealedImage -> flipCardImage(
                    holeCardPane,
                    holeCardImageView,
                    revealedImage,
                    () -> {
                        dealerScoreLabel.setText("Score: " + calculateDealerScore(2));
                        playDealerDrawAnimation(2);
                    }
                )
            );
        } catch (IllegalStateException exception) {
            animationInProgress = false;
            updateUi();
        }
    }

    private void flipCardImage(
        StackPane cardPane,
        ImageView cardImageView,
        Image revealedImage,
        Runnable onFinished
    ) {
        ScaleTransition collapse = new ScaleTransition(DEALER_FLIP_HALF_DURATION, cardPane);
        collapse.setFromX(1.0);
        collapse.setToX(0.0);
        collapse.setInterpolator(Interpolator.EASE_BOTH);
        collapse.setOnFinished(event -> cardImageView.setImage(revealedImage));

        ScaleTransition expand = new ScaleTransition(DEALER_FLIP_HALF_DURATION, cardPane);
        expand.setFromX(0.0);
        expand.setToX(1.0);
        expand.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition revealFlip = new SequentialTransition(collapse, expand);
        revealFlip.setOnFinished(event -> onFinished.run());
        revealFlip.play();
    }

    private void playDealerDrawAnimation(int nextCardIndex) {
        if (nextCardIndex >= gameEngine.getDealer().getHand().size()) {
            finishDealerRevealAnimation();
            return;
        }

        Card nextDealerCard = gameEngine.getDealer().getHand().getCard(nextCardIndex);
        StackPane nextCardPane = createCardView(nextDealerCard, false);
        nextCardPane.setOpacity(0.0);
        nextCardPane.setTranslateY(-18.0);
        dealerCardsContainer.getChildren().add(nextCardPane);

        PauseTransition delay = new PauseTransition(DEALER_DRAW_DELAY);
        FadeTransition fadeIn = new FadeTransition(DEALER_DRAW_DURATION, nextCardPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition settle = new TranslateTransition(DEALER_DRAW_DURATION, nextCardPane);
        settle.setFromY(-18.0);
        settle.setToY(0.0);
        settle.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition drawArrival = new ParallelTransition(fadeIn, settle);
        SequentialTransition drawSequence = new SequentialTransition(delay, drawArrival);
        drawSequence.setOnFinished(event -> {
            dealerScoreLabel.setText("Score: " + calculateDealerScore(nextCardIndex + 1));
            playDealerDrawAnimation(nextCardIndex + 1);
        });
        drawSequence.play();
    }

    private void finishDealerRevealAnimation() {
        animationInProgress = false;
        updateUi();
    }

    private void updateUiWithoutDealerArea() {
        updateBetSpinnerRange();
        updateHeader();
        updatePlayerArea();
        refreshRecommendation();
        updateFooter();
        updateControlState();
    }

    private int calculateDealerScore(int visibleCardCount) {
        Hand visibleDealerHand = new Hand();
        Hand dealerHand = gameEngine.getDealer().getHand();
        int cardCount = Math.min(visibleCardCount, dealerHand.size());

        for (int index = 0; index < cardCount; index++) {
            visibleDealerHand.addCard(dealerHand.getCard(index));
        }
        return visibleDealerHand.getBestValue();
    }

    private ImageView getCardImageView(StackPane cardPane) {
        Object imageView = cardPane.getProperties().get(CARD_IMAGE_VIEW_KEY);
        return imageView instanceof ImageView ? (ImageView) imageView : null;
    }

    private void animateChipToBetCircle(Button sourceButton, int chipAmount) {
        StackPane temporaryChip = createTemporaryChipNode(chipAmount);
        Point2D startCenter = toAnimationLayerCenter(sourceButton);
        Point2D targetCenter = toAnimationLayerCenter(betSpotPane).add(calculateChipLandingOffset());

        temporaryChip.setLayoutX(startCenter.getX() - (CHIP_SIZE / 2.0));
        temporaryChip.setLayoutY(startCenter.getY() - (CHIP_SIZE / 2.0));
        temporaryChip.setScaleX(0.92);
        temporaryChip.setScaleY(0.92);

        tableAnimationLayer.getChildren().add(temporaryChip);
        chipAnimationsInFlight++;
        queuedChipBetTotal += chipAmount;
        updateHeader();
        updateControlState();

        TranslateTransition move = new TranslateTransition(CHIP_FLIGHT_DURATION, temporaryChip);
        move.setToX(targetCenter.getX() - startCenter.getX());
        move.setToY(targetCenter.getY() - startCenter.getY());
        move.setInterpolator(Interpolator.EASE_BOTH);

        ScaleTransition scaleUp = new ScaleTransition(CHIP_FLIGHT_DURATION, temporaryChip);
        scaleUp.setToX(CHIP_FLIGHT_SCALE);
        scaleUp.setToY(CHIP_FLIGHT_SCALE);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition flight = new ParallelTransition(move, scaleUp);
        ScaleTransition settle = new ScaleTransition(CHIP_SETTLE_DURATION, temporaryChip);
        settle.setToX(1.0);
        settle.setToY(1.0);
        settle.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition chipAnimation = new SequentialTransition(flight, settle);
        chipAnimation.setOnFinished(event -> {
            tableAnimationLayer.getChildren().remove(temporaryChip);
            chipAnimationsInFlight = Math.max(0, chipAnimationsInFlight - 1);
            queuedChipBetTotal = Math.max(0, queuedChipBetTotal - chipAmount);
            applyChipBet(chipAmount);
        });
        chipAnimation.play();
    }

    private StackPane createTemporaryChipNode(int chipAmount) {
        return createChipVisualNode(chipAmount, CHIP_SIZE, 0.0, "chip-token", "chip-token-text");
    }

    private StackPane createBetStackChipNode(int chipAmount, int stackIndex, int chipIndex) {
        return createChipVisualNode(
            chipAmount,
            BET_STACK_CHIP_SIZE,
            calculateChipRotation(chipAmount, stackIndex, chipIndex),
            "chip-stack-chip",
            "chip-stack-chip-text"
        );
    }

    private StackPane createChipVisualNode(
        int chipAmount,
        double chipSize,
        double rotation,
        String containerStyleClass,
        String textStyleClass
    ) {
        StackPane chipNode = new StackPane();
        chipNode.getStyleClass().addAll(containerStyleClass, toChipValueStyleClass(chipAmount));
        chipNode.setPrefSize(chipSize, chipSize);
        chipNode.setMinSize(chipSize, chipSize);
        chipNode.setMaxSize(chipSize, chipSize);
        chipNode.setRotate(rotation);

        Label chipText = new Label(String.valueOf(chipAmount));
        chipText.getStyleClass().add(textStyleClass);
        chipNode.getChildren().add(chipText);
        return chipNode;
    }

    private void applyChipBet(int chipAmount) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
            (SpinnerValueFactory.IntegerSpinnerValueFactory) betSpinner.getValueFactory();
        int nextBet = Math.min(readSpinnerValueOrDefault() + chipAmount, gameEngine.getPlayer().getChips());
        placedBetChips.add(chipAmount);
        valueFactory.setValue(nextBet);
        betSpinner.getEditor().setText(String.valueOf(nextBet));
        updateHeader();
        updateControlState();
    }

    private void refreshBetChipStack(int displayedBetAmount) {
        betChipStackPane.getChildren().clear();
        if (displayedBetAmount <= 0) {
            chipLandingCount = 0;
            return;
        }

        int[] denominations = {25, 10, 5, 1};
        List<Integer> stackValues = new ArrayList<>();
        List<Integer> stackCounts = new ArrayList<>();

        for (int denomination : denominations) {
            int count = countPlacedChips(denomination);
            if (count > 0) {
                stackValues.add(denomination);
                stackCounts.add(count);
            }
        }

        double startOffset = stackValues.size() <= 1
            ? 0.0
            : -((stackValues.size() - 1) * BET_STACK_COLUMN_SPREAD) / 2.0;

        for (int stackIndex = 0; stackIndex < stackValues.size(); stackIndex++) {
            int chipAmount = stackValues.get(stackIndex);
            int chipCount = stackCounts.get(stackIndex);
            double centerX = 85.0 + startOffset + (stackIndex * BET_STACK_COLUMN_SPREAD);
            double step = chipCount <= 1
                ? 0.0
                : Math.max(BET_STACK_MIN_STEP, Math.min(BET_STACK_MAX_STEP, 58.0 / (chipCount - 1.0)));

            for (int chipIndex = 0; chipIndex < chipCount; chipIndex++) {
                StackPane chipNode = createBetStackChipNode(chipAmount, stackIndex, chipIndex);
                chipNode.setLayoutX(centerX - (BET_STACK_CHIP_SIZE / 2.0));
                chipNode.setLayoutY(BET_STACK_BASELINE - BET_STACK_CHIP_SIZE - (chipIndex * step));
                betChipStackPane.getChildren().add(chipNode);
            }
        }
    }

    private void refreshRoundOutcomeEffects() {
        RoundResult roundResult = gameEngine.getLastResult();
        if (gameEngine.isRoundInProgress() || roundResult == null || !roundResult.isRoundOver()) {
            clearOutcomeEffects();
            lastStyledRoundResult = null;
            hideRoundResultOverlay();
            return;
        }
        if (roundResult == lastStyledRoundResult) {
            return;
        }

        clearOutcomeEffects();
        if (roundResult.getChipDelta() > 0) {
            applyWinGlow();
            syncChipBalanceImmediately();
        } else if (roundResult.getChipDelta() < 0) {
            applyLoseGlow();
            syncChipBalanceImmediately();
        } else {
            applyPushGlow();
            syncChipBalanceImmediately();
        }
        playSettlementChipAnimation(roundResult);
        playRoundResultOverlay(roundResult);
        lastStyledRoundResult = roundResult;
    }

    private void applyWinGlow() {
        addStyleClass(playerZoneBox, "zone-win-glow");
        addStyleClass(betSpotPane, "bet-spot-win-glow");
        addStyleClass(roundStatusLabel, "status-win");
        playBetSpotPulse(1.045, 4);
    }

    private void applyLoseGlow() {
        addStyleClass(dealerZoneBox, "zone-lose-glow");
        addStyleClass(roundStatusLabel, "status-lose");
    }

    private void applyPushGlow() {
        addStyleClass(playerZoneBox, "zone-push-glow");
        addStyleClass(betSpotPane, "bet-spot-push-glow");
        addStyleClass(roundStatusLabel, "status-push");
        playBetSpotPulse(1.02, 2);
    }

    private void playBetSpotPulse(double targetScale, int cycles) {
        stopOutcomePulse();
        ScaleTransition pulse = new ScaleTransition(BET_SPOT_PULSE_DURATION, betSpotPane);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(targetScale);
        pulse.setToY(targetScale);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(cycles);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.setOnFinished(event -> {
            betSpotPane.setScaleX(1.0);
            betSpotPane.setScaleY(1.0);
            outcomePulseAnimation = null;
        });
        outcomePulseAnimation = pulse;
        pulse.play();
    }

    private void clearOutcomeEffects() {
        stopOutcomePulse();
        removeStyleClass(playerZoneBox, "zone-win-glow");
        removeStyleClass(playerZoneBox, "zone-push-glow");
        removeStyleClass(dealerZoneBox, "zone-lose-glow");
        removeStyleClass(betSpotPane, "bet-spot-win-glow");
        removeStyleClass(betSpotPane, "bet-spot-push-glow");
        removeStyleClass(roundStatusLabel, "status-win");
        removeStyleClass(roundStatusLabel, "status-lose");
        removeStyleClass(roundStatusLabel, "status-push");
        betSpotPane.setScaleX(1.0);
        betSpotPane.setScaleY(1.0);
    }

    private void resetRoundVisualState() {
        initialDealAnimationInProgress = false;
        clearOutcomeEffects();
        hideRoundResultOverlay();
        stopChipBalanceAnimation(true);
        lastStyledRoundResult = null;
        ensureBankrollPanelVisible();
        ensureActionBarVisible();
    }

    private void ensureBankrollPanelVisible() {
        if (topInfoBar == null) {
            return;
        }

        topInfoBar.setManaged(true);
        topInfoBar.setVisible(true);
        topInfoBar.setOpacity(1.0);
        topInfoBar.toFront();
    }

    private void ensureActionBarVisible() {
        if (activeRoundActionBar == null) {
            return;
        }

        activeRoundActionBar.setManaged(true);
        activeRoundActionBar.setVisible(true);
        activeRoundActionBar.setOpacity(1.0);
        activeRoundActionBar.toFront();
    }

    private void updateBetAmountGlow(int displayedBetAmount) {
        boolean hasBet = displayedBetAmount > 0;
        toggleStyleClass(currentBetLabel, "bet-amount-active", hasBet);
        toggleStyleClass(tableBetLabel, "bet-total-active", hasBet);
        toggleStyleClass(betSpotPane, "bet-spot-active", hasBet);
    }

    private String formatChipMetric(int amount) {
        return String.valueOf(amount);
    }

    private void updateChipBalanceDisplay() {
        displayedChipBalance = gameEngine.getPlayer().getChips();
        chipBalanceLabel.setText(formatChipMetric(displayedChipBalance));
    }

    private void animateChipBalanceCountUp(int fromValue, int toValue) {
        stopChipBalanceAnimation(false);
        displayedChipBalance = fromValue;
        chipBalanceLabel.setText(formatChipMetric(displayedChipBalance));
        chipBalanceAnimationInProgress = true;

        SimpleIntegerProperty animatedBalance = new SimpleIntegerProperty(fromValue);
        animatedBalance.addListener((observable, oldValue, newValue) -> {
            displayedChipBalance = newValue.intValue();
            chipBalanceLabel.setText(formatChipMetric(displayedChipBalance));
        });

        chipBalanceCountTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(animatedBalance, fromValue)),
            new KeyFrame(CHIP_BALANCE_COUNT_DURATION, new KeyValue(animatedBalance, toValue, Interpolator.EASE_OUT))
        );
        chipBalanceCountTimeline.setOnFinished(event -> {
            displayedChipBalance = toValue;
            chipBalanceLabel.setText(formatChipMetric(displayedChipBalance));
            chipBalanceAnimationInProgress = false;
            chipBalanceCountTimeline = null;
        });
        chipBalanceCountTimeline.play();
    }

    private void syncChipBalanceImmediately() {
        stopChipBalanceAnimation(true);
    }

    private void stopChipBalanceAnimation(boolean syncToActualValue) {
        if (chipBalanceCountTimeline != null) {
            chipBalanceCountTimeline.stop();
            chipBalanceCountTimeline = null;
        }
        chipBalanceAnimationInProgress = false;
        if (syncToActualValue) {
            displayedChipBalance = gameEngine.getPlayer().getChips();
            chipBalanceLabel.setText(formatChipMetric(displayedChipBalance));
        }
    }

    private void stopOutcomePulse() {
        if (outcomePulseAnimation != null) {
            outcomePulseAnimation.stop();
            outcomePulseAnimation = null;
        }
    }

    private void playSettlementChipAnimation(RoundResult roundResult) {
        if (tableAnimationLayer == null || betSpotPane == null) {
            return;
        }

        List<Integer> chipRecipe = buildSyntheticChipRecipe(
            roundResult.getChipDelta() >= 0 ? roundResult.getTotalPayoutAmount() : roundResult.getTotalBetAmount()
        );

        if (chipRecipe.isEmpty()) {
            return;
        }

        Point2D startCenter = toAnimationLayerCenter(betSpotPane);
        Point2D targetCenter = roundResult.getChipDelta() < 0
            ? toAnimationLayerCenter(dealerZoneBox)
            : toAnimationLayerCenter(topInfoBar);
        int chipCount = Math.min(MAX_SETTLEMENT_ANIMATION_CHIPS, chipRecipe.size());

        for (int index = 0; index < chipCount; index++) {
            int chipAmount = chipRecipe.get(index);
            StackPane chipNode = createTemporaryChipNode(chipAmount);
            double offsetX = ((index % 4) - 1.5) * 14.0;
            double offsetY = (index / 4) * 10.0;
            chipNode.setLayoutX(startCenter.getX() - (CHIP_SIZE / 2.0) + offsetX);
            chipNode.setLayoutY(startCenter.getY() - (CHIP_SIZE / 2.0) + offsetY);
            tableAnimationLayer.getChildren().add(chipNode);

            PauseTransition delay = new PauseTransition(Duration.millis(index * 55.0));
            TranslateTransition move = new TranslateTransition(Duration.millis(360), chipNode);
            move.setToX(targetCenter.getX() - startCenter.getX() - offsetX);
            move.setToY(targetCenter.getY() - startCenter.getY() - offsetY);
            move.setInterpolator(Interpolator.EASE_BOTH);

            ScaleTransition scale = new ScaleTransition(Duration.millis(360), chipNode);
            scale.setToX(roundResult.getChipDelta() < 0 ? 0.82 : 0.94);
            scale.setToY(roundResult.getChipDelta() < 0 ? 0.82 : 0.94);
            scale.setInterpolator(Interpolator.EASE_BOTH);

            FadeTransition fade = new FadeTransition(Duration.millis(320), chipNode);
            fade.setFromValue(0.92);
            fade.setToValue(roundResult.getChipDelta() < 0 ? 0.08 : 0.18);
            fade.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition flight = new ParallelTransition(move, scale, fade);
            SequentialTransition sequence = new SequentialTransition(delay, flight);
            sequence.setOnFinished(event -> tableAnimationLayer.getChildren().remove(chipNode));
            sequence.play();
        }
    }

    private void playRoundResultOverlay(RoundResult roundResult) {
        if (roundResultOverlay == null) {
            return;
        }

        hideRoundResultOverlay();

        roundResultOverlayTitleLabel.setText(formatOverlayTitle(roundResult));
        roundResultOverlayDeltaLabel.setText(formatOverlayChipDelta(roundResult));
        roundResultOverlayMessageLabel.setText(roundResult.getMessage());

        roundResultOverlay.getStyleClass().removeAll("overlay-win", "overlay-loss", "overlay-push");
        if (roundResult.getChipDelta() > 0) {
            addStyleClass(roundResultOverlay, "overlay-win");
        } else if (roundResult.getChipDelta() < 0) {
            addStyleClass(roundResultOverlay, "overlay-loss");
        } else {
            addStyleClass(roundResultOverlay, "overlay-push");
        }

        roundResultOverlay.setManaged(true);
        roundResultOverlay.setVisible(true);
        roundResultOverlay.setOpacity(0.0);
        roundResultOverlay.setScaleX(0.94);
        roundResultOverlay.setScaleY(0.94);
        roundResultOverlay.toFront();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), roundResultOverlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(220), roundResultOverlay);
        scaleIn.setFromX(0.94);
        scaleIn.setFromY(0.94);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        PauseTransition hold = new PauseTransition(Duration.millis(1650));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(420), roundResultOverlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition sequence = new SequentialTransition(
            new ParallelTransition(fadeIn, scaleIn),
            hold,
            fadeOut
        );
        sequence.setOnFinished(event -> hideRoundResultOverlay());
        roundResultOverlayAnimation = sequence;
        sequence.play();
    }

    private void hideRoundResultOverlay() {
        if (roundResultOverlayAnimation != null) {
            roundResultOverlayAnimation.stop();
            roundResultOverlayAnimation = null;
        }
        if (roundResultOverlay == null) {
            return;
        }
        roundResultOverlay.setVisible(false);
        roundResultOverlay.setManaged(false);
        roundResultOverlay.setOpacity(0.0);
        roundResultOverlay.setScaleX(1.0);
        roundResultOverlay.setScaleY(1.0);
    }

    private String formatOverlayTitle(RoundResult roundResult) {
        if (roundResult == null) {
            return "ROUND COMPLETE";
        }

        if (roundResult.getOutcome() == RoundOutcome.PLAYER_BLACKJACK) {
            return "BLACKJACK";
        }
        if (roundResult.getChipDelta() > 0) {
            return "PLAYER WINS";
        }
        if (roundResult.getChipDelta() < 0) {
            return "DEALER WINS";
        }
        return "PUSH";
    }

    private String formatOverlayChipDelta(RoundResult roundResult) {
        if (roundResult == null || roundResult.getChipDelta() == 0) {
            return "Bet returned";
        }

        return formatSignedChipMetric(roundResult.getChipDelta()) + " chips";
    }

    private void syncPlacedBetChipsToAmount(int displayedBetAmount) {
        if (sumPlacedChips() == displayedBetAmount) {
            return;
        }
        placedBetChips.clear();
        placedBetChips.addAll(buildSyntheticChipRecipe(displayedBetAmount));
        if (displayedBetAmount <= 0) {
            chipLandingCount = 0;
        }
    }

    private List<Integer> buildSyntheticChipRecipe(int amount) {
        List<Integer> chips = new ArrayList<>();
        int remaining = Math.max(amount, 0);
        int[] denominations = {25, 10, 5, 1};

        for (int denomination : denominations) {
            while (remaining >= denomination) {
                chips.add(denomination);
                remaining -= denomination;
            }
        }
        return chips;
    }

    private int sumPlacedChips() {
        int total = 0;
        for (int chipValue : placedBetChips) {
            total += chipValue;
        }
        return total;
    }

    private int countPlacedChips(int denomination) {
        int count = 0;
        for (int chipValue : placedBetChips) {
            if (chipValue == denomination) {
                count++;
            }
        }
        return count;
    }

    private double calculateChipRotation(int chipAmount, int stackIndex, int chipIndex) {
        int seed = (chipAmount * 31) + (stackIndex * 17) + (chipIndex * 13);
        return ((seed % 9) - 4) * 0.75;
    }

    private int parseChipAmount(Button chipButton) {
        Object userData = chipButton.getUserData();
        if (userData == null) {
            return 0;
        }
        try {
            return Integer.parseInt(userData.toString());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private Point2D toAnimationLayerCenter(Node node) {
        Bounds sceneBounds = node.localToScene(node.getBoundsInLocal());
        double centerX = sceneBounds.getMinX() + (sceneBounds.getWidth() / 2.0);
        double centerY = sceneBounds.getMinY() + (sceneBounds.getHeight() / 2.0);
        return tableAnimationLayer.sceneToLocal(centerX, centerY);
    }

    private Point2D calculateChipLandingOffset() {
        int offsetIndex = chipLandingCount++;
        double angle = Math.toRadians((offsetIndex % 6) * 60.0);
        double radius = 9.0 + ((offsetIndex / 6) % 2) * 6.0;
        if (offsetIndex == 0) {
            return Point2D.ZERO;
        }
        return new Point2D(Math.cos(angle) * radius, Math.sin(angle) * radius);
    }

    private String toChipValueStyleClass(int chipAmount) {
        switch (chipAmount) {
            case 1:
                return "chip-value-1";
            case 5:
                return "chip-value-5";
            case 10:
                return "chip-value-10";
            case 25:
                return "chip-value-25";
            default:
                return "chip-value-generic";
        }
    }

    private void setSelectedChipAmount(int chipAmount) {
        selectedChipAmount = chipAmount;
        updateSelectedChipButtonStyles();
    }

    private void updateSelectedChipButtonStyles() {
        updateChipButtonSelection(chip1Button, 1);
        updateChipButtonSelection(chip5Button, 5);
        updateChipButtonSelection(chip10Button, 10);
        updateChipButtonSelection(chip25Button, 25);
    }

    private void updateChipButtonSelection(Button button, int chipAmount) {
        if (button == null) {
            return;
        }
        toggleStyleClass(button, "chip-button-selected", selectedChipAmount == chipAmount);
    }

    private void toggleStyleClass(Node node, String styleClass, boolean enabled) {
        if (enabled) {
            addStyleClass(node, styleClass);
        } else {
            removeStyleClass(node, styleClass);
        }
    }

    private void addStyleClass(Node node, String styleClass) {
        if (!node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }
    }

    private void removeStyleClass(Node node, String styleClass) {
        node.getStyleClass().remove(styleClass);
    }
}
