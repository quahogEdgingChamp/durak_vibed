package com.example.durak.game

class GameRules {
    fun canBeat(attack: Card, defense: Card, trumpSuit: Suit): Boolean {
        if (attack.suit == defense.suit) {
            return defense.rank.strength > attack.rank.strength
        }
        return defense.suit == trumpSuit && attack.suit != trumpSuit
    }

    fun canAttack(state: GameState, playerId: Int, card: Card): Boolean =
        state.status == GameStatus.IN_PROGRESS &&
            playerId == state.attackerIndex &&
            state.table.isEmpty() &&
            card in state.players[playerId].hand

    fun canDefend(state: GameState, playerId: Int, attackCard: Card, defenseCard: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || playerId != state.defenderIndex) return false
        if (defenseCard !in state.players[playerId].hand) return false
        val tableCard = state.table.firstOrNull { it.attack == attackCard } ?: return false
        return tableCard.defense == null && canBeat(attackCard, defenseCard, state.trumpSuit)
    }

    fun canDefend(state: GameState, playerId: Int, defenseCard: Card): Boolean {
        val attackCard = state.table.firstOrNull { it.defense == null }?.attack ?: return false
        return canDefend(state, playerId, attackCard, defenseCard)
    }

    fun canThrowIn(state: GameState, playerId: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || card !in state.players[playerId].hand) return false
        if (state.table.isEmpty() || state.needsDefense) return false
        if (playerId != state.attackerIndex) return false
        if (state.settings.gameMode !in setOf(GameMode.THROW_IN, GameMode.CASUAL)) return false
        if (card.rank !in tableRanks(state)) return false
        return state.settings.gameMode == GameMode.CASUAL || attackCount(state) < state.boutDefenderCardLimit
    }

    fun canPass(state: GameState, playerId: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || playerId != state.defenderIndex) return false
        if (state.settings.gameMode != GameMode.PASSING) return false
        if (card !in state.players[playerId].hand || state.table.isEmpty()) return false
        if (state.table.any { it.defense != null }) return false
        if (card.rank !in state.table.map { it.attack.rank }.toSet()) return false

        val nextDefender = nextActiveIndex(state, state.defenderIndex) ?: return false
        if (nextDefender == playerId) return false
        val newAttackCount = attackCount(state) + 1
        return state.players[nextDefender].hand.size >= newAttackCount
    }

    fun getLegalAttackCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canAttack(state, playerId, it) }.orEmpty()

    fun getLegalDefenseCards(state: GameState, playerId: Int, attackCard: Card): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canDefend(state, playerId, attackCard, it) }.orEmpty()

    fun getLegalThrowInCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canThrowIn(state, playerId, it) }.orEmpty()

    fun getLegalPassCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canPass(state, playerId, it) }.orEmpty()

    fun getAvailableActions(state: GameState, playerId: Int): Set<GameAction> {
        if (state.status != GameStatus.IN_PROGRESS || state.currentActorIndex != playerId) return emptySet()
        val actions = mutableSetOf<GameAction>()
        if (playerId == state.defenderIndex && state.table.isNotEmpty()) {
            actions += GameAction.TAKE
            if (getLegalPassCards(state, playerId).isNotEmpty()) actions += GameAction.PASS
        }
        if (canEndAttack(state, playerId)) actions += GameAction.DONE
        return actions
    }

    fun legalCards(state: GameState, playerId: Int): Set<Card> {
        val hand = state.players.getOrNull(playerId)?.hand ?: return emptySet()
        val openAttack = state.table.firstOrNull { it.defense == null }?.attack
        return hand.filterTo(mutableSetOf()) { card ->
            canAttack(state, playerId, card) ||
                canThrowIn(state, playerId, card) ||
                canPass(state, playerId, card) ||
                (openAttack != null && canDefend(state, playerId, openAttack, card))
        }
    }

    fun canAnyPass(state: GameState, playerId: Int): Boolean =
        getLegalPassCards(state, playerId).isNotEmpty()

    fun canEndAttack(state: GameState, playerId: Int): Boolean =
        state.status == GameStatus.IN_PROGRESS &&
            playerId == state.attackerIndex &&
            state.table.isNotEmpty() &&
            !state.needsDefense

    fun nextActiveIndex(state: GameState, afterIndex: Int): Int? {
        if (state.players.none { it.hand.isNotEmpty() }) return null
        for (offset in 1..state.players.size) {
            val index = (afterIndex + offset) % state.players.size
            if (state.players[index].hand.isNotEmpty()) return index
        }
        return null
    }

    private fun attackCount(state: GameState): Int = state.table.size

    private fun tableRanks(state: GameState): Set<Rank> =
        state.table.flatMap { listOfNotNull(it.attack.rank, it.defense?.rank) }.toSet()
}
