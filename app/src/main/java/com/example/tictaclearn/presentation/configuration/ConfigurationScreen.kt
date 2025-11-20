package com.example.tictaclearn.presentation.configuration

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
import androidx.compose.material.icons.filled.* // Imports genéricos
// 👇 NUEVOS IMPORTS: Los iconos específicos para tu "Neural Arena"
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Filter1
import androidx.compose.material.icons.rounded.Filter2
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Visibility
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
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameMode
import com.example.tictaclearn.ui.theme.*
import androidx.compose.ui.text.TextStyle // Import necesario para el Shadow
import androidx.compose.ui.graphics.Shadow // Import necesario para el Shadow

@Composable
fun ConfigurationScreen(
    onStartGame: (moodId: String, gameModeId: String) -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animación de entrada
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

            // --- HEADER (Mejora de Branding con GLOW) ---
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
                    // 🚨 CAMBIO: Aplicamos un estilo de texto más impactante y GLOW para el branding
                    text = "TicTacLearn",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = 2.sp,
                        shadow = Shadow( // Efecto Glow
                            color = NeonOrange.copy(alpha = 0.7f),
                            offset = Offset(0f, 0f),
                            blurRadius = 15f
                        )
                    ),
                    color = NeonOrange, // Usamos NeonOrange para el título

                )
            }

            // --- ZONA DE CONTROL ---

            // 1. Selector de Modo (Título alineado)
            Text(
                text = "MODO DE JUEGO",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .graphicsLayer { alpha = entranceAlpha.value }
            )
            Spacer(modifier = Modifier.height(12.dp))

            GameModeSelector(
                currentMode = uiState.selectedGameMode,
                availableModes = uiState.availableGameModes,
                onModeSelected = viewModel::onGameModeSelected,
                modifier = Modifier.graphicsLayer { translationX = entranceOffset.value * 0.5f }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Selector de Ánimo (Título alineado)
            Text(
                text = "NIVEL DE AMENAZA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
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

                // 3. Tarjeta Principal (Mejorada visualmente)
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    // 🚨 REEMPLAZO: MoodDescriptionCard contiene la nueva estética
                    MoodDescriptionCard(uiState.currentMood)
                }

                Spacer(modifier = Modifier.height(48.dp))

                // --- BOTONES DE ACCIÓN (Mejora de Jerarquía) ---
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            translationY = entranceOffset.value
                            alpha = entranceAlpha.value
                        }
                ) {
                    // 🚨 PRIMARIO: Botón de Neón Naranja (Mayor altura, más redondeado)
                    Button(
                        onClick = { onStartGame(uiState.currentMood.id, uiState.selectedGameMode.id) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp), // Más alto para el CTA principal
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

                    // 🚨 SECUNDARIO: Botón Outlined de Neón Rojo (Menor altura, color de acento)
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

// --- COMPONENTES ACTUALIZADOS ---

@Composable
fun GameModeSelector(
    currentMode: GameMode,
    availableModes: List<GameMode>,
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(availableModes) { mode ->
            val isSelected = mode == currentMode
            val color = if (isSelected) NeonCyan else SurfaceLight
            val icon = if (mode == GameMode.GOMOKU) Icons.Rounded.GridOn else Icons.Rounded.Star

            // 🚨 CAMBIO: FilterChip para un look más moderno y mejor feedback (Estilo Neón)
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
                    selectedContainerColor = color.copy(alpha = 0.15f), // Fondo sutil del color neón
                    selectedLabelColor = color,
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if(isSelected) color else Color.Transparent,
                    borderWidth = 2.dp, // Borde más grueso para resaltar el neón
                    enabled = true, selected = isSelected
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

            // 🚨 CAMBIO: Uso de FilterChip para consistencia y mejor feedback (Estilo Neón)
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
        // 🚨 CAMBIO: Borde sutil y más redondeado
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
                // 🚨 CAMBIO: Icono grande con fondo circular sutil (Glow/Aura)
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
                    // 🚨 CAMBIO: Descripción corta debajo del título
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

            // 🚨 CAMBIO: Contenido Expandido
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

                    val intelligenceType = if (mood.minimaxDepth > 0) "ESTRATEGA (Lógica)" else "CREATIVA (Aprendizaje)"
                    AttributeRow(label = "Tipo de Mente", value = intelligenceType, color = color)

                    val (styleLabel, styleValue) = getPlayStyle(mood)
                    AttributeRow(label = styleLabel, value = styleValue, color = TextWhite)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🚨 CAMBIO: Barra de Progreso mejorada
                    if (mood.minimaxDepth == 0) {
                        BehaviorBar(
                            label = "Nivel de Caos / Creatividad",
                            value = mood.epsilon.toFloat(),
                            color = color
                        )
                    } else {
                        val normalizedDepth = (mood.minimaxDepth / 5f).coerceIn(0f, 1f)
                        BehaviorBar(
                            label = "Potencia de Cálculo",
                            value = normalizedDepth,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

// --- UTILS (Mejoras menores de estilo) ---

@Composable
fun AttributeRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
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
        Spacer(modifier = Modifier.height(8.dp))
        val animatedProgress by animateFloatAsState(targetValue = value, label = "bar")
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = SurfaceLight.copy(alpha = 0.2f)
        )
    }
}

fun getShortPersonaDescription(mood: Mood): String {
    return when {
        mood.minimaxDepth >= 4 -> "⚠️ Peligro: Letal."
        mood.minimaxDepth in 1..3 -> "Calculadora y fría."
        mood.epsilon > 0.5 -> "Distraída y experimental."
        mood.epsilon < 0.2 -> "Juega de memoria."
        else -> "Un rival digno."
    }
}

fun getPlayStyle(mood: Mood): Pair<String, String> {
    return if (mood.minimaxDepth > 0) {
        "Visión Futura" to when (mood.minimaxDepth) {
            1 -> "1 Turno"
            2 -> "2 Turnos"
            3 -> "3 Turnos"
            else -> "Omnisciente"
        }
    } else {
        "Estilo" to when {
            mood.epsilon > 0.6 -> "Errático"
            mood.epsilon > 0.3 -> "Balanceado"
            else -> "Maestro"
        }
    }
}

// 👇 Iconos y Colores para los Moods
fun getMoodVisuals(moodId: String): Pair<Color, ImageVector> {
    return when (moodId.lowercase()) {
        // CLÁSICO (Q-Learning)
        "somnoliento" -> Color(0xFFB0BEC5) to Icons.Rounded.Bedtime // 🌙 Luna (Gris)
        "relajado" -> Color(0xFF81C784) to Icons.Rounded.Spa         // 🌸 Spa/Zen (Verde)
        "normal" -> NeonOrange to Icons.Rounded.SentimentNeutral     // 😐 Neutral (Naranja)
        "atento" -> Color(0xFFFB8C00) to Icons.Rounded.Visibility    // 👁️ Ojo (Naranja Fuerte)
        "concentrado" -> NeonRed to Icons.Rounded.Psychology         // 🧠 Cerebro (Rojo)

        // GOMOKU (Minimax)
        "gomoku_facil" -> Color(0xFF78909C) to Icons.Rounded.Filter1 // 1️⃣ Nivel 1
        "gomoku_medio" -> NeonCyan to Icons.Rounded.Filter2          // 2️⃣ Nivel 2
        "gomoku_dificil" -> Color(0xFFD500F9) to Icons.Rounded.Bolt  // ⚡ Rayo (Violeta)

        else -> NeonCyan to Icons.Rounded.SentimentNeutral
    }
}