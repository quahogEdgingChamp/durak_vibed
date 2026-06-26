package com.example.durak.ui.components

import com.example.durak.game.Card
import com.example.durak.game.Rank
import com.example.durak.game.Suit

object CardImageProvider {
    fun assetPathFor(card: Card): String =
        "cards/${rankCode(card.rank)}${suitCode(card.suit)}.png"

    fun cardBackAssetPath(): String = "cards/back.png"

    private fun rankCode(rank: Rank): String =
        when (rank) {
            Rank.ACE -> "A"
            Rank.KING -> "K"
            Rank.QUEEN -> "Q"
            Rank.JACK -> "J"
            else -> rank.label
        }

    private fun suitCode(suit: Suit): String =
        when (suit) {
            Suit.SPADES -> "S"
            Suit.HEARTS -> "H"
            Suit.DIAMONDS -> "D"
            Suit.CLUBS -> "C"
        }
}
