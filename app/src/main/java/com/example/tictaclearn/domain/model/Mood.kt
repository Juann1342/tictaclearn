package com.example.tictaclearn.domain.model

data class Mood(
    val id: String,
    val displayName: String,
    val description: String,
    val epsilon: Double // Probabilidad de movimiento aleatorio (0.0 a 1.0)
) {
    companion object {
        // NIVEL 1: Muy fácil
        val SOMNOLIENTO = Mood(
            id = "somnoliento",
            displayName = "😴 Somnoliento",
            description = "Juega casi al azar. Ideal para aprender.",
            epsilon = 0.8
        )

        // NIVEL 2: Fácil
        val RELAJADO = Mood(
            id = "relajado",
            displayName = "🙂 Relajado",
            description = "Comete errores frecuentes, pero intenta jugar.",
            epsilon = 0.5
        )

        // NIVEL 3: Intermedio
        val NORMAL = Mood(
            id = "normal",
            displayName = "😐 Normal",
            description = "Un reto equilibrado. A veces se despista.",
            epsilon = 0.2
        )

        // NIVEL 4: Difícil
        val ATENTO = Mood(
            id = "atento",
            displayName = "🧐 Atento",
            description = "Juega serio. Rara vez comete errores simples.",
            epsilon = 0.05
        )

        // NIVEL 5: Experto
        val CONCENTRADO = Mood(
            id = "concentrado",
            displayName = "🧠 Concentrado",
            description = "Invencible. Usa todo su potencial.",
            epsilon = 0.0
        )

        val ALL_MOODS = listOf(SOMNOLIENTO, RELAJADO, NORMAL, ATENTO, CONCENTRADO)

        fun getDefaultDailyMood(): Mood {
            return NORMAL
        }

        fun fromId(id: String): Mood {
            return ALL_MOODS.firstOrNull { it.id == id } ?: getDefaultDailyMood()
        }
    }
}