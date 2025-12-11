package com.chifuz.tictaclearn.domain.model

import com.chifuz.tictaclearn.R

data class Mood(
    val id: String,
    val displayNameRes: Int, // Cambiado a Resource ID
    val descriptionRes: Int, // Cambiado a Resource ID
    val epsilon: Double,
    val minimaxDepth: Int = 0,
    val gomokuExplorationRate: Double = 0.0
) {
    companion object {
        // --- 3x3 CLASSIC MOODS (Q-Learning) ---

        val SOMNOLIENTO = Mood(
            id = "somnoliento",
            displayNameRes = R.string.mood_sleepy_name,
            descriptionRes = R.string.mood_sleepy_desc,
            epsilon = 0.8,
            minimaxDepth = 0
        )

        val RELAJADO = Mood(
            id = "relajado",
            displayNameRes = R.string.mood_relaxed_name,
            descriptionRes = R.string.mood_relaxed_desc,
            epsilon = 0.5,
            minimaxDepth = 0
        )

        val NORMAL = Mood(
            id = "normal",
            displayNameRes = R.string.mood_normal_name,
            descriptionRes = R.string.mood_normal_desc,
            epsilon = 0.2,
            minimaxDepth = 0
        )

        val ATENTO = Mood(
            id = "atento",
            displayNameRes = R.string.mood_attentive_name,
            descriptionRes = R.string.mood_attentive_desc,
            epsilon = 0.05,
            minimaxDepth = 0
        )

        val CONCENTRADO = Mood(
            id = "concentrado",
            displayNameRes = R.string.mood_concentrated_name,
            descriptionRes = R.string.mood_concentrated_desc,
            epsilon = 0.0,
            minimaxDepth = 0
        )

        // --- 9x9 GOMOKU MOODS (Minimax) ---

        val GOMOKU_FACIL = Mood(
            id = "gomoku_facil",
            displayNameRes = R.string.mood_gomoku_easy_name,
            descriptionRes = R.string.mood_gomoku_easy_desc,
            epsilon = 0.0,
            minimaxDepth = 1,
            gomokuExplorationRate = 0.3
        )

        val GOMOKU_MEDIO = Mood(
            id = "gomoku_medio",
            displayNameRes = R.string.mood_gomoku_medium_name,
            descriptionRes = R.string.mood_gomoku_medium_desc,
            epsilon = 0.0,
            minimaxDepth = 2,
            gomokuExplorationRate = 0.15
        )

        val GOMOKU_DIFICIL = Mood(
            id = "gomoku_dificil",
            displayNameRes = R.string.mood_gomoku_hard_name,
            descriptionRes = R.string.mood_gomoku_hard_desc,
            epsilon = 0.0,
            minimaxDepth = 3,
            gomokuExplorationRate = 0.005
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

        fun getDefaultMoodForMode(mode: GameMode): Mood {
            return when (mode) {
                GameMode.CLASSIC -> NORMAL
                GameMode.GOMOKU -> GOMOKU_FACIL
                else ->  NORMAL
            }
        }
    }
}