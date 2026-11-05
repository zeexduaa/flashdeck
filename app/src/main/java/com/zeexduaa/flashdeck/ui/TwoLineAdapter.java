package com.zeexduaa.flashdeck.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zeexduaa.flashdeck.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple list of tappable title + subtitle rows, used for both decks and cards.
 * The caller says how to turn an item into text and what a tap or long-press does.
 */
public final class TwoLineAdapter<T> extends RecyclerView.Adapter<TwoLineAdapter.Holder> {

    /** Turns an item into the two lines of text for its row. */
    public interface Binder<T> {
        CharSequence title(T item);

        CharSequence subtitle(T item);
    }

    public interface OnItemAction<T> {
        void onAction(T item);
    }

    private final Binder<T> binder;
    private final OnItemAction<T> onClick;
    private final OnItemAction<T> onLongClick;
    private final List<T> items = new ArrayList<>();

    public TwoLineAdapter(Binder<T> binder, OnItemAction<T> onClick, OnItemAction<T> onLongClick) {
        this.binder = binder;
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public void submit(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged(); // lists are short; a DiffUtil pass is on the roadmap
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_two_line, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        T item = items.get(position);
        holder.title.setText(binder.title(item));
        CharSequence subtitle = binder.subtitle(item);
        holder.subtitle.setText(subtitle);
        holder.subtitle.setVisibility(subtitle == null || subtitle.length() == 0 ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(onClick == null ? null : v -> onClick.onAction(item));
        holder.itemView.setOnLongClickListener(onLongClick == null ? null : v -> {
            onLongClick.onAction(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;

        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
        }
    }
}
