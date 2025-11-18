package com.example.tictaclearn.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.repository.AIEngineRepository
import com.example.tictaclearn.domain.service.TicTacToeGameService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

private const val MOOD_ID_KEY = "moodId"
private const val AI_THINKING_DELAY = 600L
private const val TAG = "GameViewModel"

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameService: TicTacToeGameService,
    private val aiEngineRepository: AIEngineRepository, // Necesario para el aprendizaje final
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        val moodId = savedStateHandle.get<String>(MOOD_ID_KEY)
        if (moodId != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isProcessingMove = true) }
                try {
                    gameService.initializeGame(moodId)
                    _uiState.update {
                        it.copy(
                            gameState = gameService.gameState,
                            currentMood = gameService.currentMood,
                            isProcessingMove = false
                        )
                    }
                    // Si empieza la IA, juega
                    if (gameService.gameState.currentPlayer == Player.AI) {
                        handleAiTurn()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error init", e)
                    _uiState.update { it.copy(isProcessingMove = false) }
                }
            }
        }
    }

    fun onCellClicked(position: Int) {
        if (_uiState.value.isProcessingMove || _uiState.value.gameState.isFinished) return

        // Bloqueo UI
        _uiState.update { it.copy(isProcessingMove = true) }

        viewModelScope.launch {
            try {
                // 1. Turno Humano
                var state = gameService.handleHumanTurn(position)
                _uiState.update { it.copy(gameState = state) }

                if (state.isFinished) {
                    // 🚨 MOMENTO CRÍTICO: El juego terminó tras movimiento humano.
                    // La IA debe aprender que su estado anterior permitió esto.
                    performLearning(state)
                    _uiState.update { it.copy(isProcessingMove = false) }
                } else {
                    // Turno IA
                    handleAiTurn()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error game loop", e)
                _uiState.update { it.copy(isProcessingMove = false) }
            }
        }
    }

    private suspend fun handleAiTurn() {
        // Ya estamos en viewModelScope y con isProcessingMove=true (o lo forzamos si viene de restart)
        _uiState.update { it.copy(isProcessingMove = true) }
        delay(AI_THINKING_DELAY)

        val state = gameService.handleAiTurn()
        _uiState.update { it.copy(gameState = state, isProcessingMove = false) }

        if (state.isFinished) {
            // La IA ganó o empató, también debe aprender de esto.
            performLearning(state)
        }
    }

    /**
     * Llama al repositorio para ejecutar el algoritmo de aprendizaje (Q-Learning)
     * sobre el historial completo de la partida recién terminada.
     */
    private fun performLearning(finalState: GameState) {
        Log.d(TAG, "Juego terminado. Iniciando aprendizaje con ${finalState.gameHistory.size} estados.")
        viewModelScope.launch {
            // Llamamos al updateMemory del Repositorio, que tiene la lógica "Visión de Futuro"
            aiEngineRepository.updateMemory(finalState.gameHistory)
        }
    }

    fun onResetGameClicked() {
        if (_uiState.value.isProcessingMove) return

        gameService.resetGame()
        _uiState.update { it.copy(gameState = gameService.gameState, isProcessingMove = false) }

        if (gameService.gameState.currentPlayer == Player.AI) {
            viewModelScope.launch { handleAiTurn() }
        }
    }
}