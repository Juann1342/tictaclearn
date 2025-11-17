package com.example.tictaclearn.presentation.navigation

// presentation/navigation/Screen.kt

/**
 * Define las rutas de la aplicación. Usamos un 'sealed class' con data class
 * para poder pasar argumentos de forma segura.
 */
sealed class Screen(val route: String) {

    // 1. Pantalla Principal / Configuración
    data object Configuration : Screen("configuration_screen")

    // 2. Pantalla del Juego
    // Esta pantalla necesita el MoodId (el estado de ánimo de la IA) como argumento
    data class Game(val moodId: String) : Screen("game_screen/{$moodId}") {

        // Función para crear la ruta real que se usa en el NavHost
        fun createRoute(): String = "game_screen/${moodId}"

        // Constante para el nombre del argumento usado en la definición del NavHost
        companion object {
            const val MOOD_ID_KEY = "moodId"
        }
    }
}