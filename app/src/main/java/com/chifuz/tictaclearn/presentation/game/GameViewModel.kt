package com.chifuz.tictaclearn.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chifuz.tictaclearn.domain.model.GameMode
import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Player
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import com.chifuz.tictaclearn.domain.service.TicTacToeGameService
import com.chifuz.tictaclearn.presentation.navigation.Screen
import com.chifuz.tictaclearn.presentation.util.SoundManager
import com.chifuz.tictaclearn.presentation.util.SoundType
import com.chifuz.tictaclearn.presentation.util.VibrationManager
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
    private val soundManager: SoundManager,         // 🚀 Nuevo
    private val vibrationManager: VibrationManager, // 🚀 Nuevo
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        val moodId = savedStateHandle.get<String>(Screen.Game.MOOD_ID_KEY) ?: ""
        val gameModeId = savedStateHandle.get<String>(Screen.Game.GAME_MODE_ID_KEY) ?: GameMode.CLASSIC.id
        startGame(moodId, gameModeId)
    }

    private fun startGame(moodId: String, gameModeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            gameService.initializeGame(moodId, gameModeId)
            _uiState.update {
                it.copy(
                    gameState = gameService.gameState,
                    currentMood = gameService.currentMood,
                    currentGameMode = gameService.getCurrentGameMode(),
                    isLoading = false
                )
            }

            val shouldAiMove = gameService.getCurrentGameMode() != GameMode.PARTY || gameService.isPartyModeAiEnabled
            if (shouldAiMove && gameService.gameState.currentPlayer == Player.AI) {
                handleAiTurn()
            }
        }
    }

    fun onCellClicked(position: Int) {
        if (_uiState.value.isProcessingMove) return

        // 🚀 FEEDBACK INMEDIATO
        if (gameService.gameState.board.isPositionAvailable(position)) {
            soundManager.play(SoundType.PLACE)
            vibrationManager.vibrateMove()
        }

        viewModelScope.launch {
            // 1. Mover el humano
            val updatedState = gameService.handleHumanTurn(position)

            _uiState.update { it.copy(gameState = updatedState) }

            // 🚀 Efectos Fin de Juego (si ganó el humano)
            checkGameFinishedEffects(updatedState)

            // 2. Comprobar si es necesario llamar a la IA
            if (updatedState.result == GameResult.Playing) {
                val shouldAiMove = gameService.getCurrentGameMode() != GameMode.PARTY || gameService.isPartyModeAiEnabled

                if (shouldAiMove && updatedState.currentPlayer == Player.AI) {
                    handleAiTurn()
                } else if (gameService.getCurrentGameMode() == GameMode.PARTY && updatedState.currentPlayer != Player.AI) {
                    // Modo Party (PvP): Siguiente jugador espera click
                }

            } else {
                performLearning(updatedState)
            }
        }
    }

    private suspend fun handleAiTurn() {
        _uiState.update { it.copy(isProcessingMove = true) }

        try {
            delay(AI_THINKING_DELAY)

            val state = gameService.handleAiTurn()

            // 🚀 FEEDBACK IA
            if (state.board != _uiState.value.gameState.board) {
                soundManager.play(SoundType.PLACE)
            }

            _uiState.update { it.copy(gameState = state) }

            // 🚀 Efectos Fin de Juego (si ganó la IA)
            checkGameFinishedEffects(state)

            if (state.isFinished) {
                performLearning(state)
            }
        } catch (e: Exception) {
            // Error silencioso para no crashear
        } finally {
            _uiState.update { it.copy(isProcessingMove = false) }
        }
    }

    // 🚀 Lógica de efectos de victoria/derrota
    private fun checkGameFinishedEffects(state: GameState) {
        if (state.result is GameResult.Win) {
            if (state.result.winner == Player.Human) {
                soundManager.play(SoundType.WIN)
                vibrationManager.vibrateWin()
            } else {
                soundManager.play(SoundType.LOSE)
                vibrationManager.vibrateLose()
            }
        }
    }

    private fun performLearning(finalState: GameState) {
        viewModelScope.launch {
            aiEngineRepository.updateMemory(finalState.gameHistory)
        }
    }

    fun onResetGameClicked() {
        onUiClick() // <--- AGREGAR ESTO        if (_uiState.value.isProcessingMove) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingMove = true) }

            gameService.resetGame()

            _uiState.update {
                it.copy(
                    gameState = gameService.gameState,
                    isProcessingMove = false
                )
            }

            val shouldAiMove = gameService.getCurrentGameMode() != GameMode.PARTY || gameService.isPartyModeAiEnabled
            if (shouldAiMove && gameService.gameState.currentPlayer == Player.AI) {
                handleAiTurn()
            }
        }
    }

    fun onGameFinished() {
        soundManager.play(SoundType.CLICK) // 🚀 Sonido UI al salir
    }

    fun getPlayerMarker(): String {
        return "Turno de ${uiState.value.gameState.currentPlayer.symbol}"
    }

    fun onUiClick() {
        soundManager.play(SoundType.CLICK)
        vibrationManager.vibrateClick() // Asegúrate de tener este método en VibrationManager
    }
}