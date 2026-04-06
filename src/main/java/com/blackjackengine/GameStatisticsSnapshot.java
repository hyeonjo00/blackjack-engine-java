package com.blackjackengine;

public class GameStatisticsSnapshot {
    private final int totalRoundsPlayed;
    private final int wins;
    private final int losses;
    private final int pushes;
    private final int blackjacks;
    private final int splitUsage;
    private final int doubleDownUsage;
    private final int totalChipProfitLoss;
    private final int currentWinStreak;
    private final int bestWinStreak;

    public GameStatisticsSnapshot(
        int totalRoundsPlayed,
        int wins,
        int losses,
        int pushes,
        int blackjacks,
        int splitUsage,
        int doubleDownUsage,
        int totalChipProfitLoss,
        int currentWinStreak,
        int bestWinStreak
    ) {
        this.totalRoundsPlayed = totalRoundsPlayed;
        this.wins = wins;
        this.losses = losses;
        this.pushes = pushes;
        this.blackjacks = blackjacks;
        this.splitUsage = splitUsage;
        this.doubleDownUsage = doubleDownUsage;
        this.totalChipProfitLoss = totalChipProfitLoss;
        this.currentWinStreak = currentWinStreak;
        this.bestWinStreak = bestWinStreak;
    }

    public int getTotalRoundsPlayed() {
        return totalRoundsPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getPushes() {
        return pushes;
    }

    public int getBlackjacks() {
        return blackjacks;
    }

    public int getSplitUsage() {
        return splitUsage;
    }

    public int getDoubleDownUsage() {
        return doubleDownUsage;
    }

    public int getTotalChipProfitLoss() {
        return totalChipProfitLoss;
    }

    public int getCurrentWinStreak() {
        return currentWinStreak;
    }

    public int getBestWinStreak() {
        return bestWinStreak;
    }
}
