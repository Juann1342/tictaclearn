package com.example.tictaclearn.domain


import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Player

/**
 * Interfaz para abstraer la lógica del juego y el comportamiento de la IA.
 */
interface GameLogicManager {
    /**
     * Devuelve el estado inicial del juego.
     */
    fun getInitialState(): GameState

    /**
     * Calcula el estado siguiente después de un movimiento de un jugador.
     */
    fun processMove(currentState: GameState, position: Int, player: Player): GameState

    /**
     * Calcula el mejor movimiento para la IA (usando Q-Learning).
     */
    suspend fun calculateAiMove(currentState: GameState): Int
}