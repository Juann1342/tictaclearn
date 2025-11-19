package com.example.tictaclearn.presentation.game.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.ui.theme.NeonCyan
import com.example.tictaclearn.ui.theme.NeonGreen
import com.example.tictaclearn.ui.theme.NeonOrange
import com.example.tictaclearn.ui.theme.NeonRed
import com.example.tictaclearn.ui.theme.TextWhite

@Composable
fun GameStatusIndicator(gameState: GameState, modifier: Modifier = Modifier) {
    val statusText = when (gameState.result) {
        GameResult.Playing -> {
            val playerText = if (gameState.currentPlayer == Player.Human) "TU TURNO (X)" else "IA PENSANDO... (O)"
            playerText
        }
        GameResult.Draw -> "¡EMPATE DE SISTEMA! 🤝"
        is GameResult.Win -> {
            val winnerName = if (gameState.result.winner == Player.Human) "¡VICTORIA HUMANA! 🎉" else "LA IA HA GANADO. ¡APRENDE! 🤖"
            winnerName
        }
    }

    // Estilos ajustados para mejor visibilidad y énfasis
    Text(
        text = statusText,
        style = MaterialTheme.typography.headlineSmall, // Tamaño grande
        fontWeight = FontWeight.Black,
        color = when (gameState.result) {
            GameResult.Playing -> if (gameState.currentPlayer == Player.Human) NeonOrange else NeonCyan // Naranja vs Cian
            GameResult.Draw -> TextWhite // Empate en blanco/gris
            is GameResult.Win -> if (gameState.result.winner == Player.Human) NeonGreen else NeonRed // Verde neón vs Rojo neón
        },
        modifier = modifier,
        letterSpacing = 1.sp
    )
}