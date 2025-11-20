package com.example.tictaclearn.presentation.game

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
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.presentation.game.components.GameBoard
import com.example.tictaclearn.presentation.game.components.GameStatusCardWrapper
import com.example.tictaclearn.presentation.game.components.GameTopBar
import com.example.tictaclearn.ui.theme.BackgroundDark
import com.example.tictaclearn.ui.theme.NeonOrange
import com.example.tictaclearn.ui.theme.NeonRed
import com.example.tictaclearn.ui.theme.TextWhite
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.sp


@Composable
fun GameScreen(
    gameModeId: String,
    moodId: String,
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { GameTopBar(currentMood = uiState.currentMood) },
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
            GameStatusCardWrapper(uiState.gameState)

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
                    .height(if (uiState.gameState.isFinished) 140.dp else 80.dp)
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
                    // 🚨 CAMBIO UI: Botón Reiniciar con Icono
                    Button(
                        onClick = viewModel::onResetGameClicked,
                        enabled = !uiState.isProcessingMove,
                        modifier = Modifier
                            .fillMaxWidth(0.9f) // Ajuste de ancho
                            .height(56.dp), // Altura estándar
                        shape = RoundedCornerShape(12.dp), // Bordes menos redondeados
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        // 🚨 IMPLEMENTACIÓN: Icono de Refresh
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reiniciar",
                            tint = BackgroundDark, // Icono oscuro sobre botón naranja
                            modifier = Modifier.size(20.dp).padding(end = 6.dp)
                        )
                        Text(
                            "REINICIAR PARTIDA",
                            fontWeight = FontWeight.Black,
                            color = BackgroundDark,
                            fontSize = 16.sp
                        )
                    }

                    if (uiState.gameState.isFinished) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // 🚨 CAMBIO UI: Botón Volver al Menú con Icono y Borde Sutil
                        OutlinedButton(
                            onClick = onGameFinished,
                            modifier = Modifier
                                .fillMaxWidth(0.9f) // Ajuste de ancho
                                .height(50.dp),
                            // 🚨 AJUSTE: Borde más sutil y color TextWhite
                            border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite)
                        ) {
                            // 🚨 IMPLEMENTACIÓN: Icono de Home
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