package com.example.tictaclearn.domain.service

import com.example.tictaclearn.data.datastore.AiMemoryDataStoreManager
import com.example.tictaclearn.domain.ai.TicTacToeQAgent
import com.example.tictaclearn.domain.model.GameState
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.Player
import com.example.tictaclearn.domain.model.Reward
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase Singleton responsable de la lógica del juego (tablero) y la orquestación
 * entre el Agente de IA y el DataStore de memoria.
 */
@Singleton
class TicTacToeGameService @Inject constructor(
    private val memoryManager: AiMemoryDataStoreManager
) {
    private var qAgent: TicTacToeQAgent? = null

    // CORRECCIÓN 1: Inicialización con el estado inicial estático.
    var gameState: GameState = GameState.initial()
        private set

    private var lastAiState: String? = null
    private var lastAiAction: Int? = null

    /**
     * Inicializa el servicio cargando la memoria (Q-Table) y configurando el agente de IA.
     * @param moodId El ID del estado de ánimo (ej: "concentrado").
     */
    suspend fun initializeGame(moodId: String) {
        val mood = Mood.ALL_MOODS.firstOrNull { it.id == moodId } ?: Mood.getDefaultDailyMood()
        val qTable = memoryManager.getQTable()

        qAgent = TicTacToeQAgent(
            qTable = qTable,
            mood = mood
        )

        // CORRECCIÓN 2: Iniciar un nuevo GameState usando el companion object.
        gameState = GameState.initial()
        lastAiState = null
        lastAiAction = null
    }

    /**
     * Ejecuta el turno del jugador humano.
     * @param position Índice de la celda (0-8).
     * @return El GameState actualizado.
     */
    fun handleHumanTurn(position: Int): GameState {
        // Usar la función de Board para verificar
        if (gameState.board.isPositionAvailable(position)) {
            gameState = gameState.move(position, Player.Human)
        }

        // Usar la propiedad computada isFinished (la lógica de guardado va en el ViewModel)
        if (gameState.isFinished) {
            clearLearningState()
        }

        return gameState
    }

    /**
     * Ejecuta el turno de la IA y actualiza su memoria.
     * @return El GameState actualizado.
     */
    suspend fun handleAiTurn(): GameState {
        val agent = qAgent ?: throw IllegalStateException("Agent not initialized")

        // 1. **APRENDIZAJE DIFERIDO (Actualización del movimiento anterior de la IA)**
        if (lastAiState != null && lastAiAction != null) {
            // La recompensa se basa en el GameResult actual
            val reward = Reward.getReward(gameState.result, Player.AI)
            agent.updateQValue(
                prevState = lastAiState!!,
                action = lastAiAction!!,
                reward = reward,
                newState = gameState.board.toStateString() // Usar la función de Board
            )
        }

        // 2. **MOVIMIENTO DE LA IA**
        if (!gameState.isFinished) {
            val prevState = gameState.board.toStateString()
            val possibleActions = gameState.board.getAvailablePositions()

            val action = agent.selectAction(prevState, possibleActions)

            if (action != -1) {
                gameState = gameState.move(action, Player.AI)

                // 3. **GUARDAR PARA PRÓXIMO APRENDIZAJE**
                lastAiState = prevState
                lastAiAction = action
            }

            // 4. Verificar si el juego terminó (victoria IA o empate).
            if (gameState.isFinished) {
                // Aplicar el aprendizaje diferido una última vez con la recompensa final.
                val finalReward = Reward.getReward(gameState.result, Player.AI)
                agent.updateQValue(
                    prevState = lastAiState!!,
                    action = lastAiAction!!,
                    reward = finalReward,
                    newState = gameState.board.toStateString()
                )
                clearLearningState()
            }
        }

        return gameState
    }

    /**
     * Guarda la Q-Table de la IA en el DataStore (Llamado desde el ViewModel).
     */
    suspend fun saveAiMemory() {
        val finalQTable = qAgent?.getCurrentQTable() ?: return
        memoryManager.saveQTable(finalQTable)
        clearLearningState()
    }

    /**
     * Limpia las variables de estado necesarias para el aprendizaje diferido.
     */
    private fun clearLearningState() {
        lastAiState = null
        lastAiAction = null
    }

    /**
     * Reinicia el estado del juego (tablero) sin afectar la memoria de la IA.
     */
    fun resetGame() {
        // CORRECCIÓN 3: Resetear el GameState usando el companion object.
        gameState = GameState.initial()
        clearLearningState()
    }
}