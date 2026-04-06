package com.blackjackengine;

public class Player {
    private final String name;
    private int chips;

    public Player(String name, int startingChips) {
        this.name = name;
        this.chips = startingChips;
    }

    public String getName() {
        return name;
    }

    public int getChips() {
        return chips;
    }

    public boolean canBet(int amount) {
        return amount > 0 && amount <= chips;
    }

    public void commitBet(int amount) {
        if (!canBet(amount)) {
            throw new IllegalArgumentException("Bet must be at least 1 and no more than your available chips.");
        }

        chips -= amount;
    }

    public void receivePayout(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payout amount cannot be negative.");
        }

        chips += amount;
    }

    public void adjustChips(int amount) {
        chips += amount;
    }
}
