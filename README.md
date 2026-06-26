# Durak

Offline Android Durak card game built with Kotlin and Jetpack Compose.

Durak runs fully offline on GrapheneOS and standard Android. It has no ads,
analytics, tracking SDKs, Google Play Services dependency, or network
permission.

## Download

[Download the latest APK](../../releases/latest/download/durak-vibed-debug.apk)

## Install on GrapheneOS

1. Download `durak-vibed-debug.apk` from the latest release.
2. Open the APK from the Files app.
3. Allow installation from that source if GrapheneOS asks.
4. Install and launch `Durak`.

## Features

- Single-player Durak against AI.
- 24-card, 36-card, and 52-card decks.
- Classic mode: matching-rank throw-ins allowed, no transfers.
- Transfer mode: transfers allowed, no throw-ins.
- Casual mode: both transfers and matching-rank throw-ins allowed.
- Easy, Normal, and Hard AI difficulty.
- Custom drawn card UI.
- Bundled local card image assets from `design_assets/poker-box-qr.zip`.
- Android launcher icon generated from `design_assets/new.png`.
- Selectable card back designs loaded from `app/src/main/assets/card_backs/`.
- Drag cards from your hand to the table to attack, defend, add matching ranks, or transfer.
- Contextual actions for `Done`, `Take`, and `Pass`.
- Local settings for AI/table animation speed, card style, hint color, hints, and confirmations.
- Distinct AI player badges, defense snap animation, and table cards flying to the correct take/discard target.
- Configurable legal move highlight color.

## Privacy

Durak is privacy-friendly by design:

- No `INTERNET` permission.
- No analytics.
- No ads.
- No third-party tracking libraries.
- No Google Play Services dependency.
- Settings are stored locally on the device.

## Assets

Card faces are bundled under `app/src/main/assets/cards/` and are loaded
locally at runtime. They were generated from the SVG files in
`design_assets/poker-box-qr.zip`.

Selectable card backs are bundled under `app/src/main/assets/card_backs/`.
The existing `app/src/main/assets/cards/back.png` remains as a fallback.

Reusable/source art archives are kept under `design_assets/`, not in the
project root. Launcher icon resources are generated from `design_assets/new.png`.

## Build From Source

Requirements:

- Android Studio or Android SDK command-line tools.
- JDK 17 or newer.

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

Output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APKs are attached to GitHub Releases, not stored in the repository root.

## Current UI Notes

- AI/table animation speed has three modes: Fast, Normal, and Slow.
- The speed setting controls AI throw timing and table card movement into take/discard targets.
- Hand cards support tap-to-play and drag-to-drop.
- The game info panel shows deck, table, and bita counts while omitting attacker/defender text to keep the prompt and event text readable.
- Card back designs can be selected in Settings and are used for opponent cards and other face-down cards.

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

## Project Structure

```text
app/src/main/kotlin/com/example/durak/
  data/        Local settings and saved-game repository structure
  game/        Cards, deck, rules, game state, engine, and AI
  ui/          Compose screens
  ui/components/
               Reusable card, table, player, and action components
  viewmodel/   GameViewModel and screen flow
```

## Status

This is a playable MVP. AI is intentionally simple, and full saved-game restore
is not enabled yet.
