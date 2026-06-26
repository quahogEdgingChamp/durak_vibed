package com.example.durak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.durak.data.AnimationSpeed
import com.example.durak.data.CardStyle
import com.example.durak.data.LegalHintColor
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
        ColorOptionGroup("Legal hint color", LegalHintColor.entries, preferences.legalHintColor) {
            viewModel.updateAppPreferences(preferences.copy(legalHintColor = it))
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
private fun ColorOptionGroup(
    label: String,
    options: List<LegalHintColor>,
    selected: LegalHintColor,
    onSelected: (LegalHintColor) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF233126))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val color = option.toComposeColor()
                val selectedOption = option == selected
                val content: @Composable () -> Unit = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(color, RoundedCornerShape(999.dp))
                                .border(1.dp, Color.Black.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                        )
                        Text(option.title, fontWeight = if (selectedOption) FontWeight.Bold else FontWeight.SemiBold)
                    }
                }
                if (selectedOption) {
                    Button(
                        onClick = { onSelected(option) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5B2A86), contentColor = Color.White),
                        content = { content() }
                    )
                } else {
                    OutlinedButton(
                        onClick = { onSelected(option) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF27372A)),
                        content = { content() }
                    )
                }
            }
        }
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
