package com.example.durak.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIPlayerTest {
    private val engine = GameEngine()

    @Test
    fun easyAiAlwaysReturnsLegalMove() {
        val state = defenseState(AiDifficulty.EASY, GameMode.THROW_IN)
        val move = AIPlayer().chooseMove(state, 1)
        assertLegalMove(state, 1, move)
    }

    @Test
    fun normalAiAvoidsTrumpDefenseWhenSameSuitDefenseAvailable() {
        val state = defenseState(AiDifficulty.NORMAL, GameMode.THROW_IN)
        val move = AIPlayer().chooseMove(state, 1)
        assertEquals(AiMove.Play(Card(Suit.CLUBS, Rank.JACK)), move)
    }

    @Test
    fun normalAiPrefersLowerLegalDefenseCard() {
        val state = defenseState(AiDifficulty.NORMAL, GameMode.THROW_IN).copy(
            players = defenseState(AiDifficulty.NORMAL, GameMode.THROW_IN).players.replace(1) {
                it.copy(hand = listOf(Card(Suit.CLUBS, Rank.JACK), Card(Suit.CLUBS, Rank.ACE), Card(Suit.HEARTS, Rank.SIX)))
            }
        )
        val move = AIPlayer().chooseMove(state, 1)
        assertEquals(AiMove.Play(Card(Suit.CLUBS, Rank.JACK)), move)
    }

    @Test
    fun hardAiAlwaysReturnsLegalMove() {
        val state = defenseState(AiDifficulty.HARD, GameMode.PASSING)
        val move = AIPlayer().chooseMove(state, 1)
        assertLegalMove(state, 1, move)
    }

    @Test
    fun hardAiPreservesTrumpWhenNonTrumpDefenseAvailable() {
        val state = defenseState(AiDifficulty.HARD, GameMode.THROW_IN)
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
    }

    @Test
    fun hardAiPassesOnlyWhenLegal() {
        val legal = passingDefenseState(AiDifficulty.HARD, nextDefenderCards = 3)
        val legalMove = AIPlayer().chooseMove(legal, 1)
        assertEquals(AiMove.Play(Card(Suit.SPADES, Rank.TEN)), legalMove)

        val illegal = passingDefenseState(AiDifficulty.HARD, nextDefenderCards = 1)
        val illegalMove = AIPlayer().chooseMove(illegal, 1)
        assertTrue(illegalMove != AiMove.Play(Card(Suit.SPADES, Rank.TEN)))
        assertLegalMove(illegal, 1, illegalMove)
    }

    @Test
    fun casualAiNeverPasses() {
        val state = passingDefenseState(AiDifficulty.HARD, nextDefenderCards = 3)
            .copy(settings = GameSettings(gameMode = GameMode.CASUAL, aiDifficulty = AiDifficulty.HARD, playerCount = 3))
        val move = AIPlayer().chooseMove(state, 1)
        assertTrue(move != AiMove.Play(Card(Suit.SPADES, Rank.TEN)))
        assertLegalMove(state, 1, move)
    }

    @Test
    fun throwInAiOnlyThrowsMatchingRanksAndRespectsDefenderLimit() {
        val state = defendedThrowInState(AiDifficulty.HARD, limit = 2)
        val move = AIPlayer().chooseMove(state, 0)
        assertTrue(move is AiMove.Play)
        if (move is AiMove.Play) assertEquals(Rank.TEN, move.card.rank)

        val atLimit = state.copy(table = state.table + TableCard(Card(Suit.DIAMONDS, Rank.TEN), Card(Suit.DIAMONDS, Rank.JACK)))
        assertEquals(AiMove.Done, AIPlayer().chooseMove(atLimit, 0))
    }

    private fun assertLegalMove(state: GameState, playerId: Int, move: AiMove) {
        when (move) {
            is AiMove.Play -> assertTrue(engine.playCard(state, playerId, move.card).state != state)
            AiMove.Take -> assertTrue(state.currentActorIndex == playerId && state.table.isNotEmpty())
            AiMove.Done -> assertTrue(GameRules().canEndAttack(state, playerId) || state.currentActorIndex != playerId)
        }
    }

    private fun attackState(difficulty: AiDifficulty, defenderCards: Int): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(gameMode = GameMode.THROW_IN, aiDifficulty = difficulty, playerCount = 2),
            players = listOf(
                Player(0, "AI 0", false, listOf(Card(Suit.CLUBS, Rank.SIX), Card(Suit.SPADES, Rank.ACE), Card(Suit.HEARTS, Rank.SEVEN))),
                Player(1, "You", true, List(defenderCards) { Card(Suit.DIAMONDS, Rank.entries[it + 2]) })
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            attackerIndex = 0,
            defenderIndex = 1,
            boutDefenderCardLimit = defenderCards
        )
    }

    private fun defenseState(difficulty: AiDifficulty, mode: GameMode): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        val attack = Card(Suit.CLUBS, Rank.TEN)
        return GameState(
            settings = GameSettings(gameMode = mode, aiDifficulty = difficulty, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.SPADES, Rank.SIX))),
                Player(1, "AI 1", false, listOf(Card(Suit.CLUBS, Rank.JACK), Card(Suit.CLUBS, Rank.ACE), Card(Suit.HEARTS, Rank.SIX))),
                Player(2, "AI 2", false, listOf(Card(Suit.DIAMONDS, Rank.SIX), Card(Suit.SPADES, Rank.SEVEN), Card(Suit.DIAMONDS, Rank.EIGHT)))
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.NINE) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(attack)),
            attackerIndex = 0,
            defenderIndex = 1,
            boutDefenderCardLimit = 3
        )
    }

    private fun passingDefenseState(difficulty: AiDifficulty, nextDefenderCards: Int): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        val attack = Card(Suit.CLUBS, Rank.TEN)
        return GameState(
            settings = GameSettings(gameMode = GameMode.PASSING, aiDifficulty = difficulty, playerCount = 3),
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
            boutDefenderCardLimit = 2
        )
    }

    private fun defendedThrowInState(difficulty: AiDifficulty, limit: Int): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(gameMode = GameMode.THROW_IN, aiDifficulty = difficulty, playerCount = 2),
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
            boutDefenderCardLimit = limit
        )
    }
}

private fun List<Player>.replace(index: Int, transform: (Player) -> Player): List<Player> =
    toMutableList().also { it[index] = transform(it[index]) }
