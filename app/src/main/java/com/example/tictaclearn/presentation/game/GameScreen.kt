package com.example.tictaclearn.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.GameMode
import com.example.tictaclearn.presentation.game.components.BoardCell
import com.example.tictaclearn.presentation.game.components.GameStatusIndicator
import com.example.tictaclearn.presentation.game.components.GameTopBar

@Composable
fun GameScreen(
    gameModeId: String,
    moodId: String,
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { GameTopBar(currentMood = uiState.currentMood) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Indicador de estado (Turno / Ganador)
            GameStatusIndicator(gameState = uiState.gameState)

            Spacer(modifier = Modifier.height(24.dp))

            // Tablero Dinámico
            // ✅ CORRECCIÓN: Usamos boardSize del modo actual para definir columnas
            val columns = uiState.currentGameMode.boardSize

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Mantiene el tablero cuadrado
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(uiState.gameState.board.cells) { index, cell ->
                    BoardCell(
                        cellContent = cell,
                        onClick = { viewModel.onCellClicked(index) },
                        fontSize = 24
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::onResetGameClicked,
                enabled = !uiState.isProcessingMove
            ) {
                Text("Reiniciar Partida")
            }

            if (uiState.gameState.isFinished) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onGameFinished) {
                    Text("Volver al Menú")
                }
            }
        }
    }
}