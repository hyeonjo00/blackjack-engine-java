package com.blackjackengine;

public class HandResult {
    private final int handNumber;
    private final RoundOutcome outcome;
    private final String message;
    private final String handDescription;
    private final int handValue;
    private final int betAmount;
    private final int payoutAmount;
    private final int chipDelta;
    private final boolean splitHand;
    private final boolean doubledDown;

    public HandResult(
        int handNumber,
        RoundOutcome outcome,
        String message,
        String handDescription,
        int handValue,
        int betAmount,
        int payoutAmount,
        int chipDelta,
        boolean splitHand,
        boolean doubledDown
    ) {
        this.handNumber = handNumber;
        this.outcome = outcome;
        this.message = message;
        this.handDescription = handDescription;
        this.handValue = handValue;
        this.betAmount = betAmount;
        this.payoutAmount = payoutAmount;
        this.chipDelta = chipDelta;
        this.splitHand = splitHand;
        this.doubledDown = doubledDown;
    }

    public int getHandNumber() {
        return handNumber;
    }

    public RoundOutcome getOutcome() {
        return outcome;
    }

    public String getMessage() {
        return message;
    }

    public String getHandDescription() {
        return handDescription;
    }

    public int getHandValue() {
        return handValue;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public int getPayoutAmount() {
        return payoutAmount;
    }

    public int getChipDelta() {
        return chipDelta;
    }

    public boolean isSplitHand() {
        return splitHand;
    }

    public boolean isDoubledDown() {
        return doubledDown;
    }
}
