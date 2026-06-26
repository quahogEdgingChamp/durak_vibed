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
            message = "Player ${attacker + 1} starts with the lowest trump."
        ))
    }

    fun playCard(state: GameState, playerIndex: Int, card: Card): MoveResult {
        if (state.status == GameStatus.FINISHED) return MoveResult(state, "Game is over.")
        return when {
            rules.canPass(state, playerIndex, card) -> pass(state, playerIndex, card)
            rules.canDefend(state, playerIndex, card) -> defend(state, playerIndex, card)
            rules.canAttack(state, playerIndex, card) -> attack(state, playerIndex, card)
            else -> MoveResult(state, "That card is not legal now.")
        }
    }

    fun take(state: GameState, playerIndex: Int): MoveResult {
        if (playerIndex != state.defenderIndex || state.table.isEmpty()) {
            return MoveResult(state, "Only the defender can take table cards.")
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
                    message = "Attack ended."
                )
            )),
            "Attack ended."
        )
    }

    fun legalCards(state: GameState, playerIndex: Int): Set<Card> = rules.legalCards(state, playerIndex)
    fun legalPassCards(state: GameState, playerIndex: Int): Set<Card> = rules.legalPassCards(state, playerIndex)
    fun canEndAttack(state: GameState, playerIndex: Int): Boolean = rules.canEndAttack(state, playerIndex)
    fun canAnyPass(state: GameState, playerIndex: Int): Boolean = rules.canAnyPass(state, playerIndex)

    private fun attack(state: GameState, playerIndex: Int, card: Card): MoveResult {
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        return MoveResult(
            withPhase(finishIfNeeded(
                state.copy(
                    players = players,
                    table = state.table + TableCard(card),
                    message = "${state.players[playerIndex].name} attacked with $card."
                )
            )),
            "Attack played."
        )
    }

    private fun defend(state: GameState, playerIndex: Int, card: Card): MoveResult {
        val firstOpen = state.table.indexOfFirst { it.defense == null }
        val table = state.table.toMutableList()
        table[firstOpen] = table[firstOpen].copy(defense = card)
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        return MoveResult(
            withPhase(finishIfNeeded(
                state.copy(
                    players = players,
                    table = table,
                    message = "${state.players[playerIndex].name} defended with $card."
                )
            )),
            "Defense played."
        )
    }

    private fun pass(state: GameState, playerIndex: Int, card: Card): MoveResult {
        val nextDefender = rules.nextActiveIndex(state, state.defenderIndex) ?: return MoveResult(state, "No player to pass to.")
        val players = state.players.removeCard(playerIndex, card, state.trumpSuit)
        return MoveResult(
            withPhase(finishIfNeeded(
                state.copy(
                    players = players,
                    table = state.table + TableCard(card),
                    defenderIndex = nextDefender,
                    message = "${state.players[playerIndex].name} passed with $card."
                )
            )),
            "Attack passed."
        )
    }

    private fun drawAfterBattle(state: GameState): GameState {
        val drawPile = state.drawPile.toMutableList()
        val players = state.players.toMutableList()
        val drawOrder = buildList {
            var index = state.attackerIndex
            repeat(players.size) {
                add(index)
                index = (index + 1) % players.size
            }
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
            return if (state.defenderIndex == 0) GamePhase.HUMAN_DEFENSE else GamePhase.AI_DEFENSE
        }
        if (state.attackerIndex == 0) {
            return if (state.table.isEmpty()) GamePhase.HUMAN_ATTACK else GamePhase.HUMAN_THROW_IN
        }
        return GamePhase.AI_ATTACK
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
