package com.zeexduaa.flashdeck.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** The on-device SQLite database. Everything stays on the phone; nothing is uploaded. */
public final class FlashdeckDatabase extends SQLiteOpenHelper {

    static final String NAME = "flashdeck.db";
    static final int VERSION = 1;

    static final String DECKS = "decks";
    static final String CARDS = "cards";

    private static volatile FlashdeckDatabase instance;

    public static FlashdeckDatabase get(Context context) {
        if (instance == null) {
            synchronized (FlashdeckDatabase.class) {
                if (instance == null) {
                    instance = new FlashdeckDatabase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private FlashdeckDatabase(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // deleting a deck deletes its cards
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DECKS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + CARDS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "deck_id INTEGER NOT NULL REFERENCES " + DECKS + "(id) ON DELETE CASCADE, "
                + "front TEXT NOT NULL, "
                + "back TEXT NOT NULL, "
                + "ease REAL NOT NULL, "
                + "interval_days INTEGER NOT NULL, "
                + "repetitions INTEGER NOT NULL, "
                + "lapses INTEGER NOT NULL, "
                + "due_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX cards_by_deck_due ON " + CARDS + "(deck_id, due_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first schema. Future versions add migrations here, one step at a time.
    }
}
