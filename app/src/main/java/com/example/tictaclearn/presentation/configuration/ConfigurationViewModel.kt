package com.example.tictaclearn.presentation.configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictaclearn.domain.model.Mood
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
    // 🔄 CAMBIO: Inyectamos el Repositorio en lugar del DataStore directo.
    // El repositorio es el encargado de gestionar tanto la Memoria (reset) como el Mood (guardar/cargar).
    private val repository: AIEngineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConfigurationUiState(
            availableMoods = Mood.ALL_MOODS,
            currentMood = Mood.getDefaultDailyMood(), // Valor temporal mientras carga
            isLoading = true // Empezamos cargando
        )
    )
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    init {
        // 🚀 AL INICIAR: Cargamos el Mood guardado del día anterior.
        loadSavedMood()
    }

    private fun loadSavedMood() {
        viewModelScope.launch {
            try {
                // El repositorio nos da el último mood guardado (o el default si es la primera vez)
                val savedMood = repository.getDailyMood()
                _uiState.update {
                    it.copy(
                        currentMood = savedMood,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando mood inicial", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Actualiza el estado de ánimo y LO GUARDA para el futuro.
     */
    fun onMoodSelected(mood: Mood) {
        viewModelScope.launch {
            // 1. Actualizamos la UI inmediatamente para que se sienta rápido
            _uiState.update { it.copy(currentMood = mood) }

            // 2. Guardamos la preferencia en segundo plano
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
                repository.clearMemory() // Usamos el método del repositorio
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

    data class ConfigurationUiState(
        val availableMoods: List<Mood> = emptyList(),
        val currentMood: Mood,
        val isLoading: Boolean = false,
        val feedbackMessage: String? = null
    )
}