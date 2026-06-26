package com.example.durak.game

import kotlin.random.Random

enum class DeckMode(val size: Int, val ranks: List<Rank>) {
    CARDS_24(
        24,
        listOf(Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING, Rank.ACE)
    ),
    CARDS_36(
        36,
        listOf(
            Rank.SIX,
            Rank.SEVEN,
            Rank.EIGHT,
            Rank.NINE,
            Rank.TEN,
            Rank.JACK,
            Rank.QUEEN,
            Rank.KING,
            Rank.ACE
        )
    ),
    CARDS_52(52, Rank.entries)
}

object Deck {
    fun create(mode: DeckMode): List<Card> =
        Suit.entries.flatMap { suit -> mode.ranks.map { rank -> Card(suit, rank) } }

    fun shuffled(mode: DeckMode, random: Random = Random.Default): List<Card> =
        create(mode).shuffled(random)
}
