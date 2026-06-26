package com.example.durak.game

class GameRules {
    fun canBeat(attack: Card, defense: Card, trumpSuit: Suit): Boolean {
        if (attack.suit == defense.suit) {
            return defense.rank.strength > attack.rank.strength
        }
        return defense.suit == trumpSuit && attack.suit != trumpSuit
    }

    fun canDefend(state: GameState, playerIndex: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || playerIndex != state.defenderIndex) return false
        val target = state.table.firstOrNull { it.defense == null } ?: return false
        return card in state.players[playerIndex].hand && canBeat(target.attack, card, state.trumpSuit)
    }

    fun canAttack(state: GameState, playerIndex: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || playerIndex != state.attackerIndex) return false
        if (state.needsDefense || card !in state.players[playerIndex].hand) return false
        if (state.table.isEmpty()) return true
        if (!hasAttackCapacity(state)) return false

        return when (state.settings.gameMode) {
            GameMode.CLASSIC -> false
            GameMode.THROW_IN, GameMode.PASSING -> card.rank in tableRanks(state)
            GameMode.CASUAL -> true
        }
    }

    fun canPass(state: GameState, playerIndex: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || playerIndex != state.defenderIndex) return false
        if (state.settings.gameMode !in setOf(GameMode.PASSING, GameMode.CASUAL)) return false
        if (card !in state.players[playerIndex].hand || state.table.isEmpty()) return false
        if (state.table.any { it.defense != null }) return false
        if (card.rank !in tableRanks(state)) return false

        val nextDefender = nextActiveIndex(state, state.defenderIndex) ?: return false
        if (nextDefender == playerIndex) return false
        val nextHandSize = state.players[nextDefender].hand.size
        val newAttackCount = state.table.size + 1
        return state.settings.gameMode == GameMode.CASUAL || nextHandSize >= newAttackCount
    }

    fun canAnyPass(state: GameState, playerIndex: Int): Boolean =
        state.players.getOrNull(playerIndex)?.hand?.any { canPass(state, playerIndex, it) } == true

    fun canEndAttack(state: GameState, playerIndex: Int): Boolean =
        state.status == GameStatus.IN_PROGRESS &&
            playerIndex == state.attackerIndex &&
            state.table.isNotEmpty() &&
            !state.needsDefense

    fun legalCards(state: GameState, playerIndex: Int): Set<Card> {
        val hand = state.players[playerIndex].hand
        return hand.filterTo(mutableSetOf()) { card ->
            canAttack(state, playerIndex, card) ||
                canDefend(state, playerIndex, card) ||
                canPass(state, playerIndex, card)
        }
    }

    fun legalPassCards(state: GameState, playerIndex: Int): Set<Card> =
        state.players.getOrNull(playerIndex)?.hand?.filterTo(mutableSetOf()) { canPass(state, playerIndex, it) }.orEmpty()

    private fun hasAttackCapacity(state: GameState): Boolean {
        val defender = state.players[state.defenderIndex]
        val defensesPlayed = state.table.count { it.defense != null }
        val defenderBattleCapacity = defender.hand.size + defensesPlayed
        return state.table.size < defenderBattleCapacity
    }

    private fun tableRanks(state: GameState): Set<Rank> =
        state.table.flatMap { listOfNotNull(it.attack.rank, it.defense?.rank) }.toSet()

    fun nextActiveIndex(state: GameState, afterIndex: Int): Int? {
        if (state.players.none { it.hand.isNotEmpty() }) return null
        for (offset in 1..state.players.size) {
            val index = (afterIndex + offset) % state.players.size
            if (state.players[index].hand.isNotEmpty()) return index
        }
        return null
    }
}
