package com.example.durak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.durak.data.CardStyle
import com.example.durak.game.Card

data class CardSize(val width: Dp = 66.dp, val height: Dp = 96.dp)

@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    cardSize: CardSize = CardSize(),
    style: CardStyle = CardStyle.CLASSIC,
    faceDown: Boolean = false,
    playable: Boolean = false,
    disabled: Boolean = false
) {
    val shape = RoundedCornerShape(8.dp)
    val cardColor = when (style) {
        CardStyle.CLASSIC -> Color(0xFFFFFBF1)
        CardStyle.MODERN -> Color(0xFFF8FAFC)
        CardStyle.MINIMAL -> Color.White
    }
    val border = when {
        playable -> Color(0xFFFFD54F)
        else -> Color(0xFFD8D4C9)
    }
    Surface(
        modifier = modifier
            .size(cardSize.width, cardSize.height)
            .alpha(if (disabled) 0.45f else 1f)
            .shadow(if (playable) 7.dp else 3.dp, shape),
        shape = shape,
        color = if (faceDown) Color(0xFF163C8C) else cardColor,
        border = BorderStroke(if (playable) 2.dp else 1.dp, border)
    ) {
        if (faceDown || card == null) {
            CardBackPattern()
        } else {
            CardFaceContent(card)
        }
    }
}

@Composable
fun MiniCardView(
    card: Card,
    modifier: Modifier = Modifier,
    style: CardStyle = CardStyle.CLASSIC
) {
    CardView(
        card = card,
        modifier = modifier,
        cardSize = CardSize(36.dp, 50.dp),
        style = style,
        playable = false,
        disabled = false
    )
}

@Composable
private fun CardFaceContent(card: Card) {
    val suitColor = if (card.suit.isRed) Color(0xFFC62828) else Color(0xFF151515)
    Box(Modifier.fillMaxSize().padding(6.dp)) {
        Corner(card, suitColor, Modifier.align(Alignment.TopStart))
        Text(
            text = card.suit.symbol,
            color = suitColor,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
        Corner(card, suitColor, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun Corner(card: Card, color: Color, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(card.rank.label, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 14.sp)
        Text(card.suit.symbol, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 13.sp)
    }
}

@Composable
private fun CardBackPattern() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF163C8C))
            .padding(8.dp)
            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Spacer(
                            Modifier
                                .size(5.dp)
                                .background(Color(0xFFE6EFFA), RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}
