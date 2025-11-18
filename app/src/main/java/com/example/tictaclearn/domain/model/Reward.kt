package com.example.tictaclearn.domain.model


/**
 * Recompensas usadas en Q-Learning para evaluar el resultado de un movimiento o partida.
 * Aumentamos la magnitud para acelerar el aprendizaje.
 */
object Reward {
    // Recompensa por ganar la partida: Aumentada de 10.0 a 50.0
    const val WIN = 50.0
    // Recompensa por empatar: Mantenida, pero menor que la ganancia.
    const val TIE = 1.0
    // Penalización por perder la partida: Aumentada de -10.0 a -50.0
    const val LOSE = -50.0
    // Recompensa por un estado intermedio (mientras el juego está en curso)
    const val DEFAULT = 0.0

    /**
     * Determina la recompensa numérica que el jugador especificado (normalmente la IA)
     * debe recibir en función del resultado final de la partida.
     *
     * @param result El resultado actual del juego (Win, Draw, o Playing).
     * @param player El jugador para el cual se calcula la recompensa.
     * @return El valor numérico de la recompensa.
     */
    fun getReward(result: GameResult, player: Player): Double {
        return when (result) {
            // Si hay un ganador, verifica si el jugador actual es el ganador o el perdedor.
            is GameResult.Win -> if (result.winner == player) WIN else LOSE
            // Si es un empate.
            GameResult.Draw -> TIE
            // Si el juego está en curso.
            GameResult.Playing -> DEFAULT
        }
    }
}