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
import com.example.tictaclearn.presentation.game.components.GameBoard
import com.example.tictaclearn.presentation.game.components.GameStatusCardWrapper // Importamos el nuevo wrapper
import com.example.tictaclearn.presentation.game.components.GameTopBar
import com.example.tictaclearn.ui.theme.*

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
        containerColor = BackgroundDark // Color de fondo forzado para la inmersión
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp), // Mayor padding horizontal para look "caro"
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Permite que el tablero ocupe el espacio
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Indicador de estado (En Card Premium)
            GameStatusCardWrapper(uiState.gameState)

            // 2. Tablero Dinámico (Weighted para ocupar el centro)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp), // Espacio entre elementos
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    board = uiState.gameState.board,
                    onCellClicked = viewModel::onCellClicked,
                    isProcessing = uiState.isProcessingMove
                )
            }

            // 3. Área de Controles - BOX FIJO (Anti-salto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // Altura fija para evitar el Layout Shift
                contentAlignment = Alignment.TopCenter
            ) {
                // Barra de Progreso (Flotando arriba, no causa shift)
                if (uiState.isProcessingMove) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = NeonCyan,
                        trackColor = SurfaceDark
                    )
                }

                // Controles y Botones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp), // Espacio para la barra de progreso
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top // Alineamos botones arriba
                ) {
                    Button(
                        onClick = viewModel::onResetGameClicked,
                        enabled = !uiState.isProcessingMove,
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Text("REINICIAR PARTIDA", fontWeight = FontWeight.Bold, color = BackgroundDark)
                    }

                    // Botón de Salida condicional (Usamos OutlinedButton para no competir con el Reset)
                    if (uiState.gameState.isFinished) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onGameFinished,
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                            border = BorderStroke(2.dp, TextGray)
                        ) {
                            Text("VOLVER AL MENÚ", color = TextWhite)
                        }
                    }
                }
            }
        }
    }
}