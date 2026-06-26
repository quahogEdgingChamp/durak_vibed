package com.example.durak.game

enum class Suit(val symbol: String, val isRed: Boolean) {
    SPADES("♠", false),
    HEARTS("♥", true),
    DIAMONDS("♦", true),
    CLUBS("♣", false)
}

enum class Rank(val label: String, val strength: Int) {
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 11),
    QUEEN("Q", 12),
    KING("K", 13),
    ACE("A", 14)
}

data class Card(val suit: Suit, val rank: Rank) : Comparable<Card> {
    override fun compareTo(other: Card): Int =
        compareValuesBy(this, other, { it.rank.strength }, { it.suit.ordinal })

    override fun toString(): String = "${rank.label}${suit.symbol}"
}
