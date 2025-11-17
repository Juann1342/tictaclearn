package com.example.tictaclearn.presentation.configuration

import com.example.tictaclearn.domain.model.Mood

// presentation/configuration/ConfigurationUiState.kt

data class ConfigurationUiState(
    // 1. Estado de Carga
    val isLoading: Boolean = true, // Es 'true' al inicio, mientras esperamos que cargue el Mood diario.

    // 2. Datos Principales
    val currentMood: Mood = Mood.NORMAL, // El Mood que se está mostrando/aplicando actualmente.

    // 3. Control de Eventos
    // Un mensaje que se activa después de una acción (ej. "Memoria borrada con éxito").
    val feedbackMessage: String? = null,

    // 4. Elementos de UI
    // Lista de todos los Moods disponibles para el selector
    val availableMoods: List<Mood> = Mood.ALL_MOODS
)