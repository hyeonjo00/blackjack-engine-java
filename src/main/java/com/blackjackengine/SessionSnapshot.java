package com.blackjackengine;

public class SessionSnapshot {
    private final int startingChips;
    private final int currentChips;
    private final int previewBetAmount;
    private final String lastRoundResultText;
    private final String themeName;
    private final GameStatisticsSnapshot statisticsSnapshot;
    private final RecommendationAnalyticsSnapshot recommendationAnalyticsSnapshot;

    public SessionSnapshot(
        int startingChips,
        int currentChips,
        int previewBetAmount,
        String lastRoundResultText,
        String themeName,
        GameStatisticsSnapshot statisticsSnapshot,
        RecommendationAnalyticsSnapshot recommendationAnalyticsSnapshot
    ) {
        this.startingChips = startingChips;
        this.currentChips = currentChips;
        this.previewBetAmount = previewBetAmount;
        this.lastRoundResultText = lastRoundResultText;
        this.themeName = themeName;
        this.statisticsSnapshot = statisticsSnapshot;
        this.recommendationAnalyticsSnapshot = recommendationAnalyticsSnapshot;
    }

    public int getStartingChips() {
        return startingChips;
    }

    public int getCurrentChips() {
        return currentChips;
    }

    public int getPreviewBetAmount() {
        return previewBetAmount;
    }

    public String getLastRoundResultText() {
        return lastRoundResultText;
    }

    public String getThemeName() {
        return themeName;
    }

    public GameStatisticsSnapshot getStatisticsSnapshot() {
        return statisticsSnapshot;
    }

    public RecommendationAnalyticsSnapshot getRecommendationAnalyticsSnapshot() {
        return recommendationAnalyticsSnapshot;
    }
}
