package com.chifuz.tictaclearn.domain.model

import kotlin.math.sqrt

/**
 * Representa el estado del tablero.
 * Soporta tableros de 3x3 (TicTacToe) y 9x9 (Gomoku) dinámicamente.
 */
data class Board(
    val cells: List<Char>
) {
    // PROPIEDAD RESTAURADA: Calcula la dimensión del lado (3 o 9)
    val sideSize: Int = sqrt(cells.size.toDouble()).toInt()

    // Constructor por defecto para 3x3 vacío (útil para tests o init)
    constructor() : this(List(9) { ' ' })

    // Constructor para crear un tablero vacío de tamaño específico (ej: 9 para Gomoku)
    constructor(size: Int) : this(List(size * size) { ' ' })

    val isFull: Boolean
        get() = cells.none { it == ' ' }

    fun isPositionAvailable(position: Int): Boolean {
        return position in cells.indices && cells[position] == ' '
    }

    fun toStateString(): String = cells.joinToString("")

    fun getAvailablePositions(): List<Int> {
        return cells.withIndex()
            .filter { it.value == ' ' }
            .map { it.index }
    }

    /**
     * Simula un movimiento sin alterar este tablero (retorna uno nuevo).
     * Útil para que la IA "imagine" jugadas.
     */
    fun simulateMove(position: Int, symbol: Char): Board {
        val newCells = cells.toMutableList()
        newCells[position] = symbol
        return Board(newCells)
    }
}

/**
 * Verifica si hay un ganador.
 * @param winningLength Cuántas en línea para ganar (3 para TicTacToe, 5 para Gomoku).
 */
fun Board.checkGameResult(winningLength: Int = if (sideSize > 3) 5 else 3): GameResult {
    val size = sideSize
    val board = cells

    // Función auxiliar para obtener celda (fila, columna) de forma segura
    fun getCell(row: Int, col: Int): Char {
        if (row !in 0 until size || col !in 0 until size) return ' '
        return board[row * size + col]
    }

    // Direcciones: Horizontal, Vertical, Diagonal \, Diagonal /
    val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)

    for (row in 0 until size) {
        for (col in 0 until size) {
            val symbol = getCell(row, col)
            if (symbol == ' ') continue

            for ((dr, dc) in directions) {
                val winningIndices = mutableListOf<Int>()
                var count = 0

                // Verificamos si hay winningLength fichas seguidas
                for (k in 0 until winningLength) {
                    val r = row + k * dr
                    val c = col + k * dc
                    if (getCell(r, c) == symbol) {
                        count++
                        winningIndices.add(r * size + c)
                    } else {
                        break
                    }
                }

                if (count == winningLength) {
                    return GameResult.Win(Player.fromSymbol(symbol), winningIndices)
                }
            }
        }
    }

    if (isFull) return GameResult.Draw
    return GameResult.Playing
}