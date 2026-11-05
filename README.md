# Flashdeck

[![CI](https://github.com/zeexduaa/flashdeck/actions/workflows/ci.yml/badge.svg)](https://github.com/zeexduaa/flashdeck/actions/workflows/ci.yml)
![Android 8.0+](https://img.shields.io/badge/android-8.0%2B-3DDC84)
![Java 17](https://img.shields.io/badge/java-17-orange)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

**A simple, offline flashcard app for Android that uses spaced repetition to show you each card right before you'd forget it.**

Make decks, add cards with a question on the front and the answer on the back, and study the ones that are due. After each card you say how well you remembered it (Again, Hard, Good or Easy), and Flashdeck schedules it further out the better you know it. Everything is stored on your phone; there are no accounts, no ads and no network access.

- **Who it's for:** students and anyone learning vocabulary, definitions, formulas or facts, who wants something lighter than Anki.
- **Built with:** Java 17, Android SDK (min Android 8.0, target 15), AndroidX, Material 3, SQLite. JUnit tests, GitHub Actions CI.

> Status: **early (v0.1)**. The core loop works: decks, cards, studying and scheduling. See the [roadmap](ROADMAP.md) for what's next.

## Features

- Decks with card and due counts on the home screen
- Add, edit and delete cards ("Save & add another" for fast entry)
- Study screen: tap to reveal, then grade; each button shows when the card would come back
- Spaced repetition based on SM-2, with forgotten cards shown again before the session ends
- Fully offline, data stays in an on-device SQLite database

## Build and run

You need [Android Studio](https://developer.android.com/studio) (any recent version) with JDK 17.

1. Clone the repository:
   ```
   git clone https://github.com/zeexduaa/flashdeck.git
   ```
2. In Android Studio choose **File → Open** and select the `flashdeck` folder. Let Gradle sync.
3. Pick an emulator or a connected phone and press **Run**.

From the command line:

```
./gradlew testDebugUnitTest   # unit tests
./gradlew assembleDebug       # builds app/build/outputs/apk/debug/app-debug.apk
```

(On Windows use `gradlew.bat`.)

## How scheduling works

Flashdeck uses a four-button version of the SM-2 algorithm:

| Button    | What happens                                                                   |
|-----------|--------------------------------------------------------------------------------|
| **Again** | You forgot. The card comes back in 10 minutes and its ease drops by 0.20.       |
| **Hard**  | Interval grows only x1.2, ease drops by 0.15.                                   |
| **Good**  | 1 day, then 6 days, then the last interval times the card's ease (starts at 2.5). |
| **Easy**  | Like Good with a x1.3 bonus (4 days the first time); ease rises by 0.15.         |

Ease never drops below 1.3, and a successful review always pushes a card at least one day further out. The rules live in [`Scheduler.java`](app/src/main/java/com/zeexduaa/flashdeck/srs/Scheduler.java) and are covered by [unit tests](app/src/test/java/com/zeexduaa/flashdeck/srs/SchedulerTest.java).

## Project layout

```
app/src/main/java/com/zeexduaa/flashdeck/
  model/       Deck, Card, DeckSummary (plain Java, no Android)
  srs/         Scheduler (SM-2), Grade, StudySession (plain Java, unit-tested)
  data/        SQLite database and FlashcardStore
  ui/          shared list adapter and dialogs
  MainActivity, DeckActivity, CardEditActivity, StudyActivity
```

The spaced-repetition logic has no Android dependencies, so it can be tested on any JVM.

## Contributing

Ideas and bug reports are welcome in the [issues](https://github.com/zeexduaa/flashdeck/issues).

## License

[MIT](LICENSE) © 2026 Syed Ali
