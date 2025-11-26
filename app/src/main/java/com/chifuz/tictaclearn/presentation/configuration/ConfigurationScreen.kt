package com.chifuz.tictaclearn.presentation.configuration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material.icons.rounded.Park


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.GameMode
import androidx.compose.ui.graphics.Shadow
import com.chifuz.tictaclearn.ui.theme.BackgroundDark
import com.chifuz.tictaclearn.ui.theme.NeonCyan
import com.chifuz.tictaclearn.ui.theme.NeonGreen
import com.chifuz.tictaclearn.ui.theme.NeonOrange
import com.chifuz.tictaclearn.ui.theme.NeonRed
import com.chifuz.tictaclearn.ui.theme.StateAtento
import com.chifuz.tictaclearn.ui.theme.StateConcentrado
import com.chifuz.tictaclearn.ui.theme.StateGomokuDificil
import com.chifuz.tictaclearn.ui.theme.StateGomokuFacil
import com.chifuz.tictaclearn.ui.theme.StateGomokuMedio
import com.chifuz.tictaclearn.ui.theme.StateNormal
import com.chifuz.tictaclearn.ui.theme.StateRelajado
import com.chifuz.tictaclearn.ui.theme.StateSomnoliento
import com.chifuz.tictaclearn.ui.theme.SurfaceDark
import com.chifuz.tictaclearn.ui.theme.SurfaceLight
import com.chifuz.tictaclearn.ui.theme.TextGray
import com.chifuz.tictaclearn.ui.theme.TextWhite


@Composable
fun ConfigurationScreen(
    onStartGame: (moodId: String, gameModeId: String) -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 🚀 FIX: Llamamos al método de recarga cada vez que la pantalla se compone/reaparece
    LaunchedEffect(Unit) {
        viewModel.loadConfigData()
    }

    val entranceOffset = remember { Animatable(50f) }
    val entranceAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entranceOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
        )
    }
    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.feedbackShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- HEADER ---
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 20.dp)
                    .graphicsLayer {
                        translationY = entranceOffset.value
                        alpha = entranceAlpha.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TicTacLearn",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        shadow = Shadow(
                            color = NeonOrange.copy(alpha = 0.7f),
                            offset = Offset(0f, 0f),
                            blurRadius = 15f
                        )
                    ),
                    color = NeonOrange,
                )
            }

            // --- ZONA DE CONTROL ---

            // 1. Selector de Modo
            Text(
                text = "MODO DE JUEGO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
                    .graphicsLayer { alpha = entranceAlpha.value }
            )
            Spacer(modifier = Modifier.height(12.dp))

            GameModeSelector(
                currentMode = uiState.selectedGameMode,
                availableModes = uiState.availableGameModes,
                onModeSelected = viewModel::onGameModeSelected,
                modifier = Modifier
                    .graphicsLayer { translationX = entranceOffset.value * 0.5f }
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🚀 Indicador de entrenamiento (solo para Classic)
            AnimatedVisibility(visible = uiState.selectedGameMode == GameMode.CLASSIC) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    TrainingProgressIndicator(
                        gamesPlayedCount = uiState.classicGamesPlayedCount,
                        maxGames = ConfigurationUiState.MAX_TRAINING_GAMES
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp)) // Espaciador fijo

            // 2. Selector de Ánimo
            Text(
                text = "NIVEL DE AMENAZA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .align(Alignment.CenterHorizontally)
                    .graphicsLayer { alpha = entranceAlpha.value }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(24.dp),
                    color = NeonCyan
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                MoodSelector(
                    currentMood = uiState.currentMood,
                    availableMoods = uiState.availableMoods,
                    onMoodSelected = viewModel::onMoodSelected,
                    modifier = Modifier.graphicsLayer { translationX = entranceOffset.value * 0.8f }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Tarjeta Principal
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    MoodDescriptionCard(uiState.currentMood)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // --- BOTONES DE ACCIÓN ---
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            translationY = entranceOffset.value
                            alpha = entranceAlpha.value
                        }
                ) {
                    Button(
                        onClick = { onStartGame(uiState.currentMood.id, uiState.selectedGameMode.id) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Text(
                            "INICIAR DESAFÍO",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = BackgroundDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🚨 BOTÓN REINICIAR MEMORIA (Solo para Classic)
                    AnimatedVisibility(visible = uiState.selectedGameMode == GameMode.CLASSIC) {
                        OutlinedButton(
                            onClick = viewModel::onResetMemoryClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed)
                        ) {
                            Text("REINICIAR MEMORIA IA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// --- NUEVO COMPONENTE: INDICADOR DE PROGRESO DE ENTRENAMIENTO ---

@Composable
fun TrainingProgressIndicator(gamesPlayedCount: Int, maxGames: Int) {
    val actualCount = gamesPlayedCount.coerceAtMost(maxGames)
    val progress = (actualCount.toFloat() / maxGames.toFloat()).coerceIn(0f, 1f)
    val color = if (progress >= 1f) NeonGreen else NeonCyan

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Progreso de Entrenamiento (Q-Learning)",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
            Text(
                text = if (progress >= 1f) "100% (¡Óptimo!)" else "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Partidas jugadas: $actualCount / $maxGames",
            style = MaterialTheme.typography.bodySmall,
            color = TextGray.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000),
            label = "Training Progress Animation"
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            color = color,
            trackColor = SurfaceLight.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

// --- COMPONENTES AUXILIARES (DEBEN ESTAR EN EL ARCHIVO) ---

@Composable
fun GameModeSelector(
    currentMode: GameMode,
    availableModes: List<GameMode>,
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        availableModes.forEach { mode ->
            val isSelected = mode == currentMode
            val color = if (mode == GameMode.GOMOKU) NeonCyan else NeonOrange
            val icon = getGameModeIcon(mode)

            FilterChip(
                selected = isSelected,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        if (mode == GameMode.GOMOKU) "GOMOKU (9x9)" else "CLÁSICO (3x3)",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                },
                leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = color) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.15f),
                    selectedLabelColor = color,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if(isSelected) color else Color.Transparent,
                    borderWidth = 2.dp,
                    enabled = true, selected = isSelected
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MoodSelector(
    currentMood: Mood,
    availableMoods: List<Mood>,
    onMoodSelected: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = modifier.fillMaxWidth()
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
                        modifier = Modifier.size(18.dp),
                        tint = activeColor
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = activeColor.copy(alpha = 0.15f),
                    selectedLabelColor = activeColor,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) activeColor else Color.Transparent,
                    borderWidth = 2.dp,
                    enabled = true, selected = isSelected
                ),
                shape = RoundedCornerShape(50) // Pill shape
            )
        }
    }
}

@Composable
fun MoodDescriptionCard(mood: Mood) {
    var isExpanded by remember { mutableStateOf(false) }
    val (color, icon) = getMoodVisuals(mood.id)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceLight.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Icono grande con fondo circular sutil (Glow/Aura)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(color.copy(alpha = 0.1f)), // Fondo sutil para el glow
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mood.displayName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = color,
                        letterSpacing = 0.5.sp
                    )
                    // Descripción corta debajo del título
                    Text(
                        text = getShortPersonaDescription(mood),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }

                // Flecha de Expansión
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ver detalles",
                    tint = TextGray,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = if (isExpanded) 90f else 0f }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción larga (siempre visible para este componente)
            Text(
                text = mood.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite,
                lineHeight = 24.sp
            )

            // Contenido Expandido
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    // Separador sutil
                    HorizontalDivider(
                        color = SurfaceLight.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "ANÁLISIS DE IA",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val intelligenceType = if (mood.minimaxDepth > 0) "ESTRATEGA (Minimax)" else "ADAPTATIVA (Q-Learning)"
                    AttributeRow(label = "Tipo de Mente", value = intelligenceType, color = color)

                    val (styleLabel, styleValue) = getPlayStyle(mood)
                    AttributeRow(label = styleLabel, value = styleValue, color = TextWhite)

                    // 🚨 NUEVO: Mostrar tasa de exploración para Gomoku
                    if (mood.minimaxDepth > 0) {
                        val failColor = if (mood.gomokuExplorationRate > 0.1) NeonRed.copy(alpha = 0.8f) else TextGray.copy(alpha = 0.8f)
                        AttributeRow(
                            label = "Probabilidad de Fallo (Exploración)",
                            value = "${(mood.gomokuExplorationRate * 100).toInt()}%",
                            color = failColor
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Barra de Progreso mejorada
                    if (mood.minimaxDepth == 0) {
                        BehaviorBar(
                            label = "Potencia de Memoria (1 - $\\epsilon$)",
                            value = 1f - mood.epsilon.toFloat(),
                            color = color
                        )
                    } else {
                        // Normalizamos sobre 3 (la profundidad máxima de Gomoku)
                        val normalizedDepth = (mood.minimaxDepth / 3f).coerceIn(0f, 1f)
                        BehaviorBar(
                            label = "Potencia de Cálculo (Profundidad: ${mood.minimaxDepth})",
                            value = normalizedDepth,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

fun getShortPersonaDescription(mood: Mood): String {
    return when {
        // Ajustamos la descripción corta del Gomoku
        mood.minimaxDepth == 3 -> "Máxima concentración."
        mood.minimaxDepth > 0 && mood.gomokuExplorationRate > 0.5 -> "Calculadora muy distraída."
        mood.minimaxDepth > 0 -> "Calculadora, pero se distrae a veces."
        mood.epsilon > 0.5 -> "Distraída y experimental."
        mood.epsilon < 0.2 -> "Juega de memoria."
        else -> "Un rival digno."
    }
}

fun getPlayStyle(mood: Mood): Pair<String, String> {
    return if (mood.minimaxDepth > 0) {
        "Visión Futura" to when (mood.minimaxDepth) {
            1 -> "1 Turno (Reactivo)"
            2 -> "2 Turnos (Previsor)"
            3 -> "3 Turnos (Estratégico)"
            else -> "Error de Config"
        }
    } else {
        "Estilo Q-Learning" to when {
            mood.epsilon > 0.6 -> "Errático"
            mood.epsilon > 0.3 -> "Balanceado"
            else -> "Maestro"
        }
    }
}

fun getMoodVisuals(moodId: String): Pair<Color, ImageVector> {
    return when (moodId.lowercase()) {
        // CLÁSICO (Q-Learning)
        "somnoliento" -> StateSomnoliento to Icons.Rounded.Bedtime
        "relajado" -> StateRelajado to Icons.Rounded.Spa
        "normal" -> StateNormal to Icons.Rounded.SentimentNeutral
        "atento" -> StateAtento to Icons.Rounded.Visibility
        "concentrado" -> StateConcentrado to Icons.Rounded.Psychology

        // GOMOKU (Minimax)
        "gomoku_facil" -> StateGomokuFacil to Icons.Rounded.Water
        "gomoku_medio" -> StateGomokuMedio to Icons.Rounded.Park
        "gomoku_dificil" -> StateGomokuDificil to Icons.Rounded.Bolt

        else -> NeonCyan to Icons.Rounded.SentimentNeutral
    }
}

@Composable
fun AttributeRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun BehaviorBar(label: String, value: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextGray)
            Text("${(value * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value },
            color = color,
            trackColor = SurfaceLight.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

fun getGameModeIcon(mode: GameMode): ImageVector {
    return when (mode) {
        GameMode.CLASSIC -> Icons.Rounded.GridOn
        GameMode.GOMOKU -> Icons.Rounded.Star
        else -> Icons.Rounded.GridOn
    }
}