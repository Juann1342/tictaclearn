package com.example.tictaclearn.presentation.game.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player

@Composable
fun GameStatusIndicator(gameState: GameState, modifier: Modifier = Modifier) {
    val statusText = when (gameState.result) {
        GameResult.Playing -> {
            // ✅ CORREGIDO: Usamos Player.Human en lugar de Player.HUMAN
            val playerText = if (gameState.currentPlayer == Player.Human) "Humano (X)" else "IA (O)"
            "Turno de $playerText"
        }
        GameResult.Draw -> "¡EMPATE! 🤝"
        is GameResult.Win -> {
            // ✅ CORREGIDO: Usamos Player.Human en lugar de Player.HUMAN
            val winnerName = if (gameState.result.winner == Player.Human) "¡GANASTE! 🎉" else "La IA ganó... ¡aprende! 🤖"
            winnerName
        }
    }

    // Estilos ajustados para mejor visibilidad y énfasis
    Text(
        text = statusText,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = when (gameState.result) {
            GameResult.Playing -> MaterialTheme.colorScheme.primary
            GameResult.Draw -> MaterialTheme.colorScheme.secondary
            is GameResult.Win -> if (gameState.result.winner == Player.Human) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        },
        modifier = modifier
    )
}