package com.chifuz.tictaclearn.presentation.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.chifuz.tictaclearn.ui.theme.NeonCyan
import com.chifuz.tictaclearn.ui.theme.NeonRed
import androidx.compose.ui.unit.sp
import com.chifuz.tictaclearn.ui.theme.TextWhite
import androidx.compose.ui.unit.dp
import com.chifuz.tictaclearn.domain.model.GameMode
import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Player
import com.chifuz.tictaclearn.ui.theme.NeonPurple
import com.chifuz.tictaclearn.ui.theme.NeonYellow
import com.chifuz.tictaclearn.ui.theme.NeonOrange

@Composable
fun GameStatusIndicator(
    gameState: GameState,
    isAiThinking: Boolean, // Estado de procesamiento de la IA
    currentGameMode: GameMode, // Modo de juego actual
    modifier: Modifier = Modifier
) {
    val statusText = when (gameState.result) {
        GameResult.Playing -> {
            // 1. Prioridad: ¿Está la IA pensando? (Aplica a todos los modos con IA)
            if (gameState.currentPlayer == Player.AI && isAiThinking) {
                "IA PENSANDO..."
            } else {
                // 2. Si no está pensando, definimos el texto según el modo de juego
                when (currentGameMode) {
                    GameMode.CLASSIC, GameMode.GOMOKU -> {
                        // Modo Player vs AI (PvE): Usar "Humano" o "IA"
                        when (gameState.currentPlayer) {
                            Player.Human -> "Turno de Humano" // 'X'
                            Player.AI -> "Turno de IA"       // 'O' (Esperando handleAiTurn)
                            // Otros jugadores no deberían aparecer aquí.
                            else -> "Turno de ${gameState.currentPlayer.symbol}"
                        }
                    }
                    GameMode.PARTY -> {
                        // Modo Party (PvP o PvPvAI): Usar el símbolo del jugador (X, O, △, ☆)
                        "Turno de ${gameState.currentPlayer.symbol}"
                    }

                    else -> {"Cargando..."}
                }
            }
        }
        GameResult.Draw -> "¡EMPATE!"
        is GameResult.Win -> {
            val winnerSymbol = gameState.result.winner.symbol
            "¡${winnerSymbol} GANA LA PARTIDA!"
        }
    }

    val textColor = when (gameState.currentPlayer) {
        Player.Human -> NeonOrange
        Player.AI -> NeonCyan
        Player.Triangle -> NeonPurple
        Player.Star -> NeonYellow
    }

    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            ),
            color = when (gameState.result) {
                GameResult.Playing -> textColor
                GameResult.Draw -> TextWhite
                is GameResult.Win -> {
                    when (gameState.result.winner) {
                        Player.Human -> NeonOrange
                        Player.AI -> NeonCyan
                        Player.Triangle -> NeonPurple
                        Player.Star -> NeonYellow
                    }
                }
            }
        )

        // Sub-texto para mostrar el símbolo (X, O, △, ☆)
        if (gameState.result == GameResult.Playing) {
            val (markerText, markerColor) = when (gameState.currentPlayer) {
                Player.Human -> "(${Player.Human.symbol})" to NeonOrange
                Player.AI -> "(${Player.AI.symbol})" to NeonCyan
                Player.Triangle -> "(${Player.Triangle.symbol})" to NeonPurple
                Player.Star -> "(${Player.Star.symbol})" to NeonYellow
            }

            Text(
                text = markerText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = markerColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (gameState.result is GameResult.Win && gameState.result.winner == Player.AI) {
            // Mensaje de aprendizaje solo si la IA gana
            Text(
                text = "¡APRENDE DE TU RIVAL!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = NeonRed.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}