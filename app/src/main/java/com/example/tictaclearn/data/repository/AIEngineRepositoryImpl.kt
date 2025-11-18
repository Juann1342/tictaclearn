package com.example.tictaclearn.data.repository

import android.util.Log
import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.data.datastore.MoodDataStoreManager
import com.example.tictaclearn.data.datastore.QTable
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.GameResult
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.repository.AIEngineRepository
import com.example.tictaclearn.domain.model.checkGameResult
import com.example.tictaclearn.domain.model.Reward
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.math.abs

private const val TAG = "AIEngineRepo"

@Singleton
class AIEngineRepositoryImpl @Inject constructor(
    private val moodDataStoreManager: MoodDataStoreManager,
    private val aiMemoryDataStoreManager: AiMemoryDataStoreManager
) : AIEngineRepository {

    private val mutex = Mutex()
    private val random = Random.Default
    private var qTable: QTable = emptyMap()

    private companion object {
        const val LEARNING_RATE_ALPHA = 0.5
        const val DISCOUNT_FACTOR_GAMMA = 0.9
        const val BOARD_SIZE = 9
    }

    // --- Helper functions omitidas para brevedad (son las mismas de antes) ---
    private fun boardToState(board: Board): String = board.toStateString()

    private fun findLastAIMove(previousBoard: Board, currentBoard: Board): Int? {
        val aiSymbol = Player.AI.symbol
        for (i in 0 until BOARD_SIZE) {
            if (previousBoard.cells[i] == ' ' && currentBoard.cells[i] == aiSymbol) return i
        }
        return null
    }

    private fun getHeuristicQValues(state: String): List<Double> {
        val qValues = MutableList(BOARD_SIZE) { 0.0 }
        val center = 4
        val corners = listOf(0, 2, 6, 8)
        val edges = listOf(1, 3, 5, 7)
        for (i in 0 until BOARD_SIZE) {
            val cellChar = state.getOrElse(i) { ' ' }
            val isEmpty = cellChar != Player.Human.symbol && cellChar != Player.AI.symbol
            if (isEmpty) {
                qValues[i] = when (i) {
                    center -> 0.5
                    in corners -> 0.2
                    in edges -> 0.1
                    else -> 0.0
                }
            } else {
                qValues[i] = -1.0
            }
        }
        return qValues
    }

    // 🔄 CAMBIO PRINCIPAL AQUÍ
    override suspend fun getDailyMood(): Mood {
        // 1. Antes de devolver nada, verificamos si cambiamos de día.
        // Si es un nuevo día, esta función actualizará el Mood guardado en el DataStore.
        moodDataStoreManager.updateDailyMoodSequenceIfNeeded()

        // 2. Ahora recuperamos el Mood actual (ya sea el nuevo del día o el que estaba)
        val moodId = moodDataStoreManager.getMoodId()
        return Mood.fromId(moodId)
    }

    override suspend fun saveDailyMood(mood: Mood) {
        moodDataStoreManager.saveMoodId(mood.id)
    }

    override suspend fun getNextMove(board: Board, currentMood: Mood): Int? {
        mutex.withLock {
            if (qTable.isEmpty()) { qTable = aiMemoryDataStoreManager.getQTable() }
        }

        val emptyActions = board.getAvailablePositions()
        if (emptyActions.isEmpty()) return null

        // Reglas de supervivencia (desactivadas para niveles bajos)
        val useInstincts = currentMood.id != Mood.SOMNOLIENTO.id && currentMood.id != Mood.RELAJADO.id

        if (useInstincts) {
            val winningMove = board.findWinningMove(Player.AI.symbol)
            if (winningMove != null) return winningMove
            val blockingMove = board.findWinningMove(Player.Human.symbol)
            if (blockingMove != null) return blockingMove
        }

        // Memoria + Heurística
        val state = boardToState(board)
        val qValues = qTable[state] ?: getHeuristicQValues(state)

        if (random.nextDouble() < currentMood.epsilon) {
            return emptyActions.randomOrNull()
        } else {
            val maxQ = qValues.maxOrNull() ?: 0.0
            val bestActions = emptyActions.filter { actionIndex ->
                abs(qValues[actionIndex] - maxQ) < 0.0001
            }
            return bestActions.randomOrNull() ?: emptyActions.randomOrNull()
        }
    }

    override suspend fun updateMemory(gameHistory: List<Board>) {
        mutex.withLock {
            if (gameHistory.size < 2) return

            qTable = aiMemoryDataStoreManager.getQTable()
            val currentQTable = qTable.toMutableMap()
            var learningEvents = 0

            for (i in gameHistory.size - 2 downTo 0) {
                val stateBeforeAiMove = gameHistory[i]
                val stateAfterAiMove = gameHistory[i + 1]
                val actionIndex = findLastAIMove(stateBeforeAiMove, stateAfterAiMove) ?: continue

                val stateKey = boardToState(stateBeforeAiMove)
                var reward = 0.0
                var maxFutureQ = 0.0

                val resultImmediate = stateAfterAiMove.checkGameResult()

                if (resultImmediate is GameResult.Win && resultImmediate.winner == Player.AI) {
                    reward = Reward.WIN
                    maxFutureQ = 0.0
                } else if (resultImmediate is GameResult.Draw) {
                    reward = Reward.TIE
                    maxFutureQ = 0.0
                } else {
                    if (i + 2 < gameHistory.size) {
                        val stateAfterHumanMove = gameHistory[i + 2]
                        val resultAfterHuman = stateAfterHumanMove.checkGameResult()

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
                }

                val currentQValues = currentQTable[stateKey]?.toMutableList() ?: getHeuristicQValues(stateKey).toMutableList()
                val oldQ = currentQValues[actionIndex]
                val newQ = oldQ + LEARNING_RATE_ALPHA * (reward + DISCOUNT_FACTOR_GAMMA * maxFutureQ - oldQ)

                currentQValues[actionIndex] = newQ
                currentQTable[stateKey] = currentQValues
                learningEvents++
            }

            aiMemoryDataStoreManager.saveQTable(currentQTable)
            qTable = currentQTable
            Log.d(TAG, "Memoria actualizada ($learningEvents pasos).")
        }
    }

    override suspend fun clearMemory() {
        mutex.withLock {
            qTable = emptyMap()
            aiMemoryDataStoreManager.clearQTable()
        }
    }
}