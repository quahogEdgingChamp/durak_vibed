package com.example.durak.game

sealed interface DropTarget {
    data object None : DropTarget
    data object Table : DropTarget
    data class AttackCard(val attackCard: Card) : DropTarget
    data class DefenseSlot(val attackCard: Card) : DropTarget
}
