package com.example.durak.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

@Composable
fun RulesScreen(viewModel: GameViewModel) {
    MenuPanel {
        Text("Rules", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        RuleText("Classic: the attacker plays one card. The defender must beat it. If every attack is defended, the defender becomes the next attacker.")
        RuleText("Throw-in: after a defense, the attacker may add cards whose ranks already appear on the table. The attack cannot exceed the defender's available cards.")
        RuleText("Passing: the defender may pass the attack by dragging a matching-rank card to the table, if the next defender has enough cards.")
        RuleText("Casual: relaxed play that allows more forgiving throw-ins and passing.")
        RuleText("Decks: 24 cards use 9 through A, 36 cards use 6 through A, and 52 cards use 2 through A.")
        RuleText("In this app: drag highlighted cards from your hand to the table. Use Done to finish a defended attack, Take to pick up, and Pass when it is available.")
        Button(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun RuleText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
}
