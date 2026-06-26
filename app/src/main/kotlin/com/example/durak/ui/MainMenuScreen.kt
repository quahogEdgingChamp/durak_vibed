package com.example.durak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.durak.ui.components.CardBackFan
import com.example.durak.ui.components.GameLogo
import com.example.durak.ui.components.GamePanel
import com.example.durak.ui.components.MenuButton
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF062D20), Color(0xFF0E6947), Color(0xFF031E15))
                )
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(22.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardBackFan()
            GameLogo()
            GamePanel {
                MenuButton("New Game", onClick = { viewModel.goTo(Screen.NEW_GAME) }, primary = true)
                MenuButton("Rules", onClick = { viewModel.goTo(Screen.RULES) })
                MenuButton("Settings", onClick = { viewModel.goTo(Screen.SETTINGS) })
            }
        }
    }
}
