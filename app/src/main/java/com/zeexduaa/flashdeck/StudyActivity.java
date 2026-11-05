package com.zeexduaa.flashdeck;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zeexduaa.flashdeck.data.FlashcardStore;
import com.zeexduaa.flashdeck.model.Card;
import com.zeexduaa.flashdeck.model.Deck;
import com.zeexduaa.flashdeck.srs.Grade;
import com.zeexduaa.flashdeck.srs.Scheduler;
import com.zeexduaa.flashdeck.srs.StudySession;

/**
 * Studying a deck: see the front, try to remember, reveal the back, then say how it went.
 * Each answer is saved straight away, so leaving halfway loses nothing.
 */
public class StudyActivity extends AppCompatActivity {

    private static final String EXTRA_DECK_ID = "deck_id";
    private static final int MAX_CARDS_PER_SESSION = 200;

    private final Scheduler scheduler = new Scheduler();
    private FlashcardStore store;
    private StudySession session;

    private TextView progress;
    private View card;
    private TextView front;
    private TextView back;
    private View divider;
    private View showAnswer;
    private View grades;
    private View finished;
    private TextView finishedStats;
    private final Button[] gradeButtons = new Button[Grade.values().length];

    public static Intent intent(Context context, long deckId) {
        return new Intent(context, StudyActivity.class).putExtra(EXTRA_DECK_ID, deckId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        store = new FlashcardStore(this);

        long deckId = getIntent().getLongExtra(EXTRA_DECK_ID, -1);
        Deck deck = store.getDeck(deckId);
        if (deck == null) {
            finish();
            return;
        }
        setTitle(deck.getName());

        progress = findViewById(R.id.progress);
        card = findViewById(R.id.card);
        front = findViewById(R.id.front);
        back = findViewById(R.id.back);
        divider = findViewById(R.id.divider);
        showAnswer = findViewById(R.id.show_answer);
        grades = findViewById(R.id.grades);
        finished = findViewById(R.id.finished);
        finishedStats = findViewById(R.id.finished_stats);
        gradeButtons[Grade.AGAIN.ordinal()] = findViewById(R.id.grade_again);
        gradeButtons[Grade.HARD.ordinal()] = findViewById(R.id.grade_hard);
        gradeButtons[Grade.GOOD.ordinal()] = findViewById(R.id.grade_good);
        gradeButtons[Grade.EASY.ordinal()] = findViewById(R.id.grade_easy);

        for (Grade grade : Grade.values()) {
            gradeButtons[grade.ordinal()].setOnClickListener(v -> answer(grade));
        }
        showAnswer.setOnClickListener(v -> reveal());
        card.setOnClickListener(v -> {
            if (showAnswer.getVisibility() == View.VISIBLE) reveal();
        });
        findViewById(R.id.back_to_deck).setOnClickListener(v -> finish());

        session = new StudySession(
                store.dueCards(deckId, System.currentTimeMillis(), MAX_CARDS_PER_SESSION), scheduler);
        showCurrent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showCurrent() {
        Card current = session.current();
        if (current == null) {
            showFinished();
            return;
        }
        progress.setText(getResources().getQuantityString(
                R.plurals.cards_left, session.remaining(), session.remaining()));
        front.setText(current.getFront());
        back.setText(current.getBack());
        back.setVisibility(View.INVISIBLE);
        divider.setVisibility(View.INVISIBLE);
        showAnswer.setVisibility(View.VISIBLE);
        grades.setVisibility(View.GONE);
    }

    private void reveal() {
        Card current = session.current();
        if (current == null) return;
        back.setVisibility(View.VISIBLE);
        divider.setVisibility(View.VISIBLE);
        showAnswer.setVisibility(View.GONE);
        grades.setVisibility(View.VISIBLE);

        long[] minutes = scheduler.previewMinutes(current, System.currentTimeMillis());
        int[] labels = {R.string.grade_again, R.string.grade_hard, R.string.grade_good, R.string.grade_easy};
        for (Grade grade : Grade.values()) {
            int i = grade.ordinal();
            gradeButtons[i].setText(getString(labels[i]) + "\n" + Scheduler.formatDelay(minutes[i]));
        }
    }

    private void answer(Grade grade) {
        Card updated = session.answer(grade, System.currentTimeMillis());
        store.updateCard(updated);
        showCurrent();
    }

    private void showFinished() {
        progress.setText("");
        card.setVisibility(View.GONE);
        showAnswer.setVisibility(View.GONE);
        grades.setVisibility(View.GONE);
        finished.setVisibility(View.VISIBLE);
        finishedStats.setText(session.reviewed() == 0
                ? getString(R.string.nothing_to_study)
                : getResources().getQuantityString(R.plurals.session_stats,
                        session.reviewed(), session.reviewed(), session.forgotten()));
    }
}
