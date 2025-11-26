package com.chifuz.tictaclearn.presentation.navigation

/**
 * Define las rutas de la aplicación. Usamos un 'sealed class' con data class
 * para poder pasar argumentos de forma segura.
 */
sealed class Screen(val route: String) {
    // Pantalla inicial para configurar el estado de ánimo (mood) de la IA
    data object Configuration : Screen("configuration_screen")

    // Pantalla de juego, que requiere el ID del Modo de Juego y el ID del Mood
    // Ejemplo de ruta: "game_screen/classic_3x3/concentrado"
    data object Game : Screen("game_screen/{gameModeId}/{moodId}") {
        const val GAME_MODE_ID_KEY = "gameModeId"
        const val MOOD_ID_KEY = "moodId"

        // Función para construir la ruta real, reemplazando los placeholders
        fun createRoute(gameModeId: String, moodId: String) = "game_screen/$gameModeId/$moodId"
    }
}