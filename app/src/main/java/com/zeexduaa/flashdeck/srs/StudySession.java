package com.zeexduaa.flashdeck.srs;

import com.zeexduaa.flashdeck.model.Card;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

/**
 * One sitting of study: the due cards in order, one at a time.
 *
 * <p>A card you mark "Again" goes to the back of the queue, so you see it once more
 * before the session ends. Every other grade takes it out of the session.
 */
public final class StudySession {

    private final Scheduler scheduler;
    private final Deque<Card> queue;
    private int reviewed;
    private int forgotten;

    public StudySession(Collection<Card> dueCards, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.queue = new ArrayDeque<>(dueCards);
    }

    /** The card to show now, or null when the session is over. */
    public Card current() {
        return queue.peekFirst();
    }

    public boolean isFinished() {
        return queue.isEmpty();
    }

    /** Cards still to go, counting a forgotten card again if it's queued for another look. */
    public int remaining() {
        return queue.size();
    }

    /** Answers given so far (a card answered twice counts twice). */
    public int reviewed() {
        return reviewed;
    }

    /** How many answers were "Again". */
    public int forgotten() {
        return forgotten;
    }

    /**
     * Grades the current card and moves on. Returns the card with its new schedule,
     * which the caller should save.
     */
    public Card answer(Grade grade, long now) {
        Card card = queue.pollFirst();
        if (card == null) {
            throw new IllegalStateException("The session is already finished");
        }
        Card updated = scheduler.review(card, grade, now);
        reviewed++;
        if (grade == Grade.AGAIN) {
            forgotten++;
            queue.addLast(updated);
        }
        return updated;
    }
}
