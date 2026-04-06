package com.blackjackengine;

public class PlayerHand {
    private final Hand hand;
    private int bet;
    private boolean fromSplit;
    private boolean doubledDown;
    private boolean completed;

    public PlayerHand(int bet, boolean fromSplit) {
        this.hand = new Hand();
        this.bet = bet;
        this.fromSplit = fromSplit;
    }

    public Hand getHand() {
        return hand;
    }

    public int getBet() {
        return bet;
    }

    public boolean isFromSplit() {
        return fromSplit;
    }

    public boolean isDoubledDown() {
        return doubledDown;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isNaturalBlackjack() {
        return !fromSplit && hand.isBlackjack();
    }

    public boolean canSplit() {
        return hand.hasPairOfSameRank();
    }

    public boolean canDoubleDown() {
        return hand.size() == 2 && !doubledDown && !completed;
    }

    public void addCard(Card card) {
        hand.addCard(card);
    }

    public void markCompleted() {
        completed = true;
    }

    public PlayerHand splitOff() {
        if (!canSplit()) {
            throw new IllegalStateException("This hand cannot be split.");
        }

        Card movedCard = hand.removeCardAt(1);
        fromSplit = true;

        PlayerHand newHand = new PlayerHand(bet, true);
        newHand.addCard(movedCard);
        return newHand;
    }

    public void doubleBet() {
        if (!canDoubleDown()) {
            throw new IllegalStateException("This hand cannot double down.");
        }

        bet *= 2;
        doubledDown = true;
    }
}
