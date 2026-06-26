package com.example.durak.data

import android.content.Context
import com.example.durak.game.AiDifficulty
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.game.GameSettings

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("durak_settings", Context.MODE_PRIVATE)

    fun load(): GameSettings = GameSettings(
        deckMode = prefs.getString("deckMode", null)?.let(DeckMode::valueOf) ?: DeckMode.CARDS_36,
        gameMode = prefs.getString("gameMode", null)?.let(GameMode::valueOf) ?: GameMode.THROW_IN,
        playerCount = prefs.getInt("playerCount", 2).coerceIn(2, 4),
        aiDifficulty = prefs.getString("aiDifficulty", null)?.let(AiDifficulty::valueOf) ?: AiDifficulty.NORMAL
    )

    fun save(settings: GameSettings) {
        prefs.edit()
            .putString("deckMode", settings.deckMode.name)
            .putString("gameMode", settings.gameMode.name)
            .putInt("playerCount", settings.playerCount)
            .putString("aiDifficulty", settings.aiDifficulty.name)
            .apply()
    }
}
