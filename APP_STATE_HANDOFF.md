# Durak App State Handoff

Use this file as context when starting a new ChatGPT conversation about this app. It describes the current repo state, app behavior, architecture, important implementation details, commands, and known gaps.

## Snapshot

- App name: Durak
- Repo path: `/home/qwerty/git/durak_vibed`
- Current date when this handoff was written: 2026-06-26
- Platform: Android app
- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Build system: Gradle Kotlin DSL
- Android Gradle Plugin: `9.2.1`
- Kotlin Compose plugin: `2.4.0`
- Gradle wrapper: `9.4.1`
- Java target/source compatibility: 17
- Android namespace/application id: `com.example.durak`
- SDKs: minSdk 26, targetSdk 36, compileSdk 36
- Main module: `:app`
- Main entry point: `app/src/main/kotlin/com/example/durak/MainActivity.kt`
- Current status: playable offline MVP with pending-take throw-ins and polished table/player visuals.

## Latest Update

Recent local changes on 2026-06-26:

- Added Durak throw-in-before-take handling for Classic and Casual modes.
- Added a `THROW_IN_BEFORE_TAKE` phase and pending-take fields to `GameState`.
- AI players can throw in cheap legal matching-rank cards before a defender takes, then pass.
- Transfer mode remains transfer-only and does not gain normal throw-ins.
- Added distinct AI player badges/accent colors in opponent panels.
- Added a spring-style defense-card snap/settle animation.
- Added a visible side discard/bita pile marker and table-exit animation toward it after successful defense.
- Added persisted legal hint color presets in Settings.
- Fixed take animations so cards fly toward the taking human or AI panel instead of fading in place.
- Switched hand card dragging from long-press drag to regular drag while preserving tap-to-play.
- Simplified the game info panel by removing attacker/defender text; it now prioritizes prompt, mode, deck/table/bita count, trump, and latest event.
- Animation speed now has exactly three modes: Fast, Normal, Slow.
- Animation speed controls AI card timing and table card motion into take/discard/bita targets.
- Release APKs are attached to GitHub Releases as `durak-vibed-debug.apk`; APKs should not be kept in the repo root.

## What The App Is

Durak is an offline Android Durak card game built with Kotlin and Jetpack Compose. It is designed to work on GrapheneOS and standard Android without ads, analytics, Google Play Services, network permissions, or tracking SDKs.

The app currently supports a human player plus AI opponents. The New Game UI allows 2, 3, or 4 total players. Player 0 is always the human, named `You`; players 1 through 3 are AI players named `AI 1`, `AI 2`, etc.

## Privacy And Permissions

- `AndroidManifest.xml` declares the launcher activity only.
- No `INTERNET` permission is present.
- `android:allowBackup="false"` and `android:fullBackupContent="false"`.
- A generated AndroidX dynamic receiver permission is explicitly removed via manifest tools entries.
- Settings are stored locally in `SharedPreferences`.
- Saved game persistence is not actually implemented yet.

## User-Facing Screens

Screens are controlled manually by `GameViewModel.screen`; there is no Navigation Compose graph.

- `MENU`: main menu with New Game, Rules, and Settings.
- `NEW_GAME`: deck size, game mode, player count, AI difficulty, and Start Game.
- `RULES`: static explanation of Classic, Transfer, Casual, attack limits, decks, and drag/drop basics.
- `SETTINGS`: animation speed, card style, legal hint color, legal move hints, confirm new game.
- `GAME`: main table, opponents, compact info panel, action bar, human hand, drag overlay, discard marker, pause menu.
- `END`: game result plus Play Again and Main Menu.

## Main Files

High-level app and state:

- `app/src/main/kotlin/com/example/durak/MainActivity.kt`: creates `GameViewModel`, wires repositories, and switches screens.
- `app/src/main/kotlin/com/example/durak/viewmodel/GameViewModel.kt`: owns screen flow, current game settings, preferences, game state, latest event text, AI turn scheduling, and public UI actions.

Game domain:

- `game/Card.kt`: suits, ranks, card data class, text labels.
- `game/Deck.kt`: deck modes and deck creation/shuffling.
- `game/GameSettings.kt`: deck mode, game mode, player count, AI difficulty.
- `game/GameMode.kt`: `CLASSIC`, `TRANSFER`, `CASUAL`.
- `game/GamePhase.kt`: derived phase enum for prompts and AI/human turn state.
- `game/GameAction.kt`: `DONE`, `TAKE`, `PASS`.
- `game/DropTarget.kt`: table/drop target model for drag/drop.
- `game/GameState.kt`: immutable game state.
- `game/GameRules.kt`: legality checks and helper queries.
- `game/GameEngine.kt`: state transitions for dealing, attack, defend, transfer, take, end attack, draw, phase derivation, and game-over detection.
- `game/AIPlayer.kt`: AI move selection for Easy, Normal, Hard.
- `game/AiMoveEvaluator.kt`: scoring helpers used by Normal/Hard AI.

Persistence:

- `data/SettingsRepository.kt`: `SharedPreferences` backed settings and app preferences.
- `data/SavedGameRepository.kt`: placeholder repository; save clears prefs, load returns null.

UI:

- `ui/MainMenuScreen.kt`
- `ui/NewGameScreen.kt`
- `ui/RulesScreen.kt`
- `ui/SettingsScreen.kt`
- `ui/GameScreen.kt`
- `ui/LegalHintColors.kt`
- `ui/components/CardView.kt`
- `ui/components/TableView.kt`
- `ui/components/ScrollableHandView.kt`
- `ui/components/GameInfoPanel.kt`
- `ui/components/PlayerPanel.kt`
- `ui/components/ActionBar.kt`
- `ui/components/MenuComponents.kt`
- `ui/components/TableExitOverlay.kt`
- `ui/components/CardImageProvider.kt`

Tests:

- `app/src/test/kotlin/com/example/durak/game/GameRulesTest.kt`
- `app/src/test/kotlin/com/example/durak/game/AIPlayerTest.kt`
- `app/src/test/kotlin/com/example/durak/viewmodel/GameViewModelTest.kt`

## Build And Test Commands

Build debug APK:

```bash
./gradlew :app:assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK asset name:

```text
durak-vibed-debug.apk
```

APK files are attached to GitHub Releases and intentionally not committed in the repo root.

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

Requirements:

- Android SDK command-line tools or Android Studio.
- JDK 17 or newer.

## Dependencies

From `app/build.gradle.kts`:

- Compose BOM: `androidx.compose:compose-bom:2026.06.00`
- `androidx.activity:activity-compose:1.13.0`
- `androidx.lifecycle:lifecycle-viewmodel:2.10.0`
- `androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0`
- `androidx.compose.material3:material3`
- `androidx.compose.ui:ui`
- `androidx.compose.ui:ui-tooling-preview`
- Debug: `androidx.compose.ui:ui-tooling`
- Tests: `junit:junit:4.13.2`

## Assets

- Card PNG assets live in `app/src/main/assets/cards/`.
- Card asset names are like `AS.png`, `10D.png`, `back.png`.
- `CardImageProvider` maps `Card` objects to `cards/{rank}{suit}.png`.
- If an asset cannot load, `CardView` falls back to a Compose-drawn card face/back.
- The root `poker-box-qr.zip` is the source archive for bundled card assets.
- The root `new.png` was used to generate launcher icons.
- Launcher icons are under `app/src/main/res/mipmap-*` and `app/src/main/res/drawable/ic_launcher_foreground.png`.

## Game Settings

`GameSettings` defaults:

- Deck: 36 cards.
- Mode: Transfer.
- Player count: 2.
- AI difficulty: Normal.

Deck modes:

- 24 cards: ranks 9 through A.
- 36 cards: ranks 6 through A.
- 52 cards: ranks 2 through A.

Game modes:

- Classic: matching-rank throw-ins are allowed after all attacks are defended; transfers are not allowed.
- Transfer: transfers are allowed; matching-rank throw-ins are not allowed. A successful defense automatically discards the table.
- Casual: both transfer and matching-rank throw-ins are allowed.

AI difficulties:

- Easy: mostly plays lowest legal cards, avoids expensive trump defenses in some cases.
- Normal: prefers cheap non-trump defenses, may transfer when defending would require trump or no defense exists, scores attack/throw-in moves.
- Hard: scores attack, defense, throw-in, and transfer moves more deliberately; may take instead of spending high trump early.

App preferences:

- Animation speed: Fast, Normal, Slow. Default is Normal.
- Card style: Classic, Modern, Minimal.
- Legal hint color: Green, Blue, Gold, Purple, Red. Default is Gold.
- Show legal move hints: default true.
- Confirm new game: default true.

Important note: card style currently only changes card surface colors when image assets are missing or around loaded images; all styles still use the same PNG card images when assets load successfully.

## Game Flow

`GameEngine.newGame`:

1. Creates a shuffled deck using selected `DeckMode`.
2. Creates `playerCount` players, with player 0 human and the rest AI.
3. Deals 6 cards to each player in round-robin order.
4. Chooses `trumpCard` from the last card of the remaining draw pile, or from dealt hands if the pile is empty.
5. Finds the attacker as the player holding the lowest trump.
6. Sets defender to the next active player.
7. Stores defender hand size at bout start.
8. Derives the phase and returns a `GameState`.

Card beating rules:

- Same suit: defense must have higher rank.
- Trump beats any non-trump.
- Non-trump cannot beat trump unless same-suit higher trump.

Turn model:

- `GameState.needsDefense` is true when any table attack has no defense.
- `currentActorIndex` is defender when defense is needed, otherwise attacker.
- `GameEngine.withPhase` derives human/AI attack, defense, throw-in, pass-or-defend, or game-over phases.

Attack and defense:

- Initial attack requires attacker, empty table, and card in hand.
- Defense requires defender, an open attack card, a defense card in hand, and a valid beat.
- Human can tap a legal card or drag it.
- Dragging to the general table can attack, throw in, or transfer depending on rules.
- Dragging onto an attack card or defense slot targets that attack for defense.

Throw-ins:

- Only Classic and Casual allow matching-rank additions.
- Only current attacker can add.
- Only allowed when table is not empty and all attacks are already defended.
- Added card rank must match any visible table attack or defense rank.
- Limit is `min(5, defenderHandSizeAtBoutStart)`.
- Exception: during `THROW_IN_BEFORE_TAKE`, active non-defender players may add matching visible ranks after the defender has committed to taking.

Transfers:

- Only Transfer and Casual allow transfers.
- Only defender can transfer.
- Table must contain open attacks.
- No defense can already be on the table.
- Transfer card rank must match an attack rank already on the table.
- Next active defender must exist and cannot be the current defender.
- New attack count must fit receiving defender limit: `min(5, nextDefender.hand.size)`.
- Transfer adds the transfer card as another attack and changes `defenderIndex`.

Taking:

- Only defender can take.
- Requires non-empty table with at least one open defense.
- In Classic and Casual, take first enters a pending throw-in-before-take phase.
- During pending take, eligible active players except the taking defender can throw in matching visible ranks up to the bout attack limit.
- After all eligible throw-in players pass, have no legal cards, or the attack limit is reached, the taking defender receives all table attack and defense cards.
- In Transfer mode, take still resolves immediately.
- After the take finalizes, all players draw up to 6 in draw order, with defender drawing last.
- Next attacker is next active player after the taking defender.

Ending attack:

- `Done` is only available in Classic and Casual.
- It requires current attacker, non-empty table, and all attacks defended.
- Successful defense discards all table cards.
- Then players draw up to 6.
- Defender becomes next attacker if still holding cards; otherwise next active player attacks.

Game over:

- Checked only when draw pile is empty and table is empty.
- If no players have cards: draw.
- If exactly one player has cards: that player is the loser/durak.
- Otherwise game continues.

## AI Turn Scheduling

`GameViewModel.scheduleAiTurns`:

- Cancels any previous AI job.
- Runs in `viewModelScope`.
- Loops while game is in progress and current actor is not the human.
- Sets `aiThinking = true`.
- Waits based on animation speed and AI difficulty.
- Calls `AIPlayer.chooseMove`.
- Applies the move through `GameEngine`.
- Persists via the placeholder saved-game repository.
- Adds a small after-move pause.
- Uses a guard of 80 AI moves to avoid infinite loops.
- Sets `screen = END` if the game finishes during AI turns.

The human cannot play while `aiThinking` is true.

## Drag And Drop Details

`GameScreen` tracks:

- Overall table bounds.
- Specific drop target bounds for each attack card and defense slot.
- Drag overlay state.
- Opponent bounds for table-exit animations.
- Human hand bounds for take animations.

Drop resolution:

1. If the drop point is inside a specific target bound, use that `DropTarget`.
2. Else if inside table bounds, use `DropTarget.Table`.
3. Else use `DropTarget.None`.

Hand interaction:

- `ScrollableHandView` uses long press drag.
- Legal cards are highlighted when hints are enabled.
- Non-legal cards are dimmed only when there is at least one legal card.
- Cards can also be tapped to attempt default table play.

## Current Limitations And Known Gaps

- Saved game restore is not implemented. `SavedGameRepository.save` clears prefs and `load` always returns null.
- There is no resume game button in the menu.
- The app is an MVP; AI is heuristic and not a complete strategic engine.
- Multiplayer is local human versus AI only; there is no network, online play, or pass-and-play human mode.
- `Screen.RULES` can be opened from the pause menu during a game, but returning from Rules goes to main menu rather than back to the paused game.
- `confirmNewGame` applies to restart/main menu from the in-game menu; starting from New Game always clears the current saved placeholder.
- `ROUND_RESOLUTION` exists in `GamePhase` but is not currently produced by `GameEngine.derivePhase`.
- End screen is reached automatically after AI finishes a game. If the human move directly finishes a game, the current implementation updates `gameState` and schedules AI turns; because no AI loop runs, screen transition to `END` may need checking.
- `CardStyle` options are present, but with loaded image assets the visible card face differences are limited.
- Debug drag logging exists in `GameScreen` via `Log.d("DurakDrag", ...)`.

## Tests Cover

Game rules tests cover:

- Deck sizes.
- New game card counts.
- Classic throw-ins and no transfer.
- Attack limit of `min(5, defender start hand size)`.
- Transfer legality and defender changes.
- Transfer receiving-defender limits.
- Transfers forbidden after any defense is on table.
- Transfer mode auto-discard after successful defense.
- Casual mode transfer plus later throw-in.
- Drag/drop behavior by mode.
- Win/loss detection.

AI tests cover:

- Easy and Hard return legal moves.
- Normal avoids trump when same-suit defense exists.
- Normal chooses lower legal defense.
- Hard preserves trump when possible.
- Hard attacks low cards early.
- Mode-specific AI behavior for Classic, Transfer, Casual.
- AI does not transfer after defense starts.

ViewModel test covers:

- Updating preferences does not start a game or leave the menu.

## Git Ignore Status

This file is intentionally local handoff documentation and is listed in `.gitignore` as:

```text
APP_STATE_HANDOFF.md
```

## Suggested Prompt For A New Chat

Paste this file into a new ChatGPT chat and say:

```text
This is the current state of my Android Kotlin/Jetpack Compose Durak app. Please use it as the app context. I will ask for changes next.
```
