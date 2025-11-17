package com.example.tictaclearn.presentation.configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.domain.model.Mood
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
    private val memoryManager: AiMemoryDataStoreManager
) : ViewModel() {private val _uiState = MutableStateFlow(
    ConfigurationUiState(
        availableMoods = Mood.ALL_MOODS,
        currentMood = Mood.getDefaultDailyMood()
    )
)
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()
    fun onMoodSelected(mood: Mood) {
        _uiState.update { it.copy(currentMood = mood) }
    }

    fun onResetMemoryClicked() {
        Log.d(TAG, "Reset memory clicked. Clearing QTable...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                memoryManager.clearQTable()
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
        val currentMood: Mood,              // <-- Propiedad requerida por la UI
        val isLoading: Boolean = false,     // <-- Propiedad requerida por la UI
        val feedbackMessage: String? = null // <-- Propiedad requerida por la UI
    )
}