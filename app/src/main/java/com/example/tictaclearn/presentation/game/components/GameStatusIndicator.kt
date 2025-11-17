package com.example.tictaclearn.presentation.game.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player

@Composable
fun GameStatusIndicator(gameState: GameState) {
    val statusText = when (gameState.result) {
        GameResult.Playing -> "Turno de ${if (gameState.currentPlayer == Player.HUMAN) "Humano (O)" else "IA (X)"}"
        GameResult.Draw -> "¡EMPATE!"
        is GameResult.Win -> {
            val winnerName = if (gameState.result.winner == Player.HUMAN) "¡GANASTE!" else "La IA ganó... ¡aprende!"
            winnerName
        }
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.headlineMedium,
        color = if (gameState.result is GameResult.Win) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
    )
}