package com.example.durak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.durak.data.SavedGameRepository
import com.example.durak.data.SettingsRepository
import com.example.durak.ui.EndGameScreen
import com.example.durak.ui.GameScreen
import com.example.durak.ui.MainMenuScreen
import com.example.durak.ui.NewGameScreen
import com.example.durak.ui.RulesScreen
import com.example.durak.ui.SettingsScreen
import com.example.durak.viewmodel.GameViewModel
import com.example.durak.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DurakApp() }
    }
}

@Composable
private fun DurakApp() {
    val context = LocalContext.current
    val viewModel = remember {
        GameViewModel(
            settingsRepository = SettingsRepository(context.applicationContext),
            savedGameRepository = SavedGameRepository(context.applicationContext)
        )
    }
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B4A33)
        ) {
            when (viewModel.screen) {
                Screen.MENU -> MainMenuScreen(viewModel)
                Screen.NEW_GAME -> NewGameScreen(viewModel)
                Screen.RULES -> RulesScreen(viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel)
                Screen.GAME -> GameScreen(viewModel)
                Screen.END -> EndGameScreen(viewModel)
            }
        }
    }
}
