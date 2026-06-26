# Durak

Native offline Android Durak card game built with Kotlin and Jetpack Compose.

## Privacy

- No internet permission.
- No analytics.
- No ads.
- No tracking SDKs.
- Settings are stored locally with `SharedPreferences`.

## Build APK

Open the project in Android Studio and build `:app:assembleDebug`, or run:

```bash
./gradlew :app:assembleDebug
```

The debug APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Download APK

After building locally, upload this generated APK file to a GitHub Release:

```text
app/build/outputs/apk/debug/app-debug.apk
```

`app/build/` is ignored by git, so the generated APK is not committed to the repository.

When publishing on GitHub, upload that same file to a release and name it:

```text
durak-debug.apk
```

Then this link will download it from the latest GitHub release:

[Download latest APK](../../releases/latest/download/durak-debug.apk)

## Install on GrapheneOS

1. Build the APK.
2. Copy `app-debug.apk` to the device.
3. Open it from the Files app and allow installation from that source if prompted.
4. Launch `Durak`.

The app runs fully offline and does not request network access.

## Implemented Modes

- 24-card, 36-card, and 52-card decks.
- Classic Durak.
- Throw-in / Podkidnoy Durak.
- Passing / Perevodnoy Durak.
- Casual Durak with relaxed throw-in and passing behavior.
- Human vs AI with 2 to 4 total players.
- AI attacks with low cards, defends with the lowest legal card, passes when legal, and takes when it cannot defend usefully.

## Architecture

Core game code is separated from UI:

- `Card`, `Suit`, `Rank`
- `Deck`
- `Player`
- `GameMode`
- `GameRules`
- `GameState`
- `GameEngine`
- `AIPlayer`
- `DurakViewModel`
- Compose screens in `MainActivity.kt`

## Tests

Run local unit tests with:

```bash
./gradlew testDebugUnitTest
```

Tests cover deck creation, trump comparison, valid defense, throw-in validation, passing validation, and win/loss detection.

## Known Limitations

- Card art is text-based using Unicode suits.
- AI is legal and playable, but intentionally simple.
- Multi-human local play is not exposed in the UI; the engine supports 2 to 4 players internally.
