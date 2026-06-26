package com.example.durak.viewmodel

import com.example.durak.data.AppPreferences
import com.example.durak.data.SavedGameDataSource
import com.example.durak.data.SettingsDataSource
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewModelTest {
    @Test
    fun settingsUpdateDoesNotStartGame() {
        val settings = FakeSettingsDataSource()
        val viewModel = GameViewModel(settings, FakeSavedGameDataSource())

        viewModel.updateAppPreferences(AppPreferences(showLegalMoveHints = false, confirmNewGame = false))

        assertNull(viewModel.gameState)
        assertEquals(Screen.MENU, viewModel.screen)
        assertEquals(false, viewModel.appPreferences.showLegalMoveHints)
    }
}

private class FakeSettingsDataSource : SettingsDataSource {
    private var gameSettings = GameSettings()
    private var appPreferences = AppPreferences()

    override fun loadGameSettings(): GameSettings = gameSettings
    override fun saveGameSettings(settings: GameSettings) {
        gameSettings = settings
    }

    override fun loadAppPreferences(): AppPreferences = appPreferences
    override fun saveAppPreferences(preferences: AppPreferences) {
        appPreferences = preferences
    }
}

private class FakeSavedGameDataSource : SavedGameDataSource {
    override fun save(state: GameState) = Unit
    override fun load(): GameState? = null
    override fun clear() = Unit
}
