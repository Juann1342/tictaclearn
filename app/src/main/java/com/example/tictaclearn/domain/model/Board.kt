package com.example.tictaclearn.domain.model

private const val BOARD_SIZE = 9

/**
 * Representa el estado actual del tablero de juego.
 */
data class Board(
    val cells: List<Char> = List(BOARD_SIZE) { ' ' }
) {
    val isFull: Boolean
        get() = cells.none { it == ' ' }

    fun isPositionAvailable(position: Int): Boolean {
        return position in 0 until BOARD_SIZE && cells[position] == ' '
    }

    fun toStateString(): String = cells.joinToString("")

    fun getAvailablePositions(): List<Int> {
        return cells.withIndex()
            .filter { it.value == ' ' }
            .map { it.index }
    }

    /**
     * Simula un movimiento sin alterar este tablero (retorna uno nuevo).
     */
    private fun simulateMove(position: Int, symbol: Char): Board {
        val newCells = cells.toMutableList()
        newCells[position] = symbol
        return Board(newCells)
    }

    /**
     * Busca si hay un movimiento ganador inmediato para el jugador dado.
     * @return El índice del movimiento ganador o null si no hay.
     */
    fun findWinningMove(playerSymbol: Char): Int? {
        for (pos in getAvailablePositions()) {
            val simulatedBoard = simulateMove(pos, playerSymbol)
            val result = simulatedBoard.checkGameResult()
            if (result is GameResult.Win && result.winner.symbol == playerSymbol) {
                return pos
            }
        }
        return null
    }
}

/**
 * Extensión para verificar resultados.
 */
fun Board.checkGameResult(): GameResult {
    val winConditions = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
        listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonales
    )

    for (line in winConditions) {
        val (a, b, c) = line
        val c1 = cells[a]
        val c2 = cells[b]
        val c3 = cells[c]

        if (c1 != ' ' && c1 == c2 && c2 == c3) {
            val winner = Player.fromSymbol(c1)
            return GameResult.Win(winner, line)
        }
    }

    if (isFull) return GameResult.Draw
    return GameResult.Playing
}