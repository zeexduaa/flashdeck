package com.zeexduaa.flashdeck.srs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.zeexduaa.flashdeck.model.Card;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class StudySessionTest {

    private static final long NOW = 1_800_000_000_000L;

    private static Card card(long id, String front) {
        return Card.create(1, front, "answer", NOW).withId(id);
    }

    @Test
    public void cardsComeInOrderAndTheSessionEnds() {
        StudySession session = new StudySession(
                Arrays.asList(card(1, "a"), card(2, "b")), new Scheduler());
        assertEquals("a", session.current().getFront());
        session.answer(Grade.GOOD, NOW);
        assertEquals("b", session.current().getFront());
        session.answer(Grade.EASY, NOW);
        assertTrue(session.isFinished());
        assertNull(session.current());
        assertEquals(2, session.reviewed());
    }

    @Test
    public void forgottenCardsComeBackAtTheEnd() {
        StudySession session = new StudySession(
                Arrays.asList(card(1, "a"), card(2, "b")), new Scheduler());
        Card updated = session.answer(Grade.AGAIN, NOW);
        assertEquals(0, updated.getRepetitions());
        assertEquals("b", session.current().getFront());
        assertEquals(2, session.remaining());

        session.answer(Grade.GOOD, NOW);
        assertEquals("a", session.current().getFront());
        session.answer(Grade.GOOD, NOW);
        assertTrue(session.isFinished());
        assertEquals(3, session.reviewed());
        assertEquals(1, session.forgotten());
    }

    @Test
    public void answerReturnsTheRescheduledCard() {
        StudySession session = new StudySession(Collections.singletonList(card(9, "x")), new Scheduler());
        Card updated = session.answer(Grade.GOOD, NOW);
        assertEquals(9, updated.getId());
        assertEquals(1, updated.getIntervalDays());
    }

    @Test(expected = IllegalStateException.class)
    public void answeringAfterTheEndFails() {
        StudySession session = new StudySession(Collections.<Card>emptyList(), new Scheduler());
        session.answer(Grade.GOOD, NOW);
    }
}
