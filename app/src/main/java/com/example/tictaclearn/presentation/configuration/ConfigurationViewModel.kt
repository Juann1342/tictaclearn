package com.example.tictaclearn.presentation.configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameMode
import com.example.tictaclearn.domain.repository.AIEngineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ConfigViewModel"

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    // Inyectamos el Repositorio, que gestiona el Mood y la Memoria de la IA
    private val repository: AIEngineRepository
) : ViewModel() {

    // --- Estado de la UI ---
    // Inicialización con el estado por defecto (CLASSIC)
    private val _uiState = MutableStateFlow(ConfigurationUiState())
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    init {
        // 🚀 AL INICIAR: Cargamos el Mood guardado y sincronizamos el GameMode.
        loadSavedMood()
    }

    // --- Lógica de Carga y Sincronización ---

    private fun loadSavedMood() {
        viewModelScope.launch {
            try {
                // 1. Obtenemos el Mood guardado.
                val savedMood = repository.getDailyMood()

                // 2. Determinar el GameMode asociado al Mood cargado.
                // Si el Mood está en la lista de Moods Gomoku, entonces el modo es Gomoku.
                val initialMode = if (Mood.ALL_MOODS_GOMOKU.any { it.id == savedMood.id }) {
                    GameMode.GOMOKU
                } else {
                    // Si no es Gomoku, es Classic (esto incluye el Mood.NORMAL por defecto)
                    GameMode.CLASSIC
                }

                // 3. Determinar la lista de Moods disponibles para la UI.
                val availableMoods = if (initialMode == GameMode.GOMOKU) {
                    Mood.ALL_MOODS_GOMOKU
                } else {
                    Mood.ALL_MOODS_CLASSIC
                }

                // 4. Actualizar el estado COMPLETO de la UI en un solo bloque atómico.
                _uiState.update {
                    it.copy(
                        selectedGameMode = initialMode,    // <--- CLAVE: El botón se selecciona aquí
                        currentMood = savedMood,           // Establece el Mood correcto
                        availableMoods = availableMoods,   // Establece la lista correcta de Moods
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando el mood guardado.", e)
                // Si falla, al menos quitamos el estado de carga y usamos los defaults (Classic).
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Lógica para cambiar el Modo de Juego (Classic o Gomoku)
     */
    fun onGameModeSelected(mode: GameMode) {
        // 1. Obtener el mood por defecto para el nuevo modo
        val defaultMood = Mood.getDefaultMoodForMode(mode)

        // 2. Determinar la lista de moods disponibles
        val availableMoods = if (mode == GameMode.GOMOKU) {
            Mood.ALL_MOODS_GOMOKU
        } else {
            Mood.ALL_MOODS_CLASSIC
        }

        // 3. Actualizar el estado de la UI
        _uiState.update {
            it.copy(
                selectedGameMode = mode,
                currentMood = defaultMood,
                availableMoods = availableMoods
            )
        }

        // 4. Guardar el mood por defecto del nuevo modo para persistencia
        viewModelScope.launch {
            try {
                repository.saveDailyMood(defaultMood)
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando el mood por defecto al cambiar modo", e)
            }
        }
    }

    /**
     * Lógica para cambiar el Estado de Ánimo (Mood)
     */
    fun onMoodSelected(mood: Mood) {
        _uiState.update { it.copy(currentMood = mood) }
        viewModelScope.launch {
            try {
                // El repositorio se encarga de persistir el Mood
                repository.saveDailyMood(mood)
                Log.d(TAG, "Mood guardado: ${mood.displayName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando el mood", e)
            }
        }
    }

    /**
     * Ejecuta el borrado de la Q-Table a través del repositorio.
     */
    fun onResetMemoryClicked() {
        Log.d(TAG, "Reset memory clicked.")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.clearMemory() // Borrar memoria de Q-Learning
                _uiState.update {
                    it.copy(
                        feedbackMessage = "✅ Memoria de la IA borrada con éxito.",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing QTable", e)
                _uiState.update {
                    it.copy(
                        feedbackMessage = "❌ Error al borrar la memoria.",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun feedbackShown() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}