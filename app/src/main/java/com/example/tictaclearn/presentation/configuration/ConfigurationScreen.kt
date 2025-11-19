package com.example.tictaclearn.presentation.configuration

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameMode // Importación necesaria
import com.example.tictaclearn.ui.theme.NeonCyan
import com.example.tictaclearn.ui.theme.SurfaceDark
import com.example.tictaclearn.ui.theme.SurfaceLight
import com.example.tictaclearn.ui.theme.TextGray
import com.example.tictaclearn.ui.theme.TextWhite

@Composable
fun ConfigurationScreen(
    // 🔄 CORRECCIÓN CRÍTICA: onStartGame ahora recibe moodId Y gameModeId
    // Esto resuelve el error "actual type is 'kotlin.Function2', but 'kotlin.Function1<...>' was expected"
    onStartGame: (moodId: String, gameModeId: String) -> Unit,
    // Inyección de Hilt para obtener el ViewModel (parámetro opcional por defecto)
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.feedbackShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configurar IA",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Selector de Modo de Juego
            GameModeSelector(
                currentMode = uiState.selectedGameMode,
                availableModes = uiState.availableGameModes,
                onModeSelected = viewModel::onGameModeSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Selector de Ánimo (Mood)
            Text(
                text = "Nivel de Dificultad (Exploración)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            } else {
                MoodSelector(
                    currentMood = uiState.currentMood,
                    availableMoods = uiState.availableMoods,
                    onMoodSelected = viewModel::onMoodSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Descripción del Ánimo
                MoodDescriptionCard(uiState.currentMood)

                Spacer(modifier = Modifier.height(32.dp))

                // **4. Botón de Inicio de Partida**
                Button(
                    // Uso correcto de ambos IDs.
                    onClick = { onStartGame(uiState.currentMood.id, uiState.selectedGameMode.id) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    contentPadding = PaddingValues(20.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🎮 Comenzar Partida")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // **5. Botón de Reseteo de Memoria**
                OutlinedButton(
                    onClick = viewModel::onResetMemoryClicked,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text("🚨 Borrar Memoria (Reset AI)", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// Nuevo Composable para el selector de Modo de Juego
@Composable
fun GameModeSelector(
    currentMode: GameMode,
    availableModes: List<GameMode>,
    onModeSelected: (GameMode) -> Unit
) {
    Text(
        text = "Modo de Juego:",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextGray
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableModes) { mode ->
            val isSelected = mode == currentMode
            val color = if (isSelected) MaterialTheme.colorScheme.primary else SurfaceLight

            // Chip para el modo de juego
            FilterChip(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                label = { Text(mode.displayName, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = color,
                    borderWidth = 1.dp, enabled = true, selected = isSelected
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}


@Composable
fun MoodSelector(
    currentMood: Mood,
    availableMoods: List<Mood>,
    onMoodSelected: (Mood) -> Unit
) {
    // ... (El código de MoodSelector sigue siendo el mismo)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableMoods) { mood ->
            val isSelected = mood == currentMood
            val (activeColor, icon) = getMoodVisuals(mood.id)

            // Animación de color para dar feedback visual
            val chipColor by animateColorAsState(
                targetValue = if (isSelected) SurfaceLight else SurfaceDark,
                label = "chipColorAnimation"
            )

            FilterChip(
                selected = isSelected,
                onClick = { onMoodSelected(mood) },
                label = { Text(mood.displayName, fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = activeColor // Usa el color del chip, que refleja la selección
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = activeColor.copy(alpha = 0.2f),
                    selectedLabelColor = activeColor,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) activeColor else SurfaceLight,
                    borderWidth = 1.dp, enabled = true, selected = isSelected
                ),
                shape = RoundedCornerShape(50) // Pill shape
            )
        }
    }
}

@Composable
fun MoodDescriptionCard(mood: Mood) {
    val (color, icon) = getMoodVisuals(mood.id)
    // Usamos el valor de epsilon como está (0.0 a 1.0)
    val animatedProgress by animateFloatAsState(targetValue = mood.epsilon.toFloat(), label = "epsilonProgress")

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = mood.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mood.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            val modeColor = MaterialTheme.colorScheme.secondary

            // Indicador de Nivel/Profundidad
            val levelText = if (mood.minimaxDepth > 0) {
                "Profundidad Minimax: ${mood.minimaxDepth}"
            } else {
                "Prob. de Exploración (ε): ${String.format("%.2f", mood.epsilon)}"
            }

            Text(
                text = levelText,
                style = MaterialTheme.typography.bodySmall,
                color = modeColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Si es Q-Learning, mostramos la barra de exploración
            if (mood.minimaxDepth == 0) {
                LinearProgressIndicator(
                    progress = { animatedProgress }, // Usa epsilon (0 = Explotación, 1 = Exploración)
                    color = color,
                    trackColor = color.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Explotación (Memoria)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                    Text(
                        text = "Exploración (Aleatorio)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                }
            } else {
                // Para Gomoku/Minimax, solo mostramos el texto de profundidad
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Función auxiliar para dar personalidad a cada estado (ajustada para tus 5 moods)
fun getMoodVisuals(moodId: String): Pair<Color, ImageVector> {
    return when (moodId.lowercase()) {
        "somnoliento" -> Color(0xFFB0BEC5) to Icons.Default.Settings // Gris Claro
        "relajado" -> Color(0xFF81C784) to Icons.Default.Face            // Verde Suave
        "normal" -> Color(0xFFFDD835) to Icons.Default.ThumbUp              // Amarillo
        "atento" -> Color(0xFFFB8C00) to Icons.Default.Warning      // Naranja
        "concentrado" -> Color(0xFFE53935) to Icons.Default.Search       // Rojo

        // Visuales para Gomoku (Minimax)
        "gomoku_facil" -> Color(0xFF78909C) to Icons.Default.Settings // Gris Azulado
        "gomoku_medio" -> Color(0xFF00B0FF) to Icons.Default.ThumbUp // Azul Brillante
        "gomoku_dificil" -> Color(0xFFD500F9) to Icons.Default.Search       // Violeta Intenso
        else -> NeonCyan to Icons.Default.Face
    }
}