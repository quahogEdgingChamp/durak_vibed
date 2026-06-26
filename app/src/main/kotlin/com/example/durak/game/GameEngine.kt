package com.example.durak.game

import kotlin.random.Random

class GameEngine(private val rules: GameRules = GameRules()) {
    fun newGame(settings: GameSettings, random: Random = Random.Default): GameState {
        val shuffled = Deck.shuffled(settings.deckMode, random).toMutableList()
        val players = (0 until settings.playerCount).map { index ->
            Player(
                id = index,
                name = if (index == 0) "You" else "AI $index",
                isHuman = index == 0
            )
        }.toMutableList()

        repeat(6) {
            players.indices.forEach { playerIndex ->
                if (shuffled.isNotEmpty()) {
                    players[playerIndex] = players[playerIndex].copy(
                        hand = players[playerIndex].hand + shuffled.removeAt(0)
                    )
                }
            }
        }

        val trumpCard = shuffled.lastOrNull() ?: players.flatMap { it.hand }.last()
        val attacker = players
            .mapIndexedNotNull { index, player ->
                player.hand.filter { it.suit == trumpCard.suit }.minOrNull()?.let { index to it }
            }
            .minByOrNull { it.second.rank.strength }
            ?.first ?: 0
        val defender = nextActiveIndex(players, attacker) ?: attacker

        return withPhase(GameState(
            settings = settings,
            players = players,
            drawPile = shuffled,
            trumpCard = trumpCard,
            trumpSuit = trumpCard.suit,
            attackerIndex = attacker,
            defenderIndex = defender,
            defenderHandSizeAtBoutStart = players[defender].hand.size,
            message = "Player ${attacker + 1} starts with the lowest trump."
        ))
    }

    fun playCard(state: GameState, playerIndex: Int, card: Card): MoveResult {
        return playCard(state, playerIndex, card, DropTarget.Table)
    }

    fun playCard(state: GameState, playerIndex: Int, card: Card, target: DropTarget): MoveResult {
        if (state.status == GameStatus.FINISHED) return MoveResult(state, "Game is over.")
        if (target == DropTarget.None) return MoveResult(state, "Drop the card on the table.")
        return when {
            target is DropTarget.AttackCard && rules.canDefend(state, playerIndex, target.attackCard, card) ->
                defend(state, playerIndex, target.attackCard, card)
            target is DropTarget.DefenseSlot && rules.canDefend(state, playerIndex, target.attackCard, card) ->
                defend(state, playerIndex, target.attackCard, card)
            target is DropTarget.Table && rules.canTransfer(state, playerIndex, card) -> transfer(state, playerIndex, card)
            target is DropTarget.Table && rules.canAddSameRankCard(state, playerIndex, card) -> addSameRankCard(state, playerIndex, card)
            target is DropTarget.Table && rules.canInitialAttack(state, playerIndex, card) -> attack(state, playerIndex, card)
            rules.canInitialAttack(state, playerIndex, card) -> attack(state, playerIndex, card)
            rules.canAddSameRankCard(state, playerIndex, card) -> addSameRankCard(state, playerIndex, card)
            rules.canDefend(state, playerIndex, card) -> defend(state, playerIndex, state.table.first { it.defense == null }.attack, card)
            else -> MoveResult(state, "That card is not legal now.")
        }
    }

    fun take(state: GameState, playerIndex: Int): MoveResult {
        if (playerIndex != state.defenderIndex || state.table.isEmpty() || !state.needsDefense) {
            return MoveResult(state, "Only the defender can take during defense.")
        }
        val tableCards = state.table.flatMap { listOfNotNull(it.attack, it.defense) }
        val players = state.players.replacePlayer(playerIndex) { it.copy(hand = sortHand(it.hand + tableCards, state.trumpSuit)) }
        val replenished = drawAfterBattle(state.copy(players = players, table = emptyList()))
        val nextAttacker = rules.nextActiveIndex(replenished, playerIndex) ?: playerIndex
        val nextDefender = rules.nextActiveIndex(replenished, nextAttacker) ?: nextAttacker
        return MoveResult(
            withPhase(finishIfNeeded(
                replenished.copy(
                    attackerIndex = nextAttacker,
                    defenderIndex = nextDefender,
                    defenderHandSizeAtBoutStart = replenished.players.getOrNull(nextDefender)?.hand?.size ?: 0,
                    message = "Defender took ${tableCards.size} cards."
                )
            )),
            "Defender picked up."
        )
    }

    fun endAttack(state: GameState, playerIndex: Int): MoveResult {
        if (!rules.canEndAttack(state, playerIndex)) {
            return MoveResult(state, "You can end only after all attacks are defended.")
        }
        return finishSuccessfulDefense(state, "Attack ended.")
    }

    private fun finishSuccessfulDefense(state: GameState, message: String): MoveResult {
        val discarded = state.table.flatMap { listOfNotNull(it.attack, it.defense) }
        val replenished = drawAfterBattle(
            state.copy(
                table = emptyList(),
                discardPile = state.discardPile + discarded
            )
        )
        val nextAttacker = if (replenished.players[state.defenderIndex].hand.isNotEmpty()) {
            state.defenderIndex
        } else {
            rules.nextActiveIndex(replenished, state.defenderIndex) ?: state.defenderIndex
        }
        val nextDefender = rules.nextActiveIndex(replenished, nextAttacker) ?: nextAttacker
        return MoveResult(
            withPhase(finishIfNeeded(
                replenished.copy(
                    attackerIndex = nextAttacker,
                    defenderIndex = nextDefender,
                    defenderHandSizeAtBoutStart = replenished.players.getOrNull(nextDefender)?.hand?.size ?: 0,
                    message = message
                )
            )),
            message
        )
    }

    fun legalCards(state: GameState, playerIndex: Int): Set<Card> = rules.legalCards(state, playerIndex)
    fun legalPassCards(state: GameState, playerIndex: Int): Set<Card> = rules.getLegalTransferCards(state, playerIndex).toSet()
    fun legalDefenseCards(state: GameState, playerIndex: Int, attackCard: Card): Set<Card> = rules.getLegalDefenseCards(state, playerIndex, attackCard).toSet()
    fun legalThrowInCards(state: GameState, playerIndex: Int): Set<Card> = rules.getLegalSameRankAddCards(state, playerIndex).toSet()
    fun availableActions(state: GameState, playerIndex: Int): Set<GameAction> = rules.getAvailableActions(state, playerIndex)
    fun canEndAttack(state: GameState, playerIndex: Int): Boolean = rules.canEndAttack(state, playerIndex)
    fun canAnyPass(state: GameState, playerIndex: Int): Boolean = rules.canAnyTransfer(state, playerIndex)

    private fun attack(state: GameState, playerIndex: Int, card: Card): MoveResult {
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        return MoveResult(
            withPhase(finishIfNeeded(
                state.copy(
                    players = players,
                    table = state.table + TableCard(card),
                    defenderHandSizeAtBoutStart = state.players[state.defenderIndex].hand.size,
                    message = "${state.players[playerIndex].name} attacked with $card."
                )
            )),
            "Attack played."
        )
    }

    private fun addSameRankCard(state: GameState, playerIndex: Int, card: Card): MoveResult {
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        return MoveResult(
            withPhase(finishIfNeeded(
                state.copy(
                    players = players,
                    table = state.table + TableCard(card),
                    message = "${state.players[playerIndex].name} added $card."
                )
            )),
            "Matching-rank card added."
        )
    }

    private fun defend(state: GameState, playerIndex: Int, attackCard: Card, card: Card): MoveResult {
        val firstOpen = state.table.indexOfFirst { it.attack == attackCard && it.defense == null }
        if (firstOpen < 0) return MoveResult(state, "Choose an undefended attack card.")
        val table = state.table.toMutableList()
        table[firstOpen] = table[firstOpen].copy(defense = card)
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        val defended = state.copy(
            players = players,
            table = table,
            message = "${state.players[playerIndex].name} defended with $card."
        )
        if (state.settings.gameMode == GameMode.CLASSIC && defended.table.none { it.defense == null }) {
            return finishSuccessfulDefense(defended, "Defense succeeded.")
        }
        return MoveResult(withPhase(finishIfNeeded(defended)), "Defense played.")
    }

    private fun transfer(state: GameState, playerIndex: Int, card: Card): MoveResult {
        val nextDefender = rules.nextActiveIndex(state, state.defenderIndex) ?: return MoveResult(state, "No player to transfer to.")
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        return MoveResult(
            withPhase(finishIfNeeded(
                state.copy(
                    players = players,
                    table = state.table + TableCard(card),
                    defenderIndex = nextDefender,
                    defenderHandSizeAtBoutStart = state.players[nextDefender].hand.size,
                    message = "${state.players[playerIndex].name} transferred with $card."
                )
            )),
            "Attack transferred."
        )
    }

    private fun drawAfterBattle(state: GameState): GameState {
        val drawPile = state.drawPile.toMutableList()
        val players = state.players.toMutableList()
        val drawOrder = buildList {
            var index = state.attackerIndex
            repeat(players.size) {
                if (index != state.defenderIndex) add(index)
                index = (index + 1) % players.size
            }
            if (state.defenderIndex !in this) add(state.defenderIndex)
        }

        for (index in drawOrder) {
            while (players[index].hand.size < 6 && drawPile.isNotEmpty()) {
                players[index] = players[index].copy(
                    hand = sortHand(players[index].hand + drawPile.removeAt(0), state.trumpSuit)
                )
            }
        }
        return state.copy(players = players, drawPile = drawPile)
    }

    private fun finishIfNeeded(state: GameState): GameState {
        if (state.drawPile.isNotEmpty()) return state
        if (state.table.isNotEmpty()) return state

        val active = state.players.indices.filter { state.players[it].hand.isNotEmpty() }
        return when (active.size) {
            0 -> state.copy(status = GameStatus.FINISHED, isDraw = true, loserIndex = null, message = "Draw.")
            1 -> state.copy(status = GameStatus.FINISHED, isDraw = false, loserIndex = active.first(), message = "${state.players[active.first()].name} is the durak.")
            else -> state
        }
    }

    fun withPhase(state: GameState): GameState =
        state.copy(phase = derivePhase(state))

    private fun derivePhase(state: GameState): GamePhase {
        if (state.status == GameStatus.FINISHED) return GamePhase.GAME_OVER
        if (state.needsDefense) {
            val canPass = rules.canAnyPass(state, state.defenderIndex)
            return if (state.defenderIndex == 0) {
                if (canPass) GamePhase.HUMAN_PASS_OR_DEFEND else GamePhase.HUMAN_DEFENSE
            } else {
                if (canPass) GamePhase.AI_PASS_OR_DEFEND else GamePhase.AI_DEFENSE
            }
        }
        if (state.attackerIndex == 0) {
            return if (state.table.isEmpty()) GamePhase.HUMAN_ATTACK else GamePhase.HUMAN_THROW_IN
        }
        return if (state.table.isEmpty()) GamePhase.AI_ATTACK else GamePhase.AI_THROW_IN
    }

    private fun nextActiveIndex(players: List<Player>, afterIndex: Int): Int? {
        for (offset in 1..players.size) {
            val index = (afterIndex + offset) % players.size
            if (players[index].hand.isNotEmpty()) return index
        }
        return null
    }
}

data class MoveResult(val state: GameState, val message: String)

private fun List<Player>.replacePlayer(index: Int, transform: (Player) -> Player): List<Player> =
    toMutableList().also { it[index] = transform(it[index]) }

private fun List<Player>.removeCard(playerIndex: Int, card: Card, trumpSuit: Suit): List<Player> =
    replacePlayer(playerIndex) { player ->
        player.copy(hand = sortHand(player.hand - card, trumpSuit))
    }

fun sortHand(cards: List<Card>, trumpSuit: Suit): List<Card> =
    cards.sortedWith(compareBy<Card>({ it.suit == trumpSuit }, { it.suit.ordinal }, { it.rank.strength }))
