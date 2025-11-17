package com.example.tictaclearn.presentation.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Mood // Importar Mood
import com.example.tictaclearn.domain.service.TicTacToeGameService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay // Importación necesaria para el delay de "pensamiento"
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Clave para obtener el ID del estado de ánimo de los argumentos de navegación
private const val MOOD_ID_KEY = "moodId"
private const val AI_THINKING_DELAY = 600L // Delay para que la IA 'piense'

/**
 * ViewModel que gestiona toda la lógica de una partida de TicTacToe.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameService: TicTacToeGameService,
    private val savedStateHandle: SavedStateHandle // Para obtener argumentos de navegación
) : ViewModel() {

    // --- Estado de la UI ---
    // Usamos el GameUiState definido externamente
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // 1. Obtenemos el moodId (que puede ser nulo)
        val moodId = savedStateHandle.get<String>(MOOD_ID_KEY)

        // 2. Si el moodId es nulo, actualizamos el estado de error y salimos.
        if (moodId == null) {
            _uiState.update { it.copy(errorMessage = "Error: No se encontró Mood ID.", isProcessingMove = false) }
        } else {
            // 3. Si el moodId existe, ejecutamos la lógica asíncrona de inicialización.
            viewModelScope.launch {
                _uiState.update { it.copy(isProcessingMove = true) } // Iniciar con el estado de carga
                try {
                    // Obtener y configurar el Mood
                    val currentMood = Mood.fromId(moodId)

                    // Inicializar el servicio de juego (carga la Q-Table y configura el agente)
                    gameService.initializeGame(moodId)

                    // Actualizar el estado de la UI
                    _uiState.update {
                        it.copy(
                            gameState = gameService.gameState,
                            currentMood = currentMood,
                            isProcessingMove = false // Finalizar carga, si no le toca a la IA
                        )
                    }

                    // Comprobar si la IA empieza
                    if (gameService.gameState.currentPlayer == Player.AI) {
                        // El bloqueo ya se hizo arriba, solo llamamos al turno de la IA
                        handleAiTurn()
                    }

                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Error en inicializar: ${e.message}",
                            isProcessingMove = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Se llama cuando el jugador humano (X) hace un click en una celda.
     */
    fun onCellClicked(position: Int) {
        val currentState = _uiState.value.gameState

        // 1. Bloquear si el juego terminó, la posición no está disponible o la UI está bloqueada
        if (currentState.isFinished ||
            !currentState.board.isPositionAvailable(position) ||
            _uiState.value.isProcessingMove) {
            return
        }

        // Bloquear la UI durante el turno completo (Humano + IA)
        _uiState.update { it.copy(isProcessingMove = true) }

        viewModelScope.launch {
            try {
                // 2. Procesar el movimiento del humano
                var updatedState = gameService.handleHumanTurn(position)
                _uiState.update { it.copy(gameState = updatedState) }

                // 3. Si el juego NO terminó y es turno de la IA, ejecutar su turno
                if (!updatedState.isFinished && updatedState.currentPlayer == Player.AI) {
                    handleAiTurn() // Esta función maneja su propio bloqueo/desbloqueo
                } else {
                    // Si el juego acabó con el movimiento del humano (victoria o empate)
                    saveMemoryIfFinished()
                    _uiState.update { it.copy(isProcessingMove = false) } // Desbloquear UI
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Error en el movimiento: ${e.message}",
                        isProcessingMove = false
                    )
                }
            }
        }
    }

    /**
     * Ejecuta el turno de la IA (O) y actualiza el estado.
     */
    private fun handleAiTurn() {
        viewModelScope.launch {
            try {
                // 1. Aseguramos el bloqueo, en caso de que se llame desde resetGame
                // (Aunque onResetGameClicked ya lo maneja, esto es una buena práctica de protección)
                _uiState.update { it.copy(isProcessingMove = true) }

                // 2. Mostrar un pequeño retraso para simular "pensamiento"
                delay(AI_THINKING_DELAY)

                // 3. Ejecutar el turno de la IA y actualizar el estado
                val updatedState = gameService.handleAiTurn()

                // 4. Actualizar estado y desbloquear la UI
                _uiState.update {
                    it.copy(
                        gameState = updatedState,
                        isProcessingMove = false // Desbloquear la UI después del movimiento de la IA
                    )
                }

                // 5. Guardar la memoria si el juego terminó con el movimiento de la IA
                if (updatedState.isFinished) {
                    saveMemoryIfFinished()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Error en el turno de la IA: ${e.message}",
                        isProcessingMove = false
                    )
                }
            }
        }
    }

    /**
     * Reinicia el juego, pero mantiene la memoria de la IA intacta.
     */
    fun onResetGameClicked() {
        if (_uiState.value.isProcessingMove) return

        gameService.resetGame()

        // 1. Actualizar el estado de la UI al estado inicial
        _uiState.update {
            it.copy(
                gameState = gameService.gameState,
                isProcessingMove = false // Se asume que el humano empieza y la UI está lista
            )
        }

        // 2. Chequear si la IA debe empezar.
        if (gameService.gameState.currentPlayer == Player.AI) {
            // 💡 CORRECCIÓN CRÍTICA: Forzar el estado de procesamiento antes de llamar a la IA
            _uiState.update { it.copy(isProcessingMove = true) }
            handleAiTurn() // Inicia el turno de la IA (que ya maneja el desbloqueo)
        }
    }

    /**
     * Guarda la memoria de la IA (Q-Table) si el juego ha terminado.
     */
    private fun saveMemoryIfFinished() {
        if (_uiState.value.gameState.isFinished) {
            viewModelScope.launch {
                gameService.saveAiMemory()
            }
        }
    }
}