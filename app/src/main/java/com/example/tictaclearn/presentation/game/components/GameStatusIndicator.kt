package com.example.tictaclearn.presentation.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.ui.theme.NeonCyan
import com.example.tictaclearn.ui.theme.NeonGreen
import com.example.tictaclearn.ui.theme.NeonOrange
import com.example.tictaclearn.ui.theme.NeonRed
import androidx.compose.ui.unit.sp
import com.example.tictaclearn.ui.theme.TextWhite
import androidx.compose.ui.unit.dp

@Composable
fun GameStatusIndicator(gameState: GameState, modifier: Modifier = Modifier) {
    val statusText = when (gameState.result) {
        GameResult.Playing -> {
            val playerText = if (gameState.currentPlayer == Player.Human) "TU TURNO" else "IA PENSANDO..."
            playerText
        }
        GameResult.Draw -> "¡EMPATE!"
        is GameResult.Win -> {
            val winnerName = if (gameState.result.winner == Player.Human) "¡VICTORIA HUMANA!" else "LA IA HA GANADO."
            winnerName
        }
    }

    // 🚨 CAMBIO UI: Estilo principal para el mensaje de estado
    val baseStyle = MaterialTheme.typography.headlineSmall.copy(
        fontSize = 22.sp, // Ligeramente más grande
        letterSpacing = 2.sp,
        fontWeight = FontWeight.Black
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = statusText,
            style = baseStyle,
            color = when (gameState.result) {
                GameResult.Playing -> if (gameState.currentPlayer == Player.Human) NeonOrange else NeonCyan
                GameResult.Draw -> TextWhite
                is GameResult.Win -> if (gameState.result.winner == Player.Human) NeonGreen else NeonRed
            }
        )

        // Sub-texto de turno o mensaje de APRENDE
        if (gameState.result == GameResult.Playing) {
            val markerText = if (gameState.currentPlayer == Player.Human) "(X)" else "(O)"
            Text(
                text = markerText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (gameState.currentPlayer == Player.Human) NeonOrange else NeonCyan,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (gameState.result is GameResult.Win && gameState.result.winner == Player.AI) {
            // Mensaje de aprendizaje solo si la IA gana
            Text(
                text = "¡APRENDE DE TU RIVAL!", // Mensaje más impactante
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