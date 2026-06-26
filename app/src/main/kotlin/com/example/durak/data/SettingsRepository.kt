package com.example.durak.data

import android.content.Context
import com.example.durak.game.AiDifficulty
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.game.GameSettings

enum class AnimationSpeed(val title: String, val aiDelayMillis: Long) {
    OFF("Off", 0L),
    NORMAL("Normal", 700L),
    FAST("Fast", 300L)
}

enum class CardStyle(val title: String) {
    CLASSIC("Classic"),
    MODERN("Modern"),
    MINIMAL("Minimal")
}

data class AppPreferences(
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val cardStyle: CardStyle = CardStyle.CLASSIC,
    val showLegalMoveHints: Boolean = true,
    val confirmNewGame: Boolean = true
)

interface SettingsDataSource {
    fun loadGameSettings(): GameSettings
    fun saveGameSettings(settings: GameSettings)
    fun loadAppPreferences(): AppPreferences
    fun saveAppPreferences(preferences: AppPreferences)
}

class SettingsRepository(context: Context) : SettingsDataSource {
    private val prefs = context.getSharedPreferences("durak_settings", Context.MODE_PRIVATE)

    override fun loadGameSettings(): GameSettings = GameSettings(
        deckMode = prefs.getString("deckMode", null)?.let(DeckMode::valueOf) ?: DeckMode.CARDS_36,
        gameMode = prefs.getString("gameMode", null)?.let(GameMode::valueOf) ?: GameMode.THROW_IN,
        playerCount = prefs.getInt("playerCount", 2).coerceIn(2, 4),
        aiDifficulty = prefs.getString("aiDifficulty", null)?.let(AiDifficulty::valueOf) ?: AiDifficulty.NORMAL
    )

    override fun saveGameSettings(settings: GameSettings) {
        prefs.edit()
            .putString("deckMode", settings.deckMode.name)
            .putString("gameMode", settings.gameMode.name)
            .putInt("playerCount", settings.playerCount.coerceIn(2, 4))
            .putString("aiDifficulty", settings.aiDifficulty.name)
            .apply()
    }

    override fun loadAppPreferences(): AppPreferences = AppPreferences(
        animationSpeed = prefs.getString("animationSpeed", null)?.let(AnimationSpeed::valueOf) ?: AnimationSpeed.NORMAL,
        cardStyle = prefs.getString("cardStyle", null)?.let(CardStyle::valueOf) ?: CardStyle.CLASSIC,
        showLegalMoveHints = prefs.getBoolean("showLegalMoveHints", true),
        confirmNewGame = prefs.getBoolean("confirmNewGame", true)
    )

    override fun saveAppPreferences(preferences: AppPreferences) {
        prefs.edit()
            .putString("animationSpeed", preferences.animationSpeed.name)
            .putString("cardStyle", preferences.cardStyle.name)
            .putBoolean("showLegalMoveHints", preferences.showLegalMoveHints)
            .putBoolean("confirmNewGame", preferences.confirmNewGame)
            .apply()
    }
}
