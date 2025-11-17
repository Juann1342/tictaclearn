package com.example.tictaclearn.domain.usecase

import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.Cell
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.repository.AIEngineRepository
import javax.inject.Inject

class ProcessMoveUseCase @Inject constructor(
    private val aiEngineRepository: AIEngineRepository
) {

    /**
     * Procesa un turno completo del juego: Jugador Humano -> IA -> Actualización de Memoria.
     * @return El GameState final después de los movimientos.
     */
    suspend operator fun invoke(
        currentState: GameState,
        row: Int,
        col: Int,
        currentMood: Mood
    ): GameState {
        // --- 1. Movimiento del Jugador Humano (O) ---
        val boardAfterHumanMove = makeMove(currentState.board, row, col, Cell.O)

        // Comprobar si el humano gana o si hay empate.
        var nextState = updateGameState(currentState, boardAfterHumanMove, Player.AI) // 💡 Llamada corregida

        // Si el juego terminó (victoria Humana o Empate), actualizamos la memoria de la IA y terminamos.
        if (nextState.result != GameResult.Playing) {
            aiEngineRepository.updateMemory(listOf(currentState.board, boardAfterHumanMove))
            return nextState
        }

        // --- 2. Movimiento de la IA (X) ---

        // Pedir a la IA su mejor movimiento (usa Epsilon-Greedy basado en el Mood)
        val aiMove = aiEngineRepository.getNextMove(nextState.board, currentMood)

        // Si la IA tiene un movimiento válido:
        val boardAfterAIMove = if (aiMove != null) {
            val (aiRow, aiCol) = aiMove
            makeMove(nextState.board, aiRow, aiCol, Cell.X)
        } else {
            // Si no hay movimientos posibles (debería ser empate, pero como fallback)
            nextState.board
        }

        // Comprobar si la IA gana o si hay empate después de su movimiento.
        nextState = updateGameState(nextState, boardAfterAIMove, Player.HUMAN) // 💡 Llamada corregida

        // --- 3. Actualización de Memoria de la IA ---
        val gameHistory = listOf(currentState.board, boardAfterHumanMove, boardAfterAIMove)
        aiEngineRepository.updateMemory(gameHistory)

        return nextState
    }

    // Método auxiliar para realizar el movimiento y devolver el nuevo Board
    private fun makeMove(board: Board, row: Int, col: Int, cell: Cell): Board {
        if (board.cells[row][col] != Cell.EMPTY) return board

        val newCells = board.cells.toMutableList().map { it.toMutableList() }.toMutableList()
        newCells[row][col] = cell

        return Board(newCells)
    }

    // Método auxiliar para actualizar el estado del juego y cambiar el turno
    private fun updateGameState(
        previousState: GameState,
        board: Board,
        nextPlayer: Player
    ): GameState {
        // ✅ Usamos la función checkGameStatus de la clase Board.
        val result = board.checkGameStatus()

        return GameState(
            board = board,
            // Si el juego termina (Win/Draw), mantenemos el jugador que estaba en turno (previousState.currentPlayer).
            // Si el juego sigue, el turno pasa a 'nextPlayer'.
            currentPlayer = if (result == GameResult.Playing) nextPlayer else previousState.currentPlayer,
            result = result
        )
    }
}