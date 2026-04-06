package com.blackjackengine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();
    private int nextCardIndex;

    public Deck() {
        reset();
    }

    public final void reset() {
        cards.clear();

        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }

        nextCardIndex = 0;
    }

    public void shuffle() {
        Collections.shuffle(cards);
        nextCardIndex = 0;
    }

    public Card drawCard() {
        if (nextCardIndex >= cards.size()) {
            throw new IllegalStateException("No more cards are available in the deck.");
        }

        return cards.get(nextCardIndex++);
    }
}

