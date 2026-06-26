package com.example.durak.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.durak.data.CardStyle
import com.example.durak.game.Card

@Composable
fun ScrollableHandView(
    hand: List<Card>,
    legalCards: Set<Card>,
    cardStyle: CardStyle,
    legalHintColor: Color,
    modifier: Modifier = Modifier,
    onDragStart: (Card, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Card, Offset) -> Unit,
    onTap: (Card) -> Unit
) {
    val scrollState = rememberScrollState()
    val cardSize = if (hand.size >= 11) CardSize(64.dp, 92.dp) else CardSize(68.dp, 98.dp)
    val spacing = when {
        hand.size >= 12 -> 52.dp
        hand.size >= 8 -> 55.dp
        else -> 60.dp
    }
    val contentWidth = if (hand.isEmpty()) 1.dp else cardSize.width + spacing * (hand.size - 1) + 42.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(136.dp)
            .background(Color.Black.copy(alpha = 0.20f), RoundedCornerShape(15.dp))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(15.dp))
            .horizontalScroll(scrollState)
            .padding(start = 10.dp, top = 9.dp, bottom = 9.dp, end = 18.dp)
    ) {
        Box(Modifier.requiredWidth(contentWidth).height(114.dp)) {
            hand.forEachIndexed { index, card ->
                val playable = card in legalCards
                DraggableHandCard(
                    card = card,
                    cardStyle = cardStyle,
                    cardSize = cardSize,
                    legalHintColor = legalHintColor,
                    playable = playable,
                    disabled = legalCards.isNotEmpty() && !playable,
                    modifier = Modifier
                        .offset(x = spacing * index, y = if (playable) 0.dp else 10.dp)
                        .zIndex(index.toFloat()),
                    onDragStart = onDragStart,
                    onDragMove = onDragMove,
                    onDragEnd = onDragEnd,
                    onTap = onTap
                )
            }
        }
    }
}

@Composable
private fun DraggableHandCard(
    card: Card,
    cardStyle: CardStyle,
    cardSize: CardSize,
    legalHintColor: Color,
    playable: Boolean,
    disabled: Boolean,
    modifier: Modifier,
    onDragStart: (Card, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Card, Offset) -> Unit,
    onTap: (Card) -> Unit
) {
    var dragging by remember(card) { mutableStateOf(false) }
    var bounds by remember(card) { mutableStateOf<Rect?>(null) }
    var currentCenter by remember(card) { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .zIndex(if (dragging) 100f else 0f)
            .onGloballyPositioned { bounds = it.boundsInRoot() }
            .pointerInput(card) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        currentCenter = bounds?.center ?: Offset.Zero
                        dragging = true
                        onDragStart(card, currentCenter)
                    },
                    onDragCancel = {
                        dragging = false
                    },
                    onDragEnd = {
                        val center = currentCenter
                        dragging = false
                        onDragEnd(card, center)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        currentCenter += amount
                        onDragMove(currentCenter)
                    }
                )
            }
            .clickable { onTap(card) }
            .animateContentSize()
    ) {
        CardView(
            card = card,
            cardSize = cardSize,
            style = cardStyle,
            playable = playable,
            legalHintColor = legalHintColor,
            disabled = disabled || dragging
        )
    }
}
