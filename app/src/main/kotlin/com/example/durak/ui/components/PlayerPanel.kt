package com.example.durak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.durak.game.Player

@Composable
fun PlayerPanel(
    player: Player,
    role: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(104.dp)
            .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            player.name,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy((-18).dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(player.hand.size.coerceIn(1, 4)) {
                CardView(card = null, faceDown = true, cardSize = CardSize(34.dp, 50.dp))
            }
        }
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.13f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("$role • ${player.hand.size}", color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}
