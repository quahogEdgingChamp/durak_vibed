# Durak

Offline Android Durak card game built with Kotlin and Jetpack Compose.

Durak runs fully offline on GrapheneOS and standard Android. It has no ads,
analytics, tracking SDKs, Google Play Services dependency, or network
permission.

## Download

[Download the latest APK](../../releases/latest/download/durak-debug.apk)

## Install on GrapheneOS

1. Download `durak-debug.apk` from the latest release.
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
- Bundled local card image assets from `poker-box-qr.zip`.
- Android launcher icon generated from `icon.png`.
- Drag cards from your hand to the table to attack, defend, add matching ranks, or transfer.
- Contextual actions for `Done`, `Take`, and `Pass`.
- Local settings for animation speed, card style, hints, and confirmations.

## Privacy

Durak is privacy-friendly by design:

- No `INTERNET` permission.
- No analytics.
- No ads.
- No third-party tracking libraries.
- No Google Play Services dependency.
- Settings are stored locally on the device.

## Assets

Card faces and backs are bundled under `app/src/main/assets/cards/` and are
loaded locally at runtime. They were generated from the SVG files in
`poker-box-qr.zip`.

Launcher icon resources are generated from the root `icon.png` file.

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
