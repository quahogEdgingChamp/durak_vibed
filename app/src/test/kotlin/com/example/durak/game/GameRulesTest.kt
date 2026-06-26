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
    fun classicAllowsMatchingRankAddsButNoTransfer() {
        val defended = defendedState(GameMode.CLASSIC)
        val defense = defenseState(GameMode.CLASSIC, Card(Suit.SPADES, Rank.NINE))

        assertTrue(rules.canAddMatchingRankCard(defended, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertTrue(rules.canAddMatchingRankCard(defended, 0, Card(Suit.CLUBS, Rank.QUEEN)))
        assertFalse(rules.canAddMatchingRankCard(defended, 0, Card(Suit.HEARTS, Rank.ACE)))
        assertFalse(rules.canTransfer(defense, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(GameAction.PASS in rules.getAvailableActions(defense, 1))
    }

    @Test
    fun classicAttackLimitIsMinOfFiveAndDefenderStartHandSize() {
        val underFive = defendedTable(GameMode.CLASSIC, count = 4, defenderLimit = 6)
        val atFive = defendedTable(GameMode.CLASSIC, count = 5, defenderLimit = 6)
        val atSmallDefenderLimit = defendedTable(GameMode.CLASSIC, count = 3, defenderLimit = 3)

        assertTrue(rules.canAddMatchingRankCard(underFive, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canAddMatchingRankCard(atFive, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canAddMatchingRankCard(atSmallDefenderLimit, 0, Card(Suit.HEARTS, Rank.NINE)))
    }

    @Test
    fun transferAllowsTransferButNoMatchingRankAdds() {
        val defended = defendedState(GameMode.TRANSFER)
        val defense = defenseState(GameMode.TRANSFER, Card(Suit.SPADES, Rank.NINE))

        assertFalse(rules.canAddMatchingRankCard(defended, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertTrue(rules.canTransfer(defense, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canTransfer(defense, 1, Card(Suit.HEARTS, Rank.ACE)))
        assertTrue(GameAction.PASS in rules.getAvailableActions(defense, 1))
    }

    @Test
    fun transferChangesDefenderAndAddsAttackCard() {
        val engine = GameEngine()
        val state = defenseState(GameMode.TRANSFER, Card(Suit.SPADES, Rank.NINE))

        val result = engine.playCard(state, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state

        assertEquals(2, result.table.size)
        assertEquals(Card(Suit.HEARTS, Rank.NINE), result.table.last().attack)
        assertNull(result.table.last().defense)
        assertEquals(2, result.defenderIndex)
        assertEquals(result.players[2].hand.size, result.defenderHandSizeAtBoutStart)
    }

    @Test
    fun transferRespectsReceivingDefenderLimitAndFiveCardCap() {
        val legal = transferState(nextDefenderCards = 3, attackCount = 2)
        val shortNextDefender = transferState(nextDefenderCards = 2, attackCount = 2)
        val atFive = transferState(nextDefenderCards = 6, attackCount = 5)

        assertTrue(rules.canTransfer(legal, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canTransfer(shortNextDefender, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertFalse(rules.canTransfer(atFive, 1, Card(Suit.HEARTS, Rank.NINE)))
    }

    @Test
    fun transferIsForbiddenAfterAnyAttackCardIsDefended() {
        val state = partiallyDefendedTransferState(GameMode.TRANSFER)
        val matchingCard = Card(Suit.HEARTS, Rank.NINE)

        assertTrue(rules.hasAnyDefenseOnTable(state))
        assertFalse(rules.canTransfer(state, 1, matchingCard))
        assertTrue(rules.getLegalTransferCards(state, 1).isEmpty())
        assertFalse(GameAction.PASS in rules.getAvailableActions(state, 1))
    }

    @Test
    fun transferIsForbiddenAfterDefenseEvenWithOtherUnbeatenAttacks() {
        val engine = GameEngine()
        val state = partiallyDefendedTransferState(GameMode.TRANSFER)

        val result = engine.playCard(state, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table)

        assertEquals(state, result.state)
    }

    @Test
    fun transferAutoDiscardsAfterSuccessfulDefense() {
        val engine = GameEngine()
        val attack = Card(Suit.SPADES, Rank.NINE)
        val state = defenseState(GameMode.TRANSFER, attack)

        val result = engine.playCard(state, 1, Card(Suit.SPADES, Rank.QUEEN), DropTarget.DefenseSlot(attack)).state

        assertTrue(result.table.isEmpty())
        assertEquals(2, result.discardPile.size)
    }

    @Test
    fun casualAllowsBothMatchingRankAddAndTransfer() {
        val defended = defendedState(GameMode.CASUAL)
        val defense = defenseState(GameMode.CASUAL, Card(Suit.SPADES, Rank.NINE))

        assertTrue(rules.canAddMatchingRankCard(defended, 0, Card(Suit.HEARTS, Rank.NINE)))
        assertTrue(rules.canAddMatchingRankCard(defended, 0, Card(Suit.CLUBS, Rank.QUEEN)))
        assertFalse(rules.canAddMatchingRankCard(defended, 0, Card(Suit.HEARTS, Rank.ACE)))
        assertTrue(rules.canTransfer(defense, 1, Card(Suit.HEARTS, Rank.NINE)))
    }

    @Test
    fun defenderTakeStartsThrowInBeforeTakeInsteadOfPickingUpImmediately() {
        val engine = GameEngine()
        val state = throwInBeforeTakeState(GameMode.CLASSIC)

        val result = engine.take(state, 1).state

        assertEquals(GamePhase.THROW_IN_BEFORE_TAKE, result.phase)
        assertEquals(1, result.takingDefenderIndex)
        assertEquals(0, result.throwInActorIndex)
        assertEquals(state.table, result.table)
        assertEquals(state.players[1].hand, result.players[1].hand)
    }

    @Test
    fun attackerCanThrowInVisibleRankBeforeDefenderTakes() {
        val engine = GameEngine()
        val pending = engine.take(throwInBeforeTakeState(GameMode.CLASSIC), 1).state
        val throwIn = Card(Suit.HEARTS, Rank.TEN)

        val result = engine.playCard(pending, 0, throwIn, DropTarget.Table).state

        assertEquals(TableCard(throwIn), result.table.last())
        assertFalse(throwIn in result.players[0].hand)
        assertEquals(2, result.throwInActorIndex)
    }

    @Test
    fun nonAttackerCanThrowInBeforeDefenderTakesInThreePlayerGame() {
        val engine = GameEngine()
        val pending = engine.take(throwInBeforeTakeState(GameMode.CLASSIC), 1).state
        val afterAttackerPass = engine.passThrowInBeforeTake(pending, 0).state
        val throwIn = Card(Suit.CLUBS, Rank.NINE)

        val result = engine.playCard(afterAttackerPass, 2, throwIn, DropTarget.Table).state

        assertEquals(TableCard(throwIn), result.table.last())
        assertFalse(throwIn in result.players[2].hand)
    }

    @Test
    fun invalidRankCannotBeThrownInBeforeTake() {
        val engine = GameEngine()
        val pending = engine.take(throwInBeforeTakeState(GameMode.CLASSIC), 1).state

        val result = engine.playCard(pending, 0, Card(Suit.HEARTS, Rank.ACE), DropTarget.Table).state

        assertEquals(pending, result)
    }

    @Test
    fun takingDefenderCannotThrowInToThemselves() {
        val engine = GameEngine()
        val pending = engine.take(throwInBeforeTakeState(GameMode.CLASSIC), 1).state

        assertFalse(rules.canThrowInBeforeTake(pending, 1, Card(Suit.CLUBS, Rank.TEN)))
        assertEquals(pending, engine.playCard(pending, 1, Card(Suit.CLUBS, Rank.TEN), DropTarget.Table).state)
    }

    @Test
    fun afterAllThrowInPlayersPassDefenderTakesCardsAndNextAttackerFollowsDefender() {
        val engine = GameEngine()
        val pending = engine.take(throwInBeforeTakeState(GameMode.CLASSIC), 1).state
        val afterAttackerThrowIn = engine.playCard(pending, 0, Card(Suit.HEARTS, Rank.TEN), DropTarget.Table).state

        val result = engine.passThrowInBeforeTake(afterAttackerThrowIn, 2).state

        assertTrue(result.table.isEmpty())
        assertEquals(null, result.takingDefenderIndex)
        assertTrue(Card(Suit.SPADES, Rank.NINE) in result.players[1].hand)
        assertTrue(Card(Suit.SPADES, Rank.TEN) in result.players[1].hand)
        assertTrue(Card(Suit.HEARTS, Rank.TEN) in result.players[1].hand)
        assertEquals(2, result.attackerIndex)
    }

    @Test
    fun classicAndCasualAllowThrowInBeforeTakeButTransferDoesNot() {
        val engine = GameEngine()

        val classic = engine.take(throwInBeforeTakeState(GameMode.CLASSIC), 1).state
        val casual = engine.take(throwInBeforeTakeState(GameMode.CASUAL), 1).state
        val transfer = engine.take(throwInBeforeTakeState(GameMode.TRANSFER), 1).state

        assertEquals(GamePhase.THROW_IN_BEFORE_TAKE, classic.phase)
        assertEquals(GamePhase.THROW_IN_BEFORE_TAKE, casual.phase)
        assertFalse(rules.canAddMatchingRankCard(transfer, 0, Card(Suit.HEARTS, Rank.TEN)))
        assertEquals(null, transfer.takingDefenderIndex)
        assertTrue(transfer.table.isEmpty())
    }

    @Test
    fun takingDefenderDrawsLastAfterPendingTakeIsFinalized() {
        val engine = GameEngine()
        val state = drawOrderTakeState()
        val pending = engine.take(state, 1).state
        val afterThrowIn = engine.playCard(pending, 0, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state

        val result = engine.passThrowInBeforeTake(afterThrowIn, 2).state

        assertTrue(Card(Suit.CLUBS, Rank.SEVEN) in result.players[0].hand)
        assertTrue(Card(Suit.CLUBS, Rank.EIGHT) in result.players[0].hand)
        assertTrue(Card(Suit.CLUBS, Rank.NINE) in result.players[2].hand)
        assertTrue(Card(Suit.CLUBS, Rank.TEN) in result.players[1].hand)
        assertTrue(Card(Suit.CLUBS, Rank.JACK) in result.players[1].hand)
        assertEquals(2, result.attackerIndex)
    }

    @Test
    fun casualTransferIsForbiddenAfterDefenseStarts() {
        val state = partiallyDefendedTransferState(GameMode.CASUAL)

        assertFalse(rules.canTransfer(state, 1, Card(Suit.HEARTS, Rank.NINE)))
        assertTrue(rules.getLegalTransferCards(state, 1).isEmpty())
        assertFalse(GameAction.PASS in rules.getAvailableActions(state, 1))
    }

    @Test
    fun defenseStillWorksAfterTransferBecomesForbidden() {
        val engine = GameEngine()
        val openAttack = Card(Suit.CLUBS, Rank.NINE)
        val state = partiallyDefendedTransferState(GameMode.CASUAL)

        val result = engine.playCard(state, 1, Card(Suit.CLUBS, Rank.ACE), DropTarget.AttackCard(openAttack)).state

        assertEquals(Card(Suit.CLUBS, Rank.ACE), result.table.last().defense)
    }

    @Test
    fun casualTransferPlusLaterThrowInWorks() {
        val engine = GameEngine()
        val attack = Card(Suit.SPADES, Rank.NINE)
        val state = casualTransferThenAddState(attack)

        val transferred = engine.playCard(state, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state
        val defendedFirst = engine.playCard(transferred, 2, Card(Suit.SPADES, Rank.JACK), DropTarget.AttackCard(attack)).state
        val defendedSecond = engine.playCard(defendedFirst, 2, Card(Suit.HEARTS, Rank.JACK), DropTarget.AttackCard(Card(Suit.HEARTS, Rank.NINE))).state
        val added = engine.playCard(defendedSecond, 0, Card(Suit.CLUBS, Rank.JACK), DropTarget.Table).state

        assertEquals(Card(Suit.CLUBS, Rank.JACK), added.table.last().attack)
    }

    @Test
    fun dragDropModeBehaviorMatchesHouseRules() {
        val engine = GameEngine()
        val attack = Card(Suit.SPADES, Rank.NINE)

        val classicDefense = defenseState(GameMode.CLASSIC, attack)
        assertEquals(classicDefense, engine.playCard(classicDefense, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state)
        val classicAdd = engine.playCard(defendedState(GameMode.CLASSIC), 0, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state
        assertEquals(Card(Suit.HEARTS, Rank.NINE), classicAdd.table.last().attack)

        val transferDefense = defenseState(GameMode.TRANSFER, attack)
        val transferPass = engine.playCard(transferDefense, 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state
        assertEquals(2, transferPass.defenderIndex)
        assertEquals(defendedState(GameMode.TRANSFER), engine.playCard(defendedState(GameMode.TRANSFER), 0, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state)

        val casualPass = engine.playCard(defenseState(GameMode.CASUAL, attack), 1, Card(Suit.HEARTS, Rank.NINE), DropTarget.Table).state
        assertEquals(2, casualPass.defenderIndex)
        val casualDefend = engine.playCard(defenseState(GameMode.CASUAL, attack), 1, Card(Suit.SPADES, Rank.QUEEN), DropTarget.AttackCard(attack)).state
        assertEquals(Card(Suit.SPADES, Rank.QUEEN), casualDefend.table.first().defense)
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
            table = List(count) {
                TableCard(Card(Suit.SPADES, Rank.NINE), Card(Suit.SPADES, Rank.QUEEN))
            },
            defenderHandSizeAtBoutStart = defenderLimit
        )

    private fun partiallyDefendedTransferState(mode: GameMode): GameState =
        emptyTableState(mode).copy(
            table = listOf(
                TableCard(Card(Suit.SPADES, Rank.NINE), Card(Suit.SPADES, Rank.QUEEN)),
                TableCard(Card(Suit.CLUBS, Rank.NINE))
            )
        )

    private fun transferState(nextDefenderCards: Int, attackCount: Int): GameState =
        defenseState(GameMode.TRANSFER, Card(Suit.SPADES, Rank.NINE)).copy(
            table = List(attackCount) { TableCard(Card(Suit.SPADES, Rank.NINE)) },
            players = players().replacePlayerForTest(2) {
                it.copy(hand = List(nextDefenderCards) { index -> Card(Suit.CLUBS, Rank.entries[index + 2]) })
            }
        )

    private fun casualTransferThenAddState(attack: Card): GameState {
        val trump = Card(Suit.DIAMONDS, Rank.SIX)
        return GameState(
            settings = GameSettings(deckMode = DeckMode.CARDS_36, gameMode = GameMode.CASUAL, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.CLUBS, Rank.JACK))),
                Player(1, "AI 1", false, listOf(Card(Suit.HEARTS, Rank.NINE))),
                Player(2, "AI 2", false, listOf(Card(Suit.SPADES, Rank.JACK), Card(Suit.HEARTS, Rank.JACK), Card(Suit.CLUBS, Rank.SIX)))
            ),
            drawPile = listOf(trump),
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(attack)),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 1
        )
    }

    private fun throwInBeforeTakeState(mode: GameMode): GameState {
        val trump = Card(Suit.DIAMONDS, Rank.SIX)
        return GameState(
            settings = GameSettings(deckMode = DeckMode.CARDS_36, gameMode = mode, playerCount = 3),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.HEARTS, Rank.TEN), Card(Suit.HEARTS, Rank.ACE))),
                Player(1, "AI 1", false, listOf(Card(Suit.CLUBS, Rank.TEN))),
                Player(2, "AI 2", false, listOf(Card(Suit.CLUBS, Rank.NINE), Card(Suit.CLUBS, Rank.ACE)))
            ),
            drawPile = List(12) { Card(Suit.SPADES, Rank.SIX) },
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(
                TableCard(Card(Suit.SPADES, Rank.NINE), Card(Suit.SPADES, Rank.TEN)),
                TableCard(Card(Suit.DIAMONDS, Rank.TEN))
            ),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 4
        )
    }

    private fun drawOrderTakeState(): GameState {
        val trump = Card(Suit.DIAMONDS, Rank.SIX)
        return GameState(
            settings = GameSettings(deckMode = DeckMode.CARDS_36, gameMode = GameMode.CLASSIC, playerCount = 3),
            players = listOf(
                Player(
                    0,
                    "You",
                    true,
                    listOf(
                        Card(Suit.HEARTS, Rank.NINE),
                        Card(Suit.SPADES, Rank.SIX),
                        Card(Suit.HEARTS, Rank.SIX),
                        Card(Suit.DIAMONDS, Rank.SEVEN),
                        Card(Suit.DIAMONDS, Rank.EIGHT)
                    )
                ),
                Player(1, "AI 1", false, emptyList()),
                Player(
                    2,
                    "AI 2",
                    false,
                    listOf(
                        Card(Suit.SPADES, Rank.SEVEN),
                        Card(Suit.HEARTS, Rank.SEVEN),
                        Card(Suit.DIAMONDS, Rank.NINE),
                        Card(Suit.SPADES, Rank.EIGHT),
                        Card(Suit.HEARTS, Rank.EIGHT)
                    )
                )
            ),
            drawPile = listOf(
                Card(Suit.CLUBS, Rank.SEVEN),
                Card(Suit.CLUBS, Rank.EIGHT),
                Card(Suit.CLUBS, Rank.NINE),
                Card(Suit.CLUBS, Rank.TEN),
                Card(Suit.CLUBS, Rank.JACK)
            ),
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(TableCard(Card(Suit.SPADES, Rank.NINE))),
            attackerIndex = 0,
            defenderIndex = 1,
            defenderHandSizeAtBoutStart = 3
        )
    }

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
