package com.blackjackengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {
    private static final int STARTING_CHIPS = 100;

    private final Player player;
    private final Dealer dealer;
    private final GameStatistics statistics;
    private final List<PlayerHand> playerHands;
    private Deck deck;
    private int activeHandIndex;
    private boolean roundInProgress;
    private RoundResult lastResult;
    private int splitCountThisRound;
    private int doubleDownCountThisRound;

    public GameEngine(String playerName) {
        this(playerName, STARTING_CHIPS);
    }

    public GameEngine(String playerName, int startingChips) {
        if (startingChips < 1) {
            throw new IllegalArgumentException("Starting chips must be at least 1.");
        }

        this.player = new Player(playerName, startingChips);
        this.dealer = new Dealer();
        this.statistics = new GameStatistics();
        this.playerHands = new ArrayList<>();
        this.deck = new Deck();
        this.activeHandIndex = 0;
        this.roundInProgress = false;
        this.lastResult = RoundResult.inProgress(0, "Ready to play.");
    }

    public Player getPlayer() {
        return player;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public Card getDealerUpCard() {
        if (dealer.getHand().size() == 0) {
            return null;
        }

        return dealer.getHand().getCard(0);
    }

    public GameStatistics getStatistics() {
        return statistics;
    }

    public List<PlayerHand> getPlayerHands() {
        return Collections.unmodifiableList(playerHands);
    }

    public int getActiveHandIndex() {
        return activeHandIndex;
    }

    public PlayerHand getActiveHand() {
        if (!roundInProgress || playerHands.isEmpty()) {
            return null;
        }

        return playerHands.get(activeHandIndex);
    }

    public int getTotalCommittedBet() {
        return playerHands.stream()
            .mapToInt(PlayerHand::getBet)
            .sum();
    }

    public boolean isRoundInProgress() {
        return roundInProgress;
    }

    public RoundResult getLastResult() {
        return lastResult;
    }

    public void applySessionSnapshot(SessionSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Session snapshot cannot be null.");
        }

        playerHands.clear();
        dealer.clearHand();
        deck = new Deck();
        activeHandIndex = 0;
        roundInProgress = false;
        splitCountThisRound = 0;
        doubleDownCountThisRound = 0;
        lastResult = RoundResult.inProgress(0, "Session loaded. Ready for the next hand.");

        int chipAdjustment = snapshot.getCurrentChips() - player.getChips();
        if (chipAdjustment != 0) {
            player.adjustChips(chipAdjustment);
        }

        statistics.restore(snapshot.getStatisticsSnapshot());
    }

    public void startRound(int bet) {
        if (roundInProgress) {
            throw new IllegalStateException("A round is already in progress.");
        }

        player.commitBet(bet);

        playerHands.clear();
        dealer.clearHand();
        deck = new Deck();
        deck.shuffle();
        activeHandIndex = 0;
        splitCountThisRound = 0;
        doubleDownCountThisRound = 0;

        PlayerHand openingHand = new PlayerHand(bet, false);
        playerHands.add(openingHand);

        dealCardTo(openingHand);
        dealCardToDealer();
        dealCardTo(openingHand);
        dealCardToDealer();

        roundInProgress = true;
        lastResult = RoundResult.inProgress(getTotalCommittedBet(), "Round in progress. Playing Hand 1.");

        if (openingHand.isNaturalBlackjack() || dealer.getHand().isBlackjack()) {
            settleInitialBlackjacks();
        }
    }

    public RoundResult playerHit() {
        ensureRoundInProgress();
        PlayerHand activeHand = getRequiredActiveHand();
        dealCardTo(activeHand);

        if (activeHand.getHand().isBust()) {
            activeHand.markCompleted();
            return advanceAfterCompletedHand(getHandLabel(activeHandIndex) + " busted.");
        }

        if (activeHand.getHand().getBestValue() == 21) {
            activeHand.markCompleted();
            return advanceAfterCompletedHand(getHandLabel(activeHandIndex) + " reached 21 and stands.");
        }

        lastResult = RoundResult.inProgress(
            getTotalCommittedBet(),
            getHandLabel(activeHandIndex) + " chose hit."
        );
        return lastResult;
    }

    public RoundResult playerStand() {
        ensureRoundInProgress();
        getRequiredActiveHand().markCompleted();
        return advanceAfterCompletedHand(getHandLabel(activeHandIndex) + " stands.");
    }

    public RoundResult playerDoubleDown() {
        ensureRoundInProgress();

        if (!canDoubleDown()) {
            throw new IllegalStateException("Double down is not available for this hand.");
        }

        PlayerHand activeHand = getRequiredActiveHand();
        player.commitBet(activeHand.getBet());
        activeHand.doubleBet();
        doubleDownCountThisRound++;
        dealCardTo(activeHand);
        activeHand.markCompleted();

        if (activeHand.getHand().isBust()) {
            return advanceAfterCompletedHand(getHandLabel(activeHandIndex) + " busted after doubling down.");
        }

        return advanceAfterCompletedHand(getHandLabel(activeHandIndex) + " doubled down and stands.");
    }

    public RoundResult playerSplit() {
        ensureRoundInProgress();

        if (!canSplit()) {
            throw new IllegalStateException("Split is not available for this hand.");
        }

        PlayerHand activeHand = getRequiredActiveHand();
        player.commitBet(activeHand.getBet());
        PlayerHand newHand = activeHand.splitOff();
        playerHands.add(activeHandIndex + 1, newHand);
        splitCountThisRound++;

        dealCardTo(activeHand);
        dealCardTo(newHand);

        if (activeHand.getHand().getBestValue() == 21) {
            activeHand.markCompleted();
            return advanceAfterCompletedHand("Split complete. " + getHandLabel(activeHandIndex) + " reached 21.");
        }

        lastResult = RoundResult.inProgress(
            getTotalCommittedBet(),
            "Split complete. Continue playing " + getHandLabel(activeHandIndex) + "."
        );
        return lastResult;
    }

    public boolean canDoubleDown() {
        if (!roundInProgress) {
            return false;
        }

        PlayerHand activeHand = getRequiredActiveHand();
        return activeHand.canDoubleDown() && canCommitAdditionalBet(activeHand.getBet());
    }

    public boolean canSplit() {
        if (!roundInProgress || splitCountThisRound > 0) {
            return false;
        }

        PlayerHand activeHand = getRequiredActiveHand();
        return activeHand.canSplit() && canCommitAdditionalBet(activeHand.getBet());
    }

    public List<PlayerAction> getAvailableActions() {
        if (!roundInProgress) {
            return Collections.emptyList();
        }

        List<PlayerAction> actions = new ArrayList<>();
        actions.add(PlayerAction.HIT);
        actions.add(PlayerAction.STAND);

        if (canDoubleDown()) {
            actions.add(PlayerAction.DOUBLE_DOWN);
        }

        if (canSplit()) {
            actions.add(PlayerAction.SPLIT);
        }

        return actions;
    }

    private void settleInitialBlackjacks() {
        PlayerHand openingHand = playerHands.get(0);
        List<HandResult> handResults = new ArrayList<>();

        if (openingHand.isNaturalBlackjack() && dealer.getHand().isBlackjack()) {
            handResults.add(
                buildHandResult(
                    0,
                    openingHand,
                    RoundOutcome.PUSH,
                    openingHand.getBet(),
                    0,
                    "Both have natural blackjack. Push."
                )
            );
            finishRound(RoundOutcome.PUSH, "Both hands are natural blackjack. Push.", handResults);
            return;
        }

        if (openingHand.isNaturalBlackjack()) {
            handResults.add(
                buildHandResult(
                    0,
                    openingHand,
                    RoundOutcome.PLAYER_BLACKJACK,
                    calculateWinningPayout(openingHand),
                    openingHand.getBet(),
                    "Natural blackjack. You win."
                )
            );
            finishRound(RoundOutcome.PLAYER_BLACKJACK, "Natural blackjack. You win.", handResults);
            return;
        }

        handResults.add(
            buildHandResult(
                0,
                openingHand,
                RoundOutcome.DEALER_BLACKJACK,
                0,
                -openingHand.getBet(),
                "Dealer has natural blackjack. You lose."
            )
        );
        finishRound(RoundOutcome.DEALER_BLACKJACK, "Dealer has natural blackjack. You lose.", handResults);
    }

    private RoundResult advanceAfterCompletedHand(String actionMessage) {
        while (true) {
            int nextHandIndex = findNextOpenHand();

            if (nextHandIndex == -1) {
                roundInProgress = false;
                return settleRound(actionMessage);
            }

            activeHandIndex = nextHandIndex;
            PlayerHand activeHand = playerHands.get(activeHandIndex);

            if (activeHand.getHand().isBust() || activeHand.getHand().getBestValue() == 21) {
                activeHand.markCompleted();
                continue;
            }

            lastResult = RoundResult.inProgress(
                getTotalCommittedBet(),
                actionMessage + " Now playing " + getHandLabel(activeHandIndex) + "."
            );
            return lastResult;
        }
    }

    private int findNextOpenHand() {
        for (int index = 0; index < playerHands.size(); index++) {
            if (!playerHands.get(index).isCompleted()) {
                return index;
            }
        }

        return -1;
    }

    private RoundResult settleRound(String actionMessage) {
        List<HandResult> handResults = new ArrayList<>();
        boolean allPlayerHandsBusted = playerHands.stream()
            .allMatch(playerHand -> playerHand.getHand().isBust());

        if (!allPlayerHandsBusted) {
            playDealerTurn();
        }

        for (int index = 0; index < playerHands.size(); index++) {
            PlayerHand playerHand = playerHands.get(index);
            HandResult handResult;

            if (playerHand.getHand().isBust()) {
                handResult = buildHandResult(index, playerHand, RoundOutcome.PLAYER_BUST, 0, -playerHand.getBet(), "Busted.");
            } else if (dealer.getHand().isBust()) {
                handResult = buildHandResult(
                    index,
                    playerHand,
                    RoundOutcome.DEALER_BUST,
                    calculateWinningPayout(playerHand),
                    playerHand.getBet(),
                    "Dealer busted. You win."
                );
            } else {
                handResult = compareAgainstDealer(index, playerHand);
            }

            handResults.add(handResult);
        }

        int totalPayoutAmount = handResults.stream()
            .mapToInt(HandResult::getPayoutAmount)
            .sum();
        int chipDelta = totalPayoutAmount - getTotalCommittedBet();

        return finishRound(
            determineSummaryOutcome(handResults),
            buildRoundSummaryMessage(actionMessage, chipDelta, totalPayoutAmount, handResults),
            handResults,
            totalPayoutAmount,
            chipDelta
        );
    }

    private HandResult compareAgainstDealer(int handIndex, PlayerHand playerHand) {
        int playerValue = playerHand.getHand().getBestValue();
        int dealerValue = dealer.getHand().getBestValue();

        if (playerValue > dealerValue) {
            return buildHandResult(
                handIndex,
                playerHand,
                RoundOutcome.PLAYER_WIN,
                calculateWinningPayout(playerHand),
                playerHand.getBet(),
                "Beat the dealer."
            );
        }

        if (dealerValue > playerValue) {
            return buildHandResult(handIndex, playerHand, RoundOutcome.DEALER_WIN, 0, -playerHand.getBet(), "Dealer wins.");
        }

        return buildHandResult(handIndex, playerHand, RoundOutcome.PUSH, playerHand.getBet(), 0, "Push.");
    }

    private String buildRoundSummaryMessage(
        String actionMessage,
        int chipDelta,
        int totalPayoutAmount,
        List<HandResult> handResults
    ) {
        if (handResults.size() == 1) {
            return handResults.get(0).getMessage();
        }

        String settlementSummary = buildRoundSettlementSummary(chipDelta, totalPayoutAmount);

        if (chipDelta > 0) {
            return actionMessage + " Split round complete. " + settlementSummary;
        }

        if (chipDelta < 0) {
            return actionMessage + " Split round complete. " + settlementSummary;
        }

        return actionMessage + " Split round complete. " + settlementSummary;
    }

    private RoundResult finishRound(RoundOutcome outcome, String message, List<HandResult> handResults) {
        int totalPayoutAmount = handResults.stream()
            .mapToInt(HandResult::getPayoutAmount)
            .sum();
        int chipDelta = handResults.stream()
            .mapToInt(HandResult::getChipDelta)
            .sum();
        return finishRound(outcome, message, handResults, totalPayoutAmount, chipDelta);
    }

    private RoundResult finishRound(
        RoundOutcome outcome,
        String message,
        List<HandResult> handResults,
        int totalPayoutAmount,
        int chipDelta
    ) {
        player.receivePayout(totalPayoutAmount);
        roundInProgress = false;
        lastResult = new RoundResult(
            outcome,
            message,
            chipDelta,
            getTotalCommittedBet(),
            totalPayoutAmount,
            splitCountThisRound,
            doubleDownCountThisRound,
            handResults
        );
        statistics.recordRound(lastResult);
        return lastResult;
    }

    private RoundOutcome determineSummaryOutcome(List<HandResult> handResults) {
        RoundOutcome firstOutcome = handResults.get(0).getOutcome();

        for (HandResult handResult : handResults) {
            if (handResult.getOutcome() != firstOutcome) {
                return RoundOutcome.MIXED_RESULTS;
            }
        }

        return firstOutcome;
    }

    private HandResult buildHandResult(
        int handIndex,
        PlayerHand playerHand,
        RoundOutcome outcome,
        int payoutAmount,
        int chipDelta,
        String message
    ) {
        String settlementMessage = buildSettlementMessage(message, playerHand.getBet(), payoutAmount);
        return new HandResult(
            handIndex + 1,
            outcome,
            settlementMessage,
            playerHand.getHand().toString(),
            playerHand.getHand().getBestValue(),
            playerHand.getBet(),
            payoutAmount,
            chipDelta,
            playerHand.isFromSplit(),
            playerHand.isDoubledDown()
        );
    }

    private void playDealerTurn() {
        while (dealer.shouldDraw()) {
            dealCardToDealer();
        }
    }

    private void dealCardTo(PlayerHand playerHand) {
        playerHand.addCard(deck.drawCard());
    }

    private void dealCardToDealer() {
        dealer.getHand().addCard(deck.drawCard());
    }

    private boolean canCommitAdditionalBet(int amount) {
        return amount > 0 && amount <= player.getChips();
    }

    private String getHandLabel(int handIndex) {
        return "Hand " + (handIndex + 1);
    }

    private PlayerHand getRequiredActiveHand() {
        PlayerHand activeHand = getActiveHand();

        if (activeHand == null) {
            throw new IllegalStateException("No active hand is available.");
        }

        return activeHand;
    }

    private void ensureRoundInProgress() {
        if (!roundInProgress) {
            throw new IllegalStateException("No round is currently in progress.");
        }
    }

    private int calculateWinningPayout(PlayerHand playerHand) {
        return playerHand.getBet() * 2;
    }

    private String buildSettlementMessage(String baseMessage, int betAmount, int payoutAmount) {
        if (payoutAmount <= 0) {
            return baseMessage + " Lost " + betAmount + " chips.";
        }

        if (payoutAmount == betAmount) {
            return baseMessage + " Bet returned.";
        }

        return baseMessage + " Bet returned plus " + (payoutAmount - betAmount) + " chips.";
    }

    private String buildRoundSettlementSummary(int chipDelta, int totalPayoutAmount) {
        if (chipDelta > 0) {
            return "Returned " + totalPayoutAmount + " chips total for a net win of " + chipDelta + ".";
        }

        if (chipDelta < 0) {
            if (totalPayoutAmount > 0) {
                return "Returned " + totalPayoutAmount + " chips total for a net loss of " + Math.abs(chipDelta) + ".";
            }

            return "No chips returned. Net loss of " + Math.abs(chipDelta) + ".";
        }

        return "Returned " + totalPayoutAmount + " chips total. Push overall.";
    }
}
