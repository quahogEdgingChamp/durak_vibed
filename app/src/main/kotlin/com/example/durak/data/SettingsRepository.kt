package com.example.durak.data

import android.content.Context
import com.example.durak.game.AiDifficulty
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.game.GameSettings

enum class AnimationSpeed(val title: String, val aiDelayMillis: Long) {
    FAST("Fast", 300L),
    NORMAL("Normal", 700L),
    SLOW("Slow", 1100L)
}

enum class CardStyle(val title: String) {
    CLASSIC("Classic"),
    MODERN("Modern"),
    MINIMAL("Minimal")
}

enum class LegalHintColor(val title: String, val red: Int, val green: Int, val blue: Int) {
    GREEN("Green", 84, 214, 147),
    BLUE("Blue", 96, 177, 255),
    GOLD("Gold", 255, 200, 87),
    PURPLE("Purple", 176, 132, 255),
    RED("Red", 255, 113, 113)
}

data class AppPreferences(
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val cardStyle: CardStyle = CardStyle.CLASSIC,
    val legalHintColor: LegalHintColor = LegalHintColor.GOLD,
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
        deckMode = prefs.getString("deckMode", null).enumValueOrNull<DeckMode>() ?: DeckMode.CARDS_36,
        gameMode = prefs.getString("gameMode", null).toGameModeOrDefault(),
        playerCount = prefs.getInt("playerCount", 2).coerceIn(2, 4),
        aiDifficulty = prefs.getString("aiDifficulty", null).enumValueOrNull<AiDifficulty>() ?: AiDifficulty.NORMAL
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
        animationSpeed = prefs.getString("animationSpeed", null).enumValueOrNull<AnimationSpeed>() ?: AnimationSpeed.NORMAL,
        cardStyle = prefs.getString("cardStyle", null).enumValueOrNull<CardStyle>() ?: CardStyle.CLASSIC,
        legalHintColor = prefs.getString("legalHintColor", null).enumValueOrNull<LegalHintColor>() ?: LegalHintColor.GOLD,
        showLegalMoveHints = prefs.getBoolean("showLegalMoveHints", true),
        confirmNewGame = prefs.getBoolean("confirmNewGame", true)
    )

    override fun saveAppPreferences(preferences: AppPreferences) {
        prefs.edit()
            .putString("animationSpeed", preferences.animationSpeed.name)
            .putString("cardStyle", preferences.cardStyle.name)
            .putString("legalHintColor", preferences.legalHintColor.name)
            .putBoolean("showLegalMoveHints", preferences.showLegalMoveHints)
            .putBoolean("confirmNewGame", preferences.confirmNewGame)
            .apply()
    }
}

private inline fun <reified T : Enum<T>> String?.enumValueOrNull(): T? =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } }

private fun String?.toGameModeOrDefault(): GameMode =
    when (this) {
        "CLASSIC" -> GameMode.CLASSIC
        "TRANSFER", "PASSING" -> GameMode.TRANSFER
        "CASUAL" -> GameMode.CASUAL
        "THROW_IN" -> GameMode.CLASSIC
        else -> GameMode.TRANSFER
    }
