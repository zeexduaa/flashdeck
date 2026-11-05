package com.zeexduaa.flashdeck.srs;

/** How well you remembered a card, from the four buttons on the study screen. */
public enum Grade {
    /** Forgot it. The card starts over and comes back in a few minutes. */
    AGAIN,
    /** Remembered, but it was a struggle. */
    HARD,
    /** Remembered normally. */
    GOOD,
    /** Too easy. */
    EASY
}
