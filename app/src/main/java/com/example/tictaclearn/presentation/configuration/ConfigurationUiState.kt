// presentation/configuration/ConfigurationUiState.kt
package com.example.tictaclearn.presentation.configuration

import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameMode

data class ConfigurationUiState(
    // 1. Estado de Carga
    val isLoading: Boolean = true,

    // 2. Datos Principales
    val currentMood: Mood = Mood.getDefaultDailyMood(),
    val selectedGameMode: GameMode = GameMode.CLASSIC,

    // 3. Control de Eventos
    val feedbackMessage: String? = null,

    // 4. Elementos de UI
    val availableMoods: List<Mood> = Mood.ALL_MOODS_CLASSIC,
    val availableGameModes: List<GameMode> = GameMode.ALL_MODES,

    // 🚀 NUEVO: Contador de partidas jugadas para el modo Classic.
    val classicGamesPlayedCount: Int = 0
) {
    companion object {
        // 🚀 NUEVO: Constante para el entrenamiento 'ficticio' (50 partidas = 100%)
        const val MAX_TRAINING_GAMES = 100
    }
}