package com.chifuz.tictaclearn.presentation.configuration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chifuz.tictaclearn.R
import com.chifuz.tictaclearn.data.datastore.MoodDataStoreManager
import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.GameMode
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import com.chifuz.tictaclearn.presentation.util.SoundManager
import com.chifuz.tictaclearn.presentation.util.SoundType
import com.chifuz.tictaclearn.presentation.util.VibrationManager // 🚨 Import necesario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ConfigViewModel"

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val repository: AIEngineRepository,
    private val moodDataStoreManager: MoodDataStoreManager,
    private val soundManager: SoundManager,
    private val vibrationManager: VibrationManager // 🚨 Inyección del VibrationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigurationUiState())
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    // 🚀 Flujos para la configuración
    val isSoundEnabled = moodDataStoreManager.isSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isVibrationEnabled = moodDataStoreManager.isVibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        loadConfigData()
    }

    fun loadConfigData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val savedMood = repository.getDailyMood()
                val gamesPlayedCount = repository.getClassicGamesPlayedCount()

                val initialMode = if (Mood.ALL_MOODS_GOMOKU.any { it.id == savedMood.id }) {
                    GameMode.GOMOKU
                } else {
                    GameMode.CLASSIC
                }

                val availableMoods = if (initialMode == GameMode.GOMOKU) {
                    Mood.ALL_MOODS_GOMOKU
                } else {
                    Mood.ALL_MOODS_CLASSIC
                }

                _uiState.update {
                    it.copy(
                        selectedGameMode = initialMode,
                        currentMood = savedMood,
                        availableMoods = availableMoods,
                        classicGamesPlayedCount = gamesPlayedCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando la configuración.", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // 🚀 Funciones para el Diálogo de Ajustes
    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { moodDataStoreManager.setSoundEnabled(enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch { moodDataStoreManager.setVibrationEnabled(enabled) }
    }

    // 🚨 Función corregida para incluir vibración
    fun onUiClick() {
        soundManager.play(SoundType.CLICK)
        vibrationManager.vibrateClick()
    }

    // --- Lógica de Negocio Original ---

    fun onGameModeSelected(mode: GameMode) {
        onUiClick() // Sonido y Vibración
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

    fun onMoodSelected(mood: Mood) {
        onUiClick() // Sonido y Vibración
        _uiState.update { it.copy(currentMood = mood) }
        viewModelScope.launch {
            try {
                repository.saveDailyMood(mood)
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando el mood", e)
            }
        }
    }

    fun onResetMemoryClicked() {
        onUiClick() // Sonido y Vibración
        Log.d(TAG, "Reset memory clicked.")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.clearMemory()
                _uiState.update {
                    it.copy(
                        feedbackMessage = R.string.feedback_memory_reset_success,
                        classicGamesPlayedCount = 0,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing QTable", e)
                _uiState.update {
                    it.copy(
                        feedbackMessage = R.string.feedback_memory_reset_error,
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