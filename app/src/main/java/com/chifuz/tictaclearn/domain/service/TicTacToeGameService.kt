package com.chifuz.tictaclearn.domain.service

import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.GameMode
import com.chifuz.tictaclearn.domain.repository.AIEngineRepository
import com.chifuz.tictaclearn.domain.model.Board
import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Player
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class TicTacToeGameService @Inject constructor(
    private val aiEngineRepository: AIEngineRepository
) {
    var gameState: GameState = GameState.initial()
        private set

    var currentMood: Mood? = null
        private set

    private var currentGameMode: GameMode = GameMode.CLASSIC

    // 🚨 Configuración específica de la sesión actual
     var isPartyModeAiEnabled: Boolean = false
    private var partyPlayersCount: Int = 2

    suspend fun initializeGame(moodId: String, gameModeId: String) {
        val mood = Mood.fromId(moodId) ?: Mood.getDefaultDailyMood()

        // 🚨 DETECCIÓN DE MODO PARTY (formato: "gomoku_party|<count>|<aiEnabled>")
        if (gameModeId.startsWith("gomoku_party")) {
            currentGameMode = GameMode.PARTY
            val parts = gameModeId.split("|")
            partyPlayersCount = parts.getOrNull(1)?.toIntOrNull() ?: 2
            isPartyModeAiEnabled = parts.getOrNull(2)?.toBooleanStrictOrNull() ?: false
        } else {
            currentGameMode = GameMode.fromId(gameModeId) ?: GameMode.CLASSIC
            isPartyModeAiEnabled = true // En Classic/Normal Gomoku, la IA (Player.AI) siempre es IA
            partyPlayersCount = 2
        }

        currentMood = mood
        resetGame()
    }

    fun handleHumanTurn(position: Int): GameState {
        // Solo permitimos mover si el juego no terminó y la posición está libre
        if (!gameState.board.isPositionAvailable(position) || gameState.isFinished) return gameState

        val currentPlayer = gameState.currentPlayer

        // 🚨 LÓGICA CRÍTICA CORREGIDA:
        // ¿Es turno de la IA?
        // Esto solo ocurre si el jugador actual es Player.AI ('O') Y la IA está habilitada para esta sesión.
        val isAiTurn = isPartyModeAiEnabled && currentPlayer == Player.AI

        if (!isAiTurn) {
            // Es turno de un humano (sea X, O, △ o ☆)
            // Ya que el jugador actual NO es la IA, aplicamos el movimiento.
            // Esto cubre a Player.Human ('X'), Player.Triangle ('△'), y Player.Star ('☆')
            // Y también a Player.AI ('O') cuando isPartyModeAiEnabled es false (PvP)
            gameState = gameState.move(position, currentPlayer, currentGameMode.winningLength)
        }

        return gameState
    }

    suspend fun handleAiTurn(): GameState {
        if (gameState.isFinished) return gameState

        // 🚨 Solo actuamos si es turno de Player.AI Y la IA está habilitada para esta partida
        if (gameState.currentPlayer == Player.AI && isPartyModeAiEnabled) {
            val moveIndex = aiEngineRepository.getNextMove(
                board = gameState.board,
                currentMood = currentMood ?: Mood.getDefaultDailyMood()
            )

            if (moveIndex != null && gameState.board.isPositionAvailable(moveIndex)) {
                gameState = gameState.move(moveIndex, Player.AI, currentGameMode.winningLength)
            }
        }
        return gameState
    }

    suspend fun resetGame() {
        val emptyBoard = Board(size = currentGameMode.boardSize)

        // 🚨 Construir lista de jugadores
        val players = mutableListOf(Player.Human, Player.AI) // Mínimo 2
        if (partyPlayersCount >= 3) players.add(Player.Triangle)
        if (partyPlayersCount >= 4) players.add(Player.Star)

        // En Party, siempre empieza X (Human) por simplicidad, o random.
        // En Classic, random.
        val startingPlayer = if (currentGameMode == GameMode.PARTY) {
            Player.Human
        } else {
            if (Random.nextBoolean()) Player.Human else Player.AI
        }

        gameState = GameState(
            board = emptyBoard,
            currentPlayer = startingPlayer,
            result = GameResult.Playing,
            gameHistory = listOf(emptyBoard),
            activePlayers = players // Pasamos la lista configurada
        )
    }

    fun getCurrentGameMode() = currentGameMode
}