package com.example.tictaclearn.presentation.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.ui.theme.TicTacLearnTheme
import com.example.tictaclearn.presentation.game.components.GameTopBar

/**
 * Pantalla principal donde se juega al TicTacToe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    moodId: String,
    onGameFinished: () -> Unit, // Acción para volver al menú
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameScreenContent(
        uiState = uiState,
        onCellClicked = viewModel::onCellClicked,
        onResetGameClicked = viewModel::onResetGameClicked,
        onFinishGameClicked = onGameFinished // Pasamos la acción de finalizar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onCellClicked: (Int) -> Unit,
    onResetGameClicked: () -> Unit,
    onFinishGameClicked: () -> Unit // Nuevo parámetro
) {
    Scaffold(
        topBar = {
            GameTopBar(currentMood = uiState.currentMood)
        },
        bottomBar = {
            if (uiState.isProcessingMove) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Estado del Juego
            GameStatusMessage(uiState.gameState)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tablero
            BoardGrid(
                board = uiState.gameState.board,
                isInteractive = !uiState.gameState.isFinished &&
                        uiState.gameState.currentPlayer == Player.Human &&
                        !uiState.isProcessingMove,
                onCellClicked = onCellClicked
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. **CONTROLES DE JUEGO (Dinámicos)**
            // Usamos un Box para mantener el espacio y que los botones no "salten" demasiado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp), // Altura fija para la zona de botones
                contentAlignment = Alignment.Center
            ) {
                if (uiState.gameState.isFinished) {
                    // **CASO A: Juego Terminado -> Mostrar opciones de fin**
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón de Finalizar (Volver)
                        OutlinedButton(
                            onClick = onFinishGameClicked,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🏁 Finalizar")
                        }

                        // Botón de Jugar de Nuevo (Reiniciar)
                        Button(
                            onClick = onResetGameClicked,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔄 Otra vez")
                        }
                    }
                } else {
                    // **CASO B: Juego en Curso -> Botón de Reiniciar (Opcional)**
                    // Si quieres permitir reiniciar a mitad de partida, descomenta esto.
                    // Si prefieres que esté oculto hasta terminar, deja el Box vacío o pon un texto.

                    /* OutlinedButton(
                        onClick = onResetGameClicked,
                        enabled = !uiState.isProcessingMove
                    ) {
                        Text("Reiniciar Partida")
                    }
                    */

                    // Opción alternativa: Texto de ayuda
                    if (!uiState.isProcessingMove) {
                        Text(
                            text = "Toca una casilla para jugar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 4. Mensajes de Error
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// --- Componentes Auxiliares (Sin cambios mayores) ---

@Composable
fun GameStatusMessage(gameState: GameState) {
    val text = when (val result = gameState.result) {
        GameResult.Draw -> "¡EMPATE! 🤝"
        GameResult.Playing -> "Turno de: ${gameState.currentPlayer.symbol}"
        is GameResult.Win -> "¡GANADOR: ${result.winner.symbol}! 🎉"
    }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = when (gameState.result) {
            GameResult.Draw -> MaterialTheme.colorScheme.tertiary
            GameResult.Playing -> MaterialTheme.colorScheme.onBackground
            is GameResult.Win -> MaterialTheme.colorScheme.primary
        },
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun BoardGrid(
    board: Board,
    isInteractive: Boolean,
    onCellClicked: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.aspectRatio(1f),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(board.cells) { index, cell ->
            BoardCell(
                symbol = cell,
                onClick = { onCellClicked(index) },
                isEnabled = isInteractive && cell == ' '
            )
        }
    }
}

@Composable
fun BoardCell(
    symbol: Char,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                color = if (symbol == ' ') MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(4.dp)
            )
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol.toString().trim(),
            fontSize = 60.sp,
            fontWeight = FontWeight.Black,
            color = when (symbol) {
                'X' -> MaterialTheme.colorScheme.primary
                'O' -> MaterialTheme.colorScheme.error
                else -> Color.Transparent
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenFinishedPreview() {
    val winBoard = Board(cells = listOf('X', 'X', 'X', 'O', 'O', ' ', ' ', ' ', ' '))
    val winState = GameState(
        board = winBoard,
        currentPlayer = Player.AI,
        result = GameResult.Win(Player.Human, listOf(0, 1, 2)),
        gameHistory = listOf(winBoard)
    )
    TicTacLearnTheme {
        GameScreenContent(
            uiState = GameUiState(gameState = winState, currentMood = Mood.NORMAL, isProcessingMove = false),
            onCellClicked = {},
            onResetGameClicked = {},
            onFinishGameClicked = {}
        )
    }
}