package com.blackjackengine;

import java.util.Collections;
import java.util.List;

public class RoundResult {
    private final RoundOutcome outcome;
    private final String message;
    private final int chipDelta;
    private final int totalBetAmount;
    private final int totalPayoutAmount;
    private final int splitCount;
    private final int doubleDownCount;
    private final List<HandResult> handResults;

    public RoundResult(
        RoundOutcome outcome,
        String message,
        int chipDelta,
        int totalBetAmount,
        int totalPayoutAmount,
        int splitCount,
        int doubleDownCount,
        List<HandResult> handResults
    ) {
        this.outcome = outcome;
        this.message = message;
        this.chipDelta = chipDelta;
        this.totalBetAmount = totalBetAmount;
        this.totalPayoutAmount = totalPayoutAmount;
        this.splitCount = splitCount;
        this.doubleDownCount = doubleDownCount;
        this.handResults = List.copyOf(handResults);
    }

    public static RoundResult inProgress(int totalBetAmount, String message) {
        return new RoundResult(
            RoundOutcome.IN_PROGRESS,
            message,
            0,
            totalBetAmount,
            0,
            0,
            0,
            Collections.emptyList()
        );
    }

    public RoundOutcome getOutcome() {
        return outcome;
    }

    public String getMessage() {
        return message;
    }

    public int getChipDelta() {
        return chipDelta;
    }

    public int getTotalBetAmount() {
        return totalBetAmount;
    }

    public int getTotalPayoutAmount() {
        return totalPayoutAmount;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public int getDoubleDownCount() {
        return doubleDownCount;
    }

    public List<HandResult> getHandResults() {
        return handResults;
    }

    public boolean isRoundOver() {
        return outcome != RoundOutcome.IN_PROGRESS;
    }
}
