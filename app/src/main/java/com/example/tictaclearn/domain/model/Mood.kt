package com.example.tictaclearn.domain.model

// domain/model/Mood.kt

data class Mood(
    val id: String,         // Identificador único (ej: "somnoliento", "concentrado")
    val displayName: String,// Texto amigable para la UI (ej: " Somnoliento")
    val description: String,// Descripción del ánimo
    val epsilon: Double     // El parámetro ϵ (0.0 a 1.0) para el Q-Learning
) {
    companion object {
        // Configuramos los estados de ánimo predefinidos y sus valores de Epsilon
        val SOMNOLIENTO = Mood(
            id = "somnoliento",
            displayName = "😴 Somnoliento",
            description = "Juega muy aleatorio. Será fácil ganar.",
            epsilon = 0.7 // Alta probabilidad de exploración/movimientos tontos
        )

        val NORMAL = Mood(
            id = "normal",
            displayName = "😐 Normal",
            description = "Aprende y mejora con cada partida.",
            epsilon = 0.3 // Equilibrio entre exploración y explotación
        )

        val CONCENTRADO = Mood(
            id = "concentrado",
            displayName = "🧠 Concentrado",
            description = "Juega casi óptimamente, usando su memoria al máximo.",
            epsilon = 0.05 // Muy baja probabilidad de movimientos tontos
        )

        val ALL_MOODS = listOf(SOMNOLIENTO, NORMAL, CONCENTRADO)

        // El estado de ánimo que se asigna por defecto cada día
        fun getDefaultDailyMood(): Mood {
            // Aquí podríamos implementar una lógica más elaborada (ej. aleatorio)
            // pero por ahora, lo dejamos en Normal.
            return NORMAL
        }
    }
}