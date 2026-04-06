package com.blackjackengine;

public class GameStatistics {
    private int totalRoundsPlayed;
    private int wins;
    private int losses;
    private int pushes;
    private int blackjacks;
    private int splitUsage;
    private int doubleDownUsage;
    private int totalChipProfitLoss;
    private int currentWinStreak;
    private int bestWinStreak;

    public void recordRound(RoundResult result) {
        if (!result.isRoundOver()) {
            return;
        }

        totalRoundsPlayed++;
        splitUsage += result.getSplitCount();
        doubleDownUsage += result.getDoubleDownCount();
        totalChipProfitLoss += result.getChipDelta();
        updateWinStreak(result);

        for (HandResult handResult : result.getHandResults()) {
            switch (handResult.getOutcome()) {
                case PLAYER_BLACKJACK:
                    blackjacks++;
                    wins++;
                    break;
                case PLAYER_WIN:
                case DEALER_BUST:
                    wins++;
                    break;
                case PUSH:
                    pushes++;
                    break;
                case DEALER_BLACKJACK:
                case PLAYER_BUST:
                case DEALER_WIN:
                    losses++;
                    break;
                case IN_PROGRESS:
                case MIXED_RESULTS:
                default:
                    break;
            }
        }
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

    public int getResolvedHandCount() {
        return wins + losses + pushes;
    }

    public double getWinRate() {
        int resolvedHandCount = getResolvedHandCount();

        if (resolvedHandCount == 0) {
            return -1.0;
        }

        return (double) wins / resolvedHandCount;
    }

    public GameStatisticsSnapshot toSnapshot() {
        return new GameStatisticsSnapshot(
            totalRoundsPlayed,
            wins,
            losses,
            pushes,
            blackjacks,
            splitUsage,
            doubleDownUsage,
            totalChipProfitLoss,
            currentWinStreak,
            bestWinStreak
        );
    }

    public void restore(GameStatisticsSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Statistics snapshot cannot be null.");
        }

        totalRoundsPlayed = snapshot.getTotalRoundsPlayed();
        wins = snapshot.getWins();
        losses = snapshot.getLosses();
        pushes = snapshot.getPushes();
        blackjacks = snapshot.getBlackjacks();
        splitUsage = snapshot.getSplitUsage();
        doubleDownUsage = snapshot.getDoubleDownUsage();
        totalChipProfitLoss = snapshot.getTotalChipProfitLoss();
        currentWinStreak = snapshot.getCurrentWinStreak();
        bestWinStreak = snapshot.getBestWinStreak();
    }

    private void updateWinStreak(RoundResult result) {
        if (result.getChipDelta() > 0) {
            currentWinStreak++;
            bestWinStreak = Math.max(bestWinStreak, currentWinStreak);
            return;
        }

        currentWinStreak = 0;
    }
}
