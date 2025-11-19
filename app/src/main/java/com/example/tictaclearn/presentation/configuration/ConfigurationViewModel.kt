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

    private val _uiState = MutableStateFlow(
        ConfigurationUiState(
            availableMoods = Mood.ALL_MOODS_CLASSIC,
            currentMood = Mood.getDefaultDailyMood(),
            isLoading = true,
            selectedGameMode = GameMode.CLASSIC
        )
    )
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    init {
        loadSavedMood()
    }

    private fun loadSavedMood() {
        viewModelScope.launch {
            try {
                val savedMood = repository.getDailyMood()
                _uiState.update {
                    it.copy(
                        currentMood = savedMood,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading mood", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onGameModeSelected(mode: GameMode) {
        // ✅ CORRECCIÓN: Usamos Mood.getDefaultMoodForMode que añadimos en el paso anterior
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
    }

    fun onMoodSelected(mood: Mood) {
        _uiState.update { it.copy(currentMood = mood) }
        viewModelScope.launch {
            try {
                repository.saveDailyMood(mood)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving mood", e)
            }
        }
    }

    fun onResetMemoryClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.clearMemory()
                _uiState.update {
                    it.copy(feedbackMessage = "✅ Memoria borrada.", isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(feedbackMessage = "❌ Error al borrar.", isLoading = false)
                }
            }
        }
    }

    fun feedbackShown() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}