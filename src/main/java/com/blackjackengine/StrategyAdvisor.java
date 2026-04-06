package com.blackjackengine;

import java.util.ArrayList;
import java.util.List;

public class StrategyAdvisor {
    private static final double DEALER_BUST_TWO = 0.35;
    private static final double DEALER_BUST_THREE = 0.37;
    private static final double DEALER_BUST_FOUR = 0.40;
    private static final double DEALER_BUST_FIVE = 0.42;
    private static final double DEALER_BUST_SIX = 0.42;
    private static final double DEALER_BUST_SEVEN = 0.26;
    private static final double DEALER_BUST_EIGHT = 0.24;
    private static final double DEALER_BUST_NINE = 0.23;
    private static final double DEALER_BUST_TEN = 0.21;
    private static final double DEALER_BUST_ACE = 0.17;

    public MoveRecommendation recommend(
        PlayerHand playerHand,
        Card dealerUpCard,
        List<PlayerAction> availableActions
    ) {
        if (playerHand == null || dealerUpCard == null) {
            return new MoveRecommendation(PlayerAction.STAND, "No active hand is available.");
        }

        Hand hand = playerHand.getHand();
        int dealerValue = getDealerStrategyValue(dealerUpCard);
        boolean canSplit = availableActions.contains(PlayerAction.SPLIT);
        boolean canDoubleDown = availableActions.contains(PlayerAction.DOUBLE_DOWN);

        if (canSplit && hand.hasPairOfSameRank()) {
            MoveRecommendation splitRecommendation =
                recommendSplit(hand, hand.getCard(0).getRank(), dealerValue);

            if (splitRecommendation != null) {
                return splitRecommendation;
            }
        }

        if (hand.isSoft()) {
            return recommendSoftTotal(hand, hand.getBestValue(), dealerValue, canDoubleDown);
        }

        return recommendHardTotal(hand, hand.getBestValue(), dealerValue, canDoubleDown);
    }

    public double estimateDealerBustChance(Card dealerUpCard) {
        if (dealerUpCard == null) {
            return 0.0;
        }

        return estimateDealerBustChance(getDealerStrategyValue(dealerUpCard));
    }

    private MoveRecommendation recommendSplit(Hand hand, Card.Rank rank, int dealerValue) {
        switch (rank) {
            case ACE:
            case EIGHT:
                return recommend(PlayerAction.SPLIT, buildPairReason(rank, dealerValue), hand, dealerValue);
            case NINE:
                if (isBetween(dealerValue, 2, 6) || dealerValue == 8 || dealerValue == 9) {
                    return recommend(PlayerAction.SPLIT, buildPairReason(rank, dealerValue), hand, dealerValue);
                }
                return null;
            case SEVEN:
                if (isBetween(dealerValue, 2, 7)) {
                    return recommend(PlayerAction.SPLIT, buildPairReason(rank, dealerValue), hand, dealerValue);
                }
                return null;
            case SIX:
                if (isBetween(dealerValue, 2, 6)) {
                    return recommend(PlayerAction.SPLIT, buildPairReason(rank, dealerValue), hand, dealerValue);
                }
                return null;
            case FOUR:
                if (dealerValue == 5 || dealerValue == 6) {
                    return recommend(PlayerAction.SPLIT, buildPairReason(rank, dealerValue), hand, dealerValue);
                }
                return null;
            case THREE:
            case TWO:
                if (isBetween(dealerValue, 2, 7)) {
                    return recommend(PlayerAction.SPLIT, buildPairReason(rank, dealerValue), hand, dealerValue);
                }
                return null;
            case FIVE:
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
            default:
                return null;
        }
    }

    private MoveRecommendation recommendSoftTotal(Hand hand, int total, int dealerValue, boolean canDoubleDown) {
        switch (total) {
            case 20:
                return recommend(PlayerAction.STAND, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
            case 19:
                if (dealerValue == 6) {
                    return recommendDoubleOrStand(hand, total, dealerValue, canDoubleDown);
                }
                return recommend(PlayerAction.STAND, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
            case 18:
                if (isBetween(dealerValue, 3, 6)) {
                    return recommendDoubleOrStand(hand, total, dealerValue, canDoubleDown);
                }
                if (dealerValue == 2 || dealerValue == 7 || dealerValue == 8) {
                    return recommend(PlayerAction.STAND, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
                }
                return recommend(PlayerAction.HIT, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
            case 17:
                if (isBetween(dealerValue, 3, 6)) {
                    return recommendDoubleOrHit(hand, total, dealerValue, canDoubleDown, "Soft");
                }
                return recommend(PlayerAction.HIT, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
            case 16:
            case 15:
                if (dealerValue == 4 || dealerValue == 5 || dealerValue == 6) {
                    return recommendDoubleOrHit(hand, total, dealerValue, canDoubleDown, "Soft");
                }
                return recommend(PlayerAction.HIT, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
            case 14:
            case 13:
                if (dealerValue == 5 || dealerValue == 6) {
                    return recommendDoubleOrHit(hand, total, dealerValue, canDoubleDown, "Soft");
                }
                return recommend(PlayerAction.HIT, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
            default:
                return recommend(PlayerAction.HIT, buildTotalReason("Soft", total, dealerValue), hand, dealerValue);
        }
    }

    private MoveRecommendation recommendHardTotal(Hand hand, int total, int dealerValue, boolean canDoubleDown) {
        if (total >= 17) {
            return recommend(PlayerAction.STAND, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
        }

        if (total >= 13) {
            if (isBetween(dealerValue, 2, 6)) {
                return recommend(PlayerAction.STAND, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
            }
            return recommend(PlayerAction.HIT, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
        }

        if (total == 12) {
            if (dealerValue >= 4 && dealerValue <= 6) {
                return recommend(PlayerAction.STAND, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
            }
            return recommend(PlayerAction.HIT, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
        }

        if (total == 11) {
            if (dealerValue != 11) {
                return recommendDoubleOrHit(hand, total, dealerValue, canDoubleDown, "Hard");
            }
            return recommend(PlayerAction.HIT, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
        }

        if (total == 10) {
            if (isBetween(dealerValue, 2, 9)) {
                return recommendDoubleOrHit(hand, total, dealerValue, canDoubleDown, "Hard");
            }
            return recommend(PlayerAction.HIT, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
        }

        if (total == 9) {
            if (isBetween(dealerValue, 3, 6)) {
                return recommendDoubleOrHit(hand, total, dealerValue, canDoubleDown, "Hard");
            }
            return recommend(PlayerAction.HIT, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
        }

        return recommend(PlayerAction.HIT, buildTotalReason("Hard", total, dealerValue), hand, dealerValue);
    }

    private MoveRecommendation recommendDoubleOrHit(
        Hand hand,
        int total,
        int dealerValue,
        boolean canDoubleDown,
        String handType
    ) {
        String reason = buildTotalReason(handType, total, dealerValue);

        if (canDoubleDown) {
            return recommend(PlayerAction.DOUBLE_DOWN, reason, hand, dealerValue);
        }

        return recommend(PlayerAction.HIT, reason + "; double down is unavailable.", hand, dealerValue);
    }

    private MoveRecommendation recommendDoubleOrStand(Hand hand, int total, int dealerValue, boolean canDoubleDown) {
        String reason = buildTotalReason("Soft", total, dealerValue);

        if (canDoubleDown) {
            return recommend(PlayerAction.DOUBLE_DOWN, reason, hand, dealerValue);
        }

        return recommend(PlayerAction.STAND, reason + "; stand when double down is unavailable.", hand, dealerValue);
    }

    private MoveRecommendation recommend(PlayerAction action, String reason, Hand hand, int dealerValue) {
        double bustRisk = estimateBustRisk(action, hand);
        double dealerBustChance = estimateDealerBustChance(dealerValue);
        return new MoveRecommendation(
            action,
            reason,
            determineConfidence(action, hand, dealerValue),
            determineWinTendency(action, hand, dealerValue),
            determineRiskLevel(action, hand, dealerValue),
            estimateExpectedValue(action, hand, dealerValue, bustRisk, dealerBustChance),
            bustRisk,
            dealerBustChance
        );
    }

    private int getDealerStrategyValue(Card dealerUpCard) {
        if (dealerUpCard.isAce()) {
            return 11;
        }

        return Math.min(dealerUpCard.getValue(), 10);
    }

    private String buildTotalReason(String handType, int total, int dealerValue) {
        return handType + " " + total + " against dealer " + formatDealerValue(dealerValue);
    }

    private String buildPairReason(Card.Rank rank, int dealerValue) {
        return "Pair of " + formatPairRank(rank) + " against dealer " + formatDealerValue(dealerValue);
    }

    private String formatDealerValue(int dealerValue) {
        if (dealerValue == 11) {
            return "Ace";
        }

        return String.valueOf(dealerValue);
    }

    private String formatPairRank(Card.Rank rank) {
        switch (rank) {
            case ACE:
                return "Aces";
            case JACK:
                return "Jacks";
            case QUEEN:
                return "Queens";
            case KING:
                return "Kings";
            default:
                return rank.getLabel() + "s";
        }
    }

    private boolean isBetween(int value, int lower, int upper) {
        return value >= lower && value <= upper;
    }

    private MoveRecommendation.Confidence determineConfidence(PlayerAction action, Hand hand, int dealerValue) {
        int total = hand.getBestValue();

        switch (action) {
            case SPLIT:
                Card.Rank pairRank = hand.getCard(0).getRank();
                if (pairRank == Card.Rank.ACE || pairRank == Card.Rank.EIGHT) {
                    return MoveRecommendation.Confidence.HIGH;
                }
                return dealerValue <= 6
                    ? MoveRecommendation.Confidence.HIGH
                    : MoveRecommendation.Confidence.MEDIUM;
            case DOUBLE_DOWN:
                if (!hand.isSoft() && total == 11 && dealerValue != 11) {
                    return MoveRecommendation.Confidence.HIGH;
                }
                if (!hand.isSoft() && total == 10 && dealerValue <= 9) {
                    return MoveRecommendation.Confidence.HIGH;
                }
                return MoveRecommendation.Confidence.MEDIUM;
            case STAND:
                if (total >= 17) {
                    return MoveRecommendation.Confidence.HIGH;
                }
                if (!hand.isSoft() && total >= 12 && dealerValue <= 6) {
                    return MoveRecommendation.Confidence.MEDIUM;
                }
                return MoveRecommendation.Confidence.MEDIUM;
            case HIT:
            default:
                if (!hand.isSoft() && total <= 11) {
                    return MoveRecommendation.Confidence.HIGH;
                }
                if (!hand.isSoft() && total >= 12 && dealerValue >= 7) {
                    return MoveRecommendation.Confidence.HIGH;
                }
                return MoveRecommendation.Confidence.MEDIUM;
        }
    }

    private MoveRecommendation.WinTendency determineWinTendency(PlayerAction action, Hand hand, int dealerValue) {
        int total = hand.getBestValue();

        if (action == PlayerAction.SPLIT) {
            Card.Rank pairRank = hand.getCard(0).getRank();

            if (pairRank == Card.Rank.ACE || pairRank == Card.Rank.EIGHT) {
                return dealerValue <= 6
                    ? MoveRecommendation.WinTendency.FAVORABLE
                    : MoveRecommendation.WinTendency.BALANCED;
            }

            return dealerValue <= 6
                ? MoveRecommendation.WinTendency.FAVORABLE
                : MoveRecommendation.WinTendency.BALANCED;
        }

        if (hand.isSoft()) {
            if (total >= 19) {
                return MoveRecommendation.WinTendency.FAVORABLE;
            }
            if (total == 18) {
                return dealerValue <= 8
                    ? MoveRecommendation.WinTendency.BALANCED
                    : MoveRecommendation.WinTendency.UNFAVORABLE;
            }
            return dealerValue <= 6
                ? MoveRecommendation.WinTendency.BALANCED
                : MoveRecommendation.WinTendency.UNFAVORABLE;
        }

        if (total >= 17) {
            return dealerValue <= 6
                ? MoveRecommendation.WinTendency.FAVORABLE
                : MoveRecommendation.WinTendency.BALANCED;
        }
        if (total >= 12) {
            return dealerValue <= 6
                ? MoveRecommendation.WinTendency.BALANCED
                : MoveRecommendation.WinTendency.UNFAVORABLE;
        }
        if (action == PlayerAction.DOUBLE_DOWN && dealerValue <= 6) {
            return MoveRecommendation.WinTendency.FAVORABLE;
        }
        return MoveRecommendation.WinTendency.UNFAVORABLE;
    }

    private MoveRecommendation.RiskLevel determineRiskLevel(PlayerAction action, Hand hand, int dealerValue) {
        int total = hand.getBestValue();

        switch (action) {
            case DOUBLE_DOWN:
                return MoveRecommendation.RiskLevel.HIGH;
            case SPLIT:
                return hand.getCard(0).getRank() == Card.Rank.ACE
                    ? MoveRecommendation.RiskLevel.MEDIUM
                    : MoveRecommendation.RiskLevel.HIGH;
            case STAND:
                if (total >= 17 || (hand.isSoft() && total >= 18)) {
                    return MoveRecommendation.RiskLevel.LOW;
                }
                return MoveRecommendation.RiskLevel.MEDIUM;
            case HIT:
            default:
                if (!hand.isSoft() && total >= 12 && dealerValue >= 7) {
                    return MoveRecommendation.RiskLevel.HIGH;
                }
                return MoveRecommendation.RiskLevel.MEDIUM;
        }
    }

    private double estimateExpectedValue(
        PlayerAction action,
        Hand hand,
        int dealerValue,
        double bustRisk,
        double dealerBustChance
    ) {
        int total = hand.getBestValue();
        double expectedValue = 0.0;

        if (hand.isSoft()) {
            expectedValue += 0.10;
        }

        if (dealerValue <= 6) {
            expectedValue += 0.18;
        } else if (dealerValue >= 9 || dealerValue == 11) {
            expectedValue -= 0.12;
        }

        if (total >= 17) {
            expectedValue += 0.16;
        } else if (total <= 11) {
            expectedValue += 0.08;
        } else if (!hand.isSoft() && total >= 15 && dealerValue >= 9) {
            expectedValue -= 0.14;
        }

        switch (action) {
            case SPLIT:
                expectedValue += estimateSplitValueBonus(hand, dealerValue);
                break;
            case DOUBLE_DOWN:
                expectedValue += 0.14;
                expectedValue -= bustRisk * 0.46;
                break;
            case HIT:
                expectedValue += 0.05;
                expectedValue -= bustRisk * 0.34;
                break;
            case STAND:
            default:
                expectedValue += total >= 17 ? 0.08 : -0.03;
                expectedValue -= bustRisk * 0.08;
                break;
        }

        expectedValue += dealerBustChance * 0.42;

        return clamp(expectedValue, -0.65, 0.65);
    }

    private double estimateSplitValueBonus(Hand hand, int dealerValue) {
        Card.Rank rank = hand.getCard(0).getRank();

        if (rank == Card.Rank.ACE || rank == Card.Rank.EIGHT) {
            return dealerValue <= 6 ? 0.30 : 0.18;
        }

        if (dealerValue <= 6) {
            return 0.18;
        }

        return 0.08;
    }

    private double estimateBustRisk(PlayerAction action, Hand hand) {
        switch (action) {
            case STAND:
                return 0.0;
            case SPLIT:
                return 0.0;
            case HIT:
            case DOUBLE_DOWN:
            default:
                return estimateSingleDrawBustRisk(hand);
        }
    }

    private double estimateSingleDrawBustRisk(Hand hand) {
        if (hand == null) {
            return 0.0;
        }

        List<RankProbability> probabilities = buildRankProbabilities();
        int bustWeight = 0;
        int totalWeight = 0;

        for (RankProbability probability : probabilities) {
            Hand simulatedHand = copyHand(hand);
            simulatedHand.addCard(new Card(Card.Suit.CLUBS, probability.rank()));

            if (simulatedHand.isBust()) {
                bustWeight += probability.weight();
            }

            totalWeight += probability.weight();
        }

        if (totalWeight == 0) {
            return 0.0;
        }

        return (double) bustWeight / totalWeight;
    }

    private Hand copyHand(Hand originalHand) {
        Hand copy = new Hand();
        for (Card card : originalHand.getCards()) {
            copy.addCard(new Card(card.getSuit(), card.getRank()));
        }
        return copy;
    }

    private List<RankProbability> buildRankProbabilities() {
        List<RankProbability> probabilities = new ArrayList<>();
        probabilities.add(new RankProbability(Card.Rank.TWO, 4));
        probabilities.add(new RankProbability(Card.Rank.THREE, 4));
        probabilities.add(new RankProbability(Card.Rank.FOUR, 4));
        probabilities.add(new RankProbability(Card.Rank.FIVE, 4));
        probabilities.add(new RankProbability(Card.Rank.SIX, 4));
        probabilities.add(new RankProbability(Card.Rank.SEVEN, 4));
        probabilities.add(new RankProbability(Card.Rank.EIGHT, 4));
        probabilities.add(new RankProbability(Card.Rank.NINE, 4));
        probabilities.add(new RankProbability(Card.Rank.TEN, 16));
        probabilities.add(new RankProbability(Card.Rank.ACE, 4));
        return probabilities;
    }

    private double estimateDealerBustChance(int dealerValue) {
        switch (dealerValue) {
            case 2:
                return DEALER_BUST_TWO;
            case 3:
                return DEALER_BUST_THREE;
            case 4:
                return DEALER_BUST_FOUR;
            case 5:
                return DEALER_BUST_FIVE;
            case 6:
                return DEALER_BUST_SIX;
            case 7:
                return DEALER_BUST_SEVEN;
            case 8:
                return DEALER_BUST_EIGHT;
            case 9:
                return DEALER_BUST_NINE;
            case 10:
                return DEALER_BUST_TEN;
            case 11:
            default:
                return DEALER_BUST_ACE;
        }
    }

    private double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private record RankProbability(Card.Rank rank, int weight) {}
}
