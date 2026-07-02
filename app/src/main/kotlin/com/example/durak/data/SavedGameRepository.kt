package com.example.durak.data

import android.content.Context
import com.example.durak.game.GameState
import com.example.durak.game.GameStatus

interface SavedGameDataSource {
    fun save(state: GameState)
    fun load(): GameState?
    fun clear()
}

class SavedGameRepository(context: Context) : SavedGameDataSource {
    private val prefs = context.getSharedPreferences("durak_saved_game", Context.MODE_PRIVATE)

    override fun save(state: GameState) {
        if (state.status != GameStatus.IN_PROGRESS) {
            clear()
            return
        }
        prefs.edit().putString(KEY_STATE, GameStateCodec.encode(state)).apply()
    }

    override fun load(): GameState? =
        prefs.getString(KEY_STATE, null)
            ?.let(GameStateCodec::decode)
            ?.takeIf { it.status == GameStatus.IN_PROGRESS }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_STATE = "state_v1"
    }
}
