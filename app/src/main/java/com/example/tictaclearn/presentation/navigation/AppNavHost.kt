package com.example.tictaclearn.presentation.navigation

// presentation/navigation/AppNavHost.kt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tictaclearn.presentation.configuration.ConfigurationScreen
import com.example.tictaclearn.presentation.game.GameScreen
import com.example.tictaclearn.presentation.navigation.Screen

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
        // --- 1. RUTA DE CONFIGURACIÓN ---
        composable(Screen.Configuration.route) {
            ConfigurationScreen(
                onStartGame = { moodId ->
                    // Navegar al GameScreen, pasando el ID del Mood como argumento
                    navController.navigate(Screen.Game(moodId).createRoute())
                }
            )
        }

        // --- 2. RUTA DEL JUEGO ---
        composable(
            route = "game_screen/{${Screen.Game.MOOD_ID_KEY}}",
            arguments = listOf(
                navArgument(Screen.Game.MOOD_ID_KEY) {
                    type = NavType.StringType
                    // Si el MoodId no llega por alguna razón, fallará y forzará a revisarlo.
                    nullable = false
                }
            )
        ) { backStackEntry ->
            // Recuperamos el MoodId del argumento de navegación
            val moodId = backStackEntry.arguments?.getString(Screen.Game.MOOD_ID_KEY)

            // Si el moodId es nulo (no debería ocurrir con nullable=false), volvemos.
            if (moodId == null) {
                navController.popBackStack()
                return@composable
            }

            GameScreen(
                moodId = moodId,
                onGameFinished = {
                    // Cuando el juego termina (victoria, derrota, o empate), volvemos a la configuración
                    navController.popBackStack(
                        route = Screen.Configuration.route,
                        inclusive = false
                    )
                }
            )
        }
    }
}