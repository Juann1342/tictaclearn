// domain/model/Board.kt
package com.example.tictaclearn.domain.model
// Importaciones necesarias:
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Cell

// Asumo que tu clase Board luce así:
data class Board(
    val cells: List<List<Cell>> = List(SIZE) { List(SIZE) { Cell.EMPTY } }
) {
    companion object {
        const val SIZE = 3
    }

    // 💡 FUNCIÓN DE REGLAS DEL JUEGO: Determina el estado del Board
    fun checkGameStatus(): GameResult {
        val size = SIZE

        // 1. Verificar Filas, Columnas y Diagonales
        for (i in 0 until size) {

            // Verificar Fila
            if (cells[i][0] != Cell.EMPTY && cells[i][0] == cells[i][1] && cells[i][0] == cells[i][2]) {
                return GameResult.Win(if (cells[i][0] == Cell.X) Player.AI else Player.HUMAN)
            }

            // Verificar Columna
            if (cells[0][i] != Cell.EMPTY && cells[0][i] == cells[1][i] && cells[0][i] == cells[2][i]) {
                return GameResult.Win(if (cells[0][i] == Cell.X) Player.AI else Player.HUMAN)
            }
        }

        // Verificar Diagonal Principal (Top-Left to Bottom-Right)
        if (cells[0][0] != Cell.EMPTY && cells[0][0] == cells[1][1] && cells[0][0] == cells[2][2]) {
            return GameResult.Win(if (cells[0][0] == Cell.X) Player.AI else Player.HUMAN)
        }

        // Verificar Diagonal Secundaria (Top-Right to Bottom-Left)
        if (cells[0][2] != Cell.EMPTY && cells[0][2] == cells[1][1] && cells[0][2] == cells[2][0]) {
            return GameResult.Win(if (cells[0][2] == Cell.X) Player.AI else Player.HUMAN)
        }

        // 2. Verificar Empate (Si no hay celdas vacías)
        val isBoardFull = cells.flatten().none { it == Cell.EMPTY }
        if (isBoardFull) {
            return GameResult.Draw
        }

        // 3. El juego continúa
        return GameResult.Playing
    }
}