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
    fun newGameStartsWithCorrectDeckSize() {
        val state = GameEngine().newGame(GameSettings(deckMode = DeckMode.CARDS_36, playerCount = 2))
        val cardsInHands = state.players.sumOf { it.hand.size }
        assertEquals(36, cardsInHands + state.drawPile.size)
        assertEquals(6, state.players[0].hand.size)
        assertEquals(6, state.players[1].hand.size)
    }

    @Test
    fun classicModeAllowsDefenseButNoPassOrThrowIn() {
        val attack = Card(Suit.CLUBS, Rank.TEN)
        val state = defenseState(GameMode.CLASSIC, attack = attack)

        assertFalse(rules.canPass(state, 1, Card(Suit.HEARTS, Rank.TEN)))
        assertFalse(rules.canThrowIn(defendedState(GameMode.CLASSIC), 0, Card(Suit.HEARTS, Rank.TEN)))
        assertTrue(rules.canDefend(state, 1, attack, Card(Suit.CLUBS, Rank.QUEEN)))
        assertTrue(rules.canDefend(state, 1, attack, Card(Suit.HEARTS, Rank.TEN)))
        assertFalse(rules.canDefend(state, 1, attack, Card(Suit.CLUBS, Rank.NINE)))
        assertFalse(rules.canDefend(state, 1, attack, Card(Suit.SPADES, Rank.ACE)))
    }

    @Test
    fun throwInModeAllowsOnlyMatchingRanksWithinDefenderBoutLimit() {
        val state = defendedState(GameMode.THROW_IN).copy(boutDefenderCardLimit = 2)

        assertTrue(rules.canThrowIn(state, 0, Card(Suit.HEARTS, Rank.TEN)))
        assertTrue(rules.canThrowIn(state, 0, Card(Suit.SPADES, Rank.QUEEN)))
        assertFalse(rules.canThrowIn(state, 0, Card(Suit.HEARTS, Rank.ACE)))

        val atLimit = state.copy(table = state.table + TableCard(Card(Suit.DIAMONDS, Rank.TEN), Card(Suit.HEARTS, Rank.JACK)))
        assertFalse(rules.canThrowIn(atLimit, 0, Card(Suit.HEARTS, Rank.TEN)))
    }

    @Test
    fun passingModePassesOnlyWithMatchingRankAndEnoughNextDefenderCards() {
        val state = defenseState(GameMode.PASSING, attack = Card(Suit.CLUBS, Rank.TEN))

        assertTrue(rules.canPass(state, 1, Card(Suit.HEARTS, Rank.TEN)))
        assertFalse(rules.canPass(state, 1, Card(Suit.HEARTS, Rank.ACE)))
        assertFalse(rules.canThrowIn(defendedState(GameMode.PASSING), 0, Card(Suit.HEARTS, Rank.TEN)))

        val nextDefenderShort = state.copy(
            players = state.players.replacePlayerForTest(2) { it.copy(hand = listOf(Card(Suit.CLUBS, Rank.SIX))) }
        )
        assertFalse(rules.canPass(nextDefenderShort, 1, Card(Suit.HEARTS, Rank.TEN)))
    }

    @Test
    fun passingCardIsAddedAsAttackAndChangesDefender() {
        val engine = GameEngine()
        val state = defenseState(GameMode.PASSING, attack = Card(Suit.CLUBS, Rank.TEN))

        val result = engine.playCard(state, 1, Card(Suit.HEARTS, Rank.TEN), DropTarget.Table).state

        assertEquals(2, result.table.size)
        assertEquals(Card(Suit.HEARTS, Rank.TEN), result.table.last().attack)
        assertEquals(null, result.table.last().defense)
        assertEquals(2, result.defenderIndex)
    }

    @Test
    fun casualUsesThrowInRulesAndDoesNotAllowPassing() {
        val defense = defenseState(GameMode.CASUAL, attack = Card(Suit.CLUBS, Rank.TEN))
        val defended = defendedState(GameMode.CASUAL)

        assertFalse(rules.canPass(defense, 1, Card(Suit.HEARTS, Rank.TEN)))
        assertTrue(rules.canThrowIn(defended, 0, Card(Suit.HEARTS, Rank.TEN)))
        assertTrue(GameAction.TAKE in rules.getAvailableActions(defense, 1))
    }

    @Test
    fun legalCardsReturnedForAttackAndDefense() {
        val attackState = emptyTableState(GameMode.THROW_IN)
        assertTrue(Card(Suit.CLUBS, Rank.TEN) in rules.legalCards(attackState, 0))

        val defenseState = defenseState(GameMode.THROW_IN, attack = Card(Suit.CLUBS, Rank.TEN))
        assertTrue(Card(Suit.CLUBS, Rank.QUEEN) in rules.legalCards(defenseState, 1))
    }

    @Test
    fun dragDropLegalDefenseOnAttackOrSlotSucceeds() {
        val engine = GameEngine()
        val attack = Card(Suit.CLUBS, Rank.TEN)
        val state = defenseState(GameMode.THROW_IN, attack = attack)

        val onAttack = engine.playCard(state, 1, Card(Suit.CLUBS, Rank.QUEEN), DropTarget.AttackCard(attack)).state
        assertEquals(Card(Suit.CLUBS, Rank.QUEEN), onAttack.table.first().defense)

        val onSlot = engine.playCard(state, 1, Card(Suit.CLUBS, Rank.QUEEN), DropTarget.DefenseSlot(attack)).state
        assertEquals(Card(Suit.CLUBS, Rank.QUEEN), onSlot.table.first().defense)
    }

    @Test
    fun dragDropOutsideOrUnrelatedCardFails() {
        val engine = GameEngine()
        val attack = Card(Suit.CLUBS, Rank.TEN)
        val state = defenseState(GameMode.THROW_IN, attack = attack)

        assertEquals(state, engine.playCard(state, 1, Card(Suit.CLUBS, Rank.QUEEN), DropTarget.None).state)
        assertEquals(state, engine.playCard(state, 1, Card(Suit.SPADES, Rank.ACE), DropTarget.DefenseSlot(attack)).state)
    }

    @Test
    fun dragDropPassCardOnGeneralTablePasses() {
        val engine = GameEngine()
        val state = defenseState(GameMode.PASSING, attack = Card(Suit.CLUBS, Rank.TEN))

        val result = engine.playCard(state, 1, Card(Suit.HEARTS, Rank.TEN), DropTarget.Table).state

        assertEquals(2, result.defenderIndex)
        assertEquals(Card(Suit.HEARTS, Rank.TEN), result.table.last().attack)
    }

    @Test
    fun winLossDetectionMarksLastPlayerWithCardsAsLoser() {
        val engine = GameEngine()
        val state = defendedState(GameMode.CLASSIC).copy(
            drawPile = emptyList(),
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

    private fun emptyTableState(mode: GameMode): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(deckMode = DeckMode.CARDS_36, gameMode = mode, playerCount = 3),
            players = players(),
            drawPile = listOf(trump),
            trumpCard = trump,
            trumpSuit = trump.suit,
            attackerIndex = 0,
            defenderIndex = 1,
            boutDefenderCardLimit = 4
        )
    }

    private fun defenseState(mode: GameMode, attack: Card): GameState =
        emptyTableState(mode).copy(table = listOf(TableCard(attack)))

    private fun defendedState(mode: GameMode): GameState =
        emptyTableState(mode).copy(
            table = listOf(TableCard(Card(Suit.CLUBS, Rank.TEN), Card(Suit.CLUBS, Rank.QUEEN)))
        )

    private fun players(): List<Player> = listOf(
        Player(
            0,
            "You",
            true,
            listOf(Card(Suit.CLUBS, Rank.TEN), Card(Suit.HEARTS, Rank.TEN), Card(Suit.SPADES, Rank.QUEEN), Card(Suit.HEARTS, Rank.ACE))
        ),
        Player(
            1,
            "AI 1",
            false,
            listOf(
                Card(Suit.HEARTS, Rank.TEN),
                Card(Suit.HEARTS, Rank.ACE),
                Card(Suit.CLUBS, Rank.QUEEN),
                Card(Suit.CLUBS, Rank.NINE),
                Card(Suit.SPADES, Rank.ACE)
            )
        ),
        Player(
            2,
            "AI 2",
            false,
            listOf(Card(Suit.CLUBS, Rank.SIX), Card(Suit.DIAMONDS, Rank.SEVEN), Card(Suit.SPADES, Rank.EIGHT))
        )
    )
}

private fun List<Player>.replacePlayerForTest(index: Int, transform: (Player) -> Player): List<Player> =
    toMutableList().also { it[index] = transform(it[index]) }
