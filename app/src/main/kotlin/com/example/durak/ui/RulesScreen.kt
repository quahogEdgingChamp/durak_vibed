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
        RuleText("Classic: the attacker plays exactly one card. The defender beats that card or takes. No matching-rank adds and no passing.")
        RuleText("Transfer: after all current attacks are beaten, the attacker may add only matching-rank cards. The total attack count cannot exceed the defender's hand size at the start of the bout. No passing.")
        RuleText("Casual: matching-rank adds are allowed, and the defender may pass with a matching-rank card if the next defender has enough cards. Passing adds a new attack card; it is not a defense.")
        RuleText("Decks: 24 cards use 9 through A, 36 cards use 6 through A, and 52 cards use 2 through A.")
        RuleText("In this app: drag highlighted cards from your hand to the table. Drop onto an attack card or slot to defend. In Casual, drop a matching-rank card on the general table to pass. Use Done to finish a defended Transfer or Casual bout, and Take to pick up.")
        Button(onClick = { viewModel.goTo(Screen.MENU) }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun RuleText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
}
