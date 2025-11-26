package com.chifuz.tictaclearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// Importamos la función de navegación del archivo dedicado
import com.chifuz.tictaclearn.presentation.navigation.AppNavHost
import com.chifuz.tictaclearn.ui.theme.TicTacLearnTheme
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
                    // Llamamos a la navegación principal, que está en AppNavHost.kt
                    AppNavHost()
                }
            }
        }
    }
}

