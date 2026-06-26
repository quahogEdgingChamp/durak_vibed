package com.example.durak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.durak.data.CardStyle
import com.example.durak.game.TableCard

@Composable
fun TableView(
    table: List<TableCard>,
    cardStyle: CardStyle,
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val border = if (highlighted) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.23f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(if (highlighted) 3.dp else 1.dp, border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (table.isEmpty()) {
            Text(
                "Drop cards here",
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                table.forEach { pair ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        CardView(pair.attack, cardSize = CardSize(54.dp, 78.dp), style = cardStyle)
                        pair.defense?.let {
                            CardView(it, cardSize = CardSize(54.dp, 78.dp), style = cardStyle)
                        } ?: DefensePlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun DefensePlaceholder() {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
            .padding(horizontal = 21.dp, vertical = 29.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("?", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}
