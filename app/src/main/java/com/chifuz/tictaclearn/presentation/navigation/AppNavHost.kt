package com.chifuz.tictaclearn.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chifuz.tictaclearn.presentation.configuration.ConfigurationScreen
import com.chifuz.tictaclearn.presentation.game.GameScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Configuration.route,
        modifier = modifier
    ) {
        // --- 1. RUTA DE CONFIGURACIÓN (MENÚ) ---
        composable(Screen.Configuration.route) {
            ConfigurationScreen(
                // Ahora pasamos DOS argumentos: moodId y gameModeId
                onStartGame = { moodId, gameModeId ->
                    navController.navigate(Screen.Game.createRoute(gameModeId, moodId))
                }
            )
        }

        // --- 2. RUTA DEL JUEGO ---
        composable(
            route = Screen.Game.route, // Usamos la ruta definida en Screen.kt
            arguments = listOf(
                navArgument(Screen.Game.MOOD_ID_KEY) { type = NavType.StringType },
                navArgument(Screen.Game.GAME_MODE_ID_KEY) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Recuperamos los DOS argumentos
            val moodId = backStackEntry.arguments?.getString(Screen.Game.MOOD_ID_KEY) ?: "normal"
            val gameModeId = backStackEntry.arguments?.getString(Screen.Game.GAME_MODE_ID_KEY) ?: "classic_3x3"

            GameScreen(
                moodId = moodId,
                gameModeId = gameModeId, // Pasamos el modo al juego
                onGameFinished = {
                    // Volver al menú y limpiar la pila para no volver al juego con "Atrás"
                    navController.popBackStack(Screen.Configuration.route, inclusive = false)
                }
            )
        }
    }
}