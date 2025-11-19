package com.example.tictaclearn.presentation.game.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictaclearn.domain.model.Board

@Composable
fun GameBoard(
    board: Board,
    onCellClicked: (Int) -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    // Usamos el sideSize del tablero (3 o 9) para definir las columnas
    val columns = board.sideSize

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp), // Un poco de margen general
        userScrollEnabled = false // Evitamos scroll interno, que encaje en pantalla
    ) {
        items(board.cells.size) { index ->
            BoardCell(
                cellContent = board.cells[index],
                onClick = {
                    if (!isProcessing && board.cells[index] == ' ') {
                        onCellClicked(index)
                    }
                },
                // Ajustamos el tamaño de fuente dinámicamente: más chico si es 9x9
                fontSize = if (columns > 3) 20 else 60
            )
        }
    }
}

@Composable
fun BoardCell(
    cellContent: Char,
    onClick: () -> Unit,
    fontSize: Int
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // 🔥 CLAVE: Esto fuerza a que sea perfectamente cuadrado
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)) // Borde sutil
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (cellContent != ' ') {
            Text(
                text = cellContent.toString(),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                color = when (cellContent) {
                    'X' -> MaterialTheme.colorScheme.primary
                    'O' -> MaterialTheme.colorScheme.secondary
                    else -> Color.Unspecified
                }
            )
        }
    }
}