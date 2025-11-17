package com.example.tictaclearn.presentation.navigation

// presentation/navigation/Screen.kt

/**
 * Define las rutas de la aplicación. Usamos un 'sealed class' con data class
 * para poder pasar argumentos de forma segura.
 */
sealed class Screen(val route: String) {
    // Pantalla inicial para configurar el estado de ánimo (mood) de la IA
    data object Configuration : Screen("configuration_screen")

    // Pantalla de juego, que requiere el ID del Mood como argumento
    // Ejemplo de ruta: "game_screen/concentrado"
    data object Game : Screen("game_screen/{moodId}") {
        // CORRECCIÓN: Constante para la clave del argumento, usada en el NavHost y ViewModel.
        const val MOOD_ID_KEY = "moodId"

        // Función para construir la ruta real, reemplazando el placeholder
        fun createRoute(moodId: String) = "game_screen/$moodId"
    }
}