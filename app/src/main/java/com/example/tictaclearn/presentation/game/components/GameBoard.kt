package com.example.tictaclearn.presentation.game.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tictaclearn.domain.model.Board // Importamos Board

// Ya no necesitamos importar Cell, ya que usamos 'Char' directamente ('X', 'O', ' ')

@Composable
fun GameBoard(
    board: Board,
    onCellClicked: (Int) -> Unit, // ❌ CAMBIO: Ahora recibe solo el índice plano (0-8)
    isProcessing: Boolean // Para deshabilitar la interacción
) {
    // Usamos Column y Row para simular el grid
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Itera sobre las filas (0, 1, 2)
        (0..2).forEach { rowIndex ->
            Row {
                // Itera sobre las columnas (0, 1, 2)
                (0..2).forEach { colIndex ->
                    val position = rowIndex * 3 + colIndex // Calcula el índice plano (0-8)
                    val cellContent = board.cells[position] // Obtiene el contenido de la celda

                    BoardCell(
                        cellContent = cellContent,
                        onClick = {
                            // Solo permite el click si NO está en proceso y la celda está vacía (' ')
                            if (!isProcessing && cellContent == ' ') {
                                onCellClicked(position) // Pasa el índice plano
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BoardCell(cellContent: Char, onClick: () -> Unit) {
    // Usamos el `clickable` con un modificador para asegurar que toda la superficie sea clickable
    Box(
        modifier = Modifier
            .size(100.dp) // Tamaño de la celda ligeramente más grande para mejor tap target
            .border(2.dp, MaterialTheme.colorScheme.primary) // Borde más grueso y colorido
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val text = if (cellContent == ' ') "" else cellContent.toString()

        Text(
            text = text,
            style = MaterialTheme.typography.displayLarge, // Fuente más grande
            color = when (cellContent) {
                // 'X' (Humano) y 'O' (IA) reciben colores distintivos
                'X' -> MaterialTheme.colorScheme.primary
                'O' -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}