package com.zeexduaa.flashdeck.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CardTest {

    @Test
    public void newCardsAreDueImmediatelyWithDefaultEase() {
        Card c = Card.create(3, "  front ", " back  ", 1000);
        assertTrue(c.isNew());
        assertTrue(c.isDue(1000));
        assertEquals("front", c.getFront());
        assertEquals("back", c.getBack());
        assertEquals(Card.DEFAULT_EASE, c.getEase(), 1e-9);
        assertEquals(3, c.getDeckId());
    }

    @Test
    public void editingTextKeepsTheSchedule() {
        Card c = Card.create(1, "a", "b", 0).withSchedule(2.0, 6, 2, 1, 999);
        Card edited = c.withText("a2", "b2");
        assertEquals("a2", edited.getFront());
        assertEquals(6, edited.getIntervalDays());
        assertEquals(999, edited.getDueAt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankFrontIsRejected() {
        Card.create(1, "   ", "b", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankBackIsRejected() {
        Card.create(1, "a", "", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankDeckNameIsRejected() {
        Deck.create(" ", 0);
    }

    @Test
    public void deckNamesAreTrimmed() {
        assertEquals("Spanish", Deck.create("  Spanish ", 0).getName());
    }
}
