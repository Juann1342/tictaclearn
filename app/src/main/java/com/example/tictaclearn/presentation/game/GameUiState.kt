package com.example.tictaclearn.presentation.game

// content of presentation/game/GameUiState.kt


import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Mood

data class GameUiState(
    val gameState: GameState = GameState.initial(),
    val currentMood: Mood? = null,
    val isProcessingMove: Boolean = true,
    val errorMessage: String? = null
)