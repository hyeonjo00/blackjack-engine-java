package com.blackjackengine;

public class RecommendationDecision {
    private final int handNumber;
    private final PlayerAction recommendedAction;
    private final String recommendationReason;
    private final PlayerAction actualAction;
    private final boolean followedRecommendation;
    private RoundOutcome resolvedOutcome;
    private int resolvedChipDelta;

    public RecommendationDecision(
        int handNumber,
        PlayerAction recommendedAction,
        String recommendationReason,
        PlayerAction actualAction
    ) {
        this.handNumber = handNumber;
        this.recommendedAction = recommendedAction;
        this.recommendationReason = recommendationReason;
        this.actualAction = actualAction;
        this.followedRecommendation = recommendedAction == actualAction;
    }

    public int getHandNumber() {
        return handNumber;
    }

    public PlayerAction getRecommendedAction() {
        return recommendedAction;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public PlayerAction getActualAction() {
        return actualAction;
    }

    public boolean isFollowedRecommendation() {
        return followedRecommendation;
    }

    public boolean isIgnoredRecommendation() {
        return !followedRecommendation;
    }

    public RoundOutcome getResolvedOutcome() {
        return resolvedOutcome;
    }

    public int getResolvedChipDelta() {
        return resolvedChipDelta;
    }

    public boolean isResolved() {
        return resolvedOutcome != null;
    }

    public void attachHandResult(HandResult handResult) {
        if (handResult == null) {
            throw new IllegalArgumentException("Hand result cannot be null.");
        }

        resolvedOutcome = handResult.getOutcome();
        resolvedChipDelta = handResult.getChipDelta();
    }
}
