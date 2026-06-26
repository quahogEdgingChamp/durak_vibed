package com.example.durak.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {
    private val rules = GameRules()

    @Test
    fun deckCreationMatchesRequestedSizes() {
        assertEquals(24, Deck.create(DeckMode.CARDS_24).size)
        assertEquals(36, Deck.create(DeckMode.CARDS_36).size)
        assertEquals(52, Deck.create(DeckMode.CARDS_52).size)
    }

    @Test
    fun trumpComparisonBeatsNonTrump() {
        val attack = Card(Suit.CLUBS, Rank.ACE)
        val defense = Card(Suit.HEARTS, Rank.SIX)
        assertTrue(rules.canBeat(attack, defense, Suit.HEARTS))
    }

    @Test
    fun higherSameSuitDefendsButLowerDoesNot() {
        val attack = Card(Suit.SPADES, Rank.TEN)
        assertTrue(rules.canBeat(attack, Card(Suit.SPADES, Rank.JACK), Suit.CLUBS))
        assertFalse(rules.canBeat(attack, Card(Suit.SPADES, Rank.NINE), Suit.CLUBS))
    }

    @Test
    fun throwInRequiresRankAlreadyOnTable() {
        val state = baseState(GameMode.THROW_IN).copy(
            table = listOf(TableCard(Card(Suit.CLUBS, Rank.NINE), Card(Suit.CLUBS, Rank.TEN)))
        )
        assertTrue(rules.canAttack(state, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canAttack(state, 0, Card(Suit.HEARTS, Rank.ACE)))
    }

    @Test
    fun passingRequiresMatchingRankAndNextDefenderCapacity() {
        val state = baseState(GameMode.PASSING).copy(
            table = listOf(TableCard(Card(Suit.CLUBS, Rank.NINE)))
        )
        assertTrue(rules.canPass(state, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canPass(state, 1, Card(Suit.HEARTS, Rank.ACE)))
    }

    @Test
    fun winLossDetectionMarksLastPlayerWithCardsAsLoser() {
        val engine = GameEngine()
        val state = baseState(GameMode.CLASSIC).copy(
            drawPile = emptyList(),
            table = listOf(TableCard(Card(Suit.CLUBS, Rank.NINE), Card(Suit.CLUBS, Rank.TEN))),
            players = listOf(
                Player(0, "You", true, emptyList()),
                Player(1, "AI 1", false, emptyList()),
                Player(2, "AI 2", false, listOf(Card(Suit.SPADES, Rank.ACE)))
            ),
            attackerIndex = 0,
            defenderIndex = 1
        )

        val result = engine.endAttack(state, 0).state
        assertEquals(GameStatus.FINISHED, result.status)
        assertEquals(2, result.loserIndex)
    }

    private fun baseState(mode: GameMode): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(deckMode = DeckMode.CARDS_36, gameMode = mode, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.HEARTS, Rank.NINE), Card(Suit.HEARTS, Rank.ACE))),
                Player(1, "AI 1", false, listOf(Card(Suit.HEARTS, Rank.NINE), Card(Suit.HEARTS, Rank.ACE))),
                Player(2, "AI 2", false, listOf(Card(Suit.CLUBS, Rank.SIX), Card(Suit.DIAMONDS, Rank.SEVEN)))
            ),
            drawPile = listOf(trump),
            trumpCard = trump,
            trumpSuit = trump.suit,
            attackerIndex = 0,
            defenderIndex = 1
        )
    }
}
