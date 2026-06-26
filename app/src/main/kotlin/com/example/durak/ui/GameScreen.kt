package com.example.durak.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.durak.data.AnimationSpeed
import com.example.durak.data.CardBackStyle
import com.example.durak.data.CardStyle
import com.example.durak.game.Card
import com.example.durak.game.DropTarget
import com.example.durak.game.GameAction
import com.example.durak.game.GameState
import com.example.durak.ui.components.ActionBar
import com.example.durak.ui.components.AnimationDurations
import com.example.durak.ui.components.CardSize
import com.example.durak.ui.components.CardView
import com.example.durak.ui.components.GameInfoPanel
import com.example.durak.ui.components.PlayerPanel
import com.example.durak.ui.components.ScrollableHandView
import com.example.durak.ui.components.TableExitAnimation
import com.example.durak.ui.components.TableExitDirection
import com.example.durak.ui.components.TableExitOverlay
import com.example.durak.ui.components.TableView
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state = viewModel.gameState ?: return
    var tableBounds by remember { mutableStateOf<Rect?>(null) }
    var dragState by remember { mutableStateOf(DragState()) }
    val dropTargetBounds = remember { mutableStateMapOf<DropTarget, Rect>() }
    val opponentBounds = remember { mutableStateMapOf<Int, Rect>() }
    var discardBounds by remember { mutableStateOf<Rect?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<ConfirmAction?>(null) }
    var previousState by remember { mutableStateOf(state) }
    var tableExitAnimation by remember { mutableStateOf<TableExitAnimation?>(null) }
    var tableExitDuration by remember { mutableStateOf(0) }
    var handBounds by remember { mutableStateOf<Rect?>(null) }
    val cardStyle = viewModel.appPreferences.cardStyle
    val cardBackStyle = viewModel.appPreferences.cardBackStyle
    val legalHintColor = viewModel.appPreferences.legalHintColor.toComposeColor()
    val legalCards = if (viewModel.appPreferences.showLegalMoveHints) viewModel.getLegalCardsForHuman() else emptySet()
    val density = LocalDensity.current

    LaunchedEffect(state.table) {
        dropTargetBounds.clear()
    }

    LaunchedEffect(state, viewModel.appPreferences.animationSpeed, tableBounds, handBounds, discardBounds) {
        val oldState = previousState
        previousState = state
        val oldTable = oldState.table
        if (oldTable.isNotEmpty() && state.table.isEmpty()) {
            val took = state.message.contains("took", ignoreCase = true) ||
                state.message.contains("takes", ignoreCase = true)
            val takingPlayer = oldState.takingDefenderIndex ?: oldState.defenderIndex
            val direction = when {
                took && takingPlayer == 0 -> TableExitDirection.HUMAN_TAKE
                took -> TableExitDirection.AI_TAKE
                else -> TableExitDirection.DISCARD
            }
            val duration = tableExitDurationMillis(direction, viewModel.appPreferences.animationSpeed)
            if (duration <= 0) return@LaunchedEffect
            val targetBounds = when (direction) {
                TableExitDirection.HUMAN_TAKE -> handBounds
                TableExitDirection.AI_TAKE -> opponentBounds[takingPlayer]
                TableExitDirection.DISCARD -> discardBounds
            }
            val source = tableBounds?.center
            val target = targetBounds?.center
            val targetOffset = if (source != null && target != null) {
                with(density) {
                    Offset(
                        x = (target.x - source.x).toDp().value,
                        y = (target.y - source.y).toDp().value
                    )
                }
            } else {
                Offset.Zero
            }
            val animation = TableExitAnimation.fromTable(
                table = oldTable,
                direction = direction,
                targetOffsetX = targetOffset.x,
                targetOffsetY = targetOffset.y
            )
            tableExitDuration = duration
            tableExitAnimation = animation
            try {
                delay(duration.toLong())
            } finally {
                if (tableExitAnimation == animation) tableExitAnimation = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF093B2B), Color(0xFF0B4A33), Color(0xFF062E22))))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopOpponents(
                state = state,
                cardBackStyle = cardBackStyle,
                onBoundsChanged = { index, bounds -> opponentBounds[index] = bounds }
            )
            GameInfoPanel(
                state = state,
                prompt = viewModel.getUserPromptText(),
                latestEvent = viewModel.latestEvent,
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
                    highlightColor = legalHintColor,
                    modifier = Modifier.fillMaxSize(),
                    onTargetBoundsChanged = { target, bounds ->
                        dropTargetBounds[target] = bounds
                    }
                )
                DiscardPileMarker(
                    cardStyle = cardStyle,
                    cardBackStyle = cardBackStyle,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 7.dp)
                        .onGloballyPositioned { discardBounds = it.boundsInRoot() }
                        .zIndex(4f)
                )
                TableExitOverlay(
                    animation = tableExitAnimation,
                    cardStyle = cardStyle,
                    durationMillis = tableExitDuration,
                    modifier = Modifier.fillMaxSize()
                )
            }
            ActionBar(actions = viewModel.getAvailableActions(), onAction = { action ->
                when (action) {
                    GameAction.DONE -> viewModel.done()
                    GameAction.TAKE -> viewModel.take()
                    GameAction.PASS -> viewModel.pass()
                }
            })
            ScrollableHandView(
                hand = state.players.first().hand,
                legalCards = legalCards,
                cardStyle = cardStyle,
                legalHintColor = legalHintColor,
                modifier = Modifier.onGloballyPositioned { handBounds = it.boundsInRoot() },
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

        DragOverlay(dragState = dragState, cardStyle = cardStyle, legalHintColor = legalHintColor)
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
private fun TopOpponents(
    state: GameState,
    cardBackStyle: CardBackStyle,
    onBoundsChanged: (Int, Rect) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        state.players.drop(1).forEachIndexed { offset, player ->
            val index = offset + 1
            PlayerPanel(
                player = player,
                role = roleFor(state, index),
                cardBackStyle = cardBackStyle,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .onGloballyPositioned { onBoundsChanged(index, it.boundsInRoot()) }
            )
        }
    }
}

@Composable
private fun DragOverlay(dragState: DragState, cardStyle: CardStyle, legalHintColor: Color) {
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
        legalHintColor = legalHintColor,
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
private fun DiscardPileMarker(
    cardStyle: CardStyle,
    cardBackStyle: CardBackStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 54.dp, height = 76.dp)
            .background(Color.Black.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        CardView(
            card = null,
            faceDown = true,
            cardBackStyle = cardBackStyle,
            cardSize = CardSize(33.dp, 47.dp),
            style = cardStyle,
            modifier = Modifier.offset(x = (-5).dp, y = 4.dp)
        )
        CardView(
            card = null,
            faceDown = true,
            cardBackStyle = cardBackStyle,
            cardSize = CardSize(33.dp, 47.dp),
            style = cardStyle,
            modifier = Modifier.offset(x = 3.dp, y = (-3).dp)
        )
        Text(
            "Bita",
            color = Color.White.copy(alpha = 0.82f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
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

private fun tableExitDurationMillis(direction: TableExitDirection, speed: AnimationSpeed): Int {
    val base = when (direction) {
        TableExitDirection.HUMAN_TAKE, TableExitDirection.AI_TAKE -> AnimationDurations.TakeMs
        TableExitDirection.DISCARD -> AnimationDurations.DiscardMs
    }
    return AnimationDurations.scale(base, speed)
}

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
