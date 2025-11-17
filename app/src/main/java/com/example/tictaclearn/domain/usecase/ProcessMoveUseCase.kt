package com.example.tictaclearn.domain.usecase

import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.checkGameResult // Importado de GameState.kt
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
        position: Int, // ❌ CAMBIO: Usamos el índice plano (0-8) en lugar de row/col
        currentMood: Mood
    ): GameState {
        // --- 1. Movimiento del Jugador Humano (Human) ---
        // Usamos el método move de GameState, que ya se encarga de cambiar el Board y el Player
        val stateAfterHumanMove = currentState.move(position, Player.Human)

        // Si el juego terminó (victoria Humana o Empate), actualizamos la memoria de la IA y terminamos.
        if (stateAfterHumanMove.result != GameResult.Playing) {
            // El historial contiene solo el estado inicial y el estado final del humano
            aiEngineRepository.updateMemory(listOf(currentState.board, stateAfterHumanMove.board))
            return stateAfterHumanMove
        }

        // --- 2. Movimiento de la IA (AI) ---

        // Pedir a la IA su mejor movimiento (retorna el índice plano Int?)
        val aiMoveIndex = aiEngineRepository.getNextMove(stateAfterHumanMove.board, currentMood)

        // Si la IA tiene un movimiento válido:
        val stateAfterAIMove = if (aiMoveIndex != null) {
            // ❌ CAMBIO: Usamos el índice plano directamente y el Player es AI
            stateAfterHumanMove.move(aiMoveIndex, Player.AI)
        } else {
            // Si no hay movimientos posibles (debería ser empate)
            stateAfterHumanMove
        }

        // --- 3. Actualización de Memoria de la IA ---
        val gameHistory = listOf(currentState.board, stateAfterHumanMove.board, stateAfterAIMove.board)
        aiEngineRepository.updateMemory(gameHistory)

        return stateAfterAIMove
    }

    // Estos métodos auxiliares han sido movidos y consolidados en la clase GameState
    // y la función de extensión checkGameResult, lo que simplifica enormemente este UseCase.

}