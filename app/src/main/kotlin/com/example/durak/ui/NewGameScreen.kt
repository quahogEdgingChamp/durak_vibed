package com.example.durak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.durak.game.AiDifficulty
import com.example.durak.game.DeckMode
import com.example.durak.game.GameMode
import com.example.durak.ui.components.GamePanel
import com.example.durak.ui.components.MenuButton
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

@Composable
fun NewGameScreen(viewModel: GameViewModel) {
    val settings = viewModel.gameOptions
    MenuPanel {
        ScreenTitle("New Game")
        OptionGroup("Deck size", DeckMode.entries, settings.deckMode, { "${it.size}" }) {
            viewModel.updateGameOptions(settings.copy(deckMode = it))
        }
        OptionGroup("Mode", GameMode.entries, settings.gameMode, { it.title }) {
            viewModel.updateGameOptions(settings.copy(gameMode = it))
        }
        Text(
            modeDescription(settings.gameMode),
            color = Color(0xFF4B5C4F),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        OptionGroup("Players", listOf(2, 3, 4), settings.playerCount, { it.toString() }) {
            viewModel.updateGameOptions(settings.copy(playerCount = it))
        }
        OptionGroup("AI difficulty", AiDifficulty.entries, settings.aiDifficulty, { it.title }) {
            viewModel.updateGameOptions(settings.copy(aiDifficulty = it))
        }
        MenuButton("Start Game", onClick = viewModel::startGame, primary = true)
        MenuButton("Back", onClick = { viewModel.goTo(Screen.MENU) })
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
        GamePanel(modifier = Modifier.fillMaxWidth(), content = content)
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
        Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF233126))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                if (option == selected) {
                    Button(
                        onClick = { onSelected(option) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B2A86), contentColor = Color.White)
                    ) { Text(title(option), fontWeight = FontWeight.Bold) }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(option) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF27372A))
                    ) { Text(title(option), fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
fun ScreenTitle(text: String) {
    Text(
        text,
        color = Color(0xFF1B2B20),
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp
        )
    )
}

private fun modeDescription(mode: GameMode): String =
    when (mode) {
        GameMode.CLASSIC -> "Matching-rank throw-ins allowed. No transfers."
        GameMode.TRANSFER -> "Transfers allowed. No throw-ins."
        GameMode.CASUAL -> "Both transfers and matching-rank throw-ins allowed."
    }
