package com.example.tictaclearn.presentation.configuration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
// Concentrado
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.ui.theme.*

@Composable
fun ConfigurationScreen(
    viewModel: ConfigurationViewModel = hiltViewModel(),
    onStartGame: (moodId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.feedbackShown()
        }
    }

    // Determinamos el color y el icono según el estado actual para dar feedback visual
    // Asumo que los IDs o Nombres coinciden más o menos con lo que dijiste
    val (moodColor, moodIcon) = getMoodVisuals(uiState.currentMood.displayName)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 24.dp), // Quitamos padding horizontal global para que el scroll llegue al borde
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- HEADER ---
            Text(
                text = "TicTacLEarn",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "AI NEURAL STATUS", // Texto más "tech"
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- AI VISUALIZER (Dinámico) ---
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                moodColor.copy(alpha = 0.3f), // El brillo cambia según el ánimo
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, moodColor, CircleShape), // El borde cambia según el ánimo
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = moodIcon,
                    contentDescription = "AI State",
                    tint = moodColor,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            } else {
                // --- MOOD INDICATOR ---
                Text(
                    text = uiState.currentMood.displayName.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = moodColor // El texto también se tiñe del color del ánimo
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- MOOD SELECTION (Scrollable) ---
                // Pasamos moodColor para que el chip seleccionado tenga el color correcto
                MoodChipsSelector(
                    currentMood = uiState.currentMood,
                    availableMoods = uiState.availableMoods,
                    onMoodSelected = viewModel::onMoodSelected,
                    activeColor = moodColor
                )

                Spacer(modifier = Modifier.height(50.dp))

                // --- ACTIONS (Con padding horizontal recuperado) ---
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Button(
                        onClick = { onStartGame(uiState.currentMood.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "COMENZAR PARTIDA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BackgroundDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = viewModel::onResetMemoryClicked,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text("BORRAR MEMORIA (RESET)")
                    }
                }
            }
        }
    }
}

@Composable
fun MoodChipsSelector(
    currentMood: Mood,
    availableMoods: List<Mood>,
    onMoodSelected: (Mood) -> Unit,
    activeColor: Color
) {
    // Usamos LazyRow para permitir scroll horizontal si son muchos elementos (5 estados)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp), // Espacio al inicio y final del scroll
        horizontalArrangement = Arrangement.spacedBy(8.dp), // Espacio entre chips
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableMoods) { mood ->
            val isSelected = mood.id == currentMood.id

            FilterChip(
                selected = isSelected,
                onClick = { onMoodSelected(mood) },
                label = { Text(mood.displayName) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = activeColor.copy(alpha = 0.2f), // Fondo suave del color activo
                    selectedLabelColor = activeColor, // Texto del color activo
                    labelColor = TextGray,
                    containerColor = SurfaceDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) activeColor else SurfaceLight,
                    borderWidth = 1.dp, enabled = true, selected = false
                ),
                shape = RoundedCornerShape(50) // Pill shape
            )
        }
    }
}

// Función auxiliar para dar personalidad a cada estado
// Ajusta los nombres (strings) según como los tengas exactamente en tu base de datos/modelo
fun getMoodVisuals(moodName: String): Pair<Color, ImageVector> {
    return when (moodName.lowercase()) {
        "somnoliento" -> Color(0xFF78909C) to Icons.Default.Settings // Gris Azulado
        "relajado" -> Color(0xFF00E676) to Icons.Default.Face            // Verde
        "normal" -> NeonCyan to Icons.Default.ThumbUp              // Cyan (Nuestro color base)
        "atento" -> Color(0xFFFFD600) to Icons.Default.Warning      // Amarillo
        "concentrado" -> Color(0xFFD500F9) to Icons.Default.Search       // Violeta Intenso
        else -> NeonCyan to Icons.Default.ThumbUp
    }
}