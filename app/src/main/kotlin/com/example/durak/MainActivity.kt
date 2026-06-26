package com.example.durak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.durak.data.SettingsStore
import com.example.durak.game.AiDifficulty
import com.example.durak.game.Card
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.game.GameSettings
import com.example.durak.game.GameState
import com.example.durak.game.GameStatus
import com.example.durak.game.TableCard
import com.example.durak.ui.DurakViewModel
import com.example.durak.ui.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DurakApp() }
    }
}

@Composable
private fun DurakApp() {
    val context = LocalContext.current
    val viewModel = remember { DurakViewModel(SettingsStore(context.applicationContext)) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.snackbar) {
        if (viewModel.snackbar.isNotBlank()) {
            snackbarHostState.showSnackbar(viewModel.snackbar)
            viewModel.clearSnackbar()
        }
    }

    MaterialTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = Color(0xFF0D5C3D)
            ) {
                when (viewModel.screen) {
                    Screen.MENU -> MainMenu(viewModel)
                    Screen.NEW_GAME -> NewGameScreen(viewModel)
                    Screen.RULES -> RulesScreen(viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel)
                    Screen.GAME -> GameScreen(viewModel)
                    Screen.END -> EndGameScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun MainMenu(viewModel: DurakViewModel) {
    CenterPanel {
        Text("Durak", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { viewModel.goTo(Screen.NEW_GAME) }, modifier = Modifier.fillMaxWidth()) { Text("New Game") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.RULES) }, modifier = Modifier.fillMaxWidth()) { Text("Rules") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.SETTINGS) }, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
    }
}

@Composable
private fun NewGameScreen(viewModel: DurakViewModel) {
    SettingsForm(
        title = "New Game",
        settings = viewModel.settings,
        onSettings = viewModel::updateSettings,
        primaryText = "Start",
        onPrimary = viewModel::startGame,
        onBack = { viewModel.goTo(Screen.MENU) }
    )
}

@Composable
private fun SettingsScreen(viewModel: DurakViewModel) {
    SettingsForm(
        title = "Settings",
        settings = viewModel.settings,
        onSettings = viewModel::updateSettings,
        primaryText = "Save",
        onPrimary = { viewModel.goTo(Screen.MENU) },
        onBack = { viewModel.goTo(Screen.MENU) }
    )
}

@Composable
private fun SettingsForm(
    title: String,
    settings: GameSettings,
    onSettings: (GameSettings) -> Unit,
    primaryText: String,
    onPrimary: () -> Unit,
    onBack: () -> Unit
) {
    CenterPanel {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OptionGroup("Deck", DeckMode.entries, settings.deckMode, { "${it.size}" }) {
            onSettings(settings.copy(deckMode = it))
        }
        OptionGroup("Mode", GameMode.entries, settings.gameMode, { it.title }) {
            onSettings(settings.copy(gameMode = it))
        }
        OptionGroup("Players", listOf(2, 3, 4), settings.playerCount, { it.toString() }) {
            onSettings(settings.copy(playerCount = it))
        }
        OptionGroup("AI", AiDifficulty.entries, settings.aiDifficulty, { it.title }) {
            onSettings(settings.copy(aiDifficulty = it))
        }
        Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth()) { Text(primaryText) }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun <T> OptionGroup(
    label: String,
    options: List<T>,
    selected: T,
    title: (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                if (option == selected) {
                    Button(onClick = { onSelected(option) }) { Text(title(option)) }
                } else {
                    OutlinedButton(onClick = { onSelected(option) }) { Text(title(option)) }
                }
            }
        }
    }
}

@Composable
private fun GameScreen(viewModel: DurakViewModel) {
    val state = viewModel.gameState ?: return
    val legalCards = viewModel.legalHumanCards()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        OpponentsRow(state)
        StatusRow(state)
        TableArea(state.table)
        ActionButtons(viewModel, state)
        HandRow(state.players[0].hand, legalCards, viewModel::playCard)
    }
}

@Composable
private fun OpponentsRow(state: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        state.players.drop(1).forEachIndexed { offset, player ->
            val index = offset + 1
            val marker = when (index) {
                state.attackerIndex -> "A"
                state.defenderIndex -> "D"
                else -> ""
            }
            CardBack("${player.name} $marker", player.hand.size)
        }
    }
}

@Composable
private fun StatusRow(state: GameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Trump ${state.trumpCard}  Deck ${state.deckRemaining}",
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Attacker P${state.attackerIndex + 1}  Defender P${state.defenderIndex + 1}",
            color = Color.White
        )
    }
}

@Composable
private fun TableArea(table: List<TableCard>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .border(1.dp, Color(0x6633FFAA), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (table.isEmpty()) {
            Text("Table", color = Color.White.copy(alpha = 0.7f))
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                table.forEach { pair ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CardFace(pair.attack, highlighted = false, enabled = false, onClick = {})
                        Text(" / ", color = Color.White)
                        pair.defense?.let { CardFace(it, highlighted = false, enabled = false, onClick = {}) }
                            ?: Text("?", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(viewModel: DurakViewModel, state: GameState) {
    val isHumanTurn = state.currentActorIndex == 0 && state.status == GameStatus.IN_PROGRESS
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { viewModel.actionHint("attack") }, enabled = isHumanTurn && !state.needsDefense) { Text("Attack") }
        Button(onClick = { viewModel.actionHint("defend") }, enabled = isHumanTurn && state.needsDefense) { Text("Defend") }
        OutlinedButton(onClick = { viewModel.actionHint("pass") }, enabled = isHumanTurn && state.needsDefense) { Text("Pass") }
        OutlinedButton(onClick = viewModel::take, enabled = isHumanTurn && state.needsDefense) { Text("Take") }
        OutlinedButton(onClick = viewModel::done, enabled = isHumanTurn && !state.needsDefense && state.table.isNotEmpty()) { Text("Done") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.NEW_GAME) }) { Text("New Game") }
    }
}

@Composable
private fun HandRow(hand: List<Card>, legalCards: Set<Card>, onCard: (Card) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(hand) { card ->
            CardFace(card, highlighted = card in legalCards, enabled = true) { onCard(card) }
        }
    }
}

@Composable
private fun CardFace(card: Card, highlighted: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val textColor = if (card.suit.isRed) Color(0xFFC62828) else Color(0xFF111111)
    Card(
        modifier = Modifier
            .size(width = 58.dp, height = 86.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(7.dp),
        border = BorderStroke(if (highlighted) 3.dp else 1.dp, if (highlighted) Color(0xFFFFD54F) else Color(0xFFDDDDDD)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(card.toString(), color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CardBack(title: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 86.dp)
                .background(Color(0xFF183A8C), RoundedCornerShape(7.dp))
                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(count.toString(), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(title, color = Color.White, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun RulesScreen(viewModel: DurakViewModel) {
    CenterPanel {
        Text("Rules", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Deal 6 cards to each player. The trump suit beats non-trumps; higher cards of the same suit beat lower cards. The player with the lowest trump starts. Attack, defend, then draw back to 6 while the deck has cards. The last player holding cards after the deck is empty is the durak.",
            textAlign = TextAlign.Start
        )
        Text("Throw-in allows extra attacks matching ranks already on the table. Passing lets the defender pass with a matching rank when the next defender has enough cards. Casual relaxes throw-in and passing limits.")
        Button(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun EndGameScreen(viewModel: DurakViewModel) {
    val state = viewModel.gameState
    val result = when {
        state == null -> "Game ended."
        state.isDraw -> "Draw"
        state.loserIndex == 0 -> "You lost"
        state.loserIndex != null -> "${state.players[state.loserIndex].name} is the durak"
        else -> "Game ended"
    }
    CenterPanel {
        Text(result, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Button(onClick = viewModel::playAgain, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Main Menu") }
    }
}

@Composable
private fun CenterPanel(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF7F7F2), RoundedCornerShape(8.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
