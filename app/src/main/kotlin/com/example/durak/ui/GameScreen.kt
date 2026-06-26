package com.example.durak.ui

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.durak.data.CardStyle
import com.example.durak.game.Card
import com.example.durak.game.DropTarget
import com.example.durak.game.GameAction
import com.example.durak.game.GameState
import com.example.durak.ui.components.ActionBar
import com.example.durak.ui.components.CardSize
import com.example.durak.ui.components.CardView
import com.example.durak.ui.components.MiniCardView
import com.example.durak.ui.components.PlayerPanel
import com.example.durak.ui.components.TableView
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen
import kotlin.math.roundToInt

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state = viewModel.gameState ?: return
    var tableBounds by remember { mutableStateOf<Rect?>(null) }
    var dragState by remember { mutableStateOf(DragState()) }
    val dropTargetBounds = remember { mutableStateMapOf<DropTarget, Rect>() }
    var showMenu by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }
    val cardStyle = viewModel.appPreferences.cardStyle
    val legalCards = if (viewModel.appPreferences.showLegalMoveHints) viewModel.getLegalCardsForHuman() else emptySet()

    LaunchedEffect(state.table) {
        dropTargetBounds.clear()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B4A33))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopOpponents(state)
            GameInfoRow(
                state = state,
                prompt = viewModel.getUserPromptText(),
                cardStyle = cardStyle,
                onMenu = { showMenu = true }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { tableBounds = it.boundsInRoot() }
            ) {
                TableView(
                    table = state.table,
                    cardStyle = cardStyle,
                    highlighted = dragState.isDragging,
                    modifier = Modifier.fillMaxSize(),
                    onTargetBoundsChanged = { target, bounds ->
                        dropTargetBounds[target] = bounds
                    }
                )
            }
            ActionBar(actions = viewModel.getAvailableActions(), onAction = { action ->
                when (action) {
                    GameAction.DONE -> viewModel.done()
                    GameAction.TAKE -> viewModel.take()
                    GameAction.PASS -> viewModel.passHint()
                }
            })
            HumanHand(
                hand = state.players.first().hand,
                legalCards = legalCards,
                cardStyle = cardStyle,
                onDragStart = { card, center ->
                    Log.d("DurakDrag", "drag start card=$card center=$center")
                    dragState = DragState(card = card, currentOffset = center, isDragging = true)
                },
                onDragMove = { center ->
                    dragState = dragState.copy(currentOffset = center)
                },
                onDragEnd = { card, center ->
                    val target = resolveDropTarget(center, tableBounds, dropTargetBounds)
                    Log.d("DurakDrag", "drop card=$card center=$center target=$target")
                    dragState = DragState()
                    if (!viewModel.onCardDropped(card, target)) {
                        viewModel.invalidDrop(card)
                    }
                },
                onTap = { card ->
                    if (!viewModel.playHumanCard(card)) viewModel.invalidDrop(card)
                }
            )
        }

        DragOverlay(dragState = dragState, cardStyle = cardStyle)
    }

    if (showMenu) {
        PauseMenu(
            onDismiss = { showMenu = false },
            onRules = {
                showMenu = false
                viewModel.goTo(Screen.RULES)
            },
            onRestart = {
                showMenu = false
                if (viewModel.appPreferences.confirmNewGame) confirmAction = ConfirmAction.RESTART else viewModel.restartGame()
            },
            onMainMenu = {
                showMenu = false
                if (viewModel.appPreferences.confirmNewGame) confirmAction = ConfirmAction.MAIN_MENU else viewModel.goTo(Screen.MENU)
            }
        )
    }

    confirmAction?.let { action ->
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            title = { Text("Leave current game?") },
            text = { Text("The current game is not saved yet. Continue?") },
            confirmButton = {
                Button(onClick = {
                    confirmAction = null
                    when (action) {
                        ConfirmAction.RESTART -> viewModel.restartGame()
                        ConfirmAction.MAIN_MENU -> viewModel.goTo(Screen.MENU)
                    }
                }) { Text("Continue") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmAction = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TopOpponents(state: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        state.players.drop(1).forEachIndexed { offset, player ->
            val index = offset + 1
            PlayerPanel(player = player, role = roleFor(state, index), modifier = Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
private fun GameInfoRow(
    state: GameState,
    prompt: String,
    cardStyle: CardStyle,
    onMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xCC13291F), Color(0xAA0A1D15))),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniCardView(state.trumpCard, style = cardStyle)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(prompt, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                "${state.settings.gameMode.title} • Deck: ${state.deckRemaining} • Trump: ${state.trumpCard}",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                "Attacker P${state.attackerIndex + 1} • Defender P${state.defenderIndex + 1}",
                color = Color.White.copy(alpha = 0.64f),
                style = MaterialTheme.typography.labelSmall
            )
        }
        Button(
            onClick = onMenu,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B2A86), contentColor = Color.White),
            shape = RoundedCornerShape(10.dp)
        ) { Text("Menu") }
    }
}

@Composable
private fun HumanHand(
    hand: List<Card>,
    legalCards: Set<Card>,
    cardStyle: CardStyle,
    onDragStart: (Card, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Card, Offset) -> Unit,
    onTap: (Card) -> Unit
) {
    val scrollState = rememberScrollState()
    val spacing = 46.dp
    val cardSize = CardSize(68.dp, 98.dp)
    val width = if (hand.isEmpty()) 1.dp else cardSize.width + spacing * (hand.size - 1) + 18.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .horizontalScroll(scrollState)
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(Modifier.requiredWidth(width).height(112.dp)) {
            hand.forEachIndexed { index, card ->
                val playable = card in legalCards
                DraggableHandCard(
                    card = card,
                    cardStyle = cardStyle,
                    cardSize = cardSize,
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
                detectDragGestures(
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
            disabled = disabled || dragging
        )
    }
}

@Composable
private fun DragOverlay(dragState: DragState, cardStyle: CardStyle) {
    val card = dragState.card ?: return
    if (!dragState.isDragging) return
    val size = CardSize(68.dp, 98.dp)
    val density = LocalDensity.current
    val widthPx = with(density) { size.width.toPx() }
    val heightPx = with(density) { size.height.toPx() }
    CardView(
        card = card,
        cardSize = size,
        style = cardStyle,
        playable = true,
        modifier = Modifier
            .offset {
                IntOffset(
                    (dragState.currentOffset.x - widthPx / 2f).roundToInt(),
                    (dragState.currentOffset.y - heightPx / 2f).roundToInt()
                )
            }
            .zIndex(500f)
    )
}

@Composable
private fun PauseMenu(
    onDismiss: () -> Unit,
    onRules: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Menu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Resume") }
                OutlinedButton(onClick = onRules, modifier = Modifier.fillMaxWidth()) { Text("Rules") }
                OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Restart Game") }
                OutlinedButton(onClick = onMainMenu, modifier = Modifier.fillMaxWidth()) { Text("Main Menu") }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun EndGameScreen(viewModel: GameViewModel) {
    val state = viewModel.gameState
    val result = when {
        state == null -> "Game ended"
        state.isDraw -> "Draw"
        state.loserIndex == 0 -> "You lost"
        state.loserIndex != null -> "${state.players[state.loserIndex].name} is the durak"
        else -> "Game ended"
    }
    MenuPanel {
        Text(result, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Button(onClick = viewModel::playAgain, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Main Menu") }
    }
}

private fun roleFor(state: GameState, index: Int): String =
    when (index) {
        state.attackerIndex -> "Attacker"
        state.defenderIndex -> "Defender"
        else -> "Waiting"
    }

private enum class ConfirmAction {
    RESTART,
    MAIN_MENU
}

private data class DragState(
    val card: Card? = null,
    val currentOffset: Offset = Offset.Zero,
    val isDragging: Boolean = false
)

private fun resolveDropTarget(
    positionInRoot: Offset,
    tableBounds: Rect?,
    targetBounds: Map<DropTarget, Rect>
): DropTarget {
    val specific = targetBounds.entries
        .firstOrNull { it.value.contains(positionInRoot) }
        ?.key
    if (specific != null) return specific
    return if (tableBounds?.contains(positionInRoot) == true) DropTarget.Table else DropTarget.None
}
