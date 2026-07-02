package com.example.durak.viewmodel

import com.example.durak.data.AppPreferences
import com.example.durak.data.CardBackStyle
import com.example.durak.data.SavedGameDataSource
import com.example.durak.data.SettingsDataSource
import com.example.durak.game.GameEngine
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun continueIsOfferedOnlyWhenASavedGameExists() {
        val emptySave = FakeSavedGameDataSource()
        assertFalse(GameViewModel(FakeSettingsDataSource(), emptySave).canContinueGame)

        val withSave = FakeSavedGameDataSource()
        withSave.state = GameEngine().newGame(GameSettings())
        assertTrue(GameViewModel(FakeSettingsDataSource(), withSave).canContinueGame)
    }

    @Test
    fun cardBackPreferenceUpdatesAndPersistsInViewModelSettings() {
        val settings = FakeSettingsDataSource()
        val viewModel = GameViewModel(settings, FakeSavedGameDataSource())

        viewModel.updateAppPreferences(viewModel.appPreferences.copy(cardBackStyle = CardBackStyle.DARK))

        assertEquals(CardBackStyle.DARK, viewModel.appPreferences.cardBackStyle)
        assertEquals(CardBackStyle.DARK, settings.loadAppPreferences().cardBackStyle)
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
    var state: GameState? = null

    override fun save(state: GameState) {
        this.state = state
    }

    override fun load(): GameState? = state

    override fun clear() {
        state = null
    }
}
