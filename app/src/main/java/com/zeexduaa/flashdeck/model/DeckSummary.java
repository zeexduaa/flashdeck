package com.zeexduaa.flashdeck.model;

/** A deck plus the counts shown on the deck list. */
public final class DeckSummary {

    private final Deck deck;
    private final int totalCards;
    private final int dueCards;

    public DeckSummary(Deck deck, int totalCards, int dueCards) {
        this.deck = deck;
        this.totalCards = totalCards;
        this.dueCards = dueCards;
    }

    public Deck getDeck() {
        return deck;
    }

    public int getTotalCards() {
        return totalCards;
    }

    public int getDueCards() {
        return dueCards;
    }
}
