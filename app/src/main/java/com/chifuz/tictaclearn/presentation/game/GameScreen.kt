package com.chifuz.tictaclearn.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.presentation.game.components.GameBoard
import com.chifuz.tictaclearn.presentation.game.components.GameStatusCardWrapper
import com.chifuz.tictaclearn.presentation.game.components.GameTopBar
import com.chifuz.tictaclearn.ui.theme.BackgroundDark
import com.chifuz.tictaclearn.ui.theme.NeonOrange
import com.chifuz.tictaclearn.ui.theme.NeonRed
import com.chifuz.tictaclearn.ui.theme.TextWhite
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ExitToApp


@Composable
fun GameScreen(
    gameModeId: String,
    moodId: String,
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            GameTopBar(
                currentMood = uiState.currentMood,
                currentGameMode = uiState.currentGameMode
            )
        },
        containerColor = BackgroundDark // Fondo oscuro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Indicador de estado
            GameStatusCardWrapper(
                gameState = uiState.gameState,
                isAiThinking = uiState.isProcessingMove,
                currentGameMode = uiState.currentGameMode // Pasando el modo de juego
            )

            // 2. Tablero Dinámico
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    board = uiState.gameState.board,
                    onCellClicked = viewModel::onCellClicked,
                    isProcessing = uiState.isProcessingMove,
                    winningCells = uiState.gameState.result.let { if (it is GameResult.Win) it.winningLine else emptyList() }
                )
            }

            // 3. Área de Controles
            Box(
                // Altura dinámica para evitar saltos de layout
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Barra de Progreso
                if (uiState.isProcessingMove) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )
                }

                // Controles y Botones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Botón Reiniciar
                    Button(
                        onClick = viewModel::onResetGameClicked,
                        enabled = !uiState.isProcessingMove,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reiniciar",
                            tint = BackgroundDark,
                            modifier = Modifier.size(20.dp).padding(end = 6.dp)
                        )
                        Text(
                            "REINICIAR PARTIDA",
                            fontWeight = FontWeight.Black,
                            color = BackgroundDark,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón ABANDONAR PARTIDA
                    if (!uiState.gameState.isFinished) {
                        OutlinedButton(
                            onClick = onGameFinished,
                            enabled = !uiState.isProcessingMove,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(50.dp),
                            border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Abandonar partida",
                                tint = NeonRed,
                                modifier = Modifier.size(20.dp).padding(end = 6.dp)
                            )
                            Text("ABANDONAR PARTIDA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    if (uiState.gameState.isFinished) {
                        // Botón Volver al Menú
                        OutlinedButton(
                            onClick = onGameFinished,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(50.dp),
                            border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Volver al menú",
                                tint = TextWhite,
                                modifier = Modifier.size(20.dp).padding(end = 6.dp)
                            )
                            Text("VOLVER AL MENÚ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}