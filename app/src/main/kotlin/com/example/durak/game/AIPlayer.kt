package com.example.durak.game

class AIPlayer(
    private val rules: GameRules = GameRules(),
    private val evaluator: AiMoveEvaluator = AiMoveEvaluator(rules)
) {
    fun chooseMove(state: GameState, playerIndex: Int): AiMove =
        when (state.settings.aiDifficulty) {
            AiDifficulty.EASY -> chooseEasyMove(state, playerIndex)
            AiDifficulty.NORMAL -> chooseNormalMove(state, playerIndex)
            AiDifficulty.HARD -> chooseHardMove(state, playerIndex)
        }

    private fun chooseEasyMove(state: GameState, playerIndex: Int): AiMove {
        if (state.needsDefense && playerIndex == state.defenderIndex) {
            val attack = firstOpenAttack(state) ?: return AiMove.Done
            val defenses = rules.getLegalDefenseCards(state, playerIndex, attack)
            val chosen = defenses.sortedWith(cardComparator(state.trumpSuit)).firstOrNull()
            if (chosen != null && evaluator.cardDefenseCost(chosen, state.trumpSuit) < 34) return AiMove.Play(chosen)
            return chosen?.let { if (state.drawPile.size > 8 && it.suit == state.trumpSuit) AiMove.Take else AiMove.Play(it) } ?: AiMove.Take
        }
        return chooseLowestAttackLikeMove(state, playerIndex)
    }

    private fun chooseNormalMove(state: GameState, playerIndex: Int): AiMove {
        if (state.needsDefense && playerIndex == state.defenderIndex) {
            val pass = normalPass(state, playerIndex)
            if (pass != null) return AiMove.Play(pass)
            val attack = firstOpenAttack(state) ?: return AiMove.Done
            val defenses = rules.getLegalDefenseCards(state, playerIndex, attack)
            val nonTrump = defenses.filter { it.suit != state.trumpSuit }
            val chosen = (nonTrump.ifEmpty { defenses }).minByOrNull { evaluator.cardDefenseCost(it, state.trumpSuit) }
            return chosen?.let { AiMove.Play(it) } ?: AiMove.Take
        }

        val legal = attackLikeCards(state, playerIndex)
        val chosen = legal.maxByOrNull { card ->
            if (state.table.isEmpty()) evaluator.scoreAttackMove(state, playerIndex, card)
            else evaluator.scoreThrowInMove(state, playerIndex, card)
        }
        return chosen?.let { AiMove.Play(it) } ?: AiMove.Done
    }

    private fun chooseHardMove(state: GameState, playerIndex: Int): AiMove {
        if (state.needsDefense && playerIndex == state.defenderIndex) {
            val pass = rules.getLegalTransferCards(state, playerIndex)
                .maxByOrNull { evaluator.scorePassMove(state, playerIndex, it) }
            if (pass != null && evaluator.scorePassMove(state, playerIndex, pass) > 18) return AiMove.Play(pass)
            if (evaluator.shouldTakeInsteadOfDefend(state, playerIndex)) return AiMove.Take
            val attack = firstOpenAttack(state) ?: return AiMove.Done
            val chosen = rules.getLegalDefenseCards(state, playerIndex, attack)
                .maxByOrNull { evaluator.scoreDefenseMove(state, playerIndex, attack, it) }
            return chosen?.let { AiMove.Play(it) } ?: AiMove.Take
        }

        val legal = attackLikeCards(state, playerIndex)
        val chosen = legal.maxByOrNull { card ->
            val score = if (state.table.isEmpty()) {
                evaluator.scoreAttackMove(state, playerIndex, card)
            } else {
                evaluator.scoreThrowInMove(state, playerIndex, card)
            }
            if (state.drawPile.isEmpty()) score + card.rank.strength else score
        }
        return chosen?.let { AiMove.Play(it) } ?: AiMove.Done
    }

    private fun normalPass(state: GameState, playerIndex: Int): Card? {
        val passCards = rules.getLegalTransferCards(state, playerIndex)
        if (passCards.isEmpty()) return null
        val firstDefense = firstOpenAttack(state)?.let { rules.getLegalDefenseCards(state, playerIndex, it) }.orEmpty()
        val cheapestDefense = firstDefense.minByOrNull { evaluator.cardDefenseCost(it, state.trumpSuit) }
        if (cheapestDefense?.suit == state.trumpSuit || cheapestDefense == null) {
            return passCards.minByOrNull { evaluator.cardBaseValue(it, state.trumpSuit) }
        }
        return null
    }

    private fun chooseLowestAttackLikeMove(state: GameState, playerIndex: Int): AiMove {
        val chosen = attackLikeCards(state, playerIndex).minWithOrNull(cardComparator(state.trumpSuit))
        return chosen?.let { AiMove.Play(it) } ?: AiMove.Done
    }

    private fun attackLikeCards(state: GameState, playerIndex: Int): List<Card> =
        if (state.table.isEmpty()) {
            rules.getLegalInitialAttackCards(state, playerIndex)
        } else {
            rules.getLegalSameRankAddCards(state, playerIndex)
        }

    private fun firstOpenAttack(state: GameState): Card? =
        state.table.firstOrNull { it.defense == null }?.attack

    private fun cardComparator(trumpSuit: Suit): Comparator<Card> =
        compareBy<Card>({ it.suit == trumpSuit }, { it.rank.strength }, { it.suit.ordinal })
}

sealed interface AiMove {
    data class Play(val card: Card) : AiMove
    data object Take : AiMove
    data object Done : AiMove
}
