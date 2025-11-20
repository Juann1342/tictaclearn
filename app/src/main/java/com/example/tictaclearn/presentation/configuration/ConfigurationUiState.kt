package com.example.tictaclearn.presentation.configuration

import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameMode // Importación necesaria

data class ConfigurationUiState(
    // 1. Estado de Carga
    val isLoading: Boolean = true, // Es 'true' al inicio, mientras esperamos que cargue el Mood diario.

    // 2. Datos Principales
    val currentMood: Mood = Mood.getDefaultDailyMood(), // El Mood que se está mostrando/aplicando actualmente.
    val selectedGameMode: GameMode = GameMode.CLASSIC, // El modo de juego seleccionado

    // 3. Control de Eventos
    val feedbackMessage: String? = null,

    // 4. Elementos de UI
    // Lista de todos los Moods disponibles para el selector (cambia con el modo de juego)
    val availableMoods: List<Mood> = Mood.ALL_MOODS_CLASSIC,
    // Lista de todos los modos de juego disponibles
    val availableGameModes: List<GameMode> = GameMode.ALL_MODES
)