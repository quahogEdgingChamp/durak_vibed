package com.example.durak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.durak.ui.components.CardSize
import com.example.durak.ui.components.CardView
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF063521), Color(0xFF0D6A45), Color(0xFF062A1C))
                )
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CardFan()
            Text("Durak", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Button(onClick = { viewModel.goTo(Screen.NEW_GAME) }, modifier = Modifier.fillMaxWidth()) { Text("New Game") }
            OutlinedButton(
                onClick = viewModel::continueGame,
                enabled = viewModel.hasSavedGame,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continue Game") }
            OutlinedButton(onClick = { viewModel.goTo(Screen.RULES) }, modifier = Modifier.fillMaxWidth()) { Text("Rules") }
            OutlinedButton(onClick = { viewModel.goTo(Screen.SETTINGS) }, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
        }
    }
}

@Composable
private fun CardFan() {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy((-16).dp)) {
        CardView(null, faceDown = true, cardSize = CardSize(44.dp, 64.dp))
        CardView(null, faceDown = true, cardSize = CardSize(44.dp, 64.dp))
        CardView(null, faceDown = true, cardSize = CardSize(44.dp, 64.dp))
    }
}
