package com.example.durak.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.durak.data.AnimationSpeed
import com.example.durak.data.CardStyle
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val preferences = viewModel.appPreferences
    MenuPanel {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        OptionGroup("Animation speed", AnimationSpeed.entries, preferences.animationSpeed, { it.title }) {
            viewModel.updateAppPreferences(preferences.copy(animationSpeed = it))
        }
        OptionGroup("Card style", CardStyle.entries, preferences.cardStyle, { it.title }) {
            viewModel.updateAppPreferences(preferences.copy(cardStyle = it))
        }
        ToggleRow("Show legal move hints", preferences.showLegalMoveHints) {
            viewModel.updateAppPreferences(preferences.copy(showLegalMoveHints = it))
        }
        ToggleRow("Confirm new game", preferences.confirmNewGame) {
            viewModel.updateAppPreferences(preferences.copy(confirmNewGame = it))
        }
        Button(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
        OutlinedButton(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(if (checked) "On" else "Off", style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
