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
                    currentMood = gameService.currentMood, // Agregar currentMood
                    currentGameMode = gameService.getCurrentGameMode(), //  Modo de juego
                    isLoading = false
                )
            }

            // 3. Si la IA empieza, activamos su turno (solo en modos no-Party o si Party tiene IA)
            val shouldAiMove = gameService.getCurrentGameMode() != GameMode.PARTY || gameService.isPartyModeAiEnabled
            if (shouldAiMove && gameService.gameState.currentPlayer == Player.AI) {
                handleAiTurn()
            }
        }
    }

    fun onCellClicked(position: Int) {
        if (_uiState.value.isProcessingMove) return

        viewModelScope.launch {
            // 1. Mover el humano (o cualquier jugador que toque)
            val updatedState = gameService.handleHumanTurn(position)

            // Actualizar el estado (esto refresca la UI)
            _uiState.update { it.copy(gameState = updatedState) }

            // 2. Comprobar si es necesario llamar a la IA
            if (updatedState.result == GameResult.Playing) {
                // Esta lógica verifica si la IA está activa en el modo de juego actual.
                val shouldAiMove = gameService.getCurrentGameMode() != GameMode.PARTY || gameService.isPartyModeAiEnabled

                if (shouldAiMove && updatedState.currentPlayer == Player.AI) {
                    // Si la IA juega, bloqueamos la UI y le damos el turno a la IA.
                    handleAiTurn()
                } else if (gameService.getCurrentGameMode() == GameMode.PARTY && updatedState.currentPlayer != Player.AI) {
                    // Modo Party (PvP o PvPvAI): Si el turno NO es de la IA, el siguiente jugador es humano,
                    // y el juego espera el siguiente clic.
                }

            } else {
                performLearning(updatedState)
            }
        }
    }

    private suspend fun handleAiTurn() {
        // Bloqueamos la UI al inicio.
        _uiState.update { it.copy(isProcessingMove = true) }

        try {
            // ⏳ EL DELAY MÁGICO: Aquí la IA "piensa" antes de poner la ficha
            delay(AI_THINKING_DELAY)

            val state = gameService.handleAiTurn()

            // 1. Actualizamos el estado del juego (la IA movió)
            _uiState.update { it.copy(gameState = state) }

            if (state.isFinished) {
                performLearning(state)
            }
        } catch (e: Exception) {
            // Manejo de errores: Si la IA falla, simplemente se desbloquea la UI.
            // El turno se quedará en Player.AI, forzando un reintento si el usuario toca.
            // Para evitar que el juego se rompa por un error de AI.
        } finally {
            // 2. Desbloqueamos UI SIEMPRE, garantizando que el juego no se quede bloqueado.
            _uiState.update { it.copy(isProcessingMove = false) }
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

            // 3. Si la suerte eligió a la IA, activamos su turno
            val shouldAiMove = gameService.getCurrentGameMode() != GameMode.PARTY || gameService.isPartyModeAiEnabled
            if (shouldAiMove && gameService.gameState.currentPlayer == Player.AI) {
                handleAiTurn()
            }
        }
    }

    fun onGameFinished() {
        // Esta función no necesita implementación aquí, se usa en la navegación de la UI
    }

    // Usamos el símbolo del jugador actual para el Tooltip
    fun getPlayerMarker(): String {
        return "Turno de ${uiState.value.gameState.currentPlayer.symbol}"
    }
}