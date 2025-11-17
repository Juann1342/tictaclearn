package com.example.tictaclearn.domain.model


/**
 * Recompensas usadas en Q-Learning para evaluar el resultado de un movimiento o partida.
 * Estos valores definen el "aprendizaje" de la IA: una victoria tiene una recompensa alta,
 * una derrota, una penalización.
 */
object Reward {
    // Recompensa por ganar la partida
    const val WIN = 10.0
    // Recompensa por empatar
    const val TIE = 0.5
    // Penalización por perder la partida
    const val LOSE = -10.0
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