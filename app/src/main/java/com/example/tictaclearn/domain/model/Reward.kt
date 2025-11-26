package com.example.tictaclearn.domain.model


/**
 * Define los valores de recompensa para el algoritmo Q-Learning.
 * La IA busca maximizar estos puntos.
 */
object Reward {
    const val WIN = 10.0       // Gran premio por ganar
    const val LOSE = -10.0     // Gran castigo por perder
    const val TIE = 1.0       // Empatar es mejor que perder (premio pequeño)
    const val DEFAULT = 0.0   // Movimiento normal sin resultado inmediato

    /**
     * Calcula la recompensa basada en el resultado del juego desde la perspectiva de la IA.
     */
    fun getReward(result: GameResult, aiPlayer: Player): Double {
        return when (result) {
            is GameResult.Win -> {
                if (result.winner == aiPlayer) WIN else LOSE
            }
            GameResult.Draw -> TIE
            GameResult.Playing -> DEFAULT
        }
    }
}