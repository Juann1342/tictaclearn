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
    private val repository: AIEngineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigurationUiState())
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    init {
        // Al inicio, cargamos los datos
        loadConfigData()
    }

    // --- Lógica de Carga y Sincronización ---

    /**
     * 🚀 FUNCIÓN CRÍTICA: Carga todos los datos de configuración (Mood y Contador de partidas).
     * Ahora es pública para que la UI la llame cuando la pantalla reaparece.
     */
    fun loadConfigData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Obtenemos el Mood guardado.
                val savedMood = repository.getDailyMood()

                // 2. 🚀 OBTENEMOS EL CONTADOR DE PARTIDAS, forzando la recarga desde el repositorio
                val gamesPlayedCount = repository.getClassicGamesPlayedCount()

                // 3. Determinar el GameMode asociado al Mood cargado.
                val initialMode = if (Mood.ALL_MOODS_GOMOKU.any { it.id == savedMood.id }) {
                    GameMode.GOMOKU
                } else {
                    GameMode.CLASSIC
                }

                // 4. Determinar la lista de Moods disponibles.
                val availableMoods = if (initialMode == GameMode.GOMOKU) {
                    Mood.ALL_MOODS_GOMOKU
                } else {
                    Mood.ALL_MOODS_CLASSIC
                }

                // 5. Actualizar el estado COMPLETO de la UI.
                _uiState.update {
                    it.copy(
                        selectedGameMode = initialMode,
                        currentMood = savedMood,
                        availableMoods = availableMoods,
                        classicGamesPlayedCount = gamesPlayedCount, // <-- ¡ASIGNACIÓN CORRECTA!
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando la configuración.", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    /**
     * Lógica para cambiar el Modo de Juego (Classic o Gomoku)
     */
    fun onGameModeSelected(mode: GameMode) {
        val defaultMood = Mood.getDefaultMoodForMode(mode)

        val availableMoods = if (mode == GameMode.GOMOKU) {
            Mood.ALL_MOODS_GOMOKU
        } else {
            Mood.ALL_MOODS_CLASSIC
        }

        _uiState.update {
            it.copy(
                selectedGameMode = mode,
                currentMood = defaultMood,
                availableMoods = availableMoods
            )
        }

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
                        classicGamesPlayedCount = 0, // 🚀 RESET DE LA UI
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