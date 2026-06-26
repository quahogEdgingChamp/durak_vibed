package com.example.durak.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertEquals(6, state.defenderHandSizeAtBoutStart)
    }

    @Test
    fun classicAllowsDefenseButNoSameRankAddOrTransfer() {
        val attack = Card(Suit.SPADES, Rank.NINE)
        val sameRank = Card(Suit.HEARTS, Rank.NINE)
        val state = defenseState(GameMode.CLASSIC, attack = attack)

        assertFalse(rules.canAddSameRankCard(defendedState(GameMode.CLASSIC), 0, sameRank))
        assertFalse(rules.canTransfer(state, 1, sameRank))
        assertTrue(rules.canDefend(state, 1, attack, Card(Suit.SPADES, Rank.QUEEN)))
        assertTrue(rules.canDefend(state, 1, attack, Card(Suit.DIAMONDS, Rank.SIX)))
        assertFalse(rules.canDefend(state, 1, attack, Card(Suit.SPADES, Rank.EIGHT)))
        assertFalse(rules.canDefend(state, 1, attack, Card(Suit.CLUBS, Rank.ACE)))
    }

    @Test
    fun classicDefenseAutoEndsBoutAndDoesNotExposeDoneOrPass() {
        val engine = GameEngine()
        val attack = Card(Suit.SPADES, Rank.NINE)
        val state = defenseState(GameMode.CLASSIC, attack = attack)

        val actions = rules.getAvailableActions(state, 1)
        assertTrue(GameAction.TAKE in actions)
        assertFalse(GameAction.DONE in actions)
        assertFalse(GameAction.PASS in actions)

        val result = engine.playCard(state, 1, Card(Suit.SPADES, Rank.QUEEN), DropTarget.DefenseSlot(attack)).state

        assertTrue(result.table.isEmpty())
        assertEquals(2, result.discardPile.size)
        assertEquals(1, result.attackerIndex)
    }

    @Test
    fun transferAllowsOnlyMatchingRanksAfterDefense() {
        val state = defendedState(GameMode.TRANSFER)

        assertTrue(rules.canAddSameRankCard(state, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertTrue(rules.canAddSameRankCard(state, 0, Card(Suit.CLUBS, Rank.QUEEN)))
        assertFalse(rules.canAddSameRankCard(state, 0, Card(Suit.HEARTS, Rank.ACE)))
        assertFalse(rules.canTransfer(defenseState(GameMode.TRANSFER, Card(Suit.SPADES, Rank.NINE)), 1, Card(Suit.HEARTS, Rank.NINE)))
    }

    @Test
    fun transferAttackCountCannotExceedDefenderStartHandSize() {
        val underLimit = defendedTable(GameMode.TRANSFER, count = 5, defenderLimit = 6)
        val atLimit = defendedTable(GameMode.TRANSFER, count = 6, defenderLimit = 6)

        assertTrue(rules.canAddSameRankCard(underLimit, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canAddSameRankCard(atLimit, 0, Card(Suit.HEARTS, Rank.NINE)))
    }

    @Test
    fun transferDoneAvailableAfterAllAttacksAreDefended() {
        val state = defendedState(GameMode.TRANSFER)

        assertTrue(GameAction.DONE in rules.getAvailableActions(state, 0))
        assertFalse(GameAction.PASS in rules.getAvailableActions(state, 0))
    }

    @Test
    fun casualAllowsSameRankAddAndTransferOnlyWhenLegal() {
        val defended = defendedState(GameMode.CASUAL)
        val defense = defenseState(GameMode.CASUAL, Card(Suit.SPADES, Rank.NINE))

        assertTrue(rules.canAddSameRankCard(defended, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canAddSameRankCard(defended, 0, Card(Suit.HEARTS, Rank.ACE)))
        assertTrue(rules.canTransfer(defense, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canTransfer(defense, 1, Card(Suit.HEARTS, Rank.ACE)))

        val nextDefenderShort = defense.copy(
            players = defense.players.replacePlayerForTest(2) { it.copy(hand = listOf(Card(Suit.CLUBS, Rank.SIX))) }
        )
        assertFalse(rules.canTransfer(nextDefenderShort, 1, Card(Suit.HEARTS, Rank.NINE)))
    }

    @Test
    fun casualTransferAddsAttackAndChangesDefender() {
        val engine = GameEngine()
        val state = defenseState(GameMode.CASUAL, Card(Suit.SPADES, Rank.NINE))

        val result = engine.playCard(state, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state

        assertEquals(2, result.table.size)
        assertEquals(Card(Suit.HEARTS, Rank.NINE), result.table.last().attack)
        assertNull(result.table.last().defense)
        assertEquals(2, result.defenderIndex)
        assertEquals(result.players[2].hand.size, result.defenderHandSizeAtBoutStart)
    }

    @Test
    fun legalCardsReturnedForAttackDefenseAddAndTransfer() {
        assertTrue(Card(Suit.SPADES, Rank.NINE) in rules.legalCards(emptyTableState(GameMode.TRANSFER), 0))
        assertTrue(Card(Suit.SPADES, Rank.QUEEN) in rules.legalCards(defenseState(GameMode.TRANSFER, Card(Suit.SPADES, Rank.NINE)), 1))
        assertTrue(Card(Suit.HEARTS, Rank.NINE) in rules.legalCards(defendedState(GameMode.TRANSFER), 0))
        assertTrue(Card(Suit.HEARTS, Rank.NINE) in rules.legalCards(defenseState(GameMode.CASUAL, Card(Suit.SPADES, Rank.NINE)), 1))
    }

    @Test
    fun dragDropModeBehaviorMatchesHouseRules() {
        val engine = GameEngine()
        val attack = Card(Suit.SPADES, Rank.NINE)

        val classic = defenseState(GameMode.CLASSIC, attack)
        assertEquals(classic, engine.playCard(classic, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state)

        val transferDefense = defenseState(GameMode.TRANSFER, attack)
        assertEquals(transferDefense, engine.playCard(transferDefense, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state)

        val transferAdd = engine.playCard(defendedState(GameMode.TRANSFER), 0, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state
        assertEquals(Card(Suit.HEARTS, Rank.NINE), transferAdd.table.last().attack)

        val casualPass = engine.playCard(defenseState(GameMode.CASUAL, attack), 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state
        assertEquals(2, casualPass.defenderIndex)

        val casualDefend = engine.playCard(defenseState(GameMode.CASUAL, attack), 1, Card(Suit.SPADES, Rank.QUEEN), DropTarget.AttackCard(attack)).state
        assertEquals(Card(Suit.SPADES, Rank.QUEEN), casualDefend.table.first().defense)
    }

    @Test
    fun winLossDetectionMarksLastPlayerWithCardsAsLoser() {
        val engine = GameEngine()
        val state = defendedState(GameMode.TRANSFER).copy(
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
        val trump = Card(Suit.DIAMONDS, Rank.SIX)
        val players = players()
        return GameState(
            settings = GameSettings(deckMode = DeckMode.CARDS_36, gameMode = mode, playerCount = 3),
            players = players,
            drawPile = listOf(trump),
            trumpCard = trump,
            trumpSuit = trump.suit,
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = players[1].hand.size
        )
    }

    private fun defenseState(mode: GameMode, attack: Card): GameState =
        emptyTableState(mode).copy(table = listOf(TableCard(attack)))

    private fun defendedState(mode: GameMode): GameState =
        emptyTableState(mode).copy(
            table = listOf(TableCard(Card(Suit.SPADES, Rank.NINE), Card(Suit.SPADES, Rank.QUEEN)))
        )

    private fun defendedTable(mode: GameMode, count: Int, defenderLimit: Int): GameState =
        emptyTableState(mode).copy(
            table = List(count) { index ->
                TableCard(Card(Suit.SPADES, if (index == 0) Rank.NINE else Rank.TEN), Card(Suit.SPADES, Rank.ACE))
            },
            defenderHandSizeAtBoutStart = defenderLimit
        )

    private fun players(): List<Player> = listOf(
        Player(
            0,
            "You",
            true,
            listOf(Card(Suit.SPADES, Rank.NINE), Card(Suit.HEARTS, Rank.NINE), Card(Suit.CLUBS, Rank.QUEEN), Card(Suit.HEARTS, Rank.ACE))
        ),
        Player(
            1,
            "AI 1",
            false,
            listOf(
                Card(Suit.HEARTS, Rank.NINE),
                Card(Suit.HEARTS, Rank.ACE),
                Card(Suit.SPADES, Rank.QUEEN),
                Card(Suit.SPADES, Rank.EIGHT),
                Card(Suit.CLUBS, Rank.ACE),
                Card(Suit.DIAMONDS, Rank.SIX)
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
