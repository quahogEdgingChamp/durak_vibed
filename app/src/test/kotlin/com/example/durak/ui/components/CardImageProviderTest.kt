package com.example.durak.ui.components

import com.example.durak.data.CardBackStyle
import com.example.durak.game.Card
import com.example.durak.game.Rank
import com.example.durak.game.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardImageProviderTest {
    @Test
    fun faceCardPathsStillUseCardsFolder() {
        assertEquals("cards/AS.png", CardImageProvider.assetPathFor(Card(Suit.SPADES, Rank.ACE)))
        assertEquals("cards/10D.png", CardImageProvider.assetPathFor(Card(Suit.DIAMONDS, Rank.TEN)))
    }

    @Test
    fun cardBackPathsUseDedicatedFolderWithFallback() {
        CardBackStyle.entries.forEach { style ->
            assertTrue(style.assetPath.startsWith("card_backs/"))
            assertTrue(style.assetPath.endsWith(".png"))
            assertEquals(style.assetPath, CardImageProvider.cardBackAssetPath(style))
        }
        assertEquals("cards/back.png", CardImageProvider.fallbackCardBackAssetPath())
    }
}
