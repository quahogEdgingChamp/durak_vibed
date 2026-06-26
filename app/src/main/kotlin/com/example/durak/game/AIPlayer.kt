package com.example.durak.game

class AIPlayer(private val rules: GameRules = GameRules()) {
    fun chooseMove(state: GameState, playerIndex: Int): AiMove {
        val hand = state.players[playerIndex].hand
        if (state.needsDefense && playerIndex == state.defenderIndex) {
            val pass = hand
                .filter { rules.canPass(state, playerIndex, it) }
                .minWithOrNull(cardComparator(state.trumpSuit))
            if (pass != null) return AiMove.Play(pass)

            val defense = hand
                .filter { rules.canDefend(state, playerIndex, it) }
                .minWithOrNull(cardComparator(state.trumpSuit))
            return defense?.let { AiMove.Play(it) } ?: AiMove.Take
        }

        if (!state.needsDefense && playerIndex == state.attackerIndex) {
            val attack = hand
                .filter { rules.canAttack(state, playerIndex, it) }
                .minWithOrNull(cardComparator(state.trumpSuit))
            return attack?.let { AiMove.Play(it) } ?: AiMove.Done
        }

        return AiMove.Done
    }

    private fun cardComparator(trumpSuit: Suit): Comparator<Card> =
        compareBy<Card>({ it.suit == trumpSuit }, { it.rank.strength }, { it.suit.ordinal })
}

sealed interface AiMove {
    data class Play(val card: Card) : AiMove
    data object Take : AiMove
    data object Done : AiMove
}
