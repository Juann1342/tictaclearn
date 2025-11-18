package com.example.tictaclearn.domain.service

import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.repository.AIEngineRepository // Usamos la interfaz del repo
import com.example.tictaclearn.domain.model.Board
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio responsable de la lógica del juego.
 * Ahora DELEGA la inteligencia al AIEngineRepository para asegurar que
 * siempre se use la memoria actualizada.
 */
@Singleton
class TicTacToeGameService @Inject constructor(
    private val aiEngineRepository: AIEngineRepository // Inyectamos el Repositorio
) {
    // Estado actual del juego
    var gameState: GameState = GameState.initial()
        private set

    // Estado de ánimo actual
    var currentMood: Mood? = null
        private set

    /**
     * Inicializa el juego cargando el mood.
     */
    suspend fun initializeGame(moodId: String) {
        // Obtenemos el mood (o el diario si falla)
        val mood = Mood.fromId(moodId) ?: aiEngineRepository.getDailyMood()
        currentMood = mood

        // Reiniciamos el tablero
        resetGame()
    }

    /**
     * Maneja el movimiento del humano.
     */
    fun handleHumanTurn(position: Int): GameState {
        // Validar movimiento
        if (gameState.board.isPositionAvailable(position) && !gameState.isFinished) {
            gameState = gameState.move(position, Player.Human)
        }
        return gameState
    }

    /**
     * Maneja el turno de la IA preguntando al Repositorio.
     */
    suspend fun handleAiTurn(): GameState {
        if (gameState.isFinished || gameState.currentPlayer != Player.AI) {
            return gameState
        }

        // 1. PREGUNTAR AL CEREBRO CENTRAL (Repositorio)
        // Esto usa la Q-Table más actual y la lógica de Epsilon-Greedy
        val moveIndex = aiEngineRepository.getNextMove(
            board = gameState.board,
            currentMood = currentMood ?: Mood.getDefaultDailyMood()
        )

        // 2. Ejecutar el movimiento si el repositorio devolvió uno válido
        if (moveIndex != null && gameState.board.isPositionAvailable(moveIndex)) {
            gameState = gameState.move(moveIndex, Player.AI)
        }

        return gameState
    }

    /**
     * Reinicia solo el tablero. La memoria reside en el repositorio, así que no hay que recargarla.
     */
    fun resetGame() {
        gameState = GameState.initial()
    }

    // Eliminamos saveAiMemory() porque la responsabilidad ahora es del ViewModel -> Repository
}