package com.example.durak.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.durak.data.SettingsStore
import com.example.durak.game.AIPlayer
import com.example.durak.game.AiMove
import com.example.durak.game.Card
import com.example.durak.game.GameEngine
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import com.example.durak.game.GameStatus

enum class Screen {
    MENU,
    NEW_GAME,
    RULES,
    SETTINGS,
    GAME,
    END
}

class DurakViewModel(
    private val settingsStore: SettingsStore,
    private val engine: GameEngine = GameEngine(),
    private val ai: AIPlayer = AIPlayer()
) : ViewModel() {
    var screen by mutableStateOf(Screen.MENU)
        private set
    var settings by mutableStateOf(settingsStore.load())
        private set
    var gameState by mutableStateOf<GameState?>(null)
        private set
    var snackbar by mutableStateOf("")
        private set

    fun goTo(next: Screen) {
        screen = next
    }

    fun updateSettings(next: GameSettings) {
        settings = next.copy(playerCount = next.playerCount.coerceIn(2, 4))
        settingsStore.save(settings)
    }

    fun startGame() {
        settingsStore.save(settings)
        gameState = engine.newGame(settings)
        screen = Screen.GAME
        runAiTurns()
    }

    fun playAgain() {
        startGame()
    }

    fun playCard(card: Card) {
        val state = gameState ?: return
        if (state.currentActorIndex != 0) {
            show("Wait for your turn.")
            return
        }
        val result = engine.playCard(state, 0, card)
        gameState = result.state
        show(result.message)
        runAiTurns()
    }

    fun take() {
        val state = gameState ?: return
        val result = engine.take(state, 0)
        gameState = result.state
        show(result.message)
        runAiTurns()
    }

    fun done() {
        val state = gameState ?: return
        val result = engine.endAttack(state, 0)
        gameState = result.state
        show(result.message)
        runAiTurns()
    }

    fun legalHumanCards(): Set<Card> =
        gameState?.takeIf { it.currentActorIndex == 0 }?.let { engine.legalCards(it, 0) }.orEmpty()

    fun actionHint(action: String) {
        show("Tap a highlighted card to $action.")
    }

    fun clearSnackbar() {
        snackbar = ""
    }

    private fun runAiTurns() {
        var state = gameState ?: return
        var guard = 0
        while (state.status == GameStatus.IN_PROGRESS && state.currentActorIndex != 0 && guard < 200) {
            val actor = state.currentActorIndex
            val result = when (val move = ai.chooseMove(state, actor)) {
                is AiMove.Play -> engine.playCard(state, actor, move.card)
                AiMove.Take -> engine.take(state, actor)
                AiMove.Done -> engine.endAttack(state, actor)
            }
            state = result.state
            guard++
        }
        gameState = state
        if (state.status == GameStatus.FINISHED) screen = Screen.END
    }

    private fun show(message: String) {
        snackbar = message
    }
}
