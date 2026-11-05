package com.zeexduaa.flashdeck.ui;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zeexduaa.flashdeck.R;

/** Small dialogs shared by the screens. */
public final class Dialogs {

    public interface OnText {
        void onText(String text);
    }

    private Dialogs() {
    }

    /**
     * Asks for one line of text, such as a deck name. Saving with an empty box shows an
     * error and keeps the dialog open.
     */
    public static void askForText(Context context, @StringRes int title, @StringRes int hint,
                                  String initial, OnText onSave) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint(hint);
        input.setSingleLine(true);
        if (initial != null) {
            input.setText(initial);
            input.setSelection(initial.length());
        }

        FrameLayout box = new FrameLayout(context);
        int pad = Math.round(20 * context.getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad / 2, pad, 0);
        box.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(box)
                .setPositiveButton(R.string.save, null) // set below so an empty name can't close it
                .setNegativeButton(R.string.cancel, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                input.setError(context.getString(R.string.name_required));
                return;
            }
            onSave.onText(text);
            dialog.dismiss();
        }));
        dialog.show();
        input.requestFocus();
    }

    /** A yes/no confirmation, such as "Delete this deck?". */
    public static void confirm(Context context, @StringRes int title, CharSequence message,
                               @StringRes int confirmLabel, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(confirmLabel, (d, which) -> onConfirm.run())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
