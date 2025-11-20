package com.example.tictaclearn.domain.model

data class Mood(
    val id: String,
    val displayName: String,
    val description: String,
    // Parámetro para Q-Learning (3x3): Probabilidad de movimiento aleatorio (0.0 a 1.0)
    val epsilon: Double,
    // Parámetro para Minimax (9x9): Profundidad de búsqueda (0 para desactivado/Q-Learning)
    val minimaxDepth: Int = 0
) {
    companion object {
        // --- 3x3 CLASSIC MOODS (Q-Learning) ---

        // NIVEL 1: Muy fácil
        val SOMNOLIENTO = Mood(
            id = "somnoliento",
            displayName = "Somnoliento",
            description = "Juega casi al azar. Ideal para aprender.",
            epsilon = 0.8
        )

        // NIVEL 2: Fácil
        val RELAJADO = Mood(
            id = "relajado",
            displayName = "Relajado",
            description = "Comete errores frecuentes, pero intenta jugar.",
            epsilon = 0.5
        )

        // NIVEL 3: Intermedio
        val NORMAL = Mood(
            id = "normal",
            displayName = "Normal",
            description = "Un reto equilibrado. A veces se despista.",
            epsilon = 0.2
        )

        // NIVEL 4: Difícil
        val ATENTO = Mood(
            id = "atento",
            displayName = "Atento",
            description = "Juega serio. Rara vez comete errores simples.",
            epsilon = 0.05
        )

        // NIVEL 5: Experto
        val CONCENTRADO = Mood(
            id = "concentrado",
            displayName = "Concentrado",
            description = "Invencible. Usa todo su potencial.",
            epsilon = 0.01
        )

        // --- 9x9 GOMOKU MOODS (Minimax) ---

        val GOMOKU_FACIL = Mood(
            id = "gomoku_facil",
            displayName = "Gomoku Novato",
            description = "Profundidad 1. Defensivo básico.",
            epsilon = 0.0,
            minimaxDepth = 1
        )

        val GOMOKU_MEDIO = Mood(
            id = "gomoku_medio",
            displayName = "Gomoku Intermedio",
            description = "Profundidad 2. Juega con previsión.",
            epsilon = 0.0,
            minimaxDepth = 2
        )

        val GOMOKU_DIFICIL = Mood(
            id = "gomoku_dificil",
            displayName = "Gomoku Experto",
            description = "Profundidad 2+. Agresivo.",
            epsilon = 0.0,
            minimaxDepth = 3 // Profundidad 3 puede ser lenta en Java/Kotlin puro sin optimizar, cuidado
        )

        // Listas para la UI
        val ALL_MOODS_CLASSIC = listOf(SOMNOLIENTO, RELAJADO, NORMAL, ATENTO, CONCENTRADO)
        val ALL_MOODS_GOMOKU = listOf(GOMOKU_FACIL, GOMOKU_MEDIO, GOMOKU_DIFICIL)

        // Lista completa (fallback)
        val ALL_MOODS = ALL_MOODS_CLASSIC + ALL_MOODS_GOMOKU

        fun getDefaultDailyMood(): Mood {
            return NORMAL
        }

        fun fromId(id: String): Mood? {
            return ALL_MOODS.find { it.id == id }
        }

        /**
         * ✅ ESTA ES LA FUNCIÓN QUE FALTABA
         * Devuelve el Mood por defecto según el modo de juego seleccionado.
         */
        fun getDefaultMoodForMode(mode: GameMode): Mood {
            return when (mode) {
                GameMode.CLASSIC -> NORMAL
                GameMode.GOMOKU -> GOMOKU_MEDIO
                else -> NORMAL
            }
        }
    }
}