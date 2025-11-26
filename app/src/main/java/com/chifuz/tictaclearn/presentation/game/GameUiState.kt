package com.chifuz.tictaclearn.presentation.game

import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.GameMode

data class GameUiState(
    val gameState: GameState = GameState.initial(), // Estado inicial por defecto
    val currentMood: Mood? = null,
    // ✅ CORRECCIÓN: Añadimos el modo de juego para que la UI sepa el tamaño del grid
    val currentGameMode: GameMode = GameMode.CLASSIC,
    val isProcessingMove: Boolean = false,
    val errorMessage: String? = null
)