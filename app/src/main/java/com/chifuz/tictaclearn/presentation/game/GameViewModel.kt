package com.chifuz.tictaclearn.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import com.chifuz.tictaclearn.domain.service.TicTacToeGameService
import com.chifuz.tictaclearn.presentation.navigation.Screen // Asegúrate de importar Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val AI_THINKING_DELAY = 600L
private const val TAG = "GameViewModel"

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameService: TicTacToeGameService,
    private val aiEngineRepository: AIEngineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // ✅ CORRECCIÓN: Recuperamos AMBOS argumentos
        val moodId = savedStateHandle.get<String>(Screen.Game.MOOD_ID_KEY) ?: "normal"
        val gameModeId = savedStateHandle.get<String>(Screen.Game.GAME_MODE_ID_KEY) ?: "classic_3x3"

        initializeGame(moodId, gameModeId)
    }

    private fun initializeGame(moodId: String, gameModeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingMove = true) }

            // ✅ CORRECCIÓN: Pasamos ambos IDs al servicio
            gameService.initializeGame(moodId, gameModeId)

            _uiState.update {
                it.copy(
                    gameState = gameService.gameState,
                    currentMood = gameService.currentMood,
                    currentGameMode = gameService.getCurrentGameMode(), // Actualizamos el modo en la UI
                    isProcessingMove = false
                )
            }
        }
    }

    fun onCellClicked(position: Int) {
        val currentState = _uiState.value
        if (currentState.isProcessingMove || currentState.gameState.isFinished) return

        // 1. Turno Humano
        val updatedState = gameService.handleHumanTurn(position)
        _uiState.update { it.copy(gameState = updatedState) }

        if (!updatedState.isFinished) {
            // 2. Turno IA (si el juego sigue)
            viewModelScope.launch {
                handleAiTurn()
            }
        } else {
            performLearning(updatedState)
        }
    }

    private suspend fun handleAiTurn() {
        _uiState.update { it.copy(isProcessingMove = true) }
        delay(AI_THINKING_DELAY)

        val state = gameService.handleAiTurn()
        _uiState.update { it.copy(gameState = state, isProcessingMove = false) }

        if (state.isFinished) {
            performLearning(state)
        }
    }

    private fun performLearning(finalState: GameState) {
        viewModelScope.launch {
            aiEngineRepository.updateMemory(finalState.gameHistory)
        }
    }

    fun onResetGameClicked() {
        if (_uiState.value.isProcessingMove) return

        gameService.resetGame()
        _uiState.update {
            it.copy(
                gameState = gameService.gameState,
                isProcessingMove = false
            )
        }
    }
}