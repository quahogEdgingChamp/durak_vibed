package com.example.durak.game

class GameRules {
    fun canBeat(attack: Card, defense: Card, trumpSuit: Suit): Boolean {
        if (attack.suit == defense.suit) {
            return defense.rank.strength > attack.rank.strength
        }
        return defense.suit == trumpSuit && attack.suit != trumpSuit
    }

    fun canInitialAttack(state: GameState, playerId: Int, card: Card): Boolean =
        state.status == GameStatus.IN_PROGRESS &&
            !state.isThrowInBeforeTake &&
            playerId == state.attackerIndex &&
            state.table.isEmpty() &&
            card in state.players[playerId].hand

    fun canDefend(state: GameState, playerId: Int, attackCard: Card, defenseCard: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || playerId != state.defenderIndex) return false
        if (state.isThrowInBeforeTake) return false
        if (defenseCard !in state.players[playerId].hand) return false
        val tableCard = state.table.firstOrNull { it.attack == attackCard } ?: return false
        return tableCard.defense == null && canBeat(attackCard, defenseCard, state.trumpSuit)
    }

    fun canDefend(state: GameState, playerId: Int, defenseCard: Card): Boolean =
        firstDefendableAttack(state, playerId, defenseCard) != null

    fun firstDefendableAttack(state: GameState, playerId: Int, defenseCard: Card): Card? =
        state.table.firstOrNull { it.defense == null && canDefend(state, playerId, it.attack, defenseCard) }?.attack

    fun canAddMatchingRankCard(state: GameState, playerId: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || card !in state.players[playerId].hand) return false
        if (state.settings.gameMode !in setOf(GameMode.CLASSIC, GameMode.CASUAL)) return false
        if (state.isThrowInBeforeTake) return false
        if (state.table.isEmpty() || state.needsDefense) return false
        if (playerId != state.attackerIndex) return false
        if (attackCount(state) >= maxAttackCardsThisBout(state)) return false
        return card.rank in tableRanks(state)
    }

    fun canThrowInBeforeTake(state: GameState, playerId: Int, card: Card): Boolean =
        state.status == GameStatus.IN_PROGRESS &&
            state.isThrowInBeforeTake &&
            state.throwInActorIndex == playerId &&
            canPlayerThrowInBeforeTake(state, playerId, card)

    fun canTransfer(state: GameState, playerId: Int, card: Card): Boolean {
        if (state.status != GameStatus.IN_PROGRESS || state.settings.gameMode !in setOf(GameMode.TRANSFER, GameMode.CASUAL)) return false
        if (state.isThrowInBeforeTake) return false
        if (playerId != state.defenderIndex || card !in state.players[playerId].hand) return false
        if (state.table.isEmpty() || !state.needsDefense) return false
        if (hasAnyDefenseOnTable(state)) return false
        if (card.rank !in state.table.map { it.attack.rank }.toSet()) return false

        val nextDefender = nextActiveIndex(state, state.defenderIndex) ?: return false
        if (nextDefender == playerId) return false
        val newAttackCount = attackCount(state) + 1
        val nextDefenderLimit = minOf(5, state.players[nextDefender].hand.size)
        return newAttackCount <= nextDefenderLimit
    }

    fun getLegalInitialAttackCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canInitialAttack(state, playerId, it) }.orEmpty()

    fun getLegalDefenseCards(state: GameState, playerId: Int, attackCard: Card): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canDefend(state, playerId, attackCard, it) }.orEmpty()

    fun getLegalAddCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canAddMatchingRankCard(state, playerId, it) }.orEmpty()

    fun getLegalThrowInBeforeTakeCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canThrowInBeforeTake(state, playerId, it) }.orEmpty()

    fun getLegalTransferCards(state: GameState, playerId: Int): List<Card> =
        state.players.getOrNull(playerId)?.hand?.filter { canTransfer(state, playerId, it) }.orEmpty()

    fun getAvailableActions(state: GameState, playerId: Int): Set<GameAction> {
        if (state.status != GameStatus.IN_PROGRESS || state.currentActorIndex != playerId) return emptySet()
        val actions = mutableSetOf<GameAction>()
        if (state.isThrowInBeforeTake) {
            actions += GameAction.PASS
            return actions
        }
        if (playerId == state.defenderIndex && state.table.isNotEmpty() && state.needsDefense) {
            actions += GameAction.TAKE
            if (getLegalTransferCards(state, playerId).isNotEmpty()) actions += GameAction.PASS
        }
        if (canEndAttack(state, playerId)) actions += GameAction.DONE
        return actions
    }

    fun legalCards(state: GameState, playerId: Int): Set<Card> {
        val hand = state.players.getOrNull(playerId)?.hand ?: return emptySet()
        return hand.filterTo(mutableSetOf()) { card ->
            canInitialAttack(state, playerId, card) ||
                canAddMatchingRankCard(state, playerId, card) ||
                canThrowInBeforeTake(state, playerId, card) ||
                canTransfer(state, playerId, card) ||
                canDefend(state, playerId, card)
        }
    }

    fun canAnyTransfer(state: GameState, playerId: Int): Boolean =
        getLegalTransferCards(state, playerId).isNotEmpty()

    fun hasAnyDefenseOnTable(state: GameState): Boolean =
        state.table.any { it.defense != null }

    fun canEndAttack(state: GameState, playerId: Int): Boolean =
            state.status == GameStatus.IN_PROGRESS &&
            !state.isThrowInBeforeTake &&
            state.settings.gameMode != GameMode.TRANSFER &&
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

    fun canAttack(state: GameState, playerId: Int, card: Card): Boolean =
        canInitialAttack(state, playerId, card)

    fun canThrowIn(state: GameState, playerId: Int, card: Card): Boolean =
        canAddMatchingRankCard(state, playerId, card)

    fun canAddSameRankCard(state: GameState, playerId: Int, card: Card): Boolean =
        canAddMatchingRankCard(state, playerId, card)

    fun canPass(state: GameState, playerId: Int, card: Card): Boolean =
        canTransfer(state, playerId, card)

    fun getLegalAttackCards(state: GameState, playerId: Int): List<Card> =
        getLegalInitialAttackCards(state, playerId)

    fun getLegalThrowInCards(state: GameState, playerId: Int): List<Card> =
        getLegalAddCards(state, playerId)

    fun getLegalSameRankAddCards(state: GameState, playerId: Int): List<Card> =
        getLegalAddCards(state, playerId)

    fun getLegalPassCards(state: GameState, playerId: Int): List<Card> =
        getLegalTransferCards(state, playerId)

    fun canAnyPass(state: GameState, playerId: Int): Boolean =
        canAnyTransfer(state, playerId)

    private fun attackCount(state: GameState): Int = state.table.size

    fun maxAttackCardsThisBout(state: GameState): Int =
        minOf(5, state.defenderHandSizeAtBoutStart)

    private fun tableRanks(state: GameState): Set<Rank> =
        state.table.flatMap { listOfNotNull(it.attack.rank, it.defense?.rank) }.toSet()

    fun hasAnyLegalThrowInBeforeTake(state: GameState, playerId: Int): Boolean =
        state.players.getOrNull(playerId)?.hand?.any { canPlayerThrowInBeforeTake(state, playerId, it) } == true

    private fun canPlayerThrowInBeforeTake(state: GameState, playerId: Int, card: Card): Boolean {
        if (state.settings.gameMode !in setOf(GameMode.CLASSIC, GameMode.CASUAL)) return false
        if (!state.isThrowInBeforeTake || state.table.isEmpty()) return false
        if (playerId == state.takingDefenderIndex) return false
        if (playerId !in state.activePlayerIndexes) return false
        if (card !in state.players[playerId].hand) return false
        if (attackCount(state) >= maxAttackCardsThisBout(state)) return false
        return card.rank in tableRanks(state)
    }
}
