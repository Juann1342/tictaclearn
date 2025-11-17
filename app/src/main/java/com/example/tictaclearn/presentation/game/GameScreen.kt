package com.example.tictaclearn.presentation.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.presentation.game.components.GameBoard
import com.example.tictaclearn.presentation.game.components.GameStatusIndicator
import com.example.tictaclearn.presentation.game.components.GameTopBar
import kotlinx.coroutines.delay

// presentation/game/GameScreen.kt

@Composable
fun GameScreen(
    // El ID del mood viene de la navegación
    moodId: String,
    onGameFinished: () -> Unit,
    // hiltViewModel(key = ...) asegura que el VM se crea para este moodId específico
    viewModel: GameViewModel = hiltViewModel(key = moodId)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gameState = uiState.gameState

    // Si el juego termina (victoria o empate), volvemos a la pantalla de configuración
    LaunchedEffect(gameState.result) {
        if (gameState.result != GameResult.Playing) {
            // Damos un pequeño delay para que el usuario vea el resultado final antes de volver
            delay(2000)
            onGameFinished()
        }
    }

    Scaffold(
        topBar = {
            GameTopBar(currentMood = uiState.currentMood)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 1. Indicador de Turno/Estado
            GameStatusIndicator(gameState = gameState)

            Spacer(modifier = Modifier.height(48.dp))

            // 2. El Tablero 3x3
            GameBoard(
                board = gameState.board,
                onCellClicked = viewModel::onCellClicked,
                isProcessing = uiState.isProcessingMove // Deshabilita clicks mientras la IA piensa
            )

            // ... Podríamos añadir información adicional aquí
        }
    }
}

