package com.zeexduaa.flashdeck.srs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.zeexduaa.flashdeck.model.Card;

import org.junit.Test;

public class SchedulerTest {

    private static final long NOW = 1_800_000_000_000L;
    private static final long MINUTE = 60_000L;
    private static final long DAY = 24 * 60 * MINUTE;

    private final Scheduler scheduler = new Scheduler();

    private static Card newCard() {
        return Card.create(1, "hola", "hello", NOW);
    }

    @Test
    public void goodGoesOneDayThenSixThenTimesEase() {
        Card c = scheduler.review(newCard(), Grade.GOOD, NOW);
        assertEquals(1, c.getIntervalDays());
        assertEquals(NOW + DAY, c.getDueAt());

        c = scheduler.review(c, Grade.GOOD, NOW);
        assertEquals(6, c.getIntervalDays());

        c = scheduler.review(c, Grade.GOOD, NOW);
        assertEquals(15, c.getIntervalDays()); // 6 * 2.5
        assertEquals(3, c.getRepetitions());
        assertEquals(Card.DEFAULT_EASE, c.getEase(), 1e-9);
    }

    @Test
    public void againRelearnsInTenMinutesAndCountsALapseOnlyForLearnedCards() {
        Card fresh = scheduler.review(newCard(), Grade.AGAIN, NOW);
        assertEquals(0, fresh.getLapses());
        assertEquals(NOW + Scheduler.RELEARN_MINUTES * MINUTE, fresh.getDueAt());

        Card learned = scheduler.review(scheduler.review(newCard(), Grade.GOOD, NOW), Grade.GOOD, NOW);
        Card forgotten = scheduler.review(learned, Grade.AGAIN, NOW);
        assertEquals(1, forgotten.getLapses());
        assertEquals(0, forgotten.getRepetitions());
        assertEquals(0, forgotten.getIntervalDays());
        assertEquals(2.3, forgotten.getEase(), 1e-9);
    }

    @Test
    public void easeNeverDropsBelowTheMinimum() {
        Card c = newCard();
        for (int i = 0; i < 20; i++) {
            c = scheduler.review(c, Grade.AGAIN, NOW);
        }
        assertEquals(Scheduler.MIN_EASE, c.getEase(), 1e-9);
        c = scheduler.review(c, Grade.HARD, NOW);
        assertEquals(Scheduler.MIN_EASE, c.getEase(), 1e-9);
    }

    @Test
    public void hardGrowsSlowlyButAlwaysMovesForward() {
        Card c = scheduler.review(newCard(), Grade.GOOD, NOW); // 1 day
        c = scheduler.review(c, Grade.HARD, NOW);
        assertEquals(2, c.getIntervalDays()); // round(1.2) = 1, but at least +1
        c = scheduler.review(c, Grade.HARD, NOW);
        assertEquals(3, c.getIntervalDays());
    }

    @Test
    public void easyIsFourDaysFirstAndGrowsFasterThanGood() {
        Card easy = scheduler.review(newCard(), Grade.EASY, NOW);
        assertEquals(4, easy.getIntervalDays());
        assertEquals(2.65, easy.getEase(), 1e-9);

        Card base = scheduler.review(scheduler.review(newCard(), Grade.GOOD, NOW), Grade.GOOD, NOW);
        Card good = scheduler.review(base, Grade.GOOD, NOW);
        Card easier = scheduler.review(base, Grade.EASY, NOW);
        assertTrue(easier.getIntervalDays() > good.getIntervalDays());
    }

    @Test
    public void buttonsAreOrderedFromSoonestToLatest() {
        Card c = newCard();
        for (int step = 0; step < 6; step++) {
            long[] preview = scheduler.previewMinutes(c, NOW);
            for (int i = 1; i < preview.length; i++) {
                assertTrue("step " + step + ": " + Grade.values()[i], preview[i] >= preview[i - 1]);
            }
            c = scheduler.review(c, Grade.GOOD, NOW);
        }
    }

    @Test
    public void intervalsAreCapped() {
        Card c = newCard();
        for (int i = 0; i < 40; i++) {
            c = scheduler.review(c, Grade.EASY, NOW);
        }
        assertEquals(Scheduler.MAX_INTERVAL_DAYS, c.getIntervalDays());
        assertTrue(c.getDueAt() > NOW);
    }

    @Test
    public void reviewKeepsTextAndIdentity() {
        Card c = newCard().withId(7);
        Card reviewed = scheduler.review(c, Grade.GOOD, NOW);
        assertEquals(7, reviewed.getId());
        assertEquals("hola", reviewed.getFront());
        assertEquals("hello", reviewed.getBack());
        assertFalse(reviewed.isDue(NOW));
        assertTrue(reviewed.isDue(NOW + DAY));
    }

    @Test
    public void formatDelay() {
        assertEquals("10m", Scheduler.formatDelay(10));
        assertEquals("3h", Scheduler.formatDelay(180));
        assertEquals("1d", Scheduler.formatDelay(24 * 60));
        assertEquals("6d", Scheduler.formatDelay(6 * 24 * 60));
        assertEquals("2mo", Scheduler.formatDelay(60L * 24 * 60));
        assertEquals("1y", Scheduler.formatDelay(365L * 24 * 60));
        assertEquals("1.5y", Scheduler.formatDelay(548L * 24 * 60));
    }
}
