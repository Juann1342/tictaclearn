package com.chifuz.tictaclearn.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import com.chifuz.tictaclearn.domain.service.TicTacToeGameService
import com.chifuz.tictaclearn.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ⏱️ Aquí ajustas el tiempo de espera inicial
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
        val moodId = savedStateHandle.get<String>(Screen.Game.MOOD_ID_KEY) ?: "normal"
        val gameModeId = savedStateHandle.get<String>(Screen.Game.GAME_MODE_ID_KEY) ?: "classic_3x3"

        initializeGame(moodId, gameModeId)
    }

    private fun initializeGame(moodId: String, gameModeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingMove = true) }

            // 1. Esperamos a que el servicio configure todo (suspend)
            gameService.initializeGame(moodId, gameModeId)

            // 2. Actualizamos la UI inmediatamente con el tablero VACÍO y el modo
            _uiState.update {
                it.copy(
                    gameState = gameService.gameState,
                    currentMood = gameService.currentMood,
                    currentGameMode = gameService.getCurrentGameMode(),
                    isProcessingMove = false
                )
            }

            // 3. Comprobamos: ¿Le tocó empezar a la IA?
            if (gameService.gameState.currentPlayer == com.chifuz.tictaclearn.domain.model.Player.AI) {
                // Si sí, llamamos a la función que tiene el DELAY incorporado
                handleAiTurn()
            }
        }
    }

    fun onCellClicked(position: Int) {
        val currentState = _uiState.value
        if (currentState.isProcessingMove || currentState.gameState.isFinished) return
        if (currentState.gameState.currentPlayer != com.chifuz.tictaclearn.domain.model.Player.Human) return

        // 1. Turno Humano
        val updatedState = gameService.handleHumanTurn(position)
        _uiState.update { it.copy(gameState = updatedState) }

        if (!updatedState.isFinished) {
            // 2. Turno IA
            viewModelScope.launch {
                handleAiTurn()
            }
        } else {
            performLearning(updatedState)
        }
    }

    private suspend fun handleAiTurn() {
        _uiState.update { it.copy(isProcessingMove = true) }

        // ⏳ EL DELAY MÁGICO: Aquí la IA "piensa" antes de poner la ficha
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

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingMove = true) }

            // 1. Reiniciamos lógica (decide quién empieza pero no mueve)
            gameService.resetGame()

            // 2. Mostramos tablero vacío
            _uiState.update {
                it.copy(
                    gameState = gameService.gameState,
                    isProcessingMove = false
                )
            }

            // 3. Si la suerte eligió a la IA, activamos su turno con delay
            if (gameService.gameState.currentPlayer == com.chifuz.tictaclearn.domain.model.Player.AI) {
                handleAiTurn()
            }
        }
    }
}