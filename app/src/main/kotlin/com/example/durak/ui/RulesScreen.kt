package com.example.durak.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.durak.viewmodel.GameViewModel

@Composable
fun RulesScreen(viewModel: GameViewModel) {
    BackHandler { viewModel.backFromRules() }
    MenuPanel {
        Text("Rules", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        RuleText("Classic: matching-rank throw-ins are allowed. After every attack card is beaten, the attacker may add another card matching any visible table rank. Transfers are not allowed.")
        RuleText("Transfer: transfers are allowed. The defender may transfer with a same-rank attack card, making the next player the defender. Throw-ins are not allowed.")
        RuleText("Casual: both systems are allowed. Players may add matching-rank cards after defense, and defenders may transfer with a matching-rank attack card.")
        RuleText("Attack limit: each bout allows at most min(5, defender hand size at bout start) attack cards.")
        RuleText("Decks: 24 cards use 9 through A, 36 cards use 6 through A, and 52 cards use 2 through A.")
        RuleText("In this app: drag highlighted cards from your hand to the table. Drop onto an attack card or slot to defend. In Transfer and Casual, drop a matching-rank card on the general table to transfer. Use Done to stop adding cards in Classic and Casual, and Take to pick up.")
        Button(onClick = viewModel::backFromRules, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun RuleText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
}
