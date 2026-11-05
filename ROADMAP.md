# Roadmap

Where Flashdeck is going: a fast, private flashcard app that's pleasant enough to open every day.

Sizes are rough guesses (S = a few hours, M = about a day, L = several days, XL = a week or more).
The order may change as I use it.

## ✅ v0.1: the core loop

- Decks and cards stored in SQLite, with card and due counts
- Add, edit and delete cards
- Study screen with Again / Hard / Good / Easy and SM-2 scheduling
- Unit tests for the scheduler and study session, CI on GitHub Actions

## v0.2: solid foundations

- [ ] **Background database work**: move every `FlashcardStore` call off the main thread (executor + main-thread callbacks) and turn on StrictMode in debug builds (M)
- [ ] **Keep the study session across rotation** and process death (`ViewModel` + saved state) (M)
- [ ] **Instrumented tests** for `FlashcardStore` and the main flows with Espresso (M)
- [ ] **DiffUtil / ListAdapter** for the deck and card lists, with item animations (S)
- [ ] **Search** cards within a deck and across all decks (M)
- [ ] **Undo snackbar** after deleting a card or deck instead of a confirmation dialog (S)

## v0.3: smarter studying

- [ ] **Daily limits**: new cards per day and maximum reviews per day, per deck (M)
- [ ] **Learning steps**: configurable short steps for new and forgotten cards (e.g. 1m, 10m, 1d) (M)
- [ ] **Reverse cards**: optionally study back → front as well (M)
- [ ] **Interval fuzz** so cards added together don't all come due on the same day (S)
- [ ] **Type the answer** mode with a simple diff against the correct answer (M)
- [ ] **Bury and suspend** cards (S)

## v0.4: getting cards in and out

- [ ] **CSV import and export** (front, back, optional tags), using the Storage Access Framework (M)
- [ ] **Backup and restore** the whole database to a file (M)
- [ ] **Anki `.apkg` import** for basic note types (L)
- [ ] **Tags** on cards, and studying by tag across decks (L)

## v0.5: richer cards

- [ ] **Markdown formatting** on cards: bold, italics, lists, code (M)
- [ ] **Images** on either side, copied into app storage (L)
- [ ] **Text-to-speech** button for language decks, with a per-deck language (M)

## v0.6: motivation

- [ ] **Statistics screen**: reviews per day, retention rate, forecast of upcoming reviews, with charts (L)
- [ ] **Daily reminder notification** at a time you choose, only when cards are due (M)
- [ ] **Home screen widget** showing cards due today (M)
- [ ] **Study streaks** (S)

## v1.0: release

- [ ] Accessibility pass (TalkBack labels, touch targets, font scaling) (M)
- [ ] Translations (S per language)
- [ ] App icon and screenshots, F-Droid and Play Store listings (M)
