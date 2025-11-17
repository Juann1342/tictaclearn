package com.example.tictaclearn.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.usecase.ProcessMoveUseCase
import com.example.tictaclearn.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import java.lang.IllegalArgumentException
import kotlinx.coroutines.launch

@HiltViewModel
class GameViewModel @Inject constructor(
    private val processMoveUseCase: ProcessMoveUseCase,
    // 💡 Hilt inyecta SavedStateHandle para acceder a los argumentos de navegación
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 1. Obtención del ID del Mood de la navegación
    private val moodId: String = savedStateHandle[Screen.Game.MOOD_ID_KEY]
        ?: throw IllegalArgumentException("Mood ID missing from navigation arguments")

    // 2. Estado Observable
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Inicialización: Carga el Mood de la IA
    init {
        loadGameSettings()
    }

    /**
     * Carga el objeto Mood completo basándose en el moodId obtenido de la navegación.
     */
    private fun loadGameSettings() {
        // Marcamos isProcessingMove como true mientras buscamos el Mood
        _uiState.update { it.copy(isProcessingMove = true) }

        // ✅ CORRECCIÓN: Usar Mood.Companion para acceder a los miembros del companion object
        val mood = Mood.Companion.ALL_MOODS.find { it.id == moodId }
            ?: Mood.Companion.getDefaultDailyMood()

        _uiState.update {
            it.copy(
                currentMood = mood,
                // El juego está listo para empezar
                isProcessingMove = false
            )
        }
    }

    /**
     * Evento: Se hace click en una celda del tablero.
     * @param row La fila clicada.
     * @param col La columna clicada.
     */
    fun onCellClicked(row: Int, col: Int) {
        val currentState = _uiState.value

        // **GUARDRAILS (Protecciones):** Solo procesamos el movimiento si:
        // 1. El juego está en curso.
        // 2. No hay otro movimiento (de la IA) en proceso.
        // 3. Es el turno del jugador humano.
        if (currentState.isProcessingMove || currentState.gameState.result != GameResult.Playing || currentState.gameState.currentPlayer != Player.HUMAN) {
            return
        }

        // 1. Marcamos el inicio del proceso para deshabilitar la UI
        _uiState.update { it.copy(isProcessingMove = true) }

        viewModelScope.launch {
            try {
                // 2. Ejecutamos la lógica central del juego
                val nextState = processMoveUseCase(
                    currentState = currentState.gameState,
                    row = row,
                    col = col,
                    currentMood = currentState.currentMood!!
                )

                // 3. Actualizamos el estado de la UI con el nuevo estado del juego
                _uiState.update { it.copy(gameState = nextState) }

            } catch (e: Exception) {
                // Manejo de errores
                _uiState.update { it.copy(errorMessage = "Error en la partida: ${e.message}") }
            } finally {
                // 4. Finalizamos el proceso. La UI vuelve a estar lista para el próximo click
                _uiState.update {
                    it.copy(
                        isProcessingMove = false
                    )
                }
            }
        }
    }
}