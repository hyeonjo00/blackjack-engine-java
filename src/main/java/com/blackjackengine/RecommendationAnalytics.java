package com.blackjackengine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RecommendationAnalytics {
    private final List<RecommendationDecision> pendingDecisions = new ArrayList<>();
    private final Map<PlayerAction, Integer> ignoredRecommendationCounts = new EnumMap<>(PlayerAction.class);
    private int totalRecommendationsShown;
    private int totalRecommendationsFollowed;
    private int totalRecommendationsIgnored;
    private int resolvedFollowedCount;
    private int resolvedIgnoredCount;
    private int followedWinCount;
    private int ignoredWinCount;
    private int followedPushCount;
    private int ignoredPushCount;
    private int followedChipProfitLoss;
    private int ignoredChipProfitLoss;

    public RecommendationDecision recordDecision(
        int handNumber,
        MoveRecommendation recommendation,
        PlayerAction actualAction
    ) {
        RecommendationDecision decision = new RecommendationDecision(
            handNumber,
            recommendation.getAction(),
            recommendation.getReason(),
            actualAction
        );

        pendingDecisions.add(decision);
        totalRecommendationsShown++;

        if (decision.isFollowedRecommendation()) {
            totalRecommendationsFollowed++;
        } else {
            totalRecommendationsIgnored++;
            ignoredRecommendationCounts.merge(decision.getRecommendedAction(), 1, Integer::sum);
        }
        return decision;
    }

    public void resolveRound(RoundResult roundResult) {
        if (roundResult == null || !roundResult.isRoundOver() || pendingDecisions.isEmpty()) {
            return;
        }

        for (RecommendationDecision decision : pendingDecisions) {
            HandResult handResult = findHandResult(roundResult, decision.getHandNumber());

            if (handResult == null) {
                throw new IllegalStateException(
                    "Missing hand result for recommendation decision on hand "
                        + decision.getHandNumber()
                        + "."
                );
            }

            decision.attachHandResult(handResult);
            accumulateResolvedDecision(decision);
        }

        pendingDecisions.clear();
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

    public double getOverallFollowRate() {
        if (totalRecommendationsShown == 0) {
            return -1.0;
        }

        return (double) totalRecommendationsFollowed / totalRecommendationsShown;
    }

    public int getResolvedFollowedCount() {
        return resolvedFollowedCount;
    }

    public int getResolvedIgnoredCount() {
        return resolvedIgnoredCount;
    }

    public double getFollowedWinRate() {
        return calculateRate(resolvedFollowedCount, followedWinCount);
    }

    public double getIgnoredWinRate() {
        return calculateRate(resolvedIgnoredCount, ignoredWinCount);
    }

    public double getFollowedPushRate() {
        return calculateRate(resolvedFollowedCount, followedPushCount);
    }

    public double getIgnoredPushRate() {
        return calculateRate(resolvedIgnoredCount, ignoredPushCount);
    }

    public int getFollowedChipProfitLoss() {
        return followedChipProfitLoss;
    }

    public int getIgnoredChipProfitLoss() {
        return ignoredChipProfitLoss;
    }

    public double getRecommendationAccuracy() {
        if (resolvedFollowedCount == 0) {
            return -1.0;
        }

        return (double) (followedWinCount + followedPushCount) / resolvedFollowedCount;
    }

    public PlayerAction getMostCommonlyIgnoredRecommendedAction() {
        return ignoredRecommendationCounts.entrySet().stream()
            .max(
                Comparator
                    .comparingInt((Map.Entry<PlayerAction, Integer> entry) -> entry.getValue())
                    .thenComparingInt(entry -> -entry.getKey().ordinal())
            )
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public RecommendationAnalyticsSnapshot toSnapshot() {
        return new RecommendationAnalyticsSnapshot(
            totalRecommendationsShown,
            totalRecommendationsFollowed,
            totalRecommendationsIgnored,
            resolvedFollowedCount,
            resolvedIgnoredCount,
            followedWinCount,
            ignoredWinCount,
            followedPushCount,
            ignoredPushCount,
            followedChipProfitLoss,
            ignoredChipProfitLoss,
            ignoredRecommendationCounts.getOrDefault(PlayerAction.HIT, 0),
            ignoredRecommendationCounts.getOrDefault(PlayerAction.STAND, 0),
            ignoredRecommendationCounts.getOrDefault(PlayerAction.DOUBLE_DOWN, 0),
            ignoredRecommendationCounts.getOrDefault(PlayerAction.SPLIT, 0)
        );
    }

    public void restore(RecommendationAnalyticsSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Recommendation analytics snapshot cannot be null.");
        }

        pendingDecisions.clear();
        ignoredRecommendationCounts.clear();
        totalRecommendationsShown = snapshot.getTotalRecommendationsShown();
        totalRecommendationsFollowed = snapshot.getTotalRecommendationsFollowed();
        totalRecommendationsIgnored = snapshot.getTotalRecommendationsIgnored();
        resolvedFollowedCount = snapshot.getResolvedFollowedCount();
        resolvedIgnoredCount = snapshot.getResolvedIgnoredCount();
        followedWinCount = snapshot.getFollowedWinCount();
        ignoredWinCount = snapshot.getIgnoredWinCount();
        followedPushCount = snapshot.getFollowedPushCount();
        ignoredPushCount = snapshot.getIgnoredPushCount();
        followedChipProfitLoss = snapshot.getFollowedChipProfitLoss();
        ignoredChipProfitLoss = snapshot.getIgnoredChipProfitLoss();

        restoreIgnoredCount(PlayerAction.HIT, snapshot.getIgnoredHitRecommendations());
        restoreIgnoredCount(PlayerAction.STAND, snapshot.getIgnoredStandRecommendations());
        restoreIgnoredCount(PlayerAction.DOUBLE_DOWN, snapshot.getIgnoredDoubleDownRecommendations());
        restoreIgnoredCount(PlayerAction.SPLIT, snapshot.getIgnoredSplitRecommendations());
    }

    private void accumulateResolvedDecision(RecommendationDecision decision) {
        if (decision.isFollowedRecommendation()) {
            resolvedFollowedCount++;
            followedChipProfitLoss += decision.getResolvedChipDelta();
            if (isWin(decision.getResolvedOutcome())) {
                followedWinCount++;
            } else if (decision.getResolvedOutcome() == RoundOutcome.PUSH) {
                followedPushCount++;
            }
            return;
        }

        resolvedIgnoredCount++;
        ignoredChipProfitLoss += decision.getResolvedChipDelta();
        if (isWin(decision.getResolvedOutcome())) {
            ignoredWinCount++;
        } else if (decision.getResolvedOutcome() == RoundOutcome.PUSH) {
            ignoredPushCount++;
        }
    }

    private HandResult findHandResult(RoundResult roundResult, int handNumber) {
        for (HandResult handResult : roundResult.getHandResults()) {
            if (handResult.getHandNumber() == handNumber) {
                return handResult;
            }
        }

        return null;
    }

    private boolean isWin(RoundOutcome outcome) {
        return outcome == RoundOutcome.PLAYER_BLACKJACK
            || outcome == RoundOutcome.PLAYER_WIN
            || outcome == RoundOutcome.DEALER_BUST;
    }

    private double calculateRate(int totalCount, int matchingCount) {
        if (totalCount == 0) {
            return -1.0;
        }

        return (double) matchingCount / totalCount;
    }

    private void restoreIgnoredCount(PlayerAction action, int count) {
        if (count > 0) {
            ignoredRecommendationCounts.put(action, count);
        }
    }
}
