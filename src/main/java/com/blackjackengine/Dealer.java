package com.blackjackengine;

public class Dealer {
    private final Hand hand = new Hand();

    public Dealer() {
        // Dealer keeps only round state, not chips.
    }

    public Hand getHand() {
        return hand;
    }

    public void clearHand() {
        hand.clear();
    }

    public boolean shouldDraw() {
        return hand.getBestValue() < 17;
    }
}
