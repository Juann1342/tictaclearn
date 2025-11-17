package com.example.tictaclearn.presentation.configuration

// presentation/configuration/ConfigurationScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.presentation.navigation.Screen // Asumiendo que tienes una clase de navegación

@Composable
fun ConfigurationScreen(
    // Inyección de Hilt para obtener el ViewModel
    viewModel: ConfigurationViewModel = hiltViewModel(),
    // Acción para navegar a la pantalla de juego
    onStartGame: (moodId: String) -> Unit
) {
    // Observamos el estado del ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Controlador del SnackBar
    val snackbarHostState = remember { SnackbarHostState() }
    // No necesitamos rememberCoroutineScope() aquí a menos que hagamos un launch dentro del Composable.

    // Efecto secundario para mostrar el mensaje de feedback (ej. "Memoria borrada")
    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.feedbackShown() // Consumimos el mensaje para que no se muestre de nuevo
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "TicTacLEarn",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // **1. Estado de Ánimo Hoy**
            Text(
                text = "Hoy la IA está:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                // Muestra un indicador mientras se realiza el reset
                CircularProgressIndicator()
            } else {
                Text(
                    text = uiState.currentMood.displayName,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                // **2. Selector de Estado de Ánimo**
                MoodSelector(
                    currentMood = uiState.currentMood,
                    availableMoods = uiState.availableMoods,
                    onMoodSelected = viewModel::onMoodSelected
                )

                Spacer(modifier = Modifier.height(40.dp))

                // **3. Botón de Iniciar Partida**
                Button(
                    onClick = { onStartGame(uiState.currentMood.id) },
                    modifier = Modifier.fillMaxWidth(0.7f),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Text("🎮 Comenzar Partida")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // **4. Botón de Reseteo de Memoria**
                OutlinedButton(
                    onClick = viewModel::onResetMemoryClicked,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text("🚨 Borrar Memoria (Reset AI)")
                }
            }
        }
    }
}

// Composable para el selector de ánimo (podría ser un Dropdown o Tabs)
@Composable
fun MoodSelector(
    currentMood: Mood,
    availableMoods: List<Mood>,
    onMoodSelected: (Mood) -> Unit
) {
    // Implementación simple con un DropdownMenu
    var expanded by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { expanded = true }) {
        Text("Cambiar ánimo: ${currentMood.displayName}")
        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Cambiar ánimo")
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        availableMoods.forEach { mood ->
            DropdownMenuItem(
                text = { Text(mood.displayName) },
                onClick = {
                    onMoodSelected(mood)
                    expanded = false
                }
            )
        }
    }
}