package com.zeexduaa.flashdeck.model;

import java.util.Objects;

/**
 * One flashcard: a question on the front, the answer on the back, and the
 * spaced-repetition state that decides when it comes up again.
 *
 * <p>Cards are immutable. Reviewing or editing a card returns a new one.
 */
public final class Card {

    public static final long NEW_ID = 0;
    public static final double DEFAULT_EASE = 2.5;

    private final long id;
    private final long deckId;
    private final String front;
    private final String back;

    /** How easy the card is for you; bigger means intervals grow faster. */
    private final double ease;
    /** Days until the next review after the last successful one. 0 for new or lapsed cards. */
    private final int intervalDays;
    /** Successful reviews in a row since the card was new or last forgotten. */
    private final int repetitions;
    /** Times the card was forgotten ("Again") after being learned. */
    private final int lapses;
    /** When the card is next due, in epoch milliseconds. */
    private final long dueAt;

    public Card(long id, long deckId, String front, String back,
                double ease, int intervalDays, int repetitions, int lapses, long dueAt) {
        if (front == null || front.trim().isEmpty()) {
            throw new IllegalArgumentException("A card needs something on the front");
        }
        if (back == null || back.trim().isEmpty()) {
            throw new IllegalArgumentException("A card needs something on the back");
        }
        this.id = id;
        this.deckId = deckId;
        this.front = front.trim();
        this.back = back.trim();
        this.ease = ease;
        this.intervalDays = intervalDays;
        this.repetitions = repetitions;
        this.lapses = lapses;
        this.dueAt = dueAt;
    }

    /** A brand-new card, due immediately. */
    public static Card create(long deckId, String front, String back, long now) {
        return new Card(NEW_ID, deckId, front, back, DEFAULT_EASE, 0, 0, 0, now);
    }

    public long getId() { return id; }
    public long getDeckId() { return deckId; }
    public String getFront() { return front; }
    public String getBack() { return back; }
    public double getEase() { return ease; }
    public int getIntervalDays() { return intervalDays; }
    public int getRepetitions() { return repetitions; }
    public int getLapses() { return lapses; }
    public long getDueAt() { return dueAt; }

    public boolean isNew() {
        return repetitions == 0 && lapses == 0 && intervalDays == 0;
    }

    public boolean isDue(long now) {
        return dueAt <= now;
    }

    public Card withId(long newId) {
        return new Card(newId, deckId, front, back, ease, intervalDays, repetitions, lapses, dueAt);
    }

    /** Same card with new text; the review history is kept. */
    public Card withText(String newFront, String newBack) {
        return new Card(id, deckId, newFront, newBack, ease, intervalDays, repetitions, lapses, dueAt);
    }

    /** Same card with a new schedule, as decided by the scheduler. */
    public Card withSchedule(double newEase, int newInterval, int newRepetitions, int newLapses, long newDueAt) {
        return new Card(id, deckId, front, back, newEase, newInterval, newRepetitions, newLapses, newDueAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        Card c = (Card) o;
        return id == c.id && deckId == c.deckId && Double.compare(ease, c.ease) == 0
                && intervalDays == c.intervalDays && repetitions == c.repetitions
                && lapses == c.lapses && dueAt == c.dueAt
                && front.equals(c.front) && back.equals(c.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deckId, front, back, ease, intervalDays, repetitions, lapses, dueAt);
    }

    @Override
    public String toString() {
        return "Card{" + id + ", " + front + " -> " + back + ", every " + intervalDays + "d}";
    }
}
