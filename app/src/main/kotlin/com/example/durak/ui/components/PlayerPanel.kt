package com.example.durak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.durak.game.Player

@Composable
fun PlayerPanel(
    player: Player,
    role: String,
    modifier: Modifier = Modifier
) {
    val roleColor = when (role) {
        "Attacker" -> Color(0xFFFFD166)
        "Defender" -> Color(0xFF7DE2D1)
        else -> Color.White.copy(alpha = 0.54f)
    }
    val borderColor = when (role) {
        "Attacker" -> Color(0xFFFFD166).copy(alpha = 0.72f)
        "Defender" -> Color(0xFF7DE2D1).copy(alpha = 0.78f)
        else -> Color.White.copy(alpha = 0.12f)
    }
    Column(
        modifier = modifier
            .width(112.dp)
            .background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.30f), Color.Black.copy(alpha = 0.16f))),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                player.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            RoleDot(roleColor)
        }
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            val visibleCards = player.hand.size.coerceIn(1, 4)
            repeat(visibleCards) { index ->
                val rotation = (index - (visibleCards - 1) / 2f) * 7f
                CardView(
                    card = null,
                    faceDown = true,
                    cardSize = CardSize(31.dp, 44.dp),
                    modifier = Modifier
                        .offset(x = ((index - visibleCards / 2f) * 12).dp)
                        .graphicsLayer { rotationZ = rotation }
                        .zIndex(index.toFloat())
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFFFFFBF1), RoundedCornerShape(999.dp))
                    .border(1.dp, Color.Black.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
                    .zIndex(10f)
            ) {
                Text("${player.hand.size}", color = Color(0xFF1E2A22), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
        Box(
            modifier = Modifier
                .background(roleColor.copy(alpha = if (role == "Waiting") 0.12f else 0.22f), RoundedCornerShape(50))
                .border(1.dp, roleColor.copy(alpha = 0.45f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                "$role • ${player.hand.size} ${if (player.hand.size == 1) "card" else "cards"}",
                color = if (role == "Waiting") Color.White.copy(alpha = 0.74f) else Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RoleDot(color: Color) {
    Box(
        modifier = Modifier
            .size(9.dp)
            .background(color, RoundedCornerShape(50))
    )
}
