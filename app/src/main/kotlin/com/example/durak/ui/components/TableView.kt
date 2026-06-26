package com.example.durak.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.durak.data.CardStyle
import com.example.durak.game.Card
import com.example.durak.game.DropTarget
import com.example.durak.game.TableCard

@Composable
fun TableView(
    table: List<TableCard>,
    cardStyle: CardStyle,
    highlighted: Boolean,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    onTargetBoundsChanged: (DropTarget, Rect) -> Unit = { _, _ -> }
) {
    val border = if (highlighted) highlightColor else Color.White.copy(alpha = 0.23f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp)
            .background(
                Brush.radialGradient(
                    listOf(Color(0x2237C985), Color(0x33000000))
                ),
                RoundedCornerShape(14.dp)
            )
            .background(
                if (highlighted) highlightColor.copy(alpha = 0.10f) else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .border(if (highlighted) 3.dp else 1.dp, border, RoundedCornerShape(14.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (table.isEmpty()) {
            Text(
                if (highlighted) "Release to play" else "Drag card here",
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                table.forEach { pair ->
                    TablePairView(
                        attackCard = pair.attack,
                        defenseCard = pair.defense,
                        cardStyle = cardStyle,
                        onTargetBoundsChanged = onTargetBoundsChanged
                    )
                }
            }
        }
    }
}

@Composable
private fun TablePairView(
    attackCard: Card,
    defenseCard: Card?,
    cardStyle: CardStyle,
    onTargetBoundsChanged: (DropTarget, Rect) -> Unit
) {
    val cardSize = CardSize(54.dp, 78.dp)
    val defenseOffsetX = 24.dp
    val defenseOffsetY = 16.dp
    var defensePlaced by remember(defenseCard) { mutableStateOf(false) }
    LaunchedEffect(defenseCard) {
        defensePlaced = defenseCard != null
    }
    val defenseProgress by animateFloatAsState(
        targetValue = if (defenseCard != null && defensePlaced) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "defensePlacement"
    )
    Box(
        modifier = Modifier
            .size(width = cardSize.width + defenseOffsetX, height = cardSize.height + defenseOffsetY)
            .onGloballyPositioned {
                if (defenseCard == null) {
                    onTargetBoundsChanged(DropTarget.DefenseSlot(attackCard), it.boundsInRoot())
                }
            }
    ) {
        CardView(
            attackCard,
            cardSize = cardSize,
            style = cardStyle,
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(0f)
                .onGloballyPositioned {
                    onTargetBoundsChanged(DropTarget.AttackCard(attackCard), it.boundsInRoot())
                }
        )
        if (defenseCard != null) {
            CardView(
                defenseCard,
                cardSize = cardSize,
                style = cardStyle,
                modifier = Modifier
                    .offset(x = defenseOffsetX, y = defenseOffsetY)
                    .graphicsLayer {
                        alpha = defenseProgress
                        translationX = (1f - defenseProgress) * -8f
                        translationY = (1f - defenseProgress) * -18f
                        scaleX = 0.92f + defenseProgress * 0.08f
                        scaleY = 0.92f + defenseProgress * 0.08f
                        shadowElevation = 5f + defenseProgress * 6f
                    }
                    .zIndex(1f)
            )
        } else {
            DefensePlaceholder(
                cardSize = cardSize,
                modifier = Modifier
                    .offset(x = defenseOffsetX, y = defenseOffsetY)
                    .zIndex(1f)
                    .onGloballyPositioned {
                        onTargetBoundsChanged(DropTarget.DefenseSlot(attackCard), it.boundsInRoot())
                    }
            )
        }
    }
}

@Composable
private fun DefensePlaceholder(cardSize: CardSize, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(cardSize.width, cardSize.height)
            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("?", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}
