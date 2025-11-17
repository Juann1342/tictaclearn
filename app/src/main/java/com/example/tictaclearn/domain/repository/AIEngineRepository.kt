package com.example.tictaclearn.domain.repository

import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.model.GameState // <--- Importación necesaria

/**
 * Interfaz para la lógica compleja de la IA (Q-Learning) y la gestión de su "memoria".
 * Define el contrato que el resto de la app (UseCases, ViewModels) usará.
 */
interface AIEngineRepository {

    /**
     * Decide el próximo movimiento de la IA usando la estrategia Epsilon-Greedy.
     *
     * @param board Estado actual del tablero.
     * @param currentMood El mood actual que define la tasa de exploración (epsilon).
     * @return El índice plano (0-8) de la celda elegida, o null si no hay movimientos.
     */
    suspend fun getNextMove(board: Board, currentMood: Mood): Int?

    /**
     * Actualiza la Q-Table con la experiencia del juego terminado.
     * (Este es el método que faltaba en la interfaz).
     *
     * @param gameHistory Historial de estados del tablero durante el juego.
     */
    suspend fun updateMemory(gameHistory: List<Board>)

    /**
     * Obtiene el mood diario actual que define la estrategia de la IA.
     */
    suspend fun getDailyMood(): Mood

    /**
     * Guarda el mood diario.
     */
    suspend fun saveDailyMood(mood: Mood)

    /**
     * Borra la Q-Table (la memoria de la IA).
     */
    suspend fun clearMemory()

    // -----------------------------------------------------------------
    // MÉTODOS OBSOLETOS (Reemplazados por los de arriba)
    // Los mantenemos comentados por si AIEngineRepositoryImpl necesita
    // ser refactorizado, pero la interfaz actual de Impl NO los usa.
    // -----------------------------------------------------------------

    // /** Carga o inicializa la memoria de la IA para un 'Mood' específico. */
    // suspend fun loadOrInitializeMemory(moodId: String)

    // /** Calcula el mejor movimiento de la IA para el estado actual del juego. */
    // suspend fun calculateAiMove(gameState: GameState): Int

    // /** Guarda la memoria (Q-Table) actual de la IA en la base de datos. */
    // suspend fun saveMemory()
}