package com.example.tictaclearn.domain.model

data class Mood(
    val id: String,
    val displayName: String,
    val description: String,
    // Parámetro para Q-Learning (3x3): Probabilidad de movimiento aleatorio (0.0 a 1.0)
    val epsilon: Double,
    // Parámetro para Minimax (9x9): Profundidad de búsqueda (0 para desactivado/Q-Learning)
    val minimaxDepth: Int = 0,
    // Nuevo: Tasa de exploración (chance de moverse al azar) para Gomoku Minimax.
    // Esto hace que la IA "falle" intencionalmente en los niveles más fáciles.
    val gomokuExplorationRate: Double = 0.0
) {
    companion object {
        // --- 3x3 CLASSIC MOODS (Q-Learning) ---

        // NIVEL 1: Muy fácil
        val SOMNOLIENTO = Mood(
            id = "somnoliento",
            displayName = "Somnoliento",
            description = "Juega casi al azar. Ideal para aprender.",
            epsilon = 0.8,
            minimaxDepth = 0
        )

        // NIVEL 2: Fácil
        val RELAJADO = Mood(
            id = "relajado",
            displayName = "Relajado",
            description = "Comete errores frecuentes, pero intenta jugar.",
            epsilon = 0.5,
            minimaxDepth = 0
        )

        // NIVEL 3: Intermedio
        val NORMAL = Mood(
            id = "normal",
            displayName = "Normal",
            description = "Un reto equilibrado. A veces se despista.",
            epsilon = 0.2,
            minimaxDepth = 0
        )

        // NIVEL 4: Difícil
        val ATENTO = Mood(
            id = "atento",
            displayName = "Atento",
            description = "Juega serio. Rara vez comete errores simples.",
            epsilon = 0.05,
            minimaxDepth = 0
        )

        // NIVEL 5: Experto
        val CONCENTRADO = Mood(
            id = "concentrado",
            displayName = "Concentrado",
            description = "Invencible. Usa todo su potencial.",
            epsilon = 0.0,
            minimaxDepth = 0
        )

        // --- 9x9 GOMOKU MOODS (Minimax) ---
        // Usamos una profundidad máxima de 3 y controlamos la dificultad con la tasa de exploración.

        // NIVEL 1: Novato (Más fácil de ganar)
        val GOMOKU_FACIL = Mood(
            id = "gomoku_facil",
            displayName = "Gomoku Novato",
            description = "Minimax a profundidad 1. Se equivoca mucho.",
            epsilon = 0.0,
            minimaxDepth = 1,
            gomokuExplorationRate = 0.3 // 30% de chance de hacer un movimiento aleatorio
        )

        // NIVEL 2: Intermedio (Reto medio)
        val GOMOKU_MEDIO = Mood(
            id = "gomoku_medio",
            displayName = "Gomoku Intermedio",
            description = "Minimax a profundidad 2. Juega estratégicamente, pero es vulnerable a errores.",
            epsilon = 0.0,
            minimaxDepth = 2,
            gomokuExplorationRate = 0.15 // 15% de chance de hacer un movimiento aleatorio
        )

        // NIVEL 3: Experto (Muy difícil de ganar)
        val GOMOKU_DIFICIL = Mood(
            id = "gomoku_dificil",
            displayName = "Gomoku Experto",
            description = "Minimax a profundidad 3. Juega con el 97% de su potencial de cálculo. Busca la victoria óptima.",
            epsilon = 0.0,
            minimaxDepth = 3,
            gomokuExplorationRate = 0.03 // Siempre juega el mejor movimiento calculado
        )

        val ALL_MOODS_CLASSIC = listOf(SOMNOLIENTO, RELAJADO, NORMAL, ATENTO, CONCENTRADO)
        val ALL_MOODS_GOMOKU = listOf(GOMOKU_FACIL, GOMOKU_MEDIO, GOMOKU_DIFICIL)
        val ALL_MOODS = ALL_MOODS_CLASSIC + ALL_MOODS_GOMOKU

        fun getDefaultDailyMood(): Mood {
            return NORMAL
        }

        fun fromId(id: String): Mood? {
            return ALL_MOODS.find { it.id == id }
        }

        /**
         * Retorna el Mood por defecto según el modo de juego.
         */
        fun getDefaultMoodForMode(mode: GameMode): Mood {
            return when (mode) {
                GameMode.CLASSIC -> NORMAL
                GameMode.GOMOKU -> GOMOKU_FACIL
                else ->  NORMAL
            }
        }
    }
}