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
    TAKE,
    DISCARD
}

data class TableExitAnimation(
    val cards: List<Card>,
    val direction: TableExitDirection
) {
    companion object {
        fun fromTable(table: List<TableCard>, direction: TableExitDirection): TableExitAnimation =
            TableExitAnimation(
                cards = table.flatMap { listOfNotNull(it.attack, it.defense) },
                direction = direction
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
    val y = when (current.direction) {
        TableExitDirection.TAKE -> 150f * progress
        TableExitDirection.DISCARD -> -92f * progress
    }
    val x = when (current.direction) {
        TableExitDirection.TAKE -> 18f * progress
        TableExitDirection.DISCARD -> 78f * progress
    }

    FlowRow(
        modifier = modifier
            .fillMaxSize()
            .offset(x = x.dp, y = y.dp)
            .graphicsLayer {
                scaleX = 1f - progress * 0.12f
                scaleY = 1f - progress * 0.12f
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
