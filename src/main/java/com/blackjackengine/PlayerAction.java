package com.blackjackengine;

public enum PlayerAction {
    HIT("H", "Hit"),
    STAND("S", "Stand"),
    DOUBLE_DOWN("D", "Double Down"),
    SPLIT("P", "Split");

    private final String shortCode;
    private final String label;

    PlayerAction(String shortCode, String label) {
        this.shortCode = shortCode;
        this.label = label;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLabel() {
        return label;
    }
}
