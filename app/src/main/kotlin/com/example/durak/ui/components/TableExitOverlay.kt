package com.example.durak.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.durak.data.CardStyle
import com.example.durak.game.Card
import com.example.durak.game.TableCard

enum class TableExitDirection {
    HUMAN_TAKE,
    AI_TAKE,
    DISCARD
}

data class TableExitAnimation(
    val cards: List<Card>,
    val direction: TableExitDirection,
    val targetOffsetX: Float = 0f,
    val targetOffsetY: Float = 0f
) {
    companion object {
        fun fromTable(
            table: List<TableCard>,
            direction: TableExitDirection,
            targetOffsetX: Float = 0f,
            targetOffsetY: Float = 0f
        ): TableExitAnimation =
            TableExitAnimation(
                cards = table.flatMap { listOfNotNull(it.attack, it.defense) },
                direction = direction,
                targetOffsetX = targetOffsetX,
                targetOffsetY = targetOffsetY
            )
    }
}

@Composable
fun TableExitOverlay(
    animation: TableExitAnimation?,
    cardStyle: CardStyle,
    durationMillis: Int,
    modifier: Modifier = Modifier
) {
    val current = animation ?: return
    var started by remember(current) { mutableStateOf(false) }
    LaunchedEffect(current) { started = true }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis.coerceAtLeast(1), easing = LinearOutSlowInEasing),
        label = "tableExitProgress"
    )
    val fallbackY = when (current.direction) {
        TableExitDirection.HUMAN_TAKE -> 190f
        TableExitDirection.AI_TAKE -> -185f
        TableExitDirection.DISCARD -> -18f
    }
    val fallbackX = when (current.direction) {
        TableExitDirection.HUMAN_TAKE -> 0f
        TableExitDirection.AI_TAKE -> 0f
        TableExitDirection.DISCARD -> 190f
    }
    val y = (current.targetOffsetY.takeIf { it != 0f } ?: fallbackY) * progress
    val x = (current.targetOffsetX.takeIf { it != 0f } ?: fallbackX) * progress

    FlowRow(
        modifier = modifier
            .fillMaxSize()
            .offset(x = x.dp, y = y.dp)
            .graphicsLayer {
                scaleX = 1f - progress * if (current.direction == TableExitDirection.DISCARD) 0.18f else 0.08f
                scaleY = 1f - progress * if (current.direction == TableExitDirection.DISCARD) 0.18f else 0.08f
            }
            .alpha(1f - progress)
            .zIndex(20f),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically)
    ) {
        current.cards.forEach { card ->
            CardView(card = card, cardSize = CardSize(48.dp, 70.dp), style = cardStyle)
        }
    }
}
