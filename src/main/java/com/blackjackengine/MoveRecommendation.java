package com.blackjackengine;

public class MoveRecommendation {
    public enum Confidence {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        private final String label;

        Confidence(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum WinTendency {
        FAVORABLE("Favorable"),
        BALANCED("Balanced"),
        UNFAVORABLE("Dealer-favored");

        private final String label;

        WinTendency(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum RiskLevel {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High");

        private final String label;

        RiskLevel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final PlayerAction action;
    private final String reason;
    private final Confidence confidence;
    private final WinTendency winTendency;
    private final RiskLevel riskLevel;
    private final double expectedValue;
    private final double bustRisk;
    private final double dealerBustChance;

    public MoveRecommendation(PlayerAction action, String reason) {
        this(
            action,
            reason,
            Confidence.MEDIUM,
            WinTendency.BALANCED,
            RiskLevel.MEDIUM,
            0.0,
            0.0,
            0.0
        );
    }

    public MoveRecommendation(
        PlayerAction action,
        String reason,
        Confidence confidence,
        WinTendency winTendency,
        RiskLevel riskLevel,
        double expectedValue,
        double bustRisk,
        double dealerBustChance
    ) {
        this.action = action;
        this.reason = reason;
        this.confidence = confidence;
        this.winTendency = winTendency;
        this.riskLevel = riskLevel;
        this.expectedValue = expectedValue;
        this.bustRisk = bustRisk;
        this.dealerBustChance = dealerBustChance;
    }

    public PlayerAction getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public WinTendency getWinTendency() {
        return winTendency;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public double getExpectedValue() {
        return expectedValue;
    }

    public double getBustRisk() {
        return bustRisk;
    }

    public double getDealerBustChance() {
        return dealerBustChance;
    }
}
