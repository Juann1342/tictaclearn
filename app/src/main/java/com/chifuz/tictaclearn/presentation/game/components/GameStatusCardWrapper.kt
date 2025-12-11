package com.chifuz.tictaclearn.presentation.game.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.ui.theme.SurfaceDark
import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.domain.model.Player
import com.chifuz.tictaclearn.ui.theme.NeonGreen
import com.chifuz.tictaclearn.ui.theme.NeonRed
import com.chifuz.tictaclearn.ui.theme.SurfaceLight
import com.chifuz.tictaclearn.domain.model.GameMode // Importación necesaria

@Composable
fun GameStatusCardWrapper(
    gameState: GameState,
    isAiThinking: Boolean,
    currentGameMode: GameMode // Parámetro del modo de juego
) {
    // Lógica de colores del card...
    val cardColor = when (gameState.result) {
        GameResult.Playing -> SurfaceDark
        GameResult.Draw -> SurfaceDark
        is GameResult.Win -> if (gameState.result.winner == Player.Human) NeonGreen.copy(alpha = 0.1f) else NeonRed.copy(alpha = 0.1f)
    }
    val borderColor = when (gameState.result) {
        GameResult.Playing -> SurfaceLight.copy(alpha = 0.3f)
        GameResult.Draw -> SurfaceLight
        is GameResult.Win -> if (gameState.result.winner == Player.Human) NeonGreen.copy(alpha = 0.5f) else NeonRed.copy(alpha = 0.5f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pasar todos los parámetros requeridos al indicador
            GameStatusIndicator(
                gameState = gameState,
                isAiThinking = isAiThinking,
                currentGameMode = currentGameMode // Pasando el modo
            )
        }
    }
}