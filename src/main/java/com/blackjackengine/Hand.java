package com.blackjackengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    public void clear() {
        cards.clear();
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int size() {
        return cards.size();
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public Card removeCardAt(int index) {
        return cards.remove(index);
    }

    public boolean hasPairOfSameRank() {
        return cards.size() == 2 && cards.get(0).getRank() == cards.get(1).getRank();
    }

    public int getBestValue() {
        int total = 0;
        int aceCount = 0;

        for (Card card : cards) {
            total += card.getValue();

            if (card.isAce()) {
                aceCount++;
            }
        }

        // Downgrade Aces from 11 to 1 until the hand is no longer bust.
        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }

        return total;
    }

    public boolean isSoft() {
        int total = 0;
        int aceCount = 0;

        for (Card card : cards) {
            total += card.getValue();

            if (card.isAce()) {
                aceCount++;
            }
        }

        while (total > 21 && aceCount > 0) {
            total -= 10;
            aceCount--;
        }

        return aceCount > 0;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getBestValue() == 21;
    }

    public boolean isBust() {
        return getBestValue() > 21;
    }

    @Override
    public String toString() {
        return cards.stream()
            .map(Card::toString)
            .collect(Collectors.joining(", "));
    }
}
