package com.example.tictaclearn.presentation.game.components

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
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.Cell

@Composable
fun GameBoard(
    board: Board,
    onCellClicked: (Int, Int) -> Unit,
    isProcessing: Boolean // Para deshabilitar la interacción
) {
    // Usamos Column y Row para simular el grid
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        board.cells.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    BoardCell(
                        cell = cell,
                        onClick = {
                            // Solo permite el click si NO está en proceso y la celda está vacía
                            if (!isProcessing && cell == Cell.EMPTY) {
                                onCellClicked(rowIndex, colIndex)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BoardCell(cell: Cell, onClick: () -> Unit) {
    // Implementación simple de una celda con un botón o un Box
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(90.dp) // Tamaño de la celda
            .border(1.dp, MaterialTheme.colorScheme.onSurface),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            val text = when (cell) {
                Cell.X -> "X"
                Cell.O -> "O"
                Cell.EMPTY -> ""
            }
            Text(
                text = text,
                style = MaterialTheme.typography.displayMedium,
                color = if (cell == Cell.X) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    }
}