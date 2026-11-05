package com.zeexduaa.flashdeck;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zeexduaa.flashdeck.data.FlashcardStore;
import com.zeexduaa.flashdeck.model.Card;

/** Adding a new card to a deck, or editing an existing one's text. */
public class CardEditActivity extends AppCompatActivity {

    private static final String EXTRA_DECK_ID = "deck_id";
    private static final String EXTRA_CARD_ID = "card_id";

    private FlashcardStore store;
    private long deckId;
    private Card editing; // null when adding

    private TextInputLayout frontLayout;
    private TextInputLayout backLayout;
    private TextInputEditText front;
    private TextInputEditText back;

    public static Intent addIntent(Context context, long deckId) {
        return new Intent(context, CardEditActivity.class).putExtra(EXTRA_DECK_ID, deckId);
    }

    public static Intent editIntent(Context context, long cardId) {
        return new Intent(context, CardEditActivity.class).putExtra(EXTRA_CARD_ID, cardId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        store = new FlashcardStore(this);

        frontLayout = findViewById(R.id.front_layout);
        backLayout = findViewById(R.id.back_layout);
        front = findViewById(R.id.front);
        back = findViewById(R.id.back);
        View saveAndAdd = findViewById(R.id.save_and_add);

        long cardId = getIntent().getLongExtra(EXTRA_CARD_ID, -1);
        if (cardId != -1) {
            editing = store.getCard(cardId);
            if (editing == null) {
                finish();
                return;
            }
            deckId = editing.getDeckId();
            setTitle(R.string.edit_card);
            saveAndAdd.setVisibility(View.GONE);
            if (savedInstanceState == null) {
                front.setText(editing.getFront());
                back.setText(editing.getBack());
            }
        } else {
            deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);
            setTitle(R.string.add_card);
        }

        findViewById(R.id.save).setOnClickListener(v -> {
            if (save()) finish();
        });
        saveAndAdd.setOnClickListener(v -> {
            if (save()) {
                Toast.makeText(this, R.string.card_saved, Toast.LENGTH_SHORT).show();
                front.setText("");
                back.setText("");
                front.requestFocus();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Saves the card if both sides have text. Returns false (and shows why) otherwise. */
    private boolean save() {
        String frontText = text(front);
        String backText = text(back);
        frontLayout.setError(frontText.isEmpty() ? getString(R.string.card_side_required) : null);
        backLayout.setError(backText.isEmpty() ? getString(R.string.card_side_required) : null);
        if (frontText.isEmpty() || backText.isEmpty()) {
            return false;
        }

        if (editing != null) {
            store.updateCard(editing.withText(frontText, backText));
        } else {
            store.insertCard(Card.create(deckId, frontText, backText, System.currentTimeMillis()));
        }
        return true;
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
