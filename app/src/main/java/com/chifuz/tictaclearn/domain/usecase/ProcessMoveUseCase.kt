package com.chifuz.tictaclearn.domain.usecase

import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.Player
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import javax.inject.Inject

class ProcessMoveUseCase @Inject constructor(
    private val aiEngineRepository: AIEngineRepository
) {

    /**
     * Procesa un turno completo del juego.
     */
    suspend operator fun invoke(
        currentState: GameState,
        position: Int,
        currentMood: Mood
    ): GameState {
        // ✅ CORRECCIÓN: Determinamos winningLength basado en el tamaño del tablero actual
        // Si el lado es 9 (tablero 9x9=81), ganan 5. Si es 3 (3x3=9), ganan 3.
        val winningLength = if (currentState.board.sideSize == 9) 5 else 3

        // --- 1. Movimiento del Jugador Humano ---
        val stateAfterHumanMove = currentState.move(position, Player.Human, winningLength)

        if (stateAfterHumanMove.result != GameResult.Playing) {
            aiEngineRepository.updateMemory(listOf(currentState.board, stateAfterHumanMove.board))
            return stateAfterHumanMove
        }

        // --- 2. Movimiento de la IA ---
        val aiMoveIndex = aiEngineRepository.getNextMove(stateAfterHumanMove.board, currentMood)

        val stateAfterAIMove = if (aiMoveIndex != null) {
            stateAfterHumanMove.move(aiMoveIndex, Player.AI, winningLength)
        } else {
            stateAfterHumanMove
        }

        // --- 3. Actualización de Memoria (solo si es Q-Learning/Clásico) ---
        // Gomoku normalmente no usa Q-Table simple por el tamaño del espacio de estados,
        // pero no hace daño llamar al update si el repositorio lo maneja.
        val gameHistory = listOf(currentState.board, stateAfterHumanMove.board, stateAfterAIMove.board)
        aiEngineRepository.updateMemory(gameHistory)

        return stateAfterAIMove
    }
}