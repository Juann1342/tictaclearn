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
import kotlin.random.Random // 👈 Importante

@Singleton
class TicTacToeGameService @Inject constructor(
    private val aiEngineRepository: AIEngineRepository
) {
    var gameState: GameState = GameState.initial()
        private set

    var currentMood: Mood? = null
        private set

    private var currentGameMode: GameMode = GameMode.CLASSIC

    /**
     * Inicializa el juego.
     * Al ser suspend, el ViewModel esperará aquí hasta que termine el reset.
     */
    suspend fun initializeGame(moodId: String, gameModeId: String) {
        val mood = Mood.fromId(moodId) ?: Mood.getDefaultDailyMood()
        val mode = GameMode.fromId(gameModeId) ?: GameMode.CLASSIC

        currentMood = mood
        currentGameMode = mode

        resetGame()
    }

    fun handleHumanTurn(position: Int): GameState {
        if (gameState.board.isPositionAvailable(position) && !gameState.isFinished && gameState.currentPlayer == Player.Human) {
            gameState = gameState.move(position, Player.Human, currentGameMode.winningLength)
        }
        return gameState
    }

    suspend fun handleAiTurn(): GameState {
        if (gameState.isFinished || gameState.currentPlayer != Player.AI) {
            return gameState
        }

        val moveIndex = aiEngineRepository.getNextMove(
            board = gameState.board,
            currentMood = currentMood ?: Mood.getDefaultDailyMood()
        )

        if (moveIndex != null && gameState.board.isPositionAvailable(moveIndex)) {
            gameState = gameState.move(moveIndex, Player.AI, currentGameMode.winningLength)
        }

        return gameState
    }

    /**
     * Reinicia el tablero y decide aleatoriamente quién empieza.
     * NO hace el movimiento de la IA, solo prepara el estado.
     */
    suspend fun resetGame() {
        val emptyBoard = Board(size = currentGameMode.boardSize)

        // Decisión aleatoria 50/50
        val startingPlayer = if (Random.nextBoolean()) Player.Human else Player.AI

        gameState = GameState(
            board = emptyBoard,
            currentPlayer = startingPlayer,
            result = GameResult.Playing,
            gameHistory = listOf(emptyBoard)
        )
        // Aquí NO llamamos a handleAiTurn. Dejamos que el ViewModel lo orqueste.
    }

    fun getCurrentGameMode() = currentGameMode
}