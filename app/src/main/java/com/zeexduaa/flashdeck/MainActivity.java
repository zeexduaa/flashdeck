package com.zeexduaa.flashdeck;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zeexduaa.flashdeck.data.FlashcardStore;
import com.zeexduaa.flashdeck.model.Deck;
import com.zeexduaa.flashdeck.model.DeckSummary;
import com.zeexduaa.flashdeck.ui.Dialogs;
import com.zeexduaa.flashdeck.ui.TwoLineAdapter;

import java.util.List;

/** The home screen: your decks, with how many cards each has and how many are due. */
public class MainActivity extends AppCompatActivity {

    private FlashcardStore store;
    private TwoLineAdapter<DeckSummary> adapter;
    private View empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        store = new FlashcardStore(this);

        adapter = new TwoLineAdapter<>(
                new TwoLineAdapter.Binder<DeckSummary>() {
                    @Override
                    public CharSequence title(DeckSummary s) {
                        return s.getDeck().getName();
                    }

                    @Override
                    public CharSequence subtitle(DeckSummary s) {
                        return getResources().getQuantityString(R.plurals.deck_counts,
                                s.getTotalCards(), s.getTotalCards(), s.getDueCards());
                    }
                },
                s -> startActivity(DeckActivity.intent(this, s.getDeck().getId())),
                this::showDeckMenu);

        RecyclerView list = findViewById(R.id.deck_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        empty = findViewById(R.id.empty);

        findViewById(R.id.add_deck).setOnClickListener(v ->
                Dialogs.askForText(this, R.string.new_deck, R.string.deck_name_hint, null, name -> {
                    store.insertDeck(Deck.create(name, System.currentTimeMillis()));
                    refresh();
                }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(); // due counts change as time passes and after studying
    }

    private void refresh() {
        List<DeckSummary> decks = store.listDecks(System.currentTimeMillis());
        adapter.submit(decks);
        empty.setVisibility(decks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDeckMenu(DeckSummary summary) {
        Deck deck = summary.getDeck();
        String[] actions = {getString(R.string.rename), getString(R.string.delete)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(deck.getName())
                .setItems(actions, (d, which) -> {
                    if (which == 0) {
                        Dialogs.askForText(this, R.string.rename_deck, R.string.deck_name_hint,
                                deck.getName(), name -> {
                                    store.renameDeck(deck.getId(), name);
                                    refresh();
                                });
                    } else {
                        Dialogs.confirm(this, R.string.delete_deck,
                                getString(R.string.delete_deck_confirm, deck.getName(), summary.getTotalCards()),
                                R.string.delete, () -> {
                                    store.deleteDeck(deck.getId());
                                    refresh();
                                });
                    }
                })
                .show();
    }
}
