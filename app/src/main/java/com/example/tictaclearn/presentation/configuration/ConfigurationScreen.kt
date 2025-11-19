package com.example.tictaclearn.presentation.configuration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Asegúrate de que Filter1, Filter2, y Bolt estén aquí
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameMode
import com.example.tictaclearn.ui.theme.* // Importamos todos los colores neón

@Composable
fun ConfigurationScreen(
    onStartGame: (moodId: String, gameModeId: String) -> Unit,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark // Fondo Dark mode forzado
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título: Bolder, más "caro"
            Text(
                text = "TicTacLearn",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = NeonOrange,
                modifier = Modifier.padding(bottom = 8.dp),
                letterSpacing = 2.sp
            )
            Text(
                text = "CONFIGURAR RIVAL IA",
                style = MaterialTheme.typography.titleSmall,
                color = TextGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Selector de Modo de Juego (Gamificado)
            GameModeSelector(
                currentMode = uiState.selectedGameMode,
                availableModes = uiState.availableGameModes,
                onModeSelected = viewModel::onGameModeSelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Selector de Ánimo (Mood)
            Text(
                text = "NIVEL DE DIFICULTAD",
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

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Descripción del Ánimo (HACER COLAPSABLE)
                MoodDescriptionCard(uiState.currentMood)

                Spacer(modifier = Modifier.height(48.dp)) // Más espacio para look premium

                // **4. Botón de Inicio de Partida**
                Button(
                    onClick = { onStartGame(uiState.currentMood.id, uiState.selectedGameMode.id) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) {
                    Text("INICIAR DESAFÍO", fontWeight = FontWeight.Black, color = BackgroundDark)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // **5. Botón de Reseteo de Memoria**
                OutlinedButton(
                    onClick = viewModel::onResetMemoryClicked,
                    modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, NeonRed)
                ) {
                    Text("REINICIAR MEMORIA IA", color = NeonRed)
                }
            }
        }
    }
}

// NUEVO: GameModeSelector con texto gamificado y bordes premium
@Composable
fun GameModeSelector(
    currentMode: GameMode,
    availableModes: List<GameMode>,
    onModeSelected: (GameMode) -> Unit
) {
    Text(
        text = "MODO DE JUEGO",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextGray
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableModes) { mode ->
            val isSelected = mode == currentMode
            val color = if (isSelected) NeonCyan else SurfaceLight
            // ICONOS REEMPLAZADOS: Usamos FilterList y Star como base.
            val icon = if (mode == GameMode.GOMOKU) Icons.Default.List else Icons.Default.Star

            FilterChip(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        if (mode == GameMode.GOMOKU) "GOMOKU (9x9)" else "CLÁSICO (3x3)",
                        fontWeight = FontWeight.Black, // Letras más gruesas
                        letterSpacing = 0.5.sp
                    )
                },
                leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = color) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = color,
                    borderWidth = 2.dp, enabled = true, selected = isSelected // Borde más grueso
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}


// MODIFICADO: MoodSelector con colores más neón y bordes más gruesos
@Composable
fun MoodSelector(
    currentMood: Mood,
    availableMoods: List<Mood>,
    onMoodSelected: (Mood) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableMoods) { mood ->
            val isSelected = mood == currentMood
            val (activeColor, icon) = getMoodVisuals(mood.id)

            FilterChip(
                selected = isSelected,
                onClick = { onMoodSelected(mood) },
                label = { Text(mood.displayName.uppercase(), fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = activeColor
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = activeColor.copy(alpha = 0.25f),
                    selectedLabelColor = activeColor,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) activeColor else SurfaceLight.copy(alpha = 0.5f),
                    borderWidth = 2.dp, // Borde más grueso para premium look
                    enabled = true, selected = isSelected
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// REEMPLAZA TU FUNCIÓN MoodDescriptionCard ACTUAL POR ESTA:

@Composable
fun MoodDescriptionCard(mood: Mood) {
    var isExpanded by remember { mutableStateOf(false) }
    val (color, icon) = getMoodVisuals(mood.id)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // --- HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp).padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mood.displayName.uppercase(), // Ej: "CONCENTRADO"
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 1.sp
                    )
                    // Subtítulo corto y amigable
                    Text(
                        text = getShortPersonaDescription(mood), // Ej: "No comete errores."
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGray
                    )
                }
                // Icono de expansión animado
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Ver detalles",
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer { rotationZ = if (isExpanded) 90f else 0f }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción Principal (La que viene de la BD)
            Text(
                text = mood.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite
            )

            // --- SECCIÓN EXPANDIBLE (PERSONALIDAD, NO TÉCNICA) ---
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(
                        color = color.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "ANÁLISIS DE PERSONALIDAD:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. TIPO DE INTELIGENCIA (En vez de "Algoritmo")
                    val intelligenceType = if (mood.minimaxDepth > 0) "ESTRATEGA (Lógica Pura)" else "EVOLUTIVA (Aprendizaje)"
                    AttributeRow(label = "Tipo de Mente", value = intelligenceType, color = color)

                    // 2. ESTILO DE JUEGO (En vez de Depth/Epsilon)
                    val (styleLabel, styleValue) = getPlayStyle(mood)
                    AttributeRow(label = styleLabel, value = styleValue, color = TextWhite)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. BARRA DE COMPORTAMIENTO (Visual y divertida)
                    if (mood.minimaxDepth == 0) {
                        // Para Q-Learning: Creatividad vs Memoria
                        BehaviorBar(
                            label = "Imprevisibilidad / Creatividad",
                            value = mood.epsilon.toFloat(), // 1.0 es muy creativo/loco
                            color = color
                        )
                    } else {
                        // Para Minimax: Capacidad de Cálculo
                        // Normalizamos depth (supongamos max depth 5 para la barra)
                        val normalizedDepth = (mood.minimaxDepth / 5f).coerceIn(0f, 1f)
                        BehaviorBar(
                            label = "Poder de Cálculo Futuro",
                            value = normalizedDepth,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES PARA ESTE DISEÑO ---

@Composable
fun AttributeRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextGray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun BehaviorBar(label: String, value: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextGray)
            Text(text = "${(value * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        val animatedProgress by animateFloatAsState(targetValue = value, label = "bar")
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceLight.copy(alpha = 0.3f)
        )
    }
}

// --- LÓGICA DE TEXTOS AMIGABLES ---

fun getShortPersonaDescription(mood: Mood): String {
    return when {
        mood.minimaxDepth >= 4 -> "⚠️ Peligro: No tiene piedad."
        mood.minimaxDepth in 1..3 -> "Piensa antes de actuar."
        mood.epsilon > 0.5 -> "Está distraída o experimentando."
        mood.epsilon < 0.2 -> "Juega de memoria. Muy sólida."
        else -> "Un rival equilibrado."
    }
}

fun getPlayStyle(mood: Mood): Pair<String, String> {
    return if (mood.minimaxDepth > 0) {
        // MINIMAX
        "Visión a Futuro" to when (mood.minimaxDepth) {
            1 -> "Corto Plazo (1 turno)"
            2 -> "Táctica (2 turnos)"
            3 -> "Estratégica (3 turnos)"
            else -> "Omnisciente (+4 turnos)"
        }
    } else {
        // Q-LEARNING
        "Comportamiento" to when {
            mood.epsilon > 0.6 -> "Caótico / Curioso"
            mood.epsilon > 0.3 -> "Balanceado"
            else -> "Maestro / Serio"
        }
    }
}
// FUNCIONES DE SOPORTE (Asumimos que están definidas con colores neón en tu proyecto)
@Composable
fun rotate(degrees: Float) = Modifier.graphicsLayer { rotationZ = degrees }

// MODIFICADO: getMoodVisuals con iconos y colores sustitutos
fun getMoodVisuals(moodId: String): Pair<Color, ImageVector> {
    return when (moodId.lowercase()) {
        "somnoliento" -> Color(0xFFB0BEC5) to Icons.Default.Settings // Gris Claro
        "relajado" -> Color(0xFF81C784) to Icons.Default.Face            // Verde Suave
        "normal" -> NeonOrange to Icons.Default.ThumbUp              // Naranja Neón
        "atento" -> Color(0xFFFB8C00) to Icons.Default.Warning      // Naranja Más fuerte
        "concentrado" -> NeonRed to Icons.Default.Search       // Rojo Neón (De nuestro diseño anterior)

        // Visuales para Gomoku (Minimax) - ICONOS Y COLOR SUSTITUIDOS
        "gomoku_facil" -> Color(0xFF78909C) to Icons.Default.ThumbUp // SUSTITUTO: Filter1 (Fácil)
        "gomoku_medio" -> NeonCyan to Icons.Default.Face // SUSTITUTO: Filter2 (Medio)
        "gomoku_dificil" -> Color(0xFFD500F9) to Icons.Default.Warning // SUSTITUTO: Bolt (Díficil/Potente) y Color de código
        else -> NeonCyan to Icons.Default.Face
    }
}