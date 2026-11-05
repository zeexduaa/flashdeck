package com.zeexduaa.flashdeck;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zeexduaa.flashdeck.data.FlashcardStore;
import com.zeexduaa.flashdeck.model.Card;
import com.zeexduaa.flashdeck.model.Deck;
import com.zeexduaa.flashdeck.srs.Scheduler;
import com.zeexduaa.flashdeck.ui.Dialogs;
import com.zeexduaa.flashdeck.ui.TwoLineAdapter;

import java.util.List;

/** One deck: its cards, how many are due, and the way into studying them. */
public class DeckActivity extends AppCompatActivity {

    private static final String EXTRA_DECK_ID = "deck_id";

    private FlashcardStore store;
    private long deckId;
    private TwoLineAdapter<Card> adapter;
    private TextView summary;
    private Button study;
    private View empty;

    public static Intent intent(Context context, long deckId) {
        return new Intent(context, DeckActivity.class).putExtra(EXTRA_DECK_ID, deckId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        store = new FlashcardStore(this);
        deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);

        adapter = new TwoLineAdapter<>(
                new TwoLineAdapter.Binder<Card>() {
                    @Override
                    public CharSequence title(Card card) {
                        return card.getFront();
                    }

                    @Override
                    public CharSequence subtitle(Card card) {
                        return card.getBack() + "\n" + dueLabel(card);
                    }
                },
                card -> startActivity(CardEditActivity.editIntent(this, card.getId())),
                card -> Dialogs.confirm(this, R.string.delete_card,
                        getString(R.string.delete_card_confirm), R.string.delete, () -> {
                            store.deleteCard(card.getId());
                            refresh();
                        }));

        RecyclerView list = findViewById(R.id.card_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        list.setAdapter(adapter);
        summary = findViewById(R.id.summary);
        study = findViewById(R.id.study);
        empty = findViewById(R.id.empty);

        study.setVisibility(View.GONE); // the study screen comes next

        findViewById(R.id.add_card).setOnClickListener(v ->
                startActivity(CardEditActivity.addIntent(this, deckId)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void refresh() {
        Deck deck = store.getDeck(deckId);
        if (deck == null) { // deleted elsewhere, or a bad id
            finish();
            return;
        }
        setTitle(deck.getName());

        long now = System.currentTimeMillis();
        List<Card> cards = store.listCards(deckId);
        int due = 0;
        for (Card card : cards) {
            if (card.isDue(now)) due++;
        }
        adapter.submit(cards);
        empty.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
        summary.setText(due == 0
                ? getString(R.string.nothing_due)
                : getResources().getQuantityString(R.plurals.cards_due, due, due));
    }

    private String dueLabel(Card card) {
        if (card.isNew()) return getString(R.string.card_new);
        long now = System.currentTimeMillis();
        if (card.isDue(now)) return getString(R.string.card_due_now);
        long minutes = (card.getDueAt() - now) / 60_000L;
        return getString(R.string.card_due_in, Scheduler.formatDelay(Math.max(1, minutes)));
    }
}
