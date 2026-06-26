package com.example.durak.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.durak.data.AppPreferences
import com.example.durak.data.SavedGameDataSource
import com.example.durak.data.SettingsDataSource
import com.example.durak.game.AIPlayer
import com.example.durak.game.AiMove
import com.example.durak.game.Card
import com.example.durak.game.GameEngine
import com.example.durak.game.GameMode
import com.example.durak.game.GamePhase
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import com.example.durak.game.GameStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen {
    MENU,
    NEW_GAME,
    RULES,
    SETTINGS,
    GAME,
    END
}

enum class GameAction(val title: String) {
    DONE("Done"),
    TAKE("Take"),
    PASS("Pass")
}

class GameViewModel(
    private val settingsRepository: SettingsDataSource,
    private val savedGameRepository: SavedGameDataSource,
    private val engine: GameEngine = GameEngine(),
    private val ai: AIPlayer = AIPlayer()
) : ViewModel() {
    var screen by mutableStateOf(Screen.MENU)
        private set
    var gameOptions by mutableStateOf(settingsRepository.loadGameSettings())
        private set
    var appPreferences by mutableStateOf(settingsRepository.loadAppPreferences())
        private set
    var gameState by mutableStateOf<GameState?>(null)
        private set
    var snackbar by mutableStateOf("")
        private set
    var aiThinking by mutableStateOf(false)
        private set
    var hasSavedGame by mutableStateOf(savedGameRepository.hasSavedGame())
        private set

    private var aiJob: Job? = null

    fun goTo(next: Screen) {
        screen = next
    }

    fun updateGameOptions(next: GameSettings) {
        gameOptions = next.copy(playerCount = next.playerCount.coerceIn(2, 4))
        settingsRepository.saveGameSettings(gameOptions)
    }

    fun updateAppPreferences(next: AppPreferences) {
        appPreferences = next
        settingsRepository.saveAppPreferences(next)
    }

    fun startGame() {
        settingsRepository.saveGameSettings(gameOptions)
        savedGameRepository.clear()
        hasSavedGame = false
        gameState = engine.newGame(gameOptions)
        screen = Screen.GAME
        scheduleAiTurns()
    }

    fun restartGame() {
        startGame()
    }

    fun continueGame() {
        val saved = savedGameRepository.load()
        if (saved == null) {
            show("No saved game yet.")
            hasSavedGame = false
            return
        }
        gameState = saved
        screen = Screen.GAME
        scheduleAiTurns()
    }

    fun playAgain() {
        startGame()
    }

    fun playHumanCard(card: Card): Boolean {
        val state = gameState ?: return false
        if (state.currentActorIndex != 0 || state.status == GameStatus.FINISHED || aiThinking) {
            show("Wait for your turn.")
            return false
        }
        val before = state
        val result = engine.playCard(state, 0, card)
        gameState = result.state
        show(result.state.message.ifBlank { result.message })
        persistGame()
        scheduleAiTurns()
        return result.state != before
    }

    fun take() {
        val state = gameState ?: return
        val result = engine.take(state, 0)
        gameState = result.state
        show(result.state.message.ifBlank { result.message })
        persistGame()
        scheduleAiTurns()
    }

    fun done() {
        val state = gameState ?: return
        val result = engine.endAttack(state, 0)
        gameState = result.state
        show(result.state.message.ifBlank { result.message })
        persistGame()
        scheduleAiTurns()
    }

    fun passHint() {
        val cards = getLegalPassCardsForHuman()
        if (cards.isEmpty()) show("Pass is not legal now.") else show("Drag a matching rank to pass.")
    }

    fun invalidDrop(card: Card) {
        val legal = card in getLegalCardsForHuman()
        show(if (legal) "Drop the card on the table." else "That card is not legal now.")
    }

    fun getLegalCardsForHuman(): Set<Card> =
        gameState?.takeIf { it.currentActorIndex == 0 && !aiThinking }?.let { engine.legalCards(it, 0) }.orEmpty()

    fun getLegalPassCardsForHuman(): Set<Card> =
        gameState?.takeIf { it.currentActorIndex == 0 && !aiThinking }?.let { engine.legalPassCards(it, 0) }.orEmpty()

    fun getAvailableActions(): List<GameAction> {
        val state = gameState ?: return emptyList()
        if (state.status == GameStatus.FINISHED || state.currentActorIndex != 0 || aiThinking) return emptyList()
        val actions = mutableListOf<GameAction>()
        if (state.phase == GamePhase.HUMAN_DEFENSE) {
            actions += GameAction.TAKE
            if (state.settings.gameMode == GameMode.PASSING && engine.canAnyPass(state, 0)) actions += GameAction.PASS
        }
        if ((state.phase == GamePhase.HUMAN_THROW_IN || state.phase == GamePhase.HUMAN_ATTACK) && engine.canEndAttack(state, 0)) {
            actions += GameAction.DONE
        }
        return actions
    }

    fun getUserPromptText(): String {
        val state = gameState ?: return "Start a new game"
        if (aiThinking) return "AI is thinking..."
        return when (state.phase) {
            GamePhase.DEALING -> "Dealing cards..."
            GamePhase.HUMAN_ATTACK -> "Your attack. Drag a card to the table."
            GamePhase.HUMAN_DEFENSE -> {
                if (getLegalPassCardsForHuman().isNotEmpty()) "You can pass or defend."
                else "Your defense. Drag a card that beats the attack."
            }
            GamePhase.HUMAN_THROW_IN -> "Choose a card to throw in, or tap Done."
            GamePhase.AI_ATTACK, GamePhase.AI_DEFENSE -> "AI is thinking..."
            GamePhase.ROUND_RESOLUTION -> "Resolving the round..."
            GamePhase.GAME_OVER -> "Game over"
        }
    }

    fun clearSnackbar() {
        snackbar = ""
    }

    private fun scheduleAiTurns() {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            var state = gameState ?: return@launch
            var guard = 0
            while (state.status == GameStatus.IN_PROGRESS && state.currentActorIndex != 0 && guard < 80) {
                aiThinking = true
                val wait = appPreferences.animationSpeed.aiDelayMillis
                if (wait > 0L) delay(wait)
                val actor = state.currentActorIndex
                val move = ai.chooseMove(state, actor)
                val result = when (move) {
                    is AiMove.Play -> engine.playCard(state, actor, move.card)
                    AiMove.Take -> engine.take(state, actor)
                    AiMove.Done -> engine.endAttack(state, actor)
                }
                state = result.state
                gameState = state
                show(describeAiMove(actor, move, state.message.ifBlank { result.message }))
                persistGame()
                guard++
                if (appPreferences.animationSpeed.aiDelayMillis > 0L) delay(180L)
            }
            aiThinking = false
            if (state.status == GameStatus.FINISHED) screen = Screen.END
        }
    }

    private fun describeAiMove(actor: Int, move: AiMove, fallback: String): String =
        when (move) {
            is AiMove.Play -> {
                val verb = when {
                    fallback.contains("passed", ignoreCase = true) -> "passes"
                    fallback.contains("defended", ignoreCase = true) -> "defends"
                    else -> "attacks"
                }
                "AI $actor $verb with ${move.card}"
            }
            AiMove.Take -> "AI $actor takes"
            AiMove.Done -> "AI $actor ends the attack"
        }

    private fun persistGame() {
        gameState?.let(savedGameRepository::save)
        hasSavedGame = savedGameRepository.hasSavedGame()
    }

    private fun show(message: String) {
        snackbar = message
    }
}
