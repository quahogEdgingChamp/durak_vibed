package com.example.durak.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.durak.data.CardStyle
import com.example.durak.game.Card
import java.io.IOException

data class CardSize(val width: Dp = 66.dp, val height: Dp = 96.dp)

@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    cardSize: CardSize = CardSize(),
    style: CardStyle = CardStyle.CLASSIC,
    faceDown: Boolean = false,
    playable: Boolean = false,
    disabled: Boolean = false,
    legalHintColor: Color = Color(0xFFFFC857)
) {
    val assetPath = when {
        faceDown || card == null -> CardImageProvider.cardBackAssetPath()
        else -> CardImageProvider.assetPathFor(card)
    }
    val image = rememberAssetImage(assetPath)
    val shape = RoundedCornerShape(9.dp)
    val cardColor = when (style) {
        CardStyle.CLASSIC -> Color(0xFFFFFBF1)
        CardStyle.MODERN -> Color(0xFFF8FAFC)
        CardStyle.MINIMAL -> Color.White
    }
    val border = when {
        playable -> legalHintColor
        else -> Color(0xFFD8D4C9)
    }
    Surface(
        modifier = modifier
            .size(cardSize.width, cardSize.height)
            .alpha(if (disabled) 0.45f else 1f)
            .shadow(if (playable) 10.dp else 4.dp, shape),
        shape = shape,
        color = if (faceDown) Color(0xFF163C8C) else cardColor,
        border = BorderStroke(if (playable) 2.dp else 1.dp, border)
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = card?.toString() ?: "Card back",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            if (faceDown || card == null) {
                CardBackPattern()
            } else {
                CardFaceContent(card, cardSize)
            }
        }
    }
}

@Composable
private fun rememberAssetImage(assetPath: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(assetPath) {
        try {
            context.assets.open(assetPath).use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        } catch (_: IOException) {
            null
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
private fun CardFaceContent(card: Card, cardSize: CardSize) {
    val suitColor = if (card.suit.isRed) Color(0xFFC62828) else Color(0xFF151515)
    val compact = cardSize.width < 50.dp
    val centerSize = if (compact) 23.sp else 34.sp
    Box(Modifier.fillMaxSize().padding(if (compact) 4.dp else 6.dp)) {
        Corner(card, suitColor, compact, Modifier.align(Alignment.TopStart))
        Text(
            text = card.suit.symbol,
            color = suitColor,
            fontWeight = FontWeight.Bold,
            fontSize = centerSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
        Corner(card, suitColor, compact, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun Corner(card: Card, color: Color, compact: Boolean, modifier: Modifier) {
    val rankSize = if (compact) 10.sp else 14.sp
    val suitSize = if (compact) 9.sp else 13.sp
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(card.rank.label, color = color, fontWeight = FontWeight.Black, fontSize = rankSize, lineHeight = rankSize)
        Text(card.suit.symbol, color = color, fontWeight = FontWeight.Bold, fontSize = suitSize, lineHeight = suitSize)
    }
}

@Composable
private fun CardBackPattern() {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF123A85))
            .padding(7.dp)
            .border(1.dp, Color.White.copy(alpha = 0.48f), RoundedCornerShape(6.dp))
            .padding(5.dp)
            .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(4.dp)),
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
