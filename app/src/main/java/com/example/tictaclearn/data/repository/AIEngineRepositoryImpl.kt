package com.example.tictaclearn.data.repository

import android.util.Log
import com.example.tictaclearn.data.datastore.AiMemory
import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.data.datastore.MoodDataStoreManager
import com.example.tictaclearn.data.datastore.QTable
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Reward
import com.example.tictaclearn.domain.repository.AIEngineRepository
import com.example.tictaclearn.domain.model.checkGameResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.math.max
import kotlin.math.min

private const val TAG = "AIEngineRepo"

// Constantes de Algoritmo para Q-Learning
private const val LEARNING_RATE_ALPHA = 0.5
private const val DISCOUNT_FACTOR_GAMMA = 0.9

@Singleton
class AIEngineRepositoryImpl @Inject constructor(
    private val moodDataStoreManager: MoodDataStoreManager,
    private val aiMemoryDataStoreManager: AiMemoryDataStoreManager
) : AIEngineRepository {

    private val mutex = Mutex()
    private val random = Random.Default

    // 🚀 Estado local para la memoria completa (QTable + Contador)
    private var _aiMemory: AiMemory = AiMemory()
    private val qTable: QTable
        get() = _aiMemory.qTable


    // --- HEURÍSTICA Y MINIMAX PARA GOMOKU (9x9) ---

    private val WIN_SCORE = 10000000
    private val LOSE_SCORE = -WIN_SCORE

    /**
     * Asegura que la memoria de la IA (QTable y contador) esté cargada
     * desde el DataStore a la memoria RAM al primer uso.
     */
    private suspend fun ensureMemoryLoaded() {
        if (_aiMemory.qTable.isEmpty() && _aiMemory.gamesPlayedCount == 0) {
            _aiMemory = aiMemoryDataStoreManager.getAiMemory()
        }
    }


    /**
     * Lógica Principal: Decide qué IA usar según el Mood y el Tablero.
     * 🚀 OPTIMIZACIÓN: Usamos withContext(Dispatchers.Default) para mover el cálculo
     * fuera del hilo principal (UI Thread).
     */
    override suspend fun getNextMove(board: Board, currentMood: Mood): Int? = withContext(Dispatchers.Default) {
        // Aseguramos que la memoria se cargue para que Q-Learning funcione.
        ensureMemoryLoaded()

        if (currentMood.minimaxDepth > 0 && board.sideSize == 9) {
            // --- GOMOKU (MINIMAX CON ALPHA-BETA) ---
            findBestGomokuMove(board, currentMood.minimaxDepth, currentMood.gomokuExplorationRate)
        } else {
            // --- CLÁSICO (Q-LEARNING) ---
            findBestClassicMove(board, currentMood.epsilon)
        }
    }

    private fun findBestGomokuMove(board: Board, depth: Int, explorationRate: Double): Int? {
        val availableMoves = board.getAvailablePositions()
        if (availableMoves.isEmpty()) return null

        if (random.nextDouble() < explorationRate) {
            Log.d(TAG, "Gomoku: Exploración aleatoria ($explorationRate).")
            return availableMoves.random(random)
        }

        val centerIndex = (board.sideSize * board.sideSize) / 2
        if (board.cells.all { it == ' ' } && availableMoves.contains(centerIndex)) {
            return centerIndex
        }

        var bestMove: Int? = null
        var bestValue = Int.MIN_VALUE
        var alpha = Int.MIN_VALUE
        val beta = Int.MAX_VALUE

        val sortedMoves = availableMoves.sortedBy { move ->
            val row = move / board.sideSize
            val col = move % board.sideSize
            val center = board.sideSize / 2
            kotlin.math.abs(row - center) + kotlin.math.abs(col - center)
        }

        for (move in sortedMoves) {
            val newBoard = board.simulateMove(move, Player.AI.symbol)
            val moveValue = minimaxAlphaBeta(newBoard, depth - 1, false, alpha, beta)

            if (moveValue > bestValue) {
                bestValue = moveValue
                bestMove = move
            }
            alpha = max(alpha, bestValue)
        }

        return bestMove ?: availableMoves.random()
    }

    private fun minimaxAlphaBeta(board: Board, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int): Int {
        val result = board.checkGameResult(5)

        if (result is GameResult.Win) {
            return if (result.winner == Player.AI) WIN_SCORE + depth else LOSE_SCORE - depth
        }
        if (result is GameResult.Draw) return 0

        if (depth == 0) return evaluateGomokuBoard(board, Player.AI)

        var localAlpha = alpha
        var localBeta = beta
        val availableMoves = board.getAvailablePositions()

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in availableMoves) {
                val newBoard = board.simulateMove(move, Player.AI.symbol)
                val eval = minimaxAlphaBeta(newBoard, depth - 1, false, localAlpha, localBeta)

                maxEval = max(maxEval, eval)
                localAlpha = max(localAlpha, eval)

                if (localBeta <= localAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in availableMoves) {
                val newBoard = board.simulateMove(move, Player.Human.symbol)
                val eval = minimaxAlphaBeta(newBoard, depth - 1, true, localAlpha, localBeta)

                minEval = min(minEval, eval)
                localBeta = min(localBeta, eval)

                if (localBeta <= localAlpha) break
            }
            return minEval
        }
    }

    private fun evaluateGomokuBoard(board: Board, player: Player): Int {
        var score = 0
        val size = board.sideSize

        score += getPatternScore(board, Player.AI.symbol, size)
        score -= (getPatternScore(board, Player.Human.symbol, size) * 1.2).toInt()

        return score
    }

    private fun getPatternScore(board: Board, symbol: Char, size: Int): Int {
        var patternScore = 0
        val directions = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        val scoreMap = mapOf(4 to 10000, 3 to 1000, 2 to 100)

        for (row in 0 until size) {
            for (col in 0 until size) {
                val idx = row * size + col
                if (board.cells[idx] != symbol) continue

                for ((dr, dc) in directions) {
                    for ((length, scoreValue) in scoreMap) {
                        if (checkPattern(board, symbol, size, row, col, dr, dc, length)) {
                            val isOpenBothEnds = isPatternOpenBothEnds(board, size, row, col, dr, dc, length)
                            patternScore += if (isOpenBothEnds) scoreValue * 2 else scoreValue
                            break
                        }
                    }
                }
            }
        }
        return patternScore
    }

    private fun checkPattern(board: Board, symbol: Char, size: Int, startR: Int, startC: Int, dr: Int, dc: Int, length: Int): Boolean {
        for (k in 0 until length) {
            val r = startR + k * dr
            val c = startC + k * dc
            if (r !in 0 until size || c !in 0 until size || board.cells[r * size + c] != symbol) {
                return false
            }
        }
        val prevR = startR - dr
        val prevC = startC - dc
        val nextR = startR + length * dr
        val nextC = startC + length * dc
        val isPrevOpen = isCellEmpty(board, size, prevR, prevC)
        val isNextOpen = isCellEmpty(board, size, nextR, nextC)
        return isPrevOpen || isNextOpen
    }

    private fun isPatternOpenBothEnds(board: Board, size: Int, startR: Int, startC: Int, dr: Int, dc: Int, length: Int): Boolean {
        val prevR = startR - dr
        val prevC = startC - dc
        val nextR = startR + length * dr
        val nextC = startC + length * dc
        return isCellEmpty(board, size, prevR, prevC) && isCellEmpty(board, size, nextR, nextC)
    }

    private fun isCellEmpty(board: Board, size: Int, r: Int, c: Int): Boolean {
        if (r !in 0 until size || c !in 0 until size) return false
        return board.cells[r * size + c] == ' '
    }


    // --- LÓGICA Q-LEARNING (CLÁSICO 3x3) ---

    private fun findBestClassicMove(board: Board, epsilon: Double): Int? {
        val availableMoves = board.getAvailablePositions()
        if (availableMoves.isEmpty()) return null

        val state = boardToState(board)
        val qValues = qTable[state] ?: getHeuristicQValues(state)
        val bestAction = qValues.withIndex()
            .filter { it.index in availableMoves }
            .maxByOrNull { it.value }

        return if (random.nextDouble() < epsilon) {
            availableMoves.random(random)
        } else {
            bestAction?.index ?: availableMoves.random(random)
        }
    }

    private fun getHeuristicQValues(state: String): List<Double> {
        val values = MutableList(9) { 0.0 }
        if (state[4] == ' ') values[4] = 0.1
        listOf(0, 2, 6, 8).filter { state[it] == ' ' }.forEach { values[it] = 0.05 }

        // Actualizamos la memoria local con la nueva entrada heurística
        _aiMemory = _aiMemory.copy(qTable = qTable.toMutableMap().apply { this[state] = values })
        return values
    }

    private fun boardToState(board: Board): String {
        return board.toStateString().replace(Player.AI.symbol, 'O').replace(Player.Human.symbol, 'X')
    }

    // --- MEMORIA Y MOOD (Delegación y Update) ---

    override suspend fun updateMemory(gameHistory: List<Board>) {
        if (gameHistory.isEmpty() || gameHistory.last().sideSize != 3) {
            return // Solo aprendemos en Tic-Tac-Toe
        }

        ensureMemoryLoaded()
        val currentQTable = qTable.toMutableMap()

        mutex.withLock {
            for (i in 0 until gameHistory.size - 1) {
                val state = boardToState(gameHistory[i])
                val stateAfterAiMove = gameHistory[i + 1]
                val stateAfterHumanMove = if (i + 2 < gameHistory.size) gameHistory[i + 2] else null

                val available = gameHistory[i].getAvailablePositions()
                val occupied = stateAfterAiMove.getAvailablePositions()
                val actionIndex = available.firstOrNull { it !in occupied } ?: continue

                var reward = Reward.DEFAULT
                var maxFutureQ = 0.0
                val stateKey = boardToState(gameHistory[i])

                if (stateAfterHumanMove == null) {
                    val finalResult = stateAfterAiMove.checkGameResult(3)
                    reward = Reward.getReward(finalResult, Player.AI)
                    maxFutureQ = 0.0
                } else {
                    val resultAfterHuman = stateAfterHumanMove.checkGameResult(3)
                    if (resultAfterHuman is GameResult.Win && resultAfterHuman.winner == Player.Human) {
                        reward = Reward.LOSE
                        maxFutureQ = 0.0
                    } else if (resultAfterHuman is GameResult.Draw) {
                        reward = Reward.TIE
                    } else {
                        val nextAiStateKey = boardToState(stateAfterHumanMove)
                        val nextQValues = currentQTable[nextAiStateKey] ?: getHeuristicQValues(nextAiStateKey)
                        maxFutureQ = nextQValues.maxOrNull() ?: 0.0
                    }
                }

                val currentQValues = currentQTable[stateKey]?.toMutableList() ?: getHeuristicQValues(stateKey).toMutableList()
                val oldQ = currentQValues[actionIndex]
                val newQ = oldQ + LEARNING_RATE_ALPHA * (reward + DISCOUNT_FACTOR_GAMMA * maxFutureQ - oldQ)

                currentQValues[actionIndex] = newQ
                currentQTable[stateKey] = currentQValues
            }

            // 🚀 ACTUALIZACIÓN CLAVE: Guardamos el nuevo objeto AiMemory y actualizamos el contador.
            val updatedAiMemory = _aiMemory.copy(
                qTable = currentQTable,
                gamesPlayedCount = _aiMemory.gamesPlayedCount + 1
            )
            aiMemoryDataStoreManager.saveAiMemory(updatedAiMemory)
            _aiMemory = updatedAiMemory // Sincronizar estado en memoria
        }
    }

    override suspend fun clearMemory() {
        mutex.withLock {
            // 🚀 ACTUALIZACIÓN CLAVE: Usamos clearAiMemory y reseteamos el estado local.
            aiMemoryDataStoreManager.clearAiMemory()
            _aiMemory = AiMemory()
        }
    }

    // 🚀 IMPLEMENTACIÓN DEL NUEVO MÉTODO
    override suspend fun getClassicGamesPlayedCount(): Int {
        ensureMemoryLoaded()
        return _aiMemory.gamesPlayedCount
    }

    override suspend fun getDailyMood(): Mood {
        val id = moodDataStoreManager.getMoodId()
        return Mood.fromId(id) ?: Mood.getDefaultDailyMood()
    }

    override suspend fun saveDailyMood(mood: Mood) {
        moodDataStoreManager.saveMoodId(mood.id)
    }
}