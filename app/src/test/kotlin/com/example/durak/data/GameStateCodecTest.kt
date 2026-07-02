package com.example.durak.data

import com.example.durak.game.AiDifficulty
import com.example.durak.game.Card
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import com.example.durak.game.GameStatus
import com.example.durak.game.Player
import com.example.durak.game.Rank
import com.example.durak.game.Suit
import com.example.durak.game.TableCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameStateCodecTest {
    @Test
    fun roundTripPreservesMidGameState() {
        val state = midGameState()

        val decoded = GameStateCodec.decode(GameStateCodec.encode(state))

        assertEquals(state, decoded)
    }

    @Test
    fun roundTripPreservesPendingTakeFields() {
        val state = midGameState().copy(
            takingDefenderIndex = 1,
            throwInActorIndex = 2,
            playersPassedThrowIn = setOf(0, 2)
        )

        val decoded = GameStateCodec.decode(GameStateCodec.encode(state))

        assertEquals(state, decoded)
    }

    @Test
    fun decodeRejectsGarbage() {
        assertNull(GameStateCodec.decode("not a saved game"))
        assertNull(GameStateCodec.decode(""))
    }

    @Test
    fun decodeRejectsUnknownVersion() {
        val text = GameStateCodec.encode(midGameState()).replace("v=1", "v=999")
        assertNull(GameStateCodec.decode(text))
    }

    private fun midGameState(): GameState {
        val trump = Card(Suit.HEARTS, Rank.SIX)
        return GameState(
            settings = GameSettings(
                deckMode = DeckMode.CARDS_36,
                gameMode = GameMode.CASUAL,
                playerCount = 3,
                aiDifficulty = AiDifficulty.HARD
            ),
            players = listOf(
                Player(0, "You", true, listOf(Card(Suit.SPADES, Rank.TEN), Card(Suit.HEARTS, Rank.ACE))),
                Player(1, "AI 1", false, listOf(Card(Suit.DIAMONDS, Rank.SEVEN))),
                Player(2, "AI 2", false, listOf(Card(Suit.CLUBS, Rank.KING), Card(Suit.CLUBS, Rank.SIX)))
            ),
            drawPile = listOf(Card(Suit.DIAMONDS, Rank.QUEEN), trump),
            trumpCard = trump,
            trumpSuit = trump.suit,
            table = listOf(
                TableCard(Card(Suit.SPADES, Rank.EIGHT), Card(Suit.SPADES, Rank.NINE)),
                TableCard(Card(Suit.DIAMONDS, Rank.EIGHT), null)
            ),
            discardPile = listOf(Card(Suit.CLUBS, Rank.SEVEN), Card(Suit.CLUBS, Rank.EIGHT)),
            attackerIndex = 2,
            defenderIndex = 0,
            defenderHandSizeAtBoutStart = 4,
            status = GameStatus.IN_PROGRESS,
            message = "AI 2 attacked with 8♦ = pressure"
        )
    }
}
