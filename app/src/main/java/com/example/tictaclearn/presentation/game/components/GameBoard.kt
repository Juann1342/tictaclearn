package com.example.tictaclearn.presentation.game.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.ui.theme.*

// NUEVO: GameStatusCardWrapper (Contenedor visual para el indicador de estado)
@Composable
fun GameStatusCardWrapper(gameState: GameState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            GameStatusIndicator(gameState)
        }
    }
}

// MODIFICADO: GameBoard (Solo cambiamos el padding)
@Composable
fun GameBoard(
    board: Board,
    onCellClicked: (Int) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    val columns = board.sideSize

    // Contenedor para el tablero (Mejoramos el look del borde principal)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, SurfaceLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) // Borde exterior grueso
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(4.dp), // Menos padding
            horizontalArrangement = Arrangement.spacedBy(if (columns > 3) 1.dp else 4.dp), // Espaciado menor para 9x9
            verticalArrangement = Arrangement.spacedBy(if (columns > 3) 1.dp else 4.dp), // Espaciado menor para 9x9
            userScrollEnabled = false
        ) {
            items(board.cells.size) { index ->
                BoardCell(
                    cellContent = board.cells[index],
                    onClick = {
                        if (!isProcessing && board.cells[index] == ' ') {
                            onCellClicked(index)
                        }
                    },
                    fontSize = if (columns > 3) 24 else 60
                )
            }
        }
    }
}

// MODIFICADO: BoardCell (Efecto 'Neon Glow' en las piezas)
@Composable
fun BoardCell(
    cellContent: Char,
    onClick: () -> Unit,
    fontSize: Int
) {
    val textColor = when (cellContent) {
        'X' -> NeonOrange
        'O' -> NeonCyan
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, SurfaceLight.copy(alpha = 0.1f)) // Borde interno muy sutil
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cellContent != ' ') {
            Text(
                text = cellContent.toString(),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow( // EFECTO NEON GLOW
                        color = textColor.copy(alpha = 0.9f),
                        offset = Offset(0f, 0f),
                        blurRadius = 12f
                    )
                )
            )
        }
    }
}