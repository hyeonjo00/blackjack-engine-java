package com.blackjackengine;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine("Player");
        StrategyAdvisor strategyAdvisor = new StrategyAdvisor();
        RecommendationAnalytics recommendationAnalytics = new RecommendationAnalytics();

        try (Scanner scanner = new Scanner(System.in)) {
            printWelcome();

            while (gameEngine.getPlayer().getChips() > 0) {
                System.out.println();
                System.out.println("Chips: " + gameEngine.getPlayer().getChips());

                int bet = promptForBet(scanner, gameEngine.getPlayer().getChips());
                if (bet == -1) {
                    break;
                }

                gameEngine.startRound(bet);
                printRoundState(gameEngine, gameEngine.isRoundInProgress());

                while (gameEngine.isRoundInProgress()) {
                    MoveRecommendation recommendation = printRecommendation(gameEngine, strategyAdvisor);
                    PlayerAction action = promptForAction(scanner, gameEngine.getAvailableActions());
                    recommendationAnalytics.recordDecision(
                        gameEngine.getActiveHandIndex() + 1,
                        recommendation,
                        action
                    );
                    applyAction(gameEngine, action);
                    printRoundState(gameEngine, gameEngine.isRoundInProgress());
                }

                recommendationAnalytics.resolveRound(gameEngine.getLastResult());
                printRoundSummary(gameEngine.getLastResult(), gameEngine.getPlayer().getChips());

                if (gameEngine.getPlayer().getChips() <= 0) {
                    System.out.println("You are out of chips.");
                    break;
                }

                if (!promptPlayAgain(scanner)) {
                    break;
                }
            }
        }

        System.out.println();
        printSessionStatistics(gameEngine.getStatistics());
        System.out.println();
        printRecommendationAnalytics(recommendationAnalytics);
        System.out.println();
        System.out.println("Thanks for playing. Final chips: " + gameEngine.getPlayer().getChips());
    }

    private static void printWelcome() {
        System.out.println("Welcome to Blackjack.");
        System.out.println("You start with 100 chips.");
        System.out.println("Enter a bet each round, then choose hit, stand, double down, or split when allowed.");
    }

    private static int promptForBet(Scanner scanner, int chips) {
        while (true) {
            System.out.print("Enter your bet (1-" + chips + ") or Q to quit: ");
            String input = readLine(scanner);

            if (input == null) {
                return -1;
            }

            input = input.trim();

            if (input.equalsIgnoreCase("q")) {
                return -1;
            }

            try {
                int bet = Integer.parseInt(input);

                if (bet >= 1 && bet <= chips) {
                    return bet;
                }
            } catch (NumberFormatException ignored) {
                // Re-prompt with a friendlier message below.
            }

            System.out.println("Please enter a whole-number bet between 1 and " + chips + ".");
        }
    }

    private static PlayerAction promptForAction(Scanner scanner, List<PlayerAction> availableActions) {
        String actionText = availableActions.stream()
            .map(action -> "[" + action.getShortCode() + "]" + action.getLabel().substring(1))
            .collect(Collectors.joining(", "));

        while (true) {
            System.out.print("Choose action: " + actionText + ": ");
            String input = readLine(scanner);

            if (input == null) {
                return PlayerAction.STAND;
            }

            input = input.trim().toUpperCase();

            for (PlayerAction action : availableActions) {
                if (action.getShortCode().equals(input)) {
                    return action;
                }
            }

            System.out.println("Please choose one of the available actions.");
        }
    }

    private static boolean promptPlayAgain(Scanner scanner) {
        while (true) {
            System.out.print("Play another round? [Y/N]: ");
            String input = readLine(scanner);

            if (input == null) {
                return false;
            }

            input = input.trim().toLowerCase();

            if ("y".equals(input)) {
                return true;
            }

            if ("n".equals(input)) {
                return false;
            }

            System.out.println("Please enter Y or N.");
        }
    }

    private static void printRoundState(GameEngine gameEngine, boolean hideDealerHoleCard) {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("Total committed bet: " + gameEngine.getTotalCommittedBet());
        System.out.println("Dealer hand: " + formatHand(gameEngine.getDealer().getHand(), hideDealerHoleCard));

        if (hideDealerHoleCard) {
            System.out.println("Dealer total: Hidden");
        } else {
            System.out.println("Dealer total: " + gameEngine.getDealer().getHand().getBestValue());
        }

        List<PlayerHand> hands = gameEngine.getPlayerHands();
        for (int index = 0; index < hands.size(); index++) {
            PlayerHand hand = hands.get(index);
            String activeMarker = hideDealerHoleCard && gameEngine.isRoundInProgress() && index == gameEngine.getActiveHandIndex()
                ? " <== active"
                : "";

            System.out.println("Player hand " + (index + 1) + ": " + formatHand(hand.getHand(), false) + activeMarker);
            System.out.println("Hand " + (index + 1) + " total: " + hand.getHand().getBestValue());
            System.out.println("Hand " + (index + 1) + " bet: " + hand.getBet() + buildHandFlags(hand));
        }

        if (gameEngine.isRoundInProgress()) {
            System.out.println("Available actions: " + formatAvailableActions(gameEngine.getAvailableActions()));
        }
    }

    private static void printRoundSummary(RoundResult result, int chipsRemaining) {
        System.out.println();
        System.out.println(result.getMessage());

        for (HandResult handResult : result.getHandResults()) {
            System.out.println(
                "Hand " + handResult.getHandNumber()
                    + ": "
                    + handResult.getHandDescription()
                    + " ("
                    + handResult.getHandValue()
                    + ")"
            );
            System.out.println("Outcome: " + handResult.getMessage());
            System.out.println("Bet: " + handResult.getBetAmount() + buildHandResultFlags(handResult));
        }

        if (result.getChipDelta() > 0) {
            System.out.println("Chips won: " + result.getChipDelta());
        } else if (result.getChipDelta() < 0) {
            System.out.println("Chips lost: " + Math.abs(result.getChipDelta()));
        } else {
            System.out.println("No chips won or lost this round.");
        }

        System.out.println("Chips remaining: " + chipsRemaining);
    }

    private static void printSessionStatistics(GameStatistics statistics) {
        System.out.println("Session statistics");
        System.out.println("Rounds played: " + statistics.getTotalRoundsPlayed());
        System.out.println("Wins: " + statistics.getWins());
        System.out.println("Losses: " + statistics.getLosses());
        System.out.println("Pushes: " + statistics.getPushes());
        System.out.println("Blackjacks: " + statistics.getBlackjacks());
        System.out.println("Splits used: " + statistics.getSplitUsage());
        System.out.println("Double downs used: " + statistics.getDoubleDownUsage());
        System.out.println("Total chip profit/loss: " + statistics.getTotalChipProfitLoss());
    }

    private static MoveRecommendation printRecommendation(GameEngine gameEngine, StrategyAdvisor strategyAdvisor) {
        MoveRecommendation recommendation = strategyAdvisor.recommend(
            gameEngine.getActiveHand(),
            gameEngine.getDealerUpCard(),
            gameEngine.getAvailableActions()
        );

        System.out.println("Recommended move: " + recommendation.getAction().getLabel());
        System.out.println("Reason: " + recommendation.getReason());
        return recommendation;
    }

    private static void printRecommendationAnalytics(RecommendationAnalytics analytics) {
        System.out.println("Recommendation analytics");
        System.out.println("Recommendations shown: " + analytics.getTotalRecommendationsShown());
        System.out.println("Recommendations followed: " + analytics.getTotalRecommendationsFollowed());
        System.out.println("Recommendations ignored: " + analytics.getTotalRecommendationsIgnored());
        System.out.println(
            "Win rate when following: "
                + formatRate(analytics.getFollowedWinRate(), analytics.getResolvedFollowedCount())
        );
        System.out.println(
            "Push rate when following: "
                + formatRate(analytics.getFollowedPushRate(), analytics.getResolvedFollowedCount())
        );
        System.out.println("Chip profit/loss when following: " + analytics.getFollowedChipProfitLoss());
        System.out.println(
            "Win rate when ignoring: "
                + formatRate(analytics.getIgnoredWinRate(), analytics.getResolvedIgnoredCount())
        );
        System.out.println(
            "Push rate when ignoring: "
                + formatRate(analytics.getIgnoredPushRate(), analytics.getResolvedIgnoredCount())
        );
        System.out.println("Chip profit/loss when ignoring: " + analytics.getIgnoredChipProfitLoss());

        PlayerAction mostIgnoredAction = analytics.getMostCommonlyIgnoredRecommendedAction();
        System.out.println(
            "Most commonly ignored recommended action: "
                + (mostIgnoredAction == null ? "None" : mostIgnoredAction.getLabel())
        );
    }

    private static String formatHand(Hand hand, boolean hideDealerHoleCard) {
        List<Card> cards = hand.getCards();

        if (cards.isEmpty()) {
            return "(empty)";
        }

        if (!hideDealerHoleCard || cards.size() == 1) {
            return hand.toString();
        }

        return cards.get(0) + ", [Hidden]";
    }

    private static void applyAction(GameEngine gameEngine, PlayerAction action) {
        switch (action) {
            case HIT:
                gameEngine.playerHit();
                break;
            case STAND:
                gameEngine.playerStand();
                break;
            case DOUBLE_DOWN:
                gameEngine.playerDoubleDown();
                break;
            case SPLIT:
                gameEngine.playerSplit();
                break;
            default:
                throw new IllegalStateException("Unsupported action: " + action);
        }
    }

    private static String formatAvailableActions(List<PlayerAction> availableActions) {
        return availableActions.stream()
            .map(PlayerAction::getLabel)
            .collect(Collectors.joining(", "));
    }

    private static String buildHandFlags(PlayerHand hand) {
        StringBuilder flags = new StringBuilder();

        if (hand.isFromSplit()) {
            flags.append(" [split]");
        }

        if (hand.isDoubledDown()) {
            flags.append(" [doubled]");
        }

        return flags.toString();
    }

    private static String buildHandResultFlags(HandResult handResult) {
        StringBuilder flags = new StringBuilder();

        if (handResult.isSplitHand()) {
            flags.append(" [split]");
        }

        if (handResult.isDoubledDown()) {
            flags.append(" [doubled]");
        }

        return flags.toString();
    }

    private static String formatRate(double rate, int sampleSize) {
        if (sampleSize == 0 || rate < 0.0) {
            return "N/A";
        }

        return String.format("%.1f%%", rate * 100.0);
    }

    private static String readLine(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine();
    }
}
