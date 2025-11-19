package com.example.tictaclearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.tictaclearn.ui.theme.TicTacLearnTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacLearnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TicTacToeNavHost()
                }
            }
        }
    }
}

@Composable
fun TicTacToeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Configuration.route
    ) {
        // --- 1. Pantalla de Configuración ---
        composable(Screen.Configuration.route) {
            ConfigurationScreen(
                // 🔄 CORRECCIÓN: Ahora la lambda onStartGame recibe DOS Strings.
                onStartGame = { moodId, gameModeId ->
                    // Navega a la pantalla de juego, pasando AMBOS IDs
                    navController.navigate(Screen.Game.createRoute(gameModeId, moodId))
                }
            )
        }

        // --- 2. Pantalla de Juego ---
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument(Screen.Game.GAME_MODE_ID_KEY) { type = NavType.StringType }, // Argumento 1: Modo
                navArgument(Screen.Game.MOOD_ID_KEY) { type = NavType.StringType }      // Argumento 2: Mood
            )
        ) { backStackEntry ->
            // Recuperamos AMBOS argumentos de navegación
            val gameModeId = backStackEntry.arguments?.getString(Screen.Game.GAME_MODE_ID_KEY) ?: ""
            val moodId = backStackEntry.arguments?.getString(Screen.Game.MOOD_ID_KEY) ?: ""

            GameScreen(
                gameModeId = gameModeId,
                moodId = moodId,
                onGameFinished = { navController.popBackStack() }
            )
        }
    }
}