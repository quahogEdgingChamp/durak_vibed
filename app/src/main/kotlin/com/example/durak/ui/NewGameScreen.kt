package com.example.durak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.durak.game.AiDifficulty
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

@Composable
fun NewGameScreen(viewModel: GameViewModel) {
    val settings = viewModel.gameOptions
    MenuPanel {
        Text("New Game", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OptionGroup("Deck size", DeckMode.entries, settings.deckMode, { "${it.size}" }) {
            viewModel.updateGameOptions(settings.copy(deckMode = it))
        }
        OptionGroup("Mode", GameMode.entries, settings.gameMode, { it.title }) {
            viewModel.updateGameOptions(settings.copy(gameMode = it))
        }
        OptionGroup("Players", listOf(2, 3, 4), settings.playerCount, { it.toString() }) {
            viewModel.updateGameOptions(settings.copy(playerCount = it))
        }
        OptionGroup("AI difficulty", AiDifficulty.entries, settings.aiDifficulty, { it.title }) {
            viewModel.updateGameOptions(settings.copy(aiDifficulty = it))
        }
        Button(onClick = viewModel::startGame, modifier = Modifier.fillMaxWidth()) { Text("Start Game") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
fun MenuPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B4A33))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF7F4EA), RoundedCornerShape(10.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun <T> OptionGroup(
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
