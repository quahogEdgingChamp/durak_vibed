package com.example.durak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.durak.game.GameAction

@Composable
fun ActionBar(
    actions: List<GameAction>,
    onAction: (GameAction) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            when (action) {
                GameAction.DONE -> Button(onClick = { onAction(action) }) { Text(action.title) }
                GameAction.TAKE -> OutlinedButton(onClick = { onAction(action) }) { Text(action.title) }
                GameAction.PASS -> OutlinedButton(onClick = { onAction(action) }) { Text(action.title) }
            }
        }
    }
}
