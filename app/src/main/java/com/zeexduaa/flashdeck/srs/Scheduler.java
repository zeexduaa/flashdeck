package com.zeexduaa.flashdeck.srs;

import com.zeexduaa.flashdeck.model.Card;

/**
 * Decides when a card comes back, using a four-button version of the SM-2
 * algorithm (the one behind SuperMemo and, loosely, Anki).
 *
 * <ul>
 *   <li><b>Again</b>: the card is relearned. Repetitions reset, ease drops by 0.20,
 *       and it is due again in {@link #RELEARN_MINUTES} minutes.</li>
 *   <li><b>Hard</b>: the interval grows only a little (x1.2) and ease drops by 0.15.</li>
 *   <li><b>Good</b>: 1 day, then 6 days, then the last interval times the ease.</li>
 *   <li><b>Easy</b>: like Good with a x1.3 bonus (4 days the first time), and ease rises by 0.15.</li>
 * </ul>
 *
 * Ease never drops below {@link #MIN_EASE}, and a successful review always pushes
 * the interval at least one day further out, so cards can't get stuck.
 */
public final class Scheduler {

    public static final double MIN_EASE = 1.3;
    public static final int RELEARN_MINUTES = 10;
    public static final int MAX_INTERVAL_DAYS = 36500; // a hundred years is plenty

    static final double HARD_FACTOR = 1.2;
    static final double EASY_BONUS = 1.3;
    static final double AGAIN_EASE_PENALTY = 0.20;
    static final double HARD_EASE_PENALTY = 0.15;
    static final double EASY_EASE_BONUS = 0.15;

    private static final long MINUTE_MS = 60_000L;
    private static final long DAY_MS = 24 * 60 * MINUTE_MS;

    /** The card after being reviewed at {@code now} (epoch ms) with the given grade. */
    public Card review(Card card, Grade grade, long now) {
        double ease = card.getEase();
        int interval = card.getIntervalDays();
        int reps = card.getRepetitions();
        int lapses = card.getLapses();

        switch (grade) {
            case AGAIN: {
                boolean wasLearned = reps > 0;
                return card.withSchedule(
                        Math.max(MIN_EASE, ease - AGAIN_EASE_PENALTY),
                        0,
                        0,
                        wasLearned ? lapses + 1 : lapses,
                        now + RELEARN_MINUTES * MINUTE_MS);
            }
            case HARD: {
                int next = reps == 0 ? 1 : grow(interval, HARD_FACTOR);
                return scheduled(card, Math.max(MIN_EASE, ease - HARD_EASE_PENALTY), next, reps + 1, lapses, now);
            }
            case GOOD: {
                int next = goodInterval(interval, reps, ease);
                return scheduled(card, ease, next, reps + 1, lapses, now);
            }
            case EASY: {
                int next = reps == 0 ? 4 : grow(goodInterval(interval, reps, ease), EASY_BONUS);
                return scheduled(card, ease + EASY_EASE_BONUS, next, reps + 1, lapses, now);
            }
            default:
                throw new IllegalArgumentException("Unknown grade " + grade);
        }
    }

    /**
     * How far out each button would schedule the card, for labels like "Good · 6d".
     * Index order matches {@link Grade#values()}. Values are in minutes.
     */
    public long[] previewMinutes(Card card, long now) {
        Grade[] grades = Grade.values();
        long[] minutes = new long[grades.length];
        for (int i = 0; i < grades.length; i++) {
            minutes[i] = (review(card, grades[i], now).getDueAt() - now) / MINUTE_MS;
        }
        return minutes;
    }

    /** "10m", "1d", "3mo", "1.5y": a short label for a delay in minutes. */
    public static String formatDelay(long minutes) {
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        long days = hours / 24;
        if (days < 30) return days + "d";
        if (days < 365) return Math.round(days / 30.0) + "mo";
        double years = Math.round(days / 36.5) / 10.0;
        return (years == Math.floor(years) ? String.valueOf((long) years) : String.valueOf(years)) + "y";
    }

    private static int goodInterval(int interval, int reps, double ease) {
        if (reps == 0) return 1;
        if (reps == 1) return 6;
        return grow(interval, ease);
    }

    /** {@code interval * factor}, rounded, but always at least one day longer. */
    private static int grow(int interval, double factor) {
        long next = Math.round(interval * factor);
        return (int) Math.min(MAX_INTERVAL_DAYS, Math.max(next, interval + 1L));
    }

    private static Card scheduled(Card card, double ease, int interval, int reps, int lapses, long now) {
        int capped = Math.min(interval, MAX_INTERVAL_DAYS);
        return card.withSchedule(ease, capped, reps, lapses, now + capped * DAY_MS);
    }
}
