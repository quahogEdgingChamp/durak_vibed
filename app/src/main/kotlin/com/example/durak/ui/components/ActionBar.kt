package com.example.durak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                GameAction.DONE -> Button(onClick = { onAction(action) }) { Text(action.title, fontWeight = FontWeight.SemiBold) }
                GameAction.TAKE -> Button(
                    onClick = { onAction(action) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC857),
                        contentColor = Color(0xFF1D261F)
                    )
                ) {
                    Text(action.title, fontWeight = FontWeight.Bold)
                }
                GameAction.PASS -> OutlinedButton(
                    onClick = { onAction(action) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.22f),
                        contentColor = Color.White
                    )
                ) {
                    Text(action.title, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
