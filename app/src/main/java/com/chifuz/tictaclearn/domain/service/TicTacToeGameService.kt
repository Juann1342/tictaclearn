package com.chifuz.tictaclearn.domain.service

import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.Player
import com.chifuz.tictaclearn.domain.model.GameMode
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import com.chifuz.tictaclearn.domain.model.Board
import com.chifuz.tictaclearn.domain.model.GameResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicTacToeGameService @Inject constructor(
    private val aiEngineRepository: AIEngineRepository
) {
    var gameState: GameState = GameState.initial()
        private set

    var currentMood: Mood? = null
        private set

    // Necesitamos recordar el modo actual para los resets
    private var currentGameMode: GameMode = GameMode.CLASSIC

    /**
     * ✅ CORRECCIÓN: Recibe también gameModeId para configurar el tamaño del tablero.
     */
    suspend fun initializeGame(moodId: String, gameModeId: String) {
        val mood = Mood.fromId(moodId) ?: Mood.getDefaultDailyMood()
        val mode = GameMode.fromId(gameModeId) ?: GameMode.CLASSIC

        currentMood = mood
        currentGameMode = mode

        resetGame()
    }

    /**
     * Maneja el movimiento del humano.
     */
    fun handleHumanTurn(position: Int): GameState {
        if (gameState.board.isPositionAvailable(position) && !gameState.isFinished) {
            // Pasamos el winningLength del modo actual a la lógica de movimiento
            gameState = gameState.move(position, Player.Human, currentGameMode.winningLength)
        }
        return gameState
    }

    /**
     * Maneja el turno de la IA.
     */
    suspend fun handleAiTurn(): GameState {
        if (gameState.isFinished || gameState.currentPlayer != Player.AI) {
            return gameState
        }

        val moveIndex = aiEngineRepository.getNextMove(
            board = gameState.board,
            currentMood = currentMood ?: Mood.getDefaultDailyMood()
            // Nota: Para Gomoku, aquí habría que pasar el modo a la IA también,
            // pero por ahora asumimos que getNextMove maneja la lógica interna o solo Q-Learning básico
        )

        if (moveIndex != null && gameState.board.isPositionAvailable(moveIndex)) {
            gameState = gameState.move(moveIndex, Player.AI, currentGameMode.winningLength)
        }

        return gameState
    }

    /**
     * Reinicia el juego usando el tamaño del tablero del modo actual.
     */
    fun resetGame() {
        // Creamos un tablero vacío del tamaño correcto (3x3 o 9x9)
        val emptyBoard = Board(size = currentGameMode.boardSize)

        gameState = GameState(
            board = emptyBoard,
            currentPlayer = Player.Human,
            result = GameResult.Playing,
            gameHistory = listOf(emptyBoard)
        )
    }

    fun getCurrentGameMode() = currentGameMode
}