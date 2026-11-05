package com.zeexduaa.flashdeck.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zeexduaa.flashdeck.model.Card;
import com.zeexduaa.flashdeck.model.Deck;
import com.zeexduaa.flashdeck.model.DeckSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Reading and writing decks and cards.
 *
 * <p>Calls run on the calling thread. The data is small, so the screens call this
 * directly for now; moving it to a background executor is on the roadmap.
 */
public final class FlashcardStore {

    private static final String CARD_COLUMNS =
            "id, deck_id, front, back, ease, interval_days, repetitions, lapses, due_at";

    private final FlashdeckDatabase helper;

    public FlashcardStore(Context context) {
        this.helper = FlashdeckDatabase.get(context);
    }

    // ---- Decks -------------------------------------------------------------------------------

    /** Every deck, alphabetically, with its total and due card counts at {@code now}. */
    public List<DeckSummary> listDecks(long now) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<DeckSummary> decks = new ArrayList<>();
        String sql = "SELECT d.id, d.name, d.created_at, "
                + "COUNT(c.id), "
                + "COALESCE(SUM(CASE WHEN c.due_at <= ? THEN 1 ELSE 0 END), 0) "
                + "FROM " + FlashdeckDatabase.DECKS + " d "
                + "LEFT JOIN " + FlashdeckDatabase.CARDS + " c ON c.deck_id = d.id "
                + "GROUP BY d.id ORDER BY d.name COLLATE NOCASE";
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(now)})) {
            while (cursor.moveToNext()) {
                Deck deck = new Deck(cursor.getLong(0), cursor.getString(1), cursor.getLong(2));
                decks.add(new DeckSummary(deck, cursor.getInt(3), cursor.getInt(4)));
            }
        }
        return decks;
    }

    public Deck getDeck(long deckId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, created_at FROM " + FlashdeckDatabase.DECKS + " WHERE id = ?",
                new String[]{String.valueOf(deckId)})) {
            return cursor.moveToFirst()
                    ? new Deck(cursor.getLong(0), cursor.getString(1), cursor.getLong(2))
                    : null;
        }
    }

    /** Saves a new deck and returns it with its database id. */
    public Deck insertDeck(Deck deck) {
        ContentValues values = new ContentValues();
        values.put("name", deck.getName());
        values.put("created_at", deck.getCreatedAt());
        long id = helper.getWritableDatabase().insertOrThrow(FlashdeckDatabase.DECKS, null, values);
        return deck.withId(id);
    }

    public void renameDeck(long deckId, String newName) {
        ContentValues values = new ContentValues();
        values.put("name", newName.trim());
        helper.getWritableDatabase().update(
                FlashdeckDatabase.DECKS, values, "id = ?", new String[]{String.valueOf(deckId)});
    }

    /** Deletes the deck and, through the foreign key, all of its cards. */
    public void deleteDeck(long deckId) {
        helper.getWritableDatabase().delete(
                FlashdeckDatabase.DECKS, "id = ?", new String[]{String.valueOf(deckId)});
    }

    // ---- Cards -------------------------------------------------------------------------------

    /** All cards in a deck, oldest first. */
    public List<Card> listCards(long deckId) {
        return queryCards("deck_id = ?", new String[]{String.valueOf(deckId)}, "id", null);
    }

    /** Cards due at {@code now}, the most overdue first, at most {@code limit} of them. */
    public List<Card> dueCards(long deckId, long now, int limit) {
        return queryCards("deck_id = ? AND due_at <= ?",
                new String[]{String.valueOf(deckId), String.valueOf(now)},
                "due_at, id", String.valueOf(limit));
    }

    public Card getCard(long cardId) {
        List<Card> cards = queryCards("id = ?", new String[]{String.valueOf(cardId)}, null, "1");
        return cards.isEmpty() ? null : cards.get(0);
    }

    /** Saves a new card and returns it with its database id. */
    public Card insertCard(Card card) {
        long id = helper.getWritableDatabase()
                .insertOrThrow(FlashdeckDatabase.CARDS, null, toValues(card));
        return card.withId(id);
    }

    /** Saves the card's text and schedule. */
    public void updateCard(Card card) {
        helper.getWritableDatabase().update(FlashdeckDatabase.CARDS, toValues(card),
                "id = ?", new String[]{String.valueOf(card.getId())});
    }

    public void deleteCard(long cardId) {
        helper.getWritableDatabase().delete(
                FlashdeckDatabase.CARDS, "id = ?", new String[]{String.valueOf(cardId)});
    }

    // ---- Helpers -----------------------------------------------------------------------------

    private List<Card> queryCards(String where, String[] args, String orderBy, String limit) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Card> cards = new ArrayList<>();
        try (Cursor c = db.query(FlashdeckDatabase.CARDS, CARD_COLUMNS.split(", "),
                where, args, null, null, orderBy, limit)) {
            while (c.moveToNext()) {
                cards.add(new Card(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3),
                        c.getDouble(4), c.getInt(5), c.getInt(6), c.getInt(7), c.getLong(8)));
            }
        }
        return cards;
    }

    private static ContentValues toValues(Card card) {
        ContentValues values = new ContentValues();
        values.put("deck_id", card.getDeckId());
        values.put("front", card.getFront());
        values.put("back", card.getBack());
        values.put("ease", card.getEase());
        values.put("interval_days", card.getIntervalDays());
        values.put("repetitions", card.getRepetitions());
        values.put("lapses", card.getLapses());
        values.put("due_at", card.getDueAt());
        return values;
    }
}
