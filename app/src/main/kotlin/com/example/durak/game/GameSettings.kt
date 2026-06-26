package com.example.durak.game

enum class AiDifficulty(val title: String) {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard")
}

data class GameSettings(
    val deckMode: DeckMode = DeckMode.CARDS_36,
    val gameMode: GameMode = GameMode.THROW_IN,
    val playerCount: Int = 2,
    val aiDifficulty: AiDifficulty = AiDifficulty.NORMAL
)
