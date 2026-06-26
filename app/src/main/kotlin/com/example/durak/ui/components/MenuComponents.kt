package com.example.durak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GamePanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFFFBF0), Color(0xFFEFE2C5))),
                RoundedCornerShape(18.dp)
            )
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
fun GameLogo(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Durak",
            color = Color(0xFFFFF7D7),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(4.dp))
        )
        Text(
            text = "offline card game",
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun CardBackFan(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy((-28).dp), verticalAlignment = Alignment.CenterVertically) {
            FanCard(-18f)
            FanCard(-8f)
            FanCard(0f)
            FanCard(8f)
            FanCard(18f)
        }
    }
}

@Composable
private fun FanCard(rotation: Float) {
    CardView(
        card = null,
        faceDown = true,
        cardSize = CardSize(48.dp, 70.dp),
        modifier = Modifier.rotate(rotation)
    )
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(10.dp)
    if (primary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B2A86),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFBBB2C5),
                disabledContentColor = Color(0xFF5F5867)
            ),
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
        ) { Text(text, fontWeight = FontWeight.Bold) }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF17251D),
                disabledContentColor = Color(0xFF7D776D)
            ),
            border = BorderStroke(1.dp, if (enabled) Color(0xFF8B7E67) else Color(0xFFCDC4B6)),
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
        ) { Text(text, fontWeight = FontWeight.SemiBold) }
    }
}
