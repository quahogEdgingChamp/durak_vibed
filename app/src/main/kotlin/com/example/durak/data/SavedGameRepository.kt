package com.example.durak.data

import android.content.Context
import com.example.durak.game.GameState

interface SavedGameDataSource {
    fun hasSavedGame(): Boolean
    fun save(state: GameState)
    fun load(): GameState?
    fun clear()
}

class SavedGameRepository(context: Context) : SavedGameDataSource {
    private val prefs = context.getSharedPreferences("durak_saved_game", Context.MODE_PRIVATE)

    override fun hasSavedGame(): Boolean = prefs.getBoolean("hasSavedGame", false)

    override fun save(state: GameState) {
        // Full state serialization is intentionally not enabled until it can
        // restore every deck, hand, table, and turn field without ambiguity.
        prefs.edit().putBoolean("hasSavedGame", false).apply()
    }

    override fun load(): GameState? = null

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
