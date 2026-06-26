package com.example.durak.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AIPlayerTest {
    private val engine = GameEngine()
    private val rules = GameRules()

    @Test
    fun easyAiAlwaysReturnsLegalMove() {
        val state = defenseState(AiDifficulty.EASY, GameMode.TRANSFER)
        val move = AIPlayer().chooseMove(state, 1)
        assertLegalMove(state, 1, move)
    }

    @Test
    fun normalAiAvoidsTrumpDefenseWhenSameSuitDefenseAvailable() {
        val state = defenseState(AiDifficulty.NORMAL, GameMode.TRANSFER)
        val move = AIPlayer().chooseMove(state, 1)
        assertEquals(AiMove.Play(Card(Suit.CLUBS, Rank.JACK)), move)
    }

    @Test
    fun normalAiPrefersLowerLegalDefenseCard() {
        val base = defenseState(AiDifficulty.NORMAL, GameMode.TRANSFER)
        val state = base.copy(
            players = base.players.replace(1) {
                it.copy(hand = listOf(Card(Suit.CLUBS, Rank.JACK), Card(Suit.CLUBS, Rank.ACE), Card(Suit.HEARTS, Rank.SIX)))
            }
        )
        val move = AIPlayer().chooseMove(state, 1)
        assertEquals(AiMove.Play(Card(Suit.CLUBS, Rank.JACK)), move)
    }

    @Test
    fun hardAiAlwaysReturnsLegalMove() {
        val state = casualTransferDefenseState(AiDifficulty.HARD, nextDefenderCards = 3)
        val move = AIPlayer().chooseMove(state, 1)
        assertLegalMove(state, 1, move)
    }

    @Test
    fun hardAiPreservesTrumpWhenNonTrumpDefenseAvailable() {
        val state = defenseState(AiDifficulty.HARD, GameMode.CLASSIC)
        val move = AIPlayer().chooseMove(state, 1)
        assertEquals(AiMove.Play(Card(Suit.CLUBS, Rank.JACK)), move)
    }

    @Test
    fun hardAiAttacksLowCardsEarly() {
        val state = attackState(AiDifficulty.HARD, defenderCards = 6)
        val move = AIPlayer().chooseMove(state, 0)
        assertEquals(AiMove.Play(Card(Suit.CLUBS, Rank.SIX)), move)
    }

    @Test
    fun hardAiAttacksAggressivelyWhenOpponentHasFewCards() {
        val state = attackState(AiDifficulty.HARD, defenderCards = 1).copy(drawPile = emptyList())
        val move = AIPlayer().chooseMove(state, 0)
        assertTrue(move is AiMove.Play)
        assertLegalMove(state, 0, move)
    }

    @Test
    fun classicAiNeverTransfersButCanAddMatchingRankCards() {
        val defense = transferDefenseWithSameRank(AiDifficulty.HARD).copy(
            settings = GameSettings(gameMode = GameMode.CLASSIC, aiDifficulty = AiDifficulty.HARD, playerCount = 3)
        )
        val defenseMove = AIPlayer().chooseMove(defense, 1)
        assertFalse(defenseMove == AiMove.Play(Card(Suit.SPADES, Rank.TEN)))
        assertTrue(rules.getLegalTransferCards(defense, 1).isEmpty())
        assertLegalMove(defense, 1, defenseMove)

        val addState = defendedAddState(GameMode.CLASSIC, AiDifficulty.HARD, limit = 5)
        val addMove = AIPlayer().chooseMove(addState, 0)
        assertTrue(addMove is AiMove.Play)
        if (addMove is AiMove.Play) assertTrue(rules.canAddMatchingRankCard(addState, 0, addMove.card))
    }

    @Test
    fun transferAiCanTransferButNeverThrowsIn() {
        val addState = defendedAddState(GameMode.TRANSFER, AiDifficulty.HARD, limit = 5)
        val addMove = AIPlayer().chooseMove(addState, 0)
        assertEquals(AiMove.Done, addMove)
        assertTrue(rules.getLegalAddCards(addState, 0).isEmpty())

        val defense = transferDefenseWithSameRank(AiDifficulty.HARD)
        val defenseMove = AIPlayer().chooseMove(defense, 1)
        assertEquals(AiMove.Play(Card(Suit.SPADES, Rank.TEN)), defenseMove)
        assertLegalMove(defense, 1, defenseMove)
    }

    @Test
    fun aiDoesNotTransferAfterDefenseStarts() {
        val state = partiallyDefendedTransferState(AiDifficulty.HARD, GameMode.TRANSFER)

        assertTrue(rules.getLegalTransferCards(state, 1).isEmpty())
        val move = AIPlayer().chooseMove(state, 1)

        assertFalse(move == AiMove.Play(Card(Suit.DIAMONDS, Rank.TEN)))
        assertLegalMove(state, 1, move)
    }

    @Test
    fun casualAiCanTransferAndThrowInLegally() {
        val transferState = casualTransferDefenseState(AiDifficulty.HARD, nextDefenderCards = 3)
        val transferMove = AIPlayer().chooseMove(transferState, 1)
        assertEquals(AiMove.Play(Card(Suit.SPADES, Rank.TEN)), transferMove)
        assertLegalMove(transferState, 1, transferMove)

        val addState = defendedAddState(GameMode.CASUAL, AiDifficulty.HARD, limit = 5)
        val addMove = AIPlayer().chooseMove(addState, 0)
        assertTrue(addMove is AiMove.Play)
        if (addMove is AiMove.Play) assertTrue(rules.canAddMatchingRankCard(addState, 0, addMove.card))
    }

    @Test
    fun casualAiDoesNotTransferIllegally() {
        val illegal = casualTransferDefenseState(AiDifficulty.HARD, nextDefenderCards = 1)
        val illegalMove = AIPlayer().chooseMove(illegal, 1)
        assertFalse(illegalMove == AiMove.Play(Card(Suit.SPADES, Rank.TEN)))
        assertLegalMove(illegal, 1, illegalMove)
    }

    private fun assertLegalMove(state: GameState, playerId: Int, move: AiMove) {
        when (move) {
            is AiMove.Play -> assertTrue(engine.playCard(state, playerId, move.card).state != state)
            AiMove.Take -> assertTrue(state.currentActorIndex == playerId && state.table.isNotEmpty() && state.needsDefense)
            AiMove.Done -> assertTrue(
                rules.canEndAttack(state, playerId) ||
                    state.isThrowInBeforeTake ||
                    state.currentActorIndex != playerId
            )
        }
    }

    private fun attackState(difficulty: AiDifficulty, defenderCards: Int): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(gameMode = GameMode.CLASSIC, aiDifficulty = difficulty, playerCount = 2),
            players = listOf(
                Player(0, "AI 0", false, listOf(Card(Suit.CLUBS, Rank.SIX), Card(Suit.SPADES, Rank.ACE), Card(Suit.HEARTS, Rank.SEVEN))),
                Player(1, "You", true, List(defenderCards) { Card(Suit.DIAMONDS, Rank.entries[it + 2]) })
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = defenderCards
        )
    }

    private fun defenseState(difficulty: AiDifficulty, mode: GameMode): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        val attack = Card(Suit.CLUBS, Rank.TEN)
        return GameState(
            settings = GameSettings(gameMode = mode, aiDifficulty = difficulty, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.SPADES, Rank.SIX))),
                Player(1, "AI 1", false, listOf(Card(Suit.CLUBS, Rank.JACK), Card(Suit.CLUBS, Rank.ACE), Card(Suit.HEARTS, Rank.SIX), Card(Suit.SPADES, Rank.TEN))),
                Player(2, "AI 2", false, listOf(Card(Suit.DIAMONDS, Rank.SIX), Card(Suit.SPADES, Rank.SEVEN), Card(Suit.DIAMONDS, Rank.EIGHT)))
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(attack)),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 4
        )
    }

    private fun transferDefenseWithSameRank(difficulty: AiDifficulty): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        val attack = Card(Suit.CLUBS, Rank.TEN)
        return GameState(
            settings = GameSettings(gameMode = GameMode.TRANSFER, aiDifficulty = difficulty, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.SPADES, Rank.SIX))),
                Player(1, "AI 1", false, listOf(Card(Suit.SPADES, Rank.TEN), Card(Suit.CLUBS, Rank.JACK))),
                Player(2, "AI 2", false, listOf(Card(Suit.DIAMONDS, Rank.SIX), Card(Suit.SPADES, Rank.SEVEN), Card(Suit.DIAMONDS, Rank.EIGHT)))
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(attack)),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 2
        )
    }

    private fun partiallyDefendedTransferState(difficulty: AiDifficulty, mode: GameMode): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(gameMode = mode, aiDifficulty = difficulty, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.DIAMONDS, Rank.SIX))),
                Player(1, "AI 1", false, listOf(Card(Suit.DIAMONDS, Rank.TEN), Card(Suit.SPADES, Rank.JACK))),
                Player(2, "AI 2", false, listOf(Card(Suit.CLUBS, Rank.SIX), Card(Suit.DIAMONDS, Rank.SEVEN), Card(Suit.DIAMONDS, Rank.EIGHT)))
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(
                TableCard(Card(Suit.CLUBS, Rank.TEN), Card(Suit.CLUBS, Rank.QUEEN)),
                TableCard(Card(Suit.SPADES, Rank.TEN))
            ),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 2
        )
    }

    private fun casualTransferDefenseState(difficulty: AiDifficulty, nextDefenderCards: Int): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        val attack = Card(Suit.CLUBS, Rank.TEN)
        return GameState(
            settings = GameSettings(gameMode = GameMode.CASUAL, aiDifficulty = difficulty, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.SPADES, Rank.SIX))),
                Player(1, "AI 1", false, listOf(Card(Suit.SPADES, Rank.TEN), Card(Suit.HEARTS, Rank.ACE))),
                Player(2, "AI 2", false, List(nextDefenderCards) { Card(Suit.DIAMONDS, Rank.entries[it + 2]) })
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(attack)),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 2
        )
    }

    private fun defendedAddState(mode: GameMode, difficulty: AiDifficulty, limit: Int): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(gameMode = mode, aiDifficulty = difficulty, playerCount = 2),
            players = listOf(
                Player(0, "AI 0", false, listOf(Card(Suit.CLUBS, Rank.TEN), Card(Suit.SPADES, Rank.ACE))),
                Player(1, "You", true, List(limit) { Card(Suit.DIAMONDS, Rank.entries[it + 2]) })
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(Card(Suit.DIAMONDS, Rank.TEN), Card(Suit.DIAMONDS, Rank.JACK))),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = limit
        )
    }
}

private fun List<Player>.replace(index: Int, transform: (Player) -> Player): List<Player> =
    toMutableList().also { it[index] = transform(it[index]) }
