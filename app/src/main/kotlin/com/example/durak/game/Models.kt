package com.example.durak.game

enum class GameMode(val title: String) {
    CLASSIC("Classic"),
    THROW_IN("Throw-in"),
    PASSING("Passing"),
    CASUAL("Casual")
}

enum class AiDifficulty(val title: String) {
    EASY("Easy"),
    NORMAL("Normal")
}

enum class GameStatus {
    IN_PROGRESS,
    FINISHED
}

data class Player(
    val id: Int,
    val name: String,
    val isHuman: Boolean,
    val hand: List<Card> = emptyList()
)

data class TableCard(
    val attack: Card,
    val defense: Card? = null
)

data class GameSettings(
    val deckMode: DeckMode = DeckMode.CARDS_36,
    val gameMode: GameMode = GameMode.THROW_IN,
    val playerCount: Int = 2,
    val aiDifficulty: AiDifficulty = AiDifficulty.NORMAL
)

data class GameState(
    val settings: GameSettings,
    val players: List<Player>,
    val drawPile: List<Card>,
    val trumpCard: Card,
    val trumpSuit: Suit,
    val table: List<TableCard> = emptyList(),
    val discardPile: List<Card> = emptyList(),
    val attackerIndex: Int,
    val defenderIndex: Int,
    val status: GameStatus = GameStatus.IN_PROGRESS,
    val loserIndex: Int? = null,
    val isDraw: Boolean = false,
    val message: String = ""
) {
    val deckRemaining: Int get() = drawPile.size
    val needsDefense: Boolean get() = table.any { it.defense == null }
    val currentActorIndex: Int get() = if (needsDefense) defenderIndex else attackerIndex
    val activePlayerIndexes: List<Int>
        get() = players.indices.filter { players[it].hand.isNotEmpty() }
}
