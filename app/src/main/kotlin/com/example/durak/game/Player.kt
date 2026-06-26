package com.example.durak.game

data class Player(
    val id: Int,
    val name: String,
    val isHuman: Boolean,
    val hand: List<Card> = emptyList()
)
