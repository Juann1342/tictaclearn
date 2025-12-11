package com.chifuz.tictaclearn.domain.model



enum class Player(val symbol: Char) {
    Human('X'),      // Jugador 1
    AI('O'),         // Jugador 2 (Puede ser Humano o IA)
    Triangle('△'),   // Jugador 3
    Star('☆');       // Jugador 4

    companion object {
        fun fromSymbol(symbol: Char): Player = entries.first { it.symbol == symbol }
    }

    // 🚨 MEJORA: Función 'next' que rota sobre la lista de jugadores activos
    fun next(activePlayers: List<Player>): Player {
        val currentIndex = activePlayers.indexOf(this)
        if (currentIndex == -1) return activePlayers.first() // Fallback
        return activePlayers[(currentIndex + 1) % activePlayers.size]
    }

    // Mantenemos 'other' por compatibilidad con código legacy (solo para 2 jugadores)
    fun other(): Player = when(this) {
        Human -> AI
        else -> Human
    }
}

sealed class GameResult {
    data object Playing : GameResult()
    data object Draw : GameResult()
    data class Win(val winner: Player, val winningLine: List<Int>) : GameResult()
}

data class GameState(
    val board: Board,
    val currentPlayer: Player,
    val result: GameResult,
    val gameHistory: List<Board>,
    // 🚨 NUEVO: Lista de jugadores en esta partida específica
    val activePlayers: List<Player> = listOf(Player.Human, Player.AI)
) {
    companion object {
        fun initial() = GameState(
            board = Board(3),
            currentPlayer = Player.Human,
            result = GameResult.Playing,
            gameHistory = listOf(Board(3)),
            activePlayers = listOf(Player.Human, Player.AI)
        )
    }

    val isFinished: Boolean
        get() = result != GameResult.Playing

    fun move(position: Int, player: Player, winningLength: Int): GameState {
        if (!board.isPositionAvailable(position)) return this

        val newCells = board.cells.toMutableList().apply {
            this[position] = player.symbol
        }
        val newBoard = Board(newCells)
        val newResult = newBoard.checkGameResult(winningLength)

        val gameIsOver = newResult != GameResult.Playing
        // 🚨 CAMBIO: Usamos activePlayers para decidir el siguiente turno
        val nextPlayer = if (gameIsOver) player else player.next(activePlayers)

        val updatedHistory = gameHistory + newBoard

        return copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            result = newResult,
            gameHistory = updatedHistory
        )
    }
}