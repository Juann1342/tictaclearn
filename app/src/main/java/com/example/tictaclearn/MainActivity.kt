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

/**
 * Actividad principal y punto de entrada de la aplicación.
 * Contiene el tema de la aplicación y el NavHost (gráfico de navegación).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacLearnTheme {
                // Una superficie contenedora que utiliza el color de fondo del tema
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

/**
 * Define el NavHost de la aplicación y las transiciones entre pantallas.
 */
@Composable
fun TicTacToeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Configuration.route // La pantalla inicial
    ) {
        // --- 1. Pantalla de Configuración ---
        composable(Screen.Configuration.route) {
            ConfigurationScreen(
                onStartGame = { moodId ->
                    // Navega a la pantalla de juego, pasando el moodId
                    navController.navigate(Screen.Game.createRoute(moodId)) {
                        // Limpiamos la pila para que no se pueda volver atrás a la configuración
                        // sin querer durante la partida.
                        popUpTo(Screen.Configuration.route) { inclusive = true }
                    }
                }
            )
        }

        // --- 2. Pantalla de Juego ---
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("moodId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // CORRECCIÓN: Obtener el moodId del argumento de navegación.
            val moodId = backStackEntry.arguments?.getString("moodId")
                ?: throw IllegalStateException("moodId debe ser un argumento de navegación.")

            // CORRECCIÓN: Llamar a GameScreen con los argumentos requeridos.
            GameScreen(
                moodId = moodId,
                onGameFinished = { navController.popBackStack() } // Al terminar, volvemos atrás
            )
        }
    }
}