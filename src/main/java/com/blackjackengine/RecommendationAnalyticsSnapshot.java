package com.blackjackengine;

public class RecommendationAnalyticsSnapshot {
    private final int totalRecommendationsShown;
    private final int totalRecommendationsFollowed;
    private final int totalRecommendationsIgnored;
    private final int resolvedFollowedCount;
    private final int resolvedIgnoredCount;
    private final int followedWinCount;
    private final int ignoredWinCount;
    private final int followedPushCount;
    private final int ignoredPushCount;
    private final int followedChipProfitLoss;
    private final int ignoredChipProfitLoss;
    private final int ignoredHitRecommendations;
    private final int ignoredStandRecommendations;
    private final int ignoredDoubleDownRecommendations;
    private final int ignoredSplitRecommendations;

    public RecommendationAnalyticsSnapshot(
        int totalRecommendationsShown,
        int totalRecommendationsFollowed,
        int totalRecommendationsIgnored,
        int resolvedFollowedCount,
        int resolvedIgnoredCount,
        int followedWinCount,
        int ignoredWinCount,
        int followedPushCount,
        int ignoredPushCount,
        int followedChipProfitLoss,
        int ignoredChipProfitLoss,
        int ignoredHitRecommendations,
        int ignoredStandRecommendations,
        int ignoredDoubleDownRecommendations,
        int ignoredSplitRecommendations
    ) {
        this.totalRecommendationsShown = totalRecommendationsShown;
        this.totalRecommendationsFollowed = totalRecommendationsFollowed;
        this.totalRecommendationsIgnored = totalRecommendationsIgnored;
        this.resolvedFollowedCount = resolvedFollowedCount;
        this.resolvedIgnoredCount = resolvedIgnoredCount;
        this.followedWinCount = followedWinCount;
        this.ignoredWinCount = ignoredWinCount;
        this.followedPushCount = followedPushCount;
        this.ignoredPushCount = ignoredPushCount;
        this.followedChipProfitLoss = followedChipProfitLoss;
        this.ignoredChipProfitLoss = ignoredChipProfitLoss;
        this.ignoredHitRecommendations = ignoredHitRecommendations;
        this.ignoredStandRecommendations = ignoredStandRecommendations;
        this.ignoredDoubleDownRecommendations = ignoredDoubleDownRecommendations;
        this.ignoredSplitRecommendations = ignoredSplitRecommendations;
    }

    public int getTotalRecommendationsShown() {
        return totalRecommendationsShown;
    }

    public int getTotalRecommendationsFollowed() {
        return totalRecommendationsFollowed;
    }

    public int getTotalRecommendationsIgnored() {
        return totalRecommendationsIgnored;
    }

    public int getResolvedFollowedCount() {
        return resolvedFollowedCount;
    }

    public int getResolvedIgnoredCount() {
        return resolvedIgnoredCount;
    }

    public int getFollowedWinCount() {
        return followedWinCount;
    }

    public int getIgnoredWinCount() {
        return ignoredWinCount;
    }

    public int getFollowedPushCount() {
        return followedPushCount;
    }

    public int getIgnoredPushCount() {
        return ignoredPushCount;
    }

    public int getFollowedChipProfitLoss() {
        return followedChipProfitLoss;
    }

    public int getIgnoredChipProfitLoss() {
        return ignoredChipProfitLoss;
    }

    public int getIgnoredHitRecommendations() {
        return ignoredHitRecommendations;
    }

    public int getIgnoredStandRecommendations() {
        return ignoredStandRecommendations;
    }

    public int getIgnoredDoubleDownRecommendations() {
        return ignoredDoubleDownRecommendations;
    }

    public int getIgnoredSplitRecommendations() {
        return ignoredSplitRecommendations;
    }
}
