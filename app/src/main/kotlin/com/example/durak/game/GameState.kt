package com.example.durak.game

enum class GameStatus {
    IN_PROGRESS,
    FINISHED
}

data class TableCard(
    val attack: Card,
    val defense: Card? = null
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
    val boutDefenderCardLimit: Int = players.getOrNull(defenderIndex)?.hand?.size ?: 0,
    val phase: GamePhase = GamePhase.DEALING,
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
