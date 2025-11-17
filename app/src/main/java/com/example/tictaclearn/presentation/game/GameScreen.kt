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

/**
 * Pantalla principal donde se juega al TicTacToe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    // El ID del Mood seleccionado, viene de los argumentos de navegación
    moodId: String,
    // Acción a ejecutar cuando el juego termina y queremos volver a la configuración
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    // El ViewModel se inicializa con el moodId en su constructor (SavedStateHandle)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Manejar la navegación de vuelta cuando el juego termina (solo si no estamos cargando)
    if (uiState.gameState.isFinished && !uiState.isProcessingMove) {
        onGameFinished()
    }

    GameScreenContent(
        uiState = uiState,
        onCellClicked = viewModel::onCellClicked,
        onResetGameClicked = viewModel::onResetGameClicked
    )
}

// Composable principal para el contenido de la pantalla, separado para la Preview.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onCellClicked: (Int) -> Unit,
    onResetGameClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TicTacLEarn") },
                // Mostrar el Mood actual en el subtítulo si está disponible
                // Nota: Usamos ?.let para asegurarnos de que currentMood no sea nulo.
                // Aunque el ViewModel lo inicializa, en Composable el primer ciclo puede ser nulo.
                actions = {
                    uiState.currentMood?.let { mood ->
                        Text(
                            text = "IA: ${mood.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        // Muestra un indicador de carga mientras la IA está "pensando"
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
            verticalArrangement = Arrangement.SpaceBetween // Espacio entre el estado y el tablero
        ) {
            // 1. **Estado del Juego (Mensaje)**
            GameStatusMessage(uiState.gameState)

            // Espaciador para separar el mensaje del tablero
            Spacer(modifier = Modifier.height(32.dp))

            // 2. **Tablero de Juego**
            BoardGrid(
                board = uiState.gameState.board,
                // El tablero solo es interactivo si no está terminado y es el turno del HUMANO
                isInteractive = !uiState.gameState.isFinished &&
                        uiState.gameState.currentPlayer == Player.Human &&
                        !uiState.isProcessingMove,
                onCellClicked = onCellClicked
            )

            // Espaciador para separar el tablero del botón
            Spacer(modifier = Modifier.height(32.dp))

            // 3. **Botón de Reiniciar**
            Button(
                onClick = onResetGameClicked,
                enabled = !uiState.isProcessingMove // Deshabilitado mientras se procesa un movimiento
            ) {
                Text("🔄 Reiniciar Partida")
            }

            // 4. **Mensaje de Error/Feedback (si lo hay)**
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

/**
 * Muestra el mensaje de estado (Turno, Victoria, Empate).
 */
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

/**
 * El tablero de 3x3 para el juego.
 */
@Composable
fun BoardGrid(
    board: Board,
    isInteractive: Boolean,
    onCellClicked: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .aspectRatio(1f), // Esto asegura que sea un cuadrado perfecto
                // .width(IntrinsicSize.Max), // ❌ ELIMINADO: Esta línea causaba el crash con LazyVerticalGrid
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

/**
 * Representa una sola celda del tablero.
 */
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
        // Estilo del símbolo (X u O)
        Text(
            text = symbol.toString().trim(),
            fontSize = 60.sp,
            fontWeight = FontWeight.Black,
            color = when (symbol) {
                'X' -> MaterialTheme.colorScheme.primary // Jugador Humano
                'O' -> MaterialTheme.colorScheme.error // IA
                else -> Color.Transparent
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    val sampleBoard = Board(cells = listOf('X', 'O', ' ', 'O', 'X', ' ', ' ', ' ', ' '))

    // **CORRECCIÓN: Se añade el parámetro 'result' que faltaba.**
    val sampleGameState = GameState(
        board = sampleBoard,
        currentPlayer = Player.AI,
        result = GameResult.Playing, // <-- AÑADIDO: El juego está en curso
        gameHistory = listOf(sampleBoard)
    )

    // El estado de ánimo es necesario para la TopAppBar, aunque solo sea un mock.
    val mockMood = Mood.NORMAL

    TicTacLearnTheme {
        GameScreenContent(
            uiState = GameUiState(
                gameState = sampleGameState,
                currentMood = mockMood,
                isProcessingMove = true // Simular que la IA está pensando
            ),
            onCellClicked = { },
            onResetGameClicked = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenWinPreview() {
    val winBoard = Board(cells = listOf('X', 'X', 'X', 'O', 'O', ' ', ' ', ' ', ' '))
    val winState = GameState(
        board = winBoard,
        currentPlayer = Player.AI,
        result = GameResult.Win(Player.Human, listOf(0, 1, 2)),
        // CORRECCIÓN: También incluimos el tablero final en el historial de esta preview.
        gameHistory = listOf(winBoard)
    )

    // El estado de ánimo es necesario para la TopAppBar, aunque solo sea un mock.
    val mockMood = Mood.NORMAL

    TicTacLearnTheme {
        GameScreenContent(
            uiState = GameUiState(
                gameState = winState,
                currentMood = mockMood,
                isProcessingMove = false
            ),
            onCellClicked = { },
            onResetGameClicked = { }
        )
    }
}