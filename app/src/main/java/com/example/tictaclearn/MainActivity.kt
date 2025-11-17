package com.example.tictaclearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tictaclearn.presentation.navigation.AppNavHost
import com.example.tictaclearn.ui.theme.TicTacLearnTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // 💡 Importante para que Hilt inicie la inyección
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TicTacLearnTheme {
                // Una superficie que ocupa todo el espacio
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 💡 Llama al componente principal de navegación
                    AppNavHost()
                }
            }
        }
    }
}