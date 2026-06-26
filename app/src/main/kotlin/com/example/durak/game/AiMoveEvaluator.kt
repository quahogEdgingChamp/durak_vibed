package com.example.durak.game

class AiMoveEvaluator(private val rules: GameRules) {
    fun cardBaseValue(card: Card, trumpSuit: Suit): Int =
        card.rank.strength + if (card.suit == trumpSuit) 18 else 0

    fun cardDefenseCost(card: Card, trumpSuit: Suit): Int =
        card.rank.strength + if (card.suit == trumpSuit) 28 else 0

    fun scoreAttackMove(state: GameState, playerId: Int, card: Card): Int {
        val hand = state.players[playerId].hand
        val duplicateBonus = hand.count { it.rank == card.rank } * 5
        val trumpPenalty = if (card.suit == state.trumpSuit) 18 else 0
        val lowCardBonus = 20 - card.rank.strength
        val defender = state.players[state.defenderIndex]
        val pressureBonus = if (defender.hand.size <= 2) 20 else 0
        val lateGameBonus = if (state.drawPile.isEmpty()) card.rank.strength else 0
        return lowCardBonus + duplicateBonus + pressureBonus + lateGameBonus - trumpPenalty
    }

    fun scoreDefenseMove(state: GameState, playerId: Int, attackCard: Card, defenseCard: Card): Int {
        val sameSuitBonus = if (defenseCard.suit == attackCard.suit) 18 else 0
        val trumpPenalty = if (defenseCard.suit == state.trumpSuit && attackCard.suit != state.trumpSuit) 26 else 0
        val lateGameBonus = if (state.drawPile.isEmpty()) 12 else 0
        return 100 + sameSuitBonus + lateGameBonus - cardDefenseCost(defenseCard, state.trumpSuit) - trumpPenalty
    }

    fun scoreThrowInMove(state: GameState, playerId: Int, card: Card): Int {
        val defender = state.players[state.defenderIndex]
        val hand = state.players[playerId].hand
        val duplicateBonus = hand.count { it.rank == card.rank } * 4
        val pressureBonus = if (defender.hand.size <= 2) 24 else 8
        val trumpPenalty = if (card.suit == state.trumpSuit) 24 else 0
        val highPenalty = card.rank.strength / 2
        return pressureBonus + duplicateBonus + (14 - card.rank.strength) - trumpPenalty - highPenalty
    }

    fun scorePassMove(state: GameState, playerId: Int, card: Card): Int {
        val nextDefender = rules.nextActiveIndex(state, state.defenderIndex) ?: return Int.MIN_VALUE
        val nextHand = state.players[nextDefender].hand.size
        val preserveTrumpBonus = if (card.suit != state.trumpSuit && state.players[playerId].hand.any { it.suit == state.trumpSuit }) 10 else 0
        val cardPenalty = cardBaseValue(card, state.trumpSuit)
        val pressureBonus = if (nextHand <= state.table.size + 1) 20 else 4
        return 35 + preserveTrumpBonus + pressureBonus - cardPenalty
    }

    fun shouldTakeInsteadOfDefend(state: GameState, playerId: Int): Boolean {
        val attackCard = state.table.firstOrNull { it.defense == null }?.attack ?: return false
        val defenses = rules.getLegalDefenseCards(state, playerId, attackCard)
        if (defenses.isEmpty()) return true
        if (state.drawPile.isEmpty()) return false
        val cheapest = defenses.minBy { cardDefenseCost(it, state.trumpSuit) }
        val tableSize = state.table.size * 2
        return cheapest.suit == state.trumpSuit && cheapest.rank.strength >= Rank.QUEEN.strength && tableSize <= 2
    }
}
