package com.example.tictaclearn.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.presentation.game.components.GameTopBar
import com.example.tictaclearn.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    moodId: String,
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameScreenContent(
        uiState = uiState,
        onCellClicked = viewModel::onCellClicked,
        onResetGameClicked = viewModel::onResetGameClicked,
        onFinishGameClicked = onGameFinished
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onCellClicked: (Int) -> Unit,
    onResetGameClicked: () -> Unit,
    onFinishGameClicked: () -> Unit
) {
    Scaffold(
        topBar = { GameTopBar(currentMood = uiState.currentMood) },
        containerColor = MaterialTheme.colorScheme.background,
        // ELIMINADO: bottomBar = { ... } para evitar saltos de layout
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 1. HEADER STATUS (Marcador / Estado)
            GameStatusCard(uiState.gameState)

            // 2. BOARD (Tablero centrado)
            Box(
                modifier = Modifier
                    .weight(1f) // Ocupa todo el espacio vertical disponible
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BoardGrid(
                    board = uiState.gameState.board,
                    isInteractive = !uiState.gameState.isFinished &&
                            uiState.gameState.currentPlayer == Player.Human &&
                            !uiState.isProcessingMove,
                    onCellClicked = onCellClicked
                )
            }

            // 3. CONTROLES (Zona Estabilizada anti-saltos)
            // Usamos Box con altura fija (100.dp) para reservar el espacio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp), // Altura fija generosa
                contentAlignment = Alignment.BottomCenter // Alineado abajo
            ) {
                // A. BARRA DE PROGRESO (Flotando arriba de la caja)
                if (uiState.isProcessingMove) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter), // Se pega al tope de esta caja
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                }

                // B. CONTENIDO (Botones o Texto)
                if (uiState.gameState.isFinished) {
                    // --- BOTONES DE FIN DE PARTIDA ---
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp) // Margen para separarse de la barra si estuviera
                    ) {
                        OutlinedButton(
                            onClick = onFinishGameClicked,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                        ) {
                            Text("SALIR")
                        }

                        Button(
                            onClick = onResetGameClicked,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("REVANCHA", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // --- TEXTO DE TURNO ---
                    // Usamos 'alpha' (transparencia) en lugar de un 'if'.
                    // El texto ocupa espacio físico siempre, evitando saltos.
                    val textAlpha = if (uiState.isProcessingMove) 0f else 1f

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tu turno: Toca una casilla",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray.copy(alpha = textAlpha)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameStatusCard(gameState: GameState) {
    val (text, color) = when (val result = gameState.result) {
        GameResult.Draw -> "EMPATE DE SISTEMA" to TextGray
        GameResult.Playing -> {
            if(gameState.currentPlayer == Player.Human) "TU TURNO" to NeonOrange
            else "IA PENSANDO..." to NeonCyan
        }
        is GameResult.Win -> {
            if(result.winner == Player.Human) "¡VICTORIA HUMANA!" to NeonGreen
            else "IA DOMINA" to NeonRed
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun BoardGrid(
    board: Board,
    isInteractive: Boolean,
    onCellClicked: (Int) -> Unit
) {
    // Un contenedor con fondo para agrupar el tablero visualmente
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(12.dp)
                .aspectRatio(1f), // Mantiene el tablero cuadrado
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
}

@Composable
fun BoardCell(
    symbol: Char,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    val cellColor = MaterialTheme.colorScheme.background // Fondo oscuro para la celda

    // Selección de Icono (MANTENIENDO TUS ICONOS ACTUALES)
    val (icon, tint) = when (symbol) {
        'X' -> Icons.Rounded.Close to NeonOrange
        'O' -> Icons.Rounded.FavoriteBorder to NeonCyan
        else -> null to Color.Transparent
    }

    Card(
        onClick = onClick,
        enabled = isEnabled || symbol != ' ',
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = cellColor,
            disabledContainerColor = cellColor
        ),
        modifier = Modifier.aspectRatio(1f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.fillMaxSize(0.6f) // Icono al 60% de la celda
                )
            }
        }
    }
}